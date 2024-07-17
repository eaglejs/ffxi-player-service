import { ref } from 'vue'
import type { Ref } from 'vue'
import { defineStore } from 'pinia'

const host = window.location.hostname
const port = 8080
const protocol = window.location.protocol
const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:'
const apiPath = ref(import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/api`
  : `:${port}`);
const wsPath = ref(import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/ws`
  : `:${port + 1}`);
const fullUrl = `${protocol}//${host}${apiPath.value}`
const fullWsUrl = `${wsProtocol}//${host}${wsPath.value}`

export const useServerStore = defineStore('server', () => {
  let websocket: Ref<WebSocket> = ref(new WebSocket(`${fullWsUrl}`))
  const connectWebSocket = () => {
    if (websocket.value.readyState === websocket.value.OPEN) {
      websocket.value.close()
      websocket.value = new WebSocket(`${fullWsUrl}`)
    }
  }
  return {connectWebSocket, websocket, fullUrl}
})