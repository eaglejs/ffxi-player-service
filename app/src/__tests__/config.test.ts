import { describe, expect, it } from 'vitest'

import { buildWsUrl, resolveWsPort } from '@/helpers/config'

describe('buildWsUrl', () => {
  it('uses wss without appending port 80 when no websocket port is configured', () => {
    expect(buildWsUrl('ffxi.eaglejs.io', 'https:')).toBe('wss://ffxi.eaglejs.io/ws/players')
  })

  it('uses the configured websocket port when one is provided', () => {
    expect(buildWsUrl('localhost', 'http:', '8080')).toBe('ws://localhost:8080/ws/players')
  })

  it('omits default ports for the selected websocket protocol', () => {
    expect(buildWsUrl('localhost', 'http:', '80')).toBe('ws://localhost/ws/players')
    expect(buildWsUrl('ffxi.eaglejs.io', 'https:', '443')).toBe('wss://ffxi.eaglejs.io/ws/players')
  })
})

describe('resolveWsPort', () => {
  it('does not reuse app port for https by default', () => {
    expect(resolveWsPort('https:', '8080')).toBeUndefined()
  })

  it('reuses app port for http when websocket port is not provided', () => {
    expect(resolveWsPort('http:', '8080')).toBe('8080')
  })

  it('prefers explicit websocket port when configured', () => {
    expect(resolveWsPort('https:', '8080', '8443')).toBe('8443')
  })
})
