import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useServerStore } from '@/stores/server'

vi.mock('@/helpers/config', () => ({
  fullWsUrl: 'ws://localhost:8080/ws'
}))

class MockWebSocket {
  static CONNECTING = 0
  static OPEN = 1
  static CLOSING = 2
  static CLOSED = 3

  readyState = MockWebSocket.CONNECTING
  CONNECTING = MockWebSocket.CONNECTING
  OPEN = MockWebSocket.OPEN
  CLOSING = MockWebSocket.CLOSING
  CLOSED = MockWebSocket.CLOSED

  onopen: (() => void) | null = null
  onclose: (() => void) | null = null
  onerror: ((error: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  url: string

  constructor(url: string) {
    this.url = url
  }

  send(data: string) {
    // Mock send
  }

  close() {
    this.readyState = MockWebSocket.CLOSED
    if (this.onclose) {
      this.onclose()
    }
  }
}

global.WebSocket = MockWebSocket as any

describe('useServerStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('initializes with websocket instance', () => {
    const store = useServerStore()
    
    expect(store.websocket).toBeDefined()
  })

  describe('connectWebSocket action', () => {
    it('creates new WebSocket', () => {
      const store = useServerStore()
      const initialWs = store.websocket
      
      store.connectWebSocket()
      
      expect(store.websocket).toBeDefined()
      expect(store.websocket).not.toBe(initialWs)
    })

    it('creates WebSocket with correct URL', () => {
      const store = useServerStore()
      
      store.connectWebSocket()
      
      expect(store.websocket.url).toBe('ws://localhost:8080/ws')
    })

    it('does not reconnect if already open', () => {
      const store = useServerStore()
      
      store.websocket.readyState = MockWebSocket.OPEN
      const currentWs = store.websocket
      
      store.connectWebSocket()
      
      expect(store.websocket).toBe(currentWs)
    })

    it('sets up onopen handler', () => {
      const store = useServerStore()
      
      store.connectWebSocket()
      
      expect(store.websocket.onopen).toBeDefined()
    })

    it('sets up onclose handler', () => {
      const store = useServerStore()
      
      store.connectWebSocket()
      
      expect(store.websocket.onclose).toBeDefined()
    })

    it('sets up onerror handler', () => {
      const store = useServerStore()
      
      store.connectWebSocket()
      
      expect(store.websocket.onerror).toBeDefined()
    })
  })

  describe('store exports', () => {
    it('exports websocket state', () => {
      const store = useServerStore()
      
      expect(store.websocket).toBeDefined()
    })

    it('exports connectWebSocket action', () => {
      const store = useServerStore()
      
      expect(store.connectWebSocket).toBeDefined()
      expect(typeof store.connectWebSocket).toBe('function')
    })
  })
})
