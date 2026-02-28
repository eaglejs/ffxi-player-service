"""
Simulated FFXI player data used to seed the emulator.
Mirrors real FFXI game data: jobs, zones, titles, buff IDs, currencies, and chat messages.
"""

JOBS = ["WAR", "MNK", "WHM", "BLM", "RDM", "THF", "PLD", "DRK", "BST",
        "BRD", "RNG", "SAM", "NIN", "DRG", "SMN", "BLU", "COR", "PUP",
        "DNC", "SCH", "GEO", "RUN"]

ZONES = [
    "Bastok Markets", "Bastok Mines", "Port Bastok", "Metalworks",
    "San d'Oria", "Port San d'Oria", "Windurst Waters", "Windurst Woods",
    "Jueno", "Upper Jeuno", "Lower Jeuno", "Port Jeuno",
    "Abyssea - Vunkerl", "Abyssea - Attohwa", "Abyssea - Misareaux",
    "Dynamis - Buburimu", "Dynamis - Valkurm", "Dynamis - Qufim",
    "Escha - Zi'Tah", "Escha - Ru'Aun", "Reisenjima",
    "Alzadaal Undersea Ruins", "Nyzul Isle",
    "Walk of Echoes", "Legion",
]

TITLES = [
    "Adventurer", "Champion of the Allied Forces", "Dragon Slayer",
    "Foe of the Empire", "Knight of the Round Table", "Savior of the World",
    "The Undying", "Warrior of the Crystal", "Hero of the Dawn",
    "Master of the Hunt",
]

# FFXI buff icon IDs mapped to common buffs
BUFFS = {
    "protect": {"iconId": 56, "name": "Protect", "category": "enhancement"},
    "shell": {"iconId": 57, "name": "Shell", "category": "enhancement"},
    "haste": {"iconId": 580, "name": "Haste", "category": "enhancement"},
    "refresh": {"iconId": 41, "name": "Refresh", "category": "enhancement"},
    "regen": {"iconId": 40, "name": "Regen", "category": "enhancement"},
    "stoneskin": {"iconId": 37, "name": "Stoneskin", "category": "enhancement"},
    "blink": {"iconId": 36, "name": "Blink", "category": "enhancement"},
    "phalanx": {"iconId": 39, "name": "Phalanx", "category": "enhancement"},
    "barfire": {"iconId": 142, "name": "Barfire", "category": "enhancement"},
    "sneak": {"iconId": 71, "name": "Sneak", "category": "enhancement"},
    "invisible": {"iconId": 72, "name": "Invisible", "category": "enhancement"},
    "dia": {"iconId": 134, "name": "Dia", "category": "enfeeble"},
    "slow": {"iconId": 13, "name": "Slow", "category": "enfeeble"},
    "paralyze": {"iconId": 4, "name": "Paralyze", "category": "enfeeble"},
    "blind": {"iconId": 6, "name": "Blind", "category": "enfeeble"},
    "poison": {"iconId": 2, "name": "Poison", "category": "enfeeble"},
}

CHAT_MESSAGE_TYPES = ["say", "party", "linkshell", "tell", "shout", "emote"]

CHAT_MESSAGES = [
    "Anyone need a raise?",
    "LFP - WAR99/MNK49",
    "Selling Adaman Ore x5",
    "Need a WHM for Escha. PST",
    "WTB gil!!",
    "AFK for a bit",
    "Back!",
    "GG everyone!",
    "Nice pull!",
    "Pulling next...",
    "Need cure!",
    "Low on MP...",
    "Chain #10!",
    "Merit party at Abyssea starting. Need 2 more.",
    "Dynamis starting soon. Get ready!",
]

# FFXI Currency1 fields
CURRENCY1_TEMPLATE = {
    "conquestPointsBastok": 0,
    "conquestPointsSandoria": 0,
    "conquestPointsWindurst": 0,
    "sparksOfEminence": 0,
    "infamy": 0,
    "gallantry": 0,
    "imperialStandingPoints": 0,
    "leaderboardPoints": 0,
    "legionPoints": 0,
    "hallmarks": 0,
    "bayld": 0,
    "unity": 0,
    "segmentPoints": 0,
}

# FFXI Currency2 fields
CURRENCY2_TEMPLATE = {
    "reiveMark": 0,
    "meriPointe": 0,
    "vagaryReward": 0,
    "battlefieldPoints": 0,
    "limitPoints": 0,
    "jobPoints": 0,
    "meitouPoints": 0,
    "ambu": 0,
}

# Simulated player profiles
PLAYER_PROFILES = [
    {
        "playerId": 1001,
        "playerName": "ralphina",
        "mainJob": "WAR",
        "subJob": "MNK",
        "mainJobLevel": 99,
        "subJobLevel": 49,
        "masterLevel": 50,
        "zone": "Escha - Zi'Tah",
        "title": "Champion of the Allied Forces",
        "nationRank": 10,
        "gil": 1250000,
        "attack": 550,
        "defense": 420,
        "merits": {"total": 800, "max": 1200},
        "capacityPoints": {"total": 42000},
        "currentExemplar": 15000,
        "requiredExemplar": 30000,
    },
    {
        "playerId": 1002,
        "playerName": "zulobo",
        "mainJob": "WHM",
        "subJob": "SCH",
        "mainJobLevel": 99,
        "subJobLevel": 49,
        "masterLevel": 35,
        "zone": "Reisenjima",
        "title": "Savior of the World",
        "nationRank": 8,
        "gil": 890000,
        "attack": 220,
        "defense": 310,
        "merits": {"total": 1100, "max": 1200},
        "capacityPoints": {"total": 87000},
        "currentExemplar": 28000,
        "requiredExemplar": 30000,
    },
    {
        "playerId": 1003,
        "playerName": "darkcloud",
        "mainJob": "BLM",
        "subJob": "RDM",
        "mainJobLevel": 99,
        "subJobLevel": 49,
        "masterLevel": 20,
        "zone": "Dynamis - Buburimu",
        "title": "Dragon Slayer",
        "nationRank": 5,
        "gil": 3200000,
        "attack": 180,
        "defense": 265,
        "merits": {"total": 950, "max": 1200},
        "capacityPoints": {"total": 61000},
        "currentExemplar": 5000,
        "requiredExemplar": 30000,
    },
]
