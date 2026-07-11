import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
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
  static instances: MockWebSocket[] = []

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
  send = vi.fn()

  constructor(url: string) {
    this.url = url
    MockWebSocket.instances.push(this)
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
    vi.useFakeTimers()
    MockWebSocket.instances = []
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('initializes with websocket instance', () => {
    const store = useServerStore()

    expect(store.websocket).toBeDefined()
    expect(MockWebSocket.instances).toHaveLength(1)
  })

  describe('connectWebSocket action', () => {
    it('does not create a new websocket while connecting', () => {
      const store = useServerStore()
      const initialWs = store.websocket

      store.connectWebSocket()

      expect(store.websocket).toBe(initialWs)
      expect(MockWebSocket.instances).toHaveLength(1)
    })

    it('creates a new websocket when the current one is closed', () => {
      const store = useServerStore()
      const initialWs = store.websocket

      store.websocket.close()
      store.connectWebSocket()

      expect(store.websocket).not.toBe(initialWs)
      expect(store.websocket.url).toBe('ws://localhost:8080/ws')
      expect(MockWebSocket.instances).toHaveLength(2)
    })

    it('does not reconnect if already open', () => {
      const store = useServerStore()

      store.websocket.readyState = MockWebSocket.OPEN
      const currentWs = store.websocket

      store.connectWebSocket()

      expect(store.websocket).toBe(currentWs)
    })

    it('sets up websocket lifecycle handlers', () => {
      const store = useServerStore()

      expect(store.websocket.onopen).toBeDefined()
      expect(store.websocket.onclose).toBeDefined()
      expect(store.websocket.onerror).toBeDefined()
    })

    it('reuses the message handler after reconnecting', () => {
      const store = useServerStore()
      const handler = vi.fn()

      store.setWebSocketMessageHandler(handler)
      store.websocket.close()
      store.connectWebSocket()

      expect(store.websocket.onmessage).toBe(handler)
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

    it('exports setWebSocketMessageHandler action', () => {
      const store = useServerStore()

      expect(store.setWebSocketMessageHandler).toBeDefined()
      expect(typeof store.setWebSocketMessageHandler).toBe('function')
    })
  })
})
