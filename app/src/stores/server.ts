import { ref } from 'vue'
import type { Ref } from 'vue'
import { defineStore } from 'pinia'
import { fullWsUrl } from '@/helpers/config'


export const useServerStore = defineStore('server', () => {
  let websocket: Ref<WebSocket> = ref(new WebSocket(`${fullWsUrl}`))
  const connectWebSocket = () => {
    if (websocket.value.readyState === websocket.value.OPEN) {
      websocket.value.close()
      websocket.value = new WebSocket(`${fullWsUrl}`)
    }
  }
  return {connectWebSocket, websocket}
})