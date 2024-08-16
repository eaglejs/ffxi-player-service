import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { useServerStore } from '@/stores/server'

import { fullUrl } from '@/helpers/config'

export const useUserStore = defineStore('user', () => {
  const players = ref([] as any)
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

  websocket.onmessage = (event) => {
    if (event.data === 'ping') {
      if (websocket.readyState === websocket.OPEN) {
        websocket.send('pong');
      }
    } else {
      updatePlayer(event.data)
    }
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

  function reconnectData() {
    fetchUsers()
    connectWebSocket()
  }

  onMounted(() => {
    fetchUsers()
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        reconnectData()
      }
    })
    window.addEventListener('online', reconnectData)
  })

  return { players, fetchUsers, fetchUser }
})
