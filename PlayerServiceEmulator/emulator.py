#!/usr/bin/env python3
"""
FFXI PlayerServiceEmulator
Simulates multiple FFXI players sending stat updates to the JavaService REST API.

Usage:
    python emulator.py [--config config.json] [--interval SECONDS] [--once]

Options:
    --config PATH         Path to configuration file (default: config.json)
    --interval SECONDS    Override update_interval_seconds from config
    --once                Run exactly one tick then exit (useful for testing)
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
            continue
        ok = api.initialize_player(player.to_dict())
        if ok:
            LOG.info("Initialized player %s (id=%d)", player.playerName, player.playerId)
        else:
            LOG.warning("Failed to initialize player %s (id=%d) – service may be unavailable",
                        player.playerName, player.playerId)


# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------

def run(config: dict, interval: float, run_once: bool) -> None:
    base_url = config["service"]["base_url"]
    api = ApiClient(base_url)

    LOG.info("Connecting to PlayerService at %s", base_url)

    players = [build_player(p) for p in PLAYER_PROFILES]
    simulators = [PlayerSimulator(api, p) for p in players]

    initialize_players(api, players)

    online_interval = config["emulator"].get("online_ping_interval_seconds", 30)
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
    args = parser.parse_args()

    # Resolve config path relative to script location
    config_path = args.config
    if not os.path.isabs(config_path):
        config_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), config_path)

    config = load_config(config_path)
    interval = args.interval or config["emulator"].get("update_interval_seconds", 3)

    run(config, interval, args.once)


if __name__ == "__main__":
    main()
