import { ref } from 'vue'
import type { Ref } from 'vue'
import { defineStore } from 'pinia'
import { fullWsUrl } from '@/helpers/config'

export const useServerStore = defineStore('server', () => {
  const websocket: Ref<WebSocket> = ref(new WebSocket(fullWsUrl))
  const websocketRetry = ref()

  function connectWebSocket() {
    console.log('Checking WebSocket connection...')
    if (websocket.value.readyState === websocket.value.OPEN) {
      console.log('Already connected to WebSocket')
      return
    } else {
      websocket.value = new WebSocket(fullWsUrl)

      websocket.value.onopen = () => {
        console.log('Connected to websocket')
        clearInterval(websocketRetry.value)
        setInterval(() => {
          if (websocket.value.readyState === websocket.value.OPEN) {
            websocket.value.send('pong')
          }
        }, 5000) // Send ping every 5 seconds
      }

      websocket.value.onclose = () => {
        console.log('Connection closed...')
        if (websocketRetry.value) {
          clearInterval(websocketRetry.value)
        }
        console.log('Connection closed, retrying in 5 seconds')
        websocketRetry.value = setInterval(connectWebSocket, 5000) // Try to reconnect every 5 seconds
      }

      websocket.value.onerror = (error) => {
        console.error('WebSocket error:', error)
      }
    }
  }

  return { connectWebSocket, websocket }
})
