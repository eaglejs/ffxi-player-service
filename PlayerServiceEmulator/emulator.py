#!/usr/bin/env python3
"""
FFXI PlayerServiceEmulator
Simulates multiple FFXI players sending stat updates to the JavaService REST API.

Usage:
    python emulator.py [--config config.json] [--interval SECONDS] [--once]
    python emulator.py --replay [--speed MULTIPLIER] [--loop]

Options:
    --config PATH         Path to configuration file (default: config.json)
    --interval SECONDS    Override update_interval_seconds from config
    --once                Run exactly one tick then exit (useful for testing)
    --replay              Replay example_data.jsonl as a recorded session
    --speed MULTIPLIER    Speed multiplier for replay (default: 1.0 = real-time, 2.0 = 2× faster)
    --loop                Loop replay indefinitely (only valid with --replay)
"""
import argparse
import json
import logging
import os
import signal
import sys
import time
from typing import List

# Ensure local packages are importable regardless of cwd
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from models.player import Player, PlayerStats, Merits, CapacityPoints, ExpHistory
from services.api_client import ApiClient
from services.player_simulator import PlayerSimulator
from data.players import PLAYER_PROFILES
from data.example_data_loader import ExampleDataLoader

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(message)s",
    datefmt="%H:%M:%S",
)
LOG = logging.getLogger(__name__)

_running = True


def _handle_sigint(sig, frame):
    global _running
    LOG.info("Shutting down emulator (SIGINT)…")
    _running = False


signal.signal(signal.SIGINT, _handle_sigint)


# ---------------------------------------------------------------------------
# Config loading
# ---------------------------------------------------------------------------

def load_config(path: str) -> dict:
    if not os.path.exists(path):
        LOG.error("Config file not found: %s", path)
        sys.exit(1)
    with open(path) as f:
        return json.load(f)


# ---------------------------------------------------------------------------
# Player bootstrap
# ---------------------------------------------------------------------------

def build_player(profile: dict) -> Player:
    """Construct a Player from a profile dict."""
    p = Player(
        playerId=profile["playerId"],
        playerName=profile["playerName"],
        mainJob=profile["mainJob"],
        mainJobLevel=profile["mainJobLevel"],
        subJob=profile["subJob"],
        subJobLevel=profile["subJobLevel"],
        masterLevel=profile["masterLevel"],
        zone=profile["zone"],
        title=profile["title"],
        nationRank=profile["nationRank"],
        gil=profile["gil"],
        attack=profile["attack"],
        defense=profile["defense"],
        merits=Merits(**profile["merits"]),
        capacityPoints=CapacityPoints(**profile["capacityPoints"]),
        currentExemplar=profile["currentExemplar"],
        requiredExemplar=profile["requiredExemplar"],
        stats=PlayerStats(),
        expHistory=ExpHistory(),
    )
    return p


def initialize_players(api: ApiClient, players: List[Player]) -> None:
    """Send initialize_player for each player, skipping those already in DB."""
    for player in players:
        existing = api.get_player(player.playerId)
        if existing:
            LOG.info("Player %s (id=%d) already initialized – skipping",
                     player.playerName, player.playerId)
        else:
            ok = api.initialize_player(player.to_dict())
            if ok:
                LOG.info("Initialized player %s (id=%d)", player.playerName, player.playerId)
            else:
                LOG.warning("Failed to initialize player %s (id=%d) – service may be unavailable",
                            player.playerName, player.playerId)
                continue

        online_ok = api.set_online(player.playerId, player.playerName)
        if online_ok:
            LOG.info("Player %s (id=%d) set online", player.playerName, player.playerId)
        else:
            LOG.warning("Failed to set online for player %s (id=%d)", player.playerName, player.playerId)


# ---------------------------------------------------------------------------
# Replay mode
# ---------------------------------------------------------------------------

def _rewrite_timestamps(body: dict, wall_offset: float) -> dict:
    """Return a shallow copy of body with all timestamp fields shifted by wall_offset seconds.

    wall_offset = current_wall_time - recording_start_wall_time

    Fields rewritten:
      - lastOnline (int, Unix epoch)
      - timestamp  (str, ISO 8601) in top-level body
      - buffs[*].timestamp  (int, Unix epoch)
      - buffs[*].utc_time   (str, ISO 8601)
    """
    from datetime import datetime, timezone

    def _shift_iso(s: str) -> str:
        try:
            dt = datetime.fromisoformat(s.replace("Z", "+00:00"))
            shifted = dt.timestamp() + wall_offset
            return datetime.fromtimestamp(shifted, tz=timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        except (ValueError, OSError):
            return s

    def _shift_epoch(v: int) -> int:
        return int(v + wall_offset)

    body = dict(body)

    if "lastOnline" in body and isinstance(body["lastOnline"], int):
        body["lastOnline"] = _shift_epoch(body["lastOnline"])

    if "timestamp" in body and isinstance(body["timestamp"], str):
        body["timestamp"] = _shift_iso(body["timestamp"])

    if "buffs" in body and isinstance(body["buffs"], dict):
        new_buffs = {}
        for slot, buff in body["buffs"].items():
            buff = dict(buff)
            if "timestamp" in buff and isinstance(buff["timestamp"], int):
                buff["timestamp"] = _shift_epoch(buff["timestamp"])
            if "utc_time" in buff and isinstance(buff["utc_time"], str):
                buff["utc_time"] = _shift_iso(buff["utc_time"])
            new_buffs[slot] = buff
        body["buffs"] = new_buffs

    return body


def run_replay(config: dict, speed: float, loop: bool) -> None:
    """Replay example_data.jsonl to the service, preserving original timing.

    Timestamps in all payloads are rewritten to reflect the current wall-clock
    time so the JavaService receives records that appear to have been generated
    right now rather than from the original recording session.

    Args:
        config:  Loaded config dict (provides base_url).
        speed:   Replay speed multiplier (1.0 = real-time, 2.0 = twice as fast).
        loop:    When True, restart replay from the beginning after each pass.
    """
    base_url = config["service"]["base_url"]
    api = ApiClient(base_url)
    loader = ExampleDataLoader()

    if not loader._raw_records:
        LOG.error("No records found in example_data.jsonl – cannot replay")
        return

    total = len(loader._raw_records)
    LOG.info("Replay mode: %d records, speed=%.1fx, loop=%s", total, speed, loop)
    LOG.info("Connecting to PlayerService at %s", base_url)

    # Compute original recording start time (Unix epoch) from the first record
    first_rec_ts = loader._raw_records[0]["timestamp"]
    try:
        from datetime import datetime, timezone
        recording_start = datetime.fromisoformat(
            first_rec_ts.replace("Z", "+00:00")
        ).timestamp()
    except (ValueError, AttributeError):
        recording_start = time.time()

    pass_num = 0
    while _running:
        pass_num += 1
        sent = 0
        prev_rel = 0.0
        replay_wall_start = time.time()
        LOG.info("--- Replay pass %d ---", pass_num)

        for rel_seconds, path, body in loader.replay_records():
            if not _running:
                break

            # Sleep for the inter-record gap, scaled by speed
            gap = (rel_seconds - prev_rel) / speed
            if gap > 0:
                time.sleep(gap)
            prev_rel = rel_seconds

            # Rewrite timestamps: shift from recording epoch to current wall time
            wall_offset = time.time() - recording_start
            body = _rewrite_timestamps(body, wall_offset)

            resp = api._post(path, body)
            sent += 1
            player_name = body.get("playerName", "?")
            LOG.debug("[replay] %s %s -> %s",
                      player_name, path, resp.status_code if resp else "no response")

            if sent % 100 == 0:
                LOG.info("[replay] %d/%d records sent (%.0f%%)", sent, total, 100 * sent / total)

        LOG.info("[replay] pass %d complete: %d records sent", pass_num, sent)

        if not loop:
            break

    LOG.info("Replay stopped.")


# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------

def run(config: dict, interval: float, run_once: bool) -> None:
    base_url = config["service"]["base_url"]
    api = ApiClient(base_url)

    LOG.info("Connecting to PlayerService at %s", base_url)

    players = [build_player(p) for p in PLAYER_PROFILES]
    loader = ExampleDataLoader()
    simulators = [PlayerSimulator(api, p, loader) for p in players]

    initialize_players(api, players)

    online_interval = config["emulator"].get("online_ping_interval_seconds", 15)
    last_ping = 0.0

    LOG.info("Starting simulation loop (interval=%.1fs, players=%d)", interval, len(simulators))

    while _running:
        now = time.time()

        # Periodic online pings
        if now - last_ping >= online_interval:
            for sim in simulators:
                sim.ping_online()
            last_ping = now

        # Run one tick for every player
        for sim in simulators:
            sim.tick()
            LOG.info("[%s] tick %d | hp=%d%% mp=%d%% tp=%d zone=%s",
                     sim.player.playerName, sim._tick,
                     sim.player.hpp, sim.player.mpp, sim.player.tp,
                     sim.player.zone)

        if run_once:
            LOG.info("--once flag set, exiting after first tick")
            break

        time.sleep(interval)

    LOG.info("Emulator stopped.")


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="FFXI PlayerServiceEmulator")
    parser.add_argument("--config", default="config.json",
                        help="Path to configuration file (default: config.json)")
    parser.add_argument("--interval", type=float, default=None,
                        help="Override update_interval_seconds from config")
    parser.add_argument("--once", action="store_true",
                        help="Run one tick then exit (useful for smoke-testing)")
    parser.add_argument("--replay", action="store_true",
                        help="Replay example_data.jsonl as a recorded session")
    parser.add_argument("--speed", type=float, default=1.0,
                        help="Replay speed multiplier (default: 1.0 = real-time). Only used with --replay")
    parser.add_argument("--loop", action="store_true",
                        help="Loop replay indefinitely. Only used with --replay")
    args = parser.parse_args()

    # Resolve config path relative to script location
    config_path = args.config
    if not os.path.isabs(config_path):
        config_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), config_path)

    config = load_config(config_path)

    if args.replay:
        run_replay(config, speed=args.speed, loop=args.loop)
    else:
        interval = args.interval or config["emulator"].get("update_interval_seconds", 3)
        run(config, interval, args.once)


if __name__ == "__main__":
    main()
