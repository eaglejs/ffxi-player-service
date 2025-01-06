import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { useServerStore } from '@/stores/server'
import { fullUrl } from '@/helpers/config'


export const useUserStore = defineStore('user', () => {
  const players = ref(new Map<number, any>());
  const chatLog = ref([] as any);
  
  const { websocket, connectWebSocket } = useServerStore()

  websocket.onmessage = (event) => {
    if (event.data === 'ping') {
      if (websocket.readyState === websocket.OPEN) {
        websocket.send('pong');
      }
    } else {
      updatePlayer(event.data)
    }
  };

  async function fetchUsers() {
    const response = await axios.get(`${fullUrl}/get_users`)
    response.data.forEach((player: any) => {
      players.value.set(player.playerId, player);
    });
    return Promise.resolve(players)
  }

  async function fetchUser(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_user?playerId=${playerId}`)

    players.value.set(response.data.playerId, response.data);
    return Promise.resolve(players.value.get(playerId));
  }

  async function fetchChatLog(playerId: number) {
    const response = await axios.get(`${fullUrl}/get_chat_log?playerId=${playerId}`)
    chatLog.value = response.data;
    return Promise.resolve(chatLog);
  }

  function updatePlayer(data: string) {
    const playerId = parseInt(window.location.pathname.split('/').pop() || '0');
    const player: any = JSON.parse(data)
    const playerToUpdate = players.value.get(parseInt(player.playerId))
    if (playerToUpdate === undefined) {
      return;
    }
    if ('chatLog' in player) {
      if (playerId === player.playerId) {
        chatLog.value.push(player.chatLog)
      }
      return;
    }
    for (const key in player) {
      if (key in playerToUpdate) {
        playerToUpdate[key] = player[key];
        continue
      }
    }
    players.value.set(parseInt(player.playerId), playerToUpdate)
  }

  onMounted(() => {
    window.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        connectWebSocket()
      }
    })
    window.addEventListener('online', connectWebSocket)
  })

  return { players, chatLog, fetchUsers, fetchUser, fetchChatLog }
})
