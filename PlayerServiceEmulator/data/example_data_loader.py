"""
Loads data/example_data.jsonl and builds per-endpoint sample pools so that
the PlayerSimulator can draw realistic payloads instead of purely random values.

Also exposes replay_records() for the --replay mode, which yields records sorted
by timestamp with relative timing offsets so the emulator can recreate a real
session at configurable speed.

Usage:
    from data.example_data_loader import ExampleDataLoader
    loader = ExampleDataLoader()
    buff_snapshot = loader.sample_buffs()       # dict of slot → buff entry
    currency1     = loader.sample_currency1()   # dict of field → value
    currency2     = loader.sample_currency2()   # dict of field → value
    stats         = loader.sample_stats()       # dict of stat field → value
    status        = loader.sample_status()      # int (0, 1, or 4)
    exp_entry     = loader.sample_exp_entry()   # {"expType": int, "points": int, "chain": int}

    # Replay mode:
    for rel_seconds, path, body in loader.replay_records():
        ...  # sleep and POST at the right time
"""
import json
import logging
import os
import random
from datetime import datetime, timezone
from typing import Any, Dict, Generator, List, Optional, Tuple

LOG = logging.getLogger(__name__)

_DEFAULT_PATH = os.path.join(os.path.dirname(__file__), "example_data.jsonl")


class ExampleDataLoader:
    """Parses example_data.jsonl and exposes random samplers per endpoint."""

    def __init__(self, path: str = _DEFAULT_PATH):
        self._pools: Dict[str, List[dict]] = {}
        self._raw_records: List[dict] = []   # full parsed records, sorted by timestamp
        self._load(path)

    # -------------------------------------------------------------------------
    # Loading
    # -------------------------------------------------------------------------

    def _load(self, path: str) -> None:
        if not os.path.exists(path):
            LOG.warning("example_data.jsonl not found at %s – using fallback values", path)
            return

        parsed = 0
        raw: List[dict] = []
        with open(path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    rec = json.loads(line)
                    endpoint = rec.get("endpoint", "")
                    body_raw = rec.get("body", "{}")
                    body = json.loads(body_raw) if isinstance(body_raw, str) else body_raw
                    raw.append({
                        "endpoint":  endpoint,
                        "timestamp": rec.get("timestamp", ""),
                        "body":      body,
                    })
                    # Strip player-identity fields for sample pools
                    pool_body = {k: v for k, v in body.items() if k not in ("playerId", "playerName")}
                    self._pools.setdefault(endpoint, []).append(pool_body)
                    parsed += 1
                except (json.JSONDecodeError, KeyError):
                    pass

        # Sort records by timestamp for ordered replay
        def _parse_ts(s: str) -> float:
            try:
                return datetime.fromisoformat(s.replace("Z", "+00:00")).timestamp()
            except ValueError:
                return 0.0

        self._raw_records = sorted(raw, key=lambda r: _parse_ts(r["timestamp"]))

        LOG.info("example_data_loader: loaded %d records across %d endpoints",
                 parsed, len(self._pools))

    # -------------------------------------------------------------------------
    # Replay
    # -------------------------------------------------------------------------

    def replay_records(self) -> Generator[Tuple[float, str, dict], None, None]:
        """Yield (relative_seconds, api_path, body) for every record in timestamp order.

        relative_seconds is the offset from the first record — callers can use this
        to sleep between sends so the replay mirrors the original session timing.
        api_path is the endpoint path suitable for ApiClient._post(), e.g. '/player/set_tp'.
        body is the full payload dict (including playerId / playerName).
        """
        if not self._raw_records:
            LOG.warning("No records available for replay")
            return

        def _ts(r: dict) -> float:
            try:
                return datetime.fromisoformat(
                    r["timestamp"].replace("Z", "+00:00")
                ).timestamp()
            except ValueError:
                return 0.0

        t0 = _ts(self._raw_records[0])
        for rec in self._raw_records:
            rel = _ts(rec) - t0
            path = "/" + rec["endpoint"].lstrip("/")
            yield rel, path, rec["body"]

    # -------------------------------------------------------------------------
    # Generic sampler
    # -------------------------------------------------------------------------

    def _sample(self, endpoint: str) -> Optional[dict]:
        pool = self._pools.get(endpoint)
        if not pool:
            return None
        return dict(random.choice(pool))

    # -------------------------------------------------------------------------
    # Typed samplers
    # -------------------------------------------------------------------------

    def sample_buffs(self) -> dict:
        """Return a realistic buff snapshot dict (slot → buff entry)."""
        sample = self._sample("player/set_buffs")
        if sample and "buffs" in sample:
            return dict(sample["buffs"])
        return {}

    def sample_currency1(self) -> dict:
        """Return a realistic currency1 payload (field → value)."""
        sample = self._sample("player/set_currency1")
        if sample:
            return {k: v for k, v in sample.items() if isinstance(v, int)}
        return {}

    def sample_currency2(self) -> dict:
        """Return a realistic currency2 payload (field → value)."""
        sample = self._sample("player/set_currency2")
        if sample:
            return {k: v for k, v in sample.items() if isinstance(v, int)}
        return {}

    def sample_stats(self) -> dict:
        """Return a realistic stats payload (field → value)."""
        sample = self._sample("player/set_stats")
        if sample:
            return dict(sample)
        return {}

    def sample_status(self) -> int:
        """Return a realistic status integer (0=idle, 1=resting, 4=engaged)."""
        sample = self._sample("player/set_status")
        if sample and "status" in sample:
            return int(sample["status"])
        return 0

    def sample_exp_entry(self) -> dict:
        """Return a realistic exp entry: {expType, points, chain}."""
        sample = self._sample("player/set_exp_history")
        if sample:
            return {
                "expType": sample.get("expType", 371),
                "points":  sample.get("points", 1000),
                "chain":   sample.get("chain", 0),
            }
        return {"expType": 371, "points": 1000, "chain": 0}

    def sample_hpp(self) -> int:
        """Return a realistic hpp value."""
        sample = self._sample("player/set_hpp")
        if sample and "hpp" in sample:
            return int(sample["hpp"])
        return 100

    def sample_tp(self) -> int:
        """Return a realistic tp value."""
        sample = self._sample("player/set_tp")
        if sample and "tp" in sample:
            return int(sample["tp"])
        return 0

    def sample_zone(self) -> Optional[str]:
        """Return a realistic zone name (or None if no zone data)."""
        sample = self._sample("player/set_zone")
        if sample and "zone" in sample:
            return str(sample["zone"])
        return None

    def sample_merits(self) -> dict:
        """Return a realistic merits payload {total, max}."""
        sample = self._sample("player/set_merits")
        if sample and "total" in sample:
            return {"total": sample["total"], "max": sample.get("max", 75)}
        return {"total": 75, "max": 75}
