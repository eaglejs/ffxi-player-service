import { ref, onMounted, onUnmounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'

import { fullUrl, websocket, connectWebSocket } from '@/helpers/config'

export const useUserStore = defineStore('user', () => {
  let ws: WebSocket | null = null;
  const players = ref([] as any)

  const fetchUsers = async () => {
    const response = await axios.get(`${fullUrl}/get_users`)
    players.value = response.data
    return Promise.resolve(players.value)
  }

  const fetchUser = async (playerName: string) => {
    const response = await axios.get(`${fullUrl}/get_user?playerName=${playerName}`)
    // find player in players and update
    const index = players.value.findIndex((p: any) => p.playerName === playerName)
    if (index !== -1) {
      for (const key in response.data) {
        if (response.data.hasOwnProperty(key)) {
          players.value[index][key] = response.data[key]
        }
      }
    }
    return Promise.resolve(response.data)
  }

  websocket.onopen = () => {
    setInterval(() => {
      if (ws && websocket.readyState === websocket.OPEN) {
        websocket.send('ping');
      }
    }, 5000); // Send ping every 5 seconds
  };

  websocket.onmessage = (event) => {
    if (event.data !== 'pong') {
      updatePlayer(event.data)
    }
  };

  websocket.onclose = () => {
    setInterval(connectWebSocket, 5000); // Try to reconnect every 5 seconds
  };

  const updatePlayer = (data: any) => {
    const player = JSON.parse(data)
    const index = players.value.findIndex((p: any) => p.playerName === player.playerName)
    if (index !== -1) {
      for (const key in player) {
        if (player.hasOwnProperty(key)) {
          players.value[index][key] = player[key]
        }
      }
    }
  }

  onMounted(() => {
    fetchUsers();
    connectWebSocket();
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        fetchUsers()
        connectWebSocket()
      }
    })
    window.addEventListener('online', fetchUsers)
  })

  onUnmounted(() => {
    if (websocket.readyState === websocket.OPEN) {
      websocket.close()
    }
  })

  return { players, fetchUser }
})
