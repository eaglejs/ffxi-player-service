import { ref } from 'vue'
import type { Ref } from 'vue'
import { defineStore } from 'pinia'
import { fullWsUrl } from '@/helpers/config'

export const useServerStore = defineStore('server', () => {
  const websocketRetry = ref<ReturnType<typeof setInterval> | null>(null)
  const websocketHeartbeat = ref<ReturnType<typeof setInterval> | null>(null)
  const websocketMessageHandler = ref<((event: MessageEvent) => void) | null>(null)

  function clearWebSocketRetry() {
    if (websocketRetry.value !== null) {
      clearInterval(websocketRetry.value)
      websocketRetry.value = null
    }
  }

  function clearWebSocketHeartbeat() {
    if (websocketHeartbeat.value !== null) {
      clearInterval(websocketHeartbeat.value)
      websocketHeartbeat.value = null
    }
  }

  function createWebSocket() {
    const socket = new WebSocket(fullWsUrl)

    socket.onmessage = websocketMessageHandler.value

    socket.onopen = () => {
      if (websocket.value !== socket) {
        return
      }
      console.log('Connected to websocket')
      clearWebSocketRetry()
      clearWebSocketHeartbeat()
      websocketHeartbeat.value = setInterval(() => {
        if (websocket.value === socket && socket.readyState === socket.OPEN) {
          socket.send('pong')
        }
      }, 5000) // Send ping every 5 seconds
    }

    socket.onclose = () => {
      if (websocket.value !== socket) {
        return
      }
      console.log('Connection closed...')
      clearWebSocketHeartbeat()
      if (websocketRetry.value === null) {
        console.log('Connection closed, retrying in 5 seconds')
        websocketRetry.value = setInterval(connectWebSocket, 5000) // Try to reconnect every 5 seconds
      }
    }

    socket.onerror = (error) => {
      if (websocket.value !== socket) {
        return
      }
      console.error('WebSocket error:', error)
    }

    return socket
  }

  const websocket: Ref<WebSocket> = ref(createWebSocket())

  function connectWebSocket() {
    console.log('Checking WebSocket connection...')
    if (websocket.value.readyState !== websocket.value.CLOSED) {
      console.log('Already connected to WebSocket')
      return
    }

    websocket.value = createWebSocket()
  }

  function setWebSocketMessageHandler(handler: ((event: MessageEvent) => void) | null) {
    websocketMessageHandler.value = handler
    websocket.value.onmessage = handler
  }

  return { connectWebSocket, setWebSocketMessageHandler, websocket }
})
