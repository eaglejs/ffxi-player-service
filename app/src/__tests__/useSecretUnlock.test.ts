import { describe, it, expect, beforeEach, vi } from 'vitest'

// The real passphrase never appears in the repo: the hash check is faked so that a 14-character
// stand-in phrase hashes to the shipped hash.
const STAND_IN = 'abcdefghijklmn'
vi.mock('@/helpers/sha256', async () => {
  const actual = await vi.importActual<typeof import('@/helpers/sha256')>('@/helpers/sha256')
  return {
    sha256Hex: (text: string) =>
      text === 'abcdefghijklmn'
        ? '9b2808465ae008a6824924d4611f1e2b610c6767a21188adeaede015cb31f2fc'
        : actual.sha256Hex(text)
  }
})

const store = new Map<string, string>()
vi.stubGlobal('localStorage', {
  getItem: (k: string) => store.get(k) ?? null,
  setItem: (k: string, v: string) => void store.set(k, v),
  removeItem: (k: string) => void store.delete(k),
  clear: () => store.clear()
})

import { resetUnlockBuffer, useSecretUnlock } from '@/composables/useSecretUnlock'

function key(k: string, extra: Partial<KeyboardEvent> & { target?: EventTarget } = {}) {
  return { key: k, ctrlKey: false, metaKey: false, altKey: false, target: document.body, ...extra } as KeyboardEvent
}

function typeText(text: string, handler: (e: KeyboardEvent) => boolean) {
  let unlockedNow = false
  for (const ch of text) unlockedNow = handler(key(ch)) || unlockedNow
  return unlockedNow
}

describe('useSecretUnlock', () => {
  beforeEach(() => {
    localStorage.clear()
    const { lock } = useSecretUnlock()
    lock()
    resetUnlockBuffer()
  })

  it('starts locked', () => {
    expect(useSecretUnlock().unlocked.value).toBe(false)
  })

  it('unlocks when the phrase is typed, even after stray keys', () => {
    const { handleKeydown, unlocked } = useSecretUnlock()
    expect(typeText('qzx' + STAND_IN, handleKeydown)).toBe(true)
    expect(unlocked.value).toBe(true)
  })

  it('persists the unlock and can lock again', () => {
    const { handleKeydown, lock, unlocked } = useSecretUnlock()
    typeText(STAND_IN, handleKeydown)
    expect(localStorage.getItem('inventorySearchUnlocked')).toBe('1')
    lock()
    expect(unlocked.value).toBe(false)
    expect(localStorage.getItem('inventorySearchUnlocked')).toBeNull()
  })

  it('does not unlock on a near miss', () => {
    const { handleKeydown, unlocked } = useSecretUnlock()
    typeText('abcdefghijklmx', handleKeydown)
    typeText('bcdefghijklmn', handleKeydown)
    expect(unlocked.value).toBe(false)
  })

  it('ignores keys typed into inputs and modified keys', () => {
    const { handleKeydown, unlocked } = useSecretUnlock()
    const input = document.createElement('input')
    for (const ch of STAND_IN) handleKeydown(key(ch, { target: input }))
    for (const ch of STAND_IN) handleKeydown(key(ch, { ctrlKey: true }))
    expect(unlocked.value).toBe(false)
  })

  it('ignores non-character keys without breaking the sequence', () => {
    const { handleKeydown, unlocked } = useSecretUnlock()
    for (const ch of STAND_IN.slice(0, 7)) handleKeydown(key(ch))
    handleKeydown(key('Shift'))
    for (const ch of STAND_IN.slice(7)) handleKeydown(key(ch))
    expect(unlocked.value).toBe(true)
  })

  it('accepts the phrase from the logo-tap prompt and rejects others', () => {
    const { tryPassword, unlocked } = useSecretUnlock()
    expect(tryPassword('wrong')).toBe(false)
    expect(unlocked.value).toBe(false)
    expect(tryPassword(`  ${STAND_IN} `)).toBe(true)
    expect(unlocked.value).toBe(true)
  })
})
