"""
PlayerSimulator drives realistic per-player stat simulation.
Each method corresponds to a JavaService endpoint and advances the in-memory
player state before sending it to the service.
"""
import logging
import random
import time
from datetime import datetime, timezone
from typing import Optional

from models.player import Player, PlayerStats, ExpEntry
from services.api_client import ApiClient
from data.players import (
    BUFFS, CHAT_MESSAGES, CHAT_MESSAGE_TYPES,
    CURRENCY1_TEMPLATE, CURRENCY2_TEMPLATE, ZONES,
)

LOG = logging.getLogger(__name__)


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


class PlayerSimulator:
    def __init__(self, api: ApiClient, player: Player):
        self.api = api
        self.player = player
        self._tick = 0

    # -------------------------------------------------------------------------
    # Lifecycle helpers
    # -------------------------------------------------------------------------

    def ping_online(self) -> None:
        """Keep lastOnline timestamp current (called every ~30 s)."""
        ok = self.api.set_online(self.player.playerId, self.player.playerName)
        LOG.debug("[%s] set_online -> %s", self.player.playerName, ok)

    # -------------------------------------------------------------------------
    # Combat simulation
    # -------------------------------------------------------------------------

    def simulate_vitals(self) -> None:
        """Fluctuate HP, MP and TP as though the player is in combat."""
        p = self.player

        # HP: wander up/down, clamp to [10, 100]
        p.hpp = max(10, min(100, p.hpp + random.randint(-15, 20)))

        # MP: slowly drain in combat, restore when resting
        if p.hpp < 60:  # resting scenario
            p.mpp = min(100, p.mpp + random.randint(5, 15))
        else:
            p.mpp = max(0, p.mpp + random.randint(-10, 8))

        # TP: build toward 3000, reset when weaponskill fires
        p.tp = p.tp + random.randint(100, 250)
        if p.tp >= 3000:
            p.tp = 0  # weaponskill fired

        self.api.set_hpp(p.playerId, p.playerName, p.hpp)
        self.api.set_mpp(p.playerId, p.playerName, p.mpp)
        self.api.set_tp(p.playerId, p.playerName, p.tp)
        LOG.debug("[%s] vitals: hp=%d%% mp=%d%% tp=%d", p.playerName, p.hpp, p.mpp, p.tp)

    # -------------------------------------------------------------------------
    # Zone simulation
    # -------------------------------------------------------------------------

    def simulate_zone(self) -> None:
        """Occasionally move the player to a different zone."""
        if random.random() < 0.1:  # 10% chance per tick
            new_zone = random.choice(ZONES)
            if new_zone != self.player.zone:
                self.player.zone = new_zone
                self.api.set_zone(self.player.playerId, self.player.playerName, new_zone)
                LOG.info("[%s] moved to zone: %s", self.player.playerName, new_zone)

    # -------------------------------------------------------------------------
    # Experience / merit simulation
    # -------------------------------------------------------------------------

    def simulate_exp(self) -> None:
        """Award experience, capacity, and exemplar points."""
        p = self.player
        chain = random.randint(1, 10)

        # Experience
        exp_gained = random.randint(200, 800) * chain
        entry = ExpEntry(points=exp_gained, chain=chain, timestamp=_now_iso())
        p.expHistory.experience.append(entry)
        if len(p.expHistory.experience) > 50:
            p.expHistory.experience.pop(0)
        self.api.set_exp_history(
            p.playerId, p.playerName, "experience", exp_gained, chain, entry.timestamp
        )

        # Capacity points
        cp_gained = random.randint(100, 500) * chain
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

        # Merits accumulate slowly
        if random.random() < 0.3:
            p.merits.total = min(p.merits.max, p.merits.total + 1)
            self.api.set_merits(p.playerId, p.playerName, p.merits.total, p.merits.max)

        LOG.debug("[%s] exp +%d cp +%d ex +%d", p.playerName, exp_gained, cp_gained, ex_gained)

    # -------------------------------------------------------------------------
    # Buff simulation
    # -------------------------------------------------------------------------

    def simulate_buffs(self) -> None:
        """Randomly apply/remove buffs and send them to the service."""
        p = self.player
        buff_keys = list(BUFFS.keys())

        # Randomly drop 1-2 buffs
        active_keys = list(p.buffs.keys())
        for key in random.sample(active_keys, min(len(active_keys), random.randint(0, 2))):
            del p.buffs[key]

        # Randomly add 1-3 new buffs
        for key in random.sample(buff_keys, min(len(buff_keys), random.randint(1, 3))):
            if key not in p.buffs:
                buff = dict(BUFFS[key])
                buff["duration"] = random.randint(30, 300)
                buff["timestamp"] = int(time.time())
                p.buffs[key] = buff

        self.api.set_buffs(p.playerId, p.playerName, p.buffs)
        LOG.debug("[%s] buffs updated: %d active", p.playerName, len(p.buffs))

    # -------------------------------------------------------------------------
    # Chat simulation
    # -------------------------------------------------------------------------

    def simulate_chat(self) -> None:
        """Send a random chat message."""
        p = self.player
        if random.random() < 0.3:  # 30% chance to chat per tick
            message = random.choice(CHAT_MESSAGES)
            msg_type = random.choice(CHAT_MESSAGE_TYPES)
            messages = {
                "message": message,
                "timestamp": _now_iso(),
            }
            self.api.set_messages(p.playerId, p.playerName, messages, msg_type)
            LOG.debug("[%s] chat [%s]: %s", p.playerName, msg_type, message)

    # -------------------------------------------------------------------------
    # Stats simulation
    # -------------------------------------------------------------------------

    def simulate_stats(self) -> None:
        """Update player stats with minor fluctuations (gear swaps, buffs, etc.)."""
        p = self.player
        s = p.stats

        # Minor fluctuations to added stats (gear swaps)
        s.addedSTR = random.randint(20, 60)
        s.addedDEX = random.randint(15, 50)
        s.addedVIT = random.randint(10, 45)
        s.addedAGI = random.randint(10, 40)
        s.addedINT = random.randint(5, 30)
        s.addedMND = random.randint(5, 35)
        s.addedCHR = random.randint(5, 25)

        # Attack / defense fluctuate from gear/food
        p.attack = max(100, p.attack + random.randint(-20, 20))
        p.defense = max(80, p.defense + random.randint(-15, 15))

        stats_payload = {
            **s.to_dict(),
            "masterLevel": p.masterLevel,
            "mainJobLevel": p.mainJobLevel,
            "subJobLevel": p.subJobLevel,
            "attack": p.attack,
            "defense": p.defense,
            "title": p.title,
            "nationRank": p.nationRank,
            "currentExemplar": p.currentExemplar,
            "requiredExemplar": p.requiredExemplar,
        }
        self.api.set_stats(p.playerId, p.playerName, stats_payload)
        LOG.debug("[%s] stats updated: atk=%d def=%d", p.playerName, p.attack, p.defense)

    # -------------------------------------------------------------------------
    # Currency simulation
    # -------------------------------------------------------------------------

    def simulate_currency(self) -> None:
        """Slowly accumulate in-game currencies."""
        p = self.player
        cur1 = dict(CURRENCY1_TEMPLATE)
        cur1.update({
            "sparksOfEminence": random.randint(0, 5000),
            "hallmarks": random.randint(0, 2000),
            "bayld": random.randint(0, 10000),
            "conquestPointsBastok": random.randint(0, 3000),
        })
        cur2 = dict(CURRENCY2_TEMPLATE)
        cur2.update({
            "jobPoints": random.randint(0, 30000),
            "limitPoints": random.randint(0, 10000),
            "reiveMark": random.randint(0, 5000),
        })
        p.currency1 = cur1
        p.currency2 = cur2
        self.api.set_currency1(p.playerId, p.playerName, cur1)
        self.api.set_currency2(p.playerId, p.playerName, cur2)
        # Gil fluctuates slightly
        p.gil = max(0, p.gil + random.randint(-5000, 8000))
        self.api.set_gil(p.playerId, p.playerName, p.gil)
        LOG.debug("[%s] currency updated: gil=%d", p.playerName, p.gil)

    # -------------------------------------------------------------------------
    # Main tick
    # -------------------------------------------------------------------------

    def tick(self) -> None:
        """Run one simulation tick for this player."""
        self._tick += 1
        self.simulate_vitals()
        self.simulate_zone()
        self.simulate_chat()
        self.simulate_buffs()

        if self._tick % 5 == 0:   # every 5 ticks
            self.simulate_exp()
            self.simulate_stats()

        if self._tick % 20 == 0:  # every 20 ticks
            self.simulate_currency()
