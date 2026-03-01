"""
PlayerSimulator drives realistic per-player stat simulation.
Each method corresponds to a JavaService endpoint and advances the in-memory
player state before sending it to the service.

Realistic values are drawn from the ExampleDataLoader (parsed from example_data.jsonl)
so that simulated payloads closely mirror what a real FFXI client would send.
"""
import logging
import random
import time
from datetime import datetime, timezone
from typing import Optional

from models.player import Player, PlayerStats, ExpEntry
from services.api_client import ApiClient
from data.example_data_loader import ExampleDataLoader
from data.players import (
    BUFF_CATALOG, CHAT_MESSAGES, CHAT_MESSAGE_TYPES,
    CURRENCY1_TEMPLATE, CURRENCY2_TEMPLATE, ZONES, STATUSES,
)

LOG = logging.getLogger(__name__)


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


class PlayerSimulator:
    def __init__(self, api: ApiClient, player: Player, loader: Optional[ExampleDataLoader] = None):
        self.api = api
        self.player = player
        self.loader = loader or ExampleDataLoader()
        self._tick = 0

    # -------------------------------------------------------------------------
    # Lifecycle helpers
    # -------------------------------------------------------------------------

    def ping_online(self) -> None:
        """Keep lastOnline timestamp current (called every 15 s)."""
        ok = self.api.set_online(self.player.playerId, self.player.playerName)
        LOG.debug("[%s] set_online -> %s", self.player.playerName, ok)

    # -------------------------------------------------------------------------
    # Status simulation
    # -------------------------------------------------------------------------

    def simulate_status(self) -> None:
        """Update player status using realistic values from example data."""
        status = self.loader.sample_status() if random.random() < 0.5 else random.choice(STATUSES)
        self.player.status = status
        self.api.set_status(self.player.playerId, self.player.playerName, status)
        LOG.debug("[%s] status -> %d", self.player.playerName, status)

    # -------------------------------------------------------------------------
    # Combat simulation
    # -------------------------------------------------------------------------

    def simulate_vitals(self) -> None:
        """Fluctuate HP, MP and TP using realistic values from example data."""
        p = self.player

        # Prefer sampled hpp from example data; occasionally use pure random
        if random.random() < 0.6:
            p.hpp = self.loader.sample_hpp()
        else:
            p.hpp = max(10, min(100, p.hpp + random.randint(-15, 20)))

        # MP: slowly drain in combat, restore when resting
        if p.status == 1:   # resting
            p.mpp = min(100, p.mpp + random.randint(5, 15))
        else:
            p.mpp = max(0, p.mpp + random.randint(-10, 8))

        # TP: prefer sampled values; build toward 3000 and reset on weaponskill
        if random.random() < 0.4:
            p.tp = self.loader.sample_tp()
        else:
            p.tp = p.tp + random.randint(100, 250)
            if p.tp >= 3000:
                p.tp = 0

        self.api.set_hpp(p.playerId, p.playerName, p.hpp)
        self.api.set_mpp(p.playerId, p.playerName, p.mpp)
        self.api.set_tp(p.playerId, p.playerName, p.tp)
        LOG.debug("[%s] vitals: hp=%d%% mp=%d%% tp=%d", p.playerName, p.hpp, p.mpp, p.tp)

    # -------------------------------------------------------------------------
    # Zone simulation
    # -------------------------------------------------------------------------

    def simulate_zone(self) -> None:
        """Occasionally move the player to a different zone."""
        if random.random() < 0.1:   # 10% chance per tick
            # Prefer example-data zones when available
            new_zone = self.loader.sample_zone() or random.choice(ZONES)
            if new_zone != self.player.zone:
                self.player.zone = new_zone
                self.api.set_zone(self.player.playerId, self.player.playerName, new_zone)
                LOG.info("[%s] moved to zone: %s", self.player.playerName, new_zone)

    # -------------------------------------------------------------------------
    # Experience / merit simulation
    # -------------------------------------------------------------------------

    def simulate_exp(self) -> None:
        """Award experience, capacity, and exemplar points using realistic values."""
        p = self.player

        # Draw a realistic exp entry from example data
        exp_sample = self.loader.sample_exp_entry()
        exp_type_int = exp_sample["expType"]
        points = exp_sample["points"]
        chain = exp_sample["chain"]

        entry = ExpEntry(points=points, chain=chain, timestamp=_now_iso())
        p.expHistory.experience.append(entry)
        if len(p.expHistory.experience) > 50:
            p.expHistory.experience.pop(0)
        self.api.set_exp_history(
            p.playerId, p.playerName, "experience", points, chain, entry.timestamp
        )

        # Capacity points: random variation on top of sampled base
        cp_gained = random.randint(100, 500) * max(1, chain)
        cp_entry = ExpEntry(points=cp_gained, chain=chain, timestamp=_now_iso())
        p.expHistory.capacity.append(cp_entry)
        if len(p.expHistory.capacity) > 50:
            p.expHistory.capacity.pop(0)
        p.capacityPoints.total += cp_gained
        self.api.set_exp_history(
            p.playerId, p.playerName, "capacity", cp_gained, chain, cp_entry.timestamp
        )
        self.api.set_capacity_points(p.playerId, p.playerName, p.capacityPoints.total)

        # Exemplar points
        ex_gained = random.randint(50, 300)
        ex_entry = ExpEntry(points=ex_gained, chain=chain, timestamp=_now_iso())
        p.expHistory.exemplar.append(ex_entry)
        if len(p.expHistory.exemplar) > 50:
            p.expHistory.exemplar.pop(0)
        p.currentExemplar = min(p.requiredExemplar, p.currentExemplar + ex_gained)
        self.api.set_exp_history(
            p.playerId, p.playerName, "exemplar", ex_gained, chain, ex_entry.timestamp
        )

        # Merits: sample from example data
        merits = self.loader.sample_merits()
        p.merits.total = min(p.merits.max, merits["total"])
        p.merits.max = merits["max"]
        self.api.set_merits(p.playerId, p.playerName, p.merits.total, p.merits.max)

        LOG.debug("[%s] exp +%d cp +%d ex +%d", p.playerName, points, cp_gained, ex_gained)

    # -------------------------------------------------------------------------
    # Buff simulation
    # -------------------------------------------------------------------------

    def simulate_buffs(self) -> None:
        """Send a realistic buff snapshot sampled from example data."""
        p = self.player

        # Prefer a full snapshot from example data (real buff_ids, buff_names, buff_types)
        snapshot = self.loader.sample_buffs()
        if snapshot:
            p.buffs = snapshot
        else:
            # Fallback: build a snapshot from the catalog with runtime timestamps
            chosen = random.sample(BUFF_CATALOG, min(len(BUFF_CATALOG), random.randint(1, 5)))
            now_ts = int(time.time())
            p.buffs = {
                str(i + 1): {
                    "buff_name":     b["buff_name"],
                    "buff_id":       b["buff_id"],
                    "buff_type":     b["buff_type"],
                    "buff_duration": random.randint(60, 3600),
                    "timestamp":     now_ts,
                    "utc_time":      _now_iso(),
                }
                for i, b in enumerate(chosen)
            }

        self.api.set_buffs(p.playerId, p.playerName, p.buffs)
        LOG.debug("[%s] buffs updated: %d active", p.playerName, len(p.buffs))

    # -------------------------------------------------------------------------
    # Chat simulation
    # -------------------------------------------------------------------------

    def simulate_chat(self) -> None:
        """Send a random chat message."""
        p = self.player
        if random.random() < 0.3:   # 30% chance to chat per tick
            message = random.choice(CHAT_MESSAGES)
            msg_type = random.choice(CHAT_MESSAGE_TYPES)
            messages = {"message": message, "timestamp": _now_iso()}
            self.api.set_messages(p.playerId, p.playerName, messages, msg_type)
            LOG.debug("[%s] chat [%s]: %s", p.playerName, msg_type, message)

    # -------------------------------------------------------------------------
    # Stats simulation
    # -------------------------------------------------------------------------

    def simulate_stats(self) -> None:
        """Update player stats using realistic values sampled from example data."""
        p = self.player
        s = p.stats

        stats_sample = self.loader.sample_stats()
        if stats_sample:
            # Apply sampled base stats and added stats; keep identity fields from player
            for attr in ("baseSTR", "baseDEX", "baseVIT", "baseAGI", "baseINT", "baseMND", "baseCHR"):
                if attr in stats_sample:
                    setattr(s, attr, stats_sample[attr])
            for attr in ("addedSTR", "addedDEX", "addedVIT", "addedAGI", "addedINT", "addedMND", "addedCHR"):
                if attr in stats_sample:
                    setattr(s, attr, stats_sample[attr])
            if "attack" in stats_sample:
                p.attack = stats_sample["attack"]
            if "defense" in stats_sample:
                p.defense = stats_sample["defense"]
            if "currentExemplar" in stats_sample:
                p.currentExemplar = stats_sample["currentExemplar"]
            if "requiredExemplar" in stats_sample:
                p.requiredExemplar = stats_sample["requiredExemplar"]
        else:
            # Fallback: minor random fluctuations
            s.addedSTR = random.randint(20, 362)
            s.addedDEX = random.randint(15, 227)
            s.addedVIT = random.randint(10, 200)
            s.addedAGI = random.randint(10, 165)
            s.addedINT = random.randint(5, 145)
            s.addedMND = random.randint(5, 152)
            s.addedCHR = random.randint(5, 159)
            p.attack  = max(100, p.attack  + random.randint(-20, 20))
            p.defense = max(80,  p.defense + random.randint(-15, 15))

        stats_payload = {
            **s.to_dict(),
            "masterLevel":      p.masterLevel,
            "mainJobLevel":     p.mainJobLevel,
            "subJobLevel":      p.subJobLevel,
            "attack":           p.attack,
            "defense":          p.defense,
            "title":            p.title,
            "nationRank":       p.nationRank,
            "currentExemplar":  p.currentExemplar,
            "requiredExemplar": p.requiredExemplar,
        }
        self.api.set_stats(p.playerId, p.playerName, stats_payload)
        LOG.debug("[%s] stats updated: atk=%d def=%d", p.playerName, p.attack, p.defense)

    # -------------------------------------------------------------------------
    # Currency simulation
    # -------------------------------------------------------------------------

    def simulate_currency(self) -> None:
        """Update currencies using realistic values sampled from example data."""
        p = self.player

        cur1 = self.loader.sample_currency1()
        if not cur1:
            cur1 = dict(CURRENCY1_TEMPLATE)
            cur1.update({
                "sparksOfEminence":       random.randint(0, 99999),
                "unityAccolades":         random.randint(0, 99999),
                "conquestPointsBastok":   random.randint(0, 100000),
                "conquestPointsWindurst": random.randint(0, 10000000),
            })

        cur2 = self.loader.sample_currency2()
        if not cur2:
            cur2 = dict(CURRENCY2_TEMPLATE)
            cur2.update({
                "eschaBeads":   random.randint(0, 50000),
                "eschaSilt":    random.randint(0, 20000000),
                "gallimaufry":  random.randint(0, 2000000),
                "mogSegments":  random.randint(0, 100000),
            })

        p.currency1 = cur1
        p.currency2 = cur2
        self.api.set_currency1(p.playerId, p.playerName, cur1)
        self.api.set_currency2(p.playerId, p.playerName, cur2)

        # Gil: sample or fluctuate
        gil_sample = self.loader._sample("player/set_gil")
        if gil_sample and "gil" in gil_sample:
            p.gil = gil_sample["gil"]
        else:
            p.gil = max(0, p.gil + random.randint(-5000, 8000))
        self.api.set_gil(p.playerId, p.playerName, p.gil)
        LOG.debug("[%s] currency updated: gil=%d", p.playerName, p.gil)

    # -------------------------------------------------------------------------
    # Main tick
    # -------------------------------------------------------------------------

    def tick(self) -> None:
        """Run one simulation tick for this player."""
        self._tick += 1
        self.simulate_status()
        self.simulate_vitals()
        self.simulate_zone()
        self.simulate_chat()
        self.simulate_buffs()

        if self._tick % 5 == 0:    # every 5 ticks
            self.simulate_exp()
            self.simulate_stats()

        if self._tick % 20 == 0:   # every 20 ticks
            self.simulate_currency()
