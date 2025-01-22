import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { useServerStore } from '@/stores/server'
import { fullUrl } from '@/helpers/config'

export const useUserStore = defineStore('user', () => {
  const players = ref(new Map<number, any>())
  const chatLog = ref([] as any)

  const serverStore = useServerStore()

  // const { connectWebSocket, websocket } = useServerStore()

  function getPlayerById(playerId: number) {
    return players.value.get(playerId)
  }

  async function fetchUsers() {
    const response = await axios.get(`${fullUrl}/get_users`)
    response.data.forEach((player: any) => {
      players.value.set(player.playerId, player)
    })
    return Promise.resolve(players)
  }

  async function fetchUser(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_user?playerId=${playerId}`)

    players.value.set(response.data.playerId, response.data)
    return Promise.resolve(players.value.get(playerId))
  }

  async function fetchChatLog(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_chat_log?playerId=${playerId}`)
    chatLog.value = response.data
    return Promise.resolve(chatLog)
  }

  function updatePlayer(data: string) {
    const playerId = parseInt(window.location.pathname.split('/').pop() || '0')
    const player: any = JSON.parse(data)
    const playerToUpdate = players.value.get(parseInt(player.playerId))
    if (playerToUpdate === undefined) {
      return
    }
    if ('chatLog' in player) {
      if (playerId === player.playerId) {
        chatLog.value.push(player.chatLog)
      }
      return
    }

    Object.keys(player).forEach((key) => {
      if (key in playerToUpdate) {
        playerToUpdate[key] = player[key]
      }
    })
    players.value.set(parseInt(player.playerId), playerToUpdate)
  }

  function refreshBuffs(player: { playerId: number; playerName: string }) {
    if (serverStore.websocket.readyState !== serverStore.websocket.OPEN) {
      return Promise.reject('WebSocket not connected')
    }
    return axios.post(`${fullUrl}/refresh_buffs`, player)
  }

  function wsOnMessage (event: MessageEvent) {
    if (event.data === 'ping') {
      if (serverStore.websocket.readyState === serverStore.websocket.OPEN) {
        serverStore.websocket.send('pong');
      }
    } else {
      updatePlayer(event.data)
    }
  }

  onMounted(() => {
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        serverStore.connectWebSocket()
        if (serverStore.websocket.readyState === serverStore.websocket.OPEN && serverStore.websocket.onmessage === null) {
          serverStore.websocket.onmessage = wsOnMessage
        }
      }
    })
    window.addEventListener('online', () => {
      serverStore.connectWebSocket()
      if (serverStore.websocket.readyState === serverStore.websocket.OPEN && serverStore.websocket.onmessage === null) {
        serverStore.websocket.onmessage = wsOnMessage
      }
    })
    serverStore.websocket.onmessage = wsOnMessage
  })

  return { players, chatLog, fetchUsers, fetchUser, fetchChatLog, refreshBuffs, getPlayerById }
})
