"""
Player data model mirroring the JavaService Player domain object.
"""
from dataclasses import dataclass, field
from typing import Optional
import time


@dataclass
class PlayerStats:
    baseSTR: int = 60
    baseDEX: int = 58
    baseVIT: int = 55
    baseAGI: int = 52
    baseINT: int = 45
    baseMND: int = 48
    baseCHR: int = 44
    addedSTR: int = 0
    addedDEX: int = 0
    addedVIT: int = 0
    addedAGI: int = 0
    addedINT: int = 0
    addedMND: int = 0
    addedCHR: int = 0
    fireResistance: int = 10
    iceResistance: int = 10
    windResistance: int = 10
    earthResistance: int = 10
    lightningResistance: int = 10
    waterResistance: int = 10
    lightResistance: int = 10
    darkResistance: int = 10

    def to_dict(self) -> dict:
        return {
            "baseSTR": self.baseSTR,
            "baseDEX": self.baseDEX,
            "baseVIT": self.baseVIT,
            "baseAGI": self.baseAGI,
            "baseINT": self.baseINT,
            "baseMND": self.baseMND,
            "baseCHR": self.baseCHR,
            "addedSTR": self.addedSTR,
            "addedDEX": self.addedDEX,
            "addedVIT": self.addedVIT,
            "addedAGI": self.addedAGI,
            "addedINT": self.addedINT,
            "addedMND": self.addedMND,
            "addedCHR": self.addedCHR,
            "fireResistance": self.fireResistance,
            "iceResistance": self.iceResistance,
            "windResistance": self.windResistance,
            "earthResistance": self.earthResistance,
            "lightningResistance": self.lightningResistance,
            "waterResistance": self.waterResistance,
            "lightResistance": self.lightResistance,
            "darkResistance": self.darkResistance,
        }


@dataclass
class Merits:
    total: int = 0
    max: int = 1200

    def to_dict(self) -> dict:
        return {"total": self.total, "max": self.max}


@dataclass
class CapacityPoints:
    total: int = 0

    def to_dict(self) -> dict:
        return {"total": self.total}


@dataclass
class ExpEntry:
    points: int = 0
    chain: int = 0
    timestamp: str = ""

    def to_dict(self) -> dict:
        return {"points": self.points, "chain": self.chain, "timestamp": self.timestamp}


@dataclass
class ExpHistory:
    experience: list = field(default_factory=list)
    capacity: list = field(default_factory=list)
    exemplar: list = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "experience": [e.to_dict() if isinstance(e, ExpEntry) else e for e in self.experience],
            "capacity": [e.to_dict() if isinstance(e, ExpEntry) else e for e in self.capacity],
            "exemplar": [e.to_dict() if isinstance(e, ExpEntry) else e for e in self.exemplar],
        }


@dataclass
class Player:
    playerId: int = 0
    playerName: str = ""
    mainJob: str = "WAR"
    mainJobLevel: int = 99
    subJob: str = "MNK"
    subJobLevel: int = 49
    masterLevel: int = 50
    zone: str = "Bastok Markets"
    title: str = "Adventurer"
    nationRank: int = 1
    status: int = 0
    lastOnline: int = field(default_factory=lambda: int(time.time()))
    gil: int = 500000
    hpp: int = 100
    mpp: int = 100
    tp: int = 0
    attack: int = 450
    defense: int = 380
    stats: PlayerStats = field(default_factory=PlayerStats)
    merits: Merits = field(default_factory=Merits)
    capacityPoints: CapacityPoints = field(default_factory=CapacityPoints)
    currentExemplar: int = 0
    requiredExemplar: int = 30000
    currency1: dict = field(default_factory=dict)
    currency2: dict = field(default_factory=dict)
    buffs: dict = field(default_factory=dict)
    expHistory: ExpHistory = field(default_factory=ExpHistory)

    def to_dict(self) -> dict:
        return {
            "playerId": self.playerId,
            "playerName": self.playerName,
            "mainJob": self.mainJob,
            "mainJobLevel": self.mainJobLevel,
            "subJob": self.subJob,
            "subJobLevel": self.subJobLevel,
            "masterLevel": self.masterLevel,
            "zone": self.zone,
            "title": self.title,
            "nationRank": self.nationRank,
            "status": self.status,
            "lastOnline": self.lastOnline,
            "gil": self.gil,
            "hpp": self.hpp,
            "mpp": self.mpp,
            "tp": self.tp,
            "attack": self.attack,
            "defense": self.defense,
            "stats": self.stats.to_dict(),
            "merits": self.merits.to_dict(),
            "capacityPoints": self.capacityPoints.to_dict(),
            "currentExemplar": self.currentExemplar,
            "requiredExemplar": self.requiredExemplar,
            "currency1": self.currency1,
            "currency2": self.currency2,
            "buffs": self.buffs,
            "expHistory": self.expHistory.to_dict(),
        }
