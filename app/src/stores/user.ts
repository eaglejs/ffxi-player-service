import { ref, onMounted, onUnmounted } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'

const host = window.location.hostname
const port = 8080
const protocol = window.location.protocol

const apiPath = ref(import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/api`
  : `:${port}`);

const wsPath = ref(import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/ws`
  : `:${port + 1}`);

export const useUserStore = defineStore('user', () => {
  let ws: WebSocket | null = null;
  const players = ref([] as any)

  const fetchUsers = async () => {
    const response = await axios.get(`${protocol}//${host}${apiPath.value}/get_users`)
    players.value = response.data
  }

  const connectWebSocket = () => {
    ws = new WebSocket(`ws://${host}${wsPath.value}`)

    ws.onopen = () => {
      setInterval(() => {
        if (ws) {
          ws.send('ping');
        }
      }, 5000); // Send ping every 5 seconds
    };

    ws.onmessage = (event) => {
      if (event.data !== 'pong') {
        updatePlayer(event.data)
      }
    };

    ws.onclose = () => {
      setTimeout(connectWebSocket, 5000); // Try to reconnect every 5 seconds
    };
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
  })

  onUnmounted(() => {
    if (ws) {
      ws.close()
    }
  })

  return { players }
})
