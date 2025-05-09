import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { useServerStore } from '@/stores/server'
import { fullUrl } from '@/helpers/config'

export const usePlayerStore = defineStore('player', () => {
  let lastBuffCheck = new Date()
  const players = ref(new Map<number, any>())
  const chatLog = ref([] as any)

  const serverStore = useServerStore()

  function getPlayerById(playerId: number) {
    return players.value.get(playerId)
  }

  async function fetchPlayers() {
    const response = await axios.get(`${fullUrl}/get_players`)
    response.data.forEach((player: any) => {
      players.value.set(player.playerId, player)
    })
    return Promise.resolve(players)
  }

  async function fetchPlayer(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_player?playerId=${playerId}`)

    players.value.set(response.data.playerId, response.data)
    return Promise.resolve(players.value.get(playerId))
  }

  async function fetchChatLog(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_chat_log?playerId=${playerId}`)
    chatLog.value = response.data
    return Promise.resolve(chatLog)
  }

  async function fetchChatLogByMessageType(playerId: number, messageType: string) {
    const response = await axios.get(
      `${fullUrl}/get_chat_log_by_type?playerId=${playerId}&messageType=${messageType}`
    )
    chatLog.value = response.data
    return Promise.resolve(chatLog)
  }

  function updatePlayer(data: string) {
    const playerId = parseInt(window.location.pathname.split('/').pop() || '0')
    const player: any = JSON.parse(data)
    const playerToUpdate = players.value.get(player.playerId)
    if (playerToUpdate === undefined) {
      return
    }

    if ('chatLog' in player) {
      if (playerId === player.playerId) {
        chatLog.value = [...chatLog.value, ...player.chatLog]
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
    if (new Date().getTime() - lastBuffCheck.getTime() < 1000) {
      return Promise.reject('You can only refresh buffs once per second')
    }
    return axios.post(`${fullUrl}/refresh_buffs`, player).then((data) => {
      lastBuffCheck = new Date()
      return Promise.resolve(data)
    })
  }

  function wsOnMessage(event: MessageEvent) {
    if (event.data === 'ping') {
      if (serverStore.websocket.readyState === serverStore.websocket.OPEN) {
        serverStore.websocket.send('pong')
      }
    } else {
      updatePlayer(event.data)
    }
  }

  onMounted(() => {
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        serverStore.connectWebSocket()
        if (
          serverStore.websocket.readyState === serverStore.websocket.OPEN &&
          serverStore.websocket.onmessage === null
        ) {
          serverStore.websocket.onmessage = wsOnMessage
        }
      }
    })
    window.addEventListener('online', () => {
      serverStore.connectWebSocket()
      if (
        serverStore.websocket.readyState === serverStore.websocket.OPEN &&
        serverStore.websocket.onmessage === null
      ) {
        serverStore.websocket.onmessage = wsOnMessage
      }
    })
    setInterval(() => {
      console.log('PlayerStore: Checking WebSocket connection...')
      if (serverStore.websocket.readyState !== serverStore.websocket.OPEN) {
        serverStore.connectWebSocket()
      }
    }, 5000)
    serverStore.websocket.onmessage = wsOnMessage
  })

  return {
    players,
    chatLog,
    fetchPlayers,
    fetchPlayer,
    fetchChatLog,
    fetchChatLogByMessageType,
    refreshBuffs,
    getPlayerById
  }
})
