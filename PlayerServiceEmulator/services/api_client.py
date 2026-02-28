"""
HTTP API client for the FFXI PlayerService.
Wraps every endpoint exposed by SinglePlayerResource and PlayersResource.
"""
import json
import logging
import requests
from typing import Any, Optional

LOG = logging.getLogger(__name__)


class ApiClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    # -------------------------------------------------------------------------
    # Internal helpers
    # -------------------------------------------------------------------------

    def _post(self, path: str, payload: dict) -> Optional[requests.Response]:
        url = f"{self.base_url}{path}"
        try:
            response = self.session.post(url, data=json.dumps(payload), timeout=10)
            if not response.ok:
                LOG.warning("POST %s -> %s: %s", path, response.status_code, response.text[:200])
            return response
        except requests.exceptions.ConnectionError:
            LOG.error("Connection refused – is the service running at %s?", self.base_url)
        except requests.exceptions.Timeout:
            LOG.error("Timeout on POST %s", path)
        except Exception as exc:
            LOG.error("Unexpected error on POST %s: %s", path, exc)
        return None

    def _get(self, path: str, params: Optional[dict] = None) -> Optional[requests.Response]:
        url = f"{self.base_url}{path}"
        try:
            response = self.session.get(url, params=params, timeout=10)
            if not response.ok:
                LOG.warning("GET %s -> %s: %s", path, response.status_code, response.text[:200])
            return response
        except requests.exceptions.ConnectionError:
            LOG.error("Connection refused – is the service running at %s?", self.base_url)
        except requests.exceptions.Timeout:
            LOG.error("Timeout on GET %s", path)
        except Exception as exc:
            LOG.error("Unexpected error on GET %s: %s", path, exc)
        return None

    # -------------------------------------------------------------------------
    # PlayersResource endpoints
    # -------------------------------------------------------------------------

    def get_players(self) -> Optional[list]:
        resp = self._get("/players/get_players")
        return resp.json() if resp and resp.ok else None

    def get_player(self, player_id: int) -> Optional[dict]:
        resp = self._get("/players/get_player", params={"playerId": player_id})
        return resp.json() if resp and resp.ok else None

    # -------------------------------------------------------------------------
    # SinglePlayerResource endpoints
    # -------------------------------------------------------------------------

    def initialize_player(self, player: dict) -> bool:
        resp = self._post("/player/initialize_player", player)
        return resp is not None and resp.ok

    def set_online(self, player_id: int, player_name: str) -> bool:
        payload = {"playerId": player_id, "playerName": player_name}
        resp = self._post("/player/set_online", payload)
        return resp is not None and resp.ok

    def set_jobs(self, player_id: int, player_name: str,
                 main_job: str, main_job_level: int,
                 sub_job: str, sub_job_level: int) -> bool:
        payload = {
            "playerId": player_id,
            "playerName": player_name,
            "mainJob": main_job,
            "mainJobLevel": main_job_level,
            "subJob": sub_job,
            "subJobLevel": sub_job_level,
        }
        resp = self._post("/player/set_jobs", payload)
        return resp is not None and resp.ok

    def set_gil(self, player_id: int, player_name: str, gil: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "gil": gil}
        resp = self._post("/player/set_gil", payload)
        return resp is not None and resp.ok

    def set_status(self, player_id: int, player_name: str, status: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "status": status}
        resp = self._post("/player/set_status", payload)
        return resp is not None and resp.ok

    def set_hpp(self, player_id: int, player_name: str, hpp: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "hpp": hpp}
        resp = self._post("/player/set_hpp", payload)
        return resp is not None and resp.ok

    def set_mpp(self, player_id: int, player_name: str, mpp: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "mpp": mpp}
        resp = self._post("/player/set_mpp", payload)
        return resp is not None and resp.ok

    def set_tp(self, player_id: int, player_name: str, tp: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "tp": tp}
        resp = self._post("/player/set_tp", payload)
        return resp is not None and resp.ok

    def set_zone(self, player_id: int, player_name: str, zone: str) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "zone": zone}
        resp = self._post("/player/set_zone", payload)
        return resp is not None and resp.ok

    def set_merits(self, player_id: int, player_name: str, total: int, max_merits: int) -> bool:
        payload = {
            "playerId": player_id,
            "playerName": player_name,
            "total": total,
            "max": max_merits,
        }
        resp = self._post("/player/set_merits", payload)
        return resp is not None and resp.ok

    def set_capacity_points(self, player_id: int, player_name: str, total: int) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "total": total}
        resp = self._post("/player/set_capacity_points", payload)
        return resp is not None and resp.ok

    def set_buffs(self, player_id: int, player_name: str, buffs: dict) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, "buffs": buffs}
        resp = self._post("/player/set_buffs", payload)
        return resp is not None and resp.ok

    def set_messages(self, player_id: int, player_name: str,
                     messages: dict, message_type: str) -> bool:
        payload = {
            "playerId": player_id,
            "playerName": player_name,
            "messages": messages,
            "messageType": message_type,
        }
        resp = self._post("/player/set_messages", payload)
        return resp is not None and resp.ok

    def set_stats(self, player_id: int, player_name: str, stats_payload: dict) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, **stats_payload}
        resp = self._post("/player/set_stats", payload)
        return resp is not None and resp.ok

    def set_exp_history(self, player_id: int, player_name: str,
                        exp_type: str, points: int, chain: int, timestamp: str) -> bool:
        payload = {
            "playerId": player_id,
            "playerName": player_name,
            "expType": exp_type,
            "points": points,
            "chain": chain,
            "timestamp": timestamp,
        }
        resp = self._post("/player/set_exp_history", payload)
        return resp is not None and resp.ok

    def set_currency1(self, player_id: int, player_name: str, currency: dict) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, **currency}
        resp = self._post("/player/set_currency1", payload)
        return resp is not None and resp.ok

    def set_currency2(self, player_id: int, player_name: str, currency: dict) -> bool:
        payload = {"playerId": player_id, "playerName": player_name, **currency}
        resp = self._post("/player/set_currency2", payload)
        return resp is not None and resp.ok

    def refresh_buffs(self, player_id: int, player_name: str) -> bool:
        payload = {"playerId": player_id, "playerName": player_name}
        resp = self._post("/player/refresh_buffs", payload)
        return resp is not None and resp.ok

    def reset_exp_history(self, player_id: int, player_name: str) -> bool:
        payload = {"playerId": player_id, "playerName": player_name}
        resp = self._post("/player/reset_exp_history", payload)
        return resp is not None and resp.ok
