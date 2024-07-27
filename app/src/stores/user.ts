import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { useServerStore } from '@/stores/server'

import { fullUrl } from '@/helpers/config'

export const useUserStore = defineStore('user', () => {
  let ws: WebSocket | null = null
  const players = ref([] as any)
  const websocketRetry = ref()
  const { websocket, connectWebSocket } = useServerStore()

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
    clearInterval(websocketRetry.value);
    setInterval(() => {
      if (ws && websocket.readyState === websocket.OPEN) {
        websocket.send('pong');
      }
    }, 5000); // Send ping every 5 seconds
  };

  websocket.onmessage = (event) => {
    if (event.data === 'ping') {
      if (ws && websocket.readyState === websocket.OPEN) {
        websocket.send('pong');
      }
    } else {
      updatePlayer(event.data)
    }
  };

  websocket.onclose = () => {
    websocketRetry.value = setInterval(connectWebSocket, 5000); // Try to reconnect every 5 seconds
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

  return { players, fetchUsers, fetchUser }
})
