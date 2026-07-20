import { config, RouterLinkStub } from '@vue/test-utils'
import { vi } from 'vitest'

class TestWebSocket {
  static CONNECTING = 0
  static OPEN = 1
  static CLOSING = 2
  static CLOSED = 3

  CONNECTING = TestWebSocket.CONNECTING
  OPEN = TestWebSocket.OPEN
  CLOSING = TestWebSocket.CLOSING
  CLOSED = TestWebSocket.CLOSED

  readyState = TestWebSocket.CONNECTING
  url: string

  onopen: (() => void) | null = null
  onclose: (() => void) | null = null
  onerror: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null

  constructor(url: string) {
    this.url = url
  }

  send() {
    // Intentionally noop in test setup.
  }

  close() {
    this.readyState = TestWebSocket.CLOSED
    this.onclose?.()
  }
}

config.global.stubs = {
  RouterLink: RouterLinkStub,
  RouterView: true
}

// jsdom does not implement scrollTo and throws DATA_CLONE_ERR (code 25) when
// ScrollOptions are passed to a partially-detached element during test teardown.
window.scrollTo = vi.fn()
Element.prototype.scrollTo = vi.fn()

// Avoid network websocket side-effects in tests that import stores/components.
vi.stubGlobal('WebSocket', TestWebSocket as unknown as typeof WebSocket)
