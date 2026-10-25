import { describe, it, expect, beforeEach, vi } from 'vitest'

const store = new Map<string, string>()
vi.stubGlobal('localStorage', {
  getItem: (k: string) => store.get(k) ?? null,
  setItem: (k: string, v: string) => void store.set(k, v),
  removeItem: (k: string) => void store.delete(k),
  clear: () => store.clear()
})

import router from '@/router'
import { useSecretUnlock } from '@/composables/useSecretUnlock'

describe('inventory route guard', () => {
  beforeEach(async () => {
    useSecretUnlock().lock()
    await router.push('/')
  })

  it('sends visitors home while the feature is locked', async () => {
    await router.push('/inventory')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('leaves the REMA page open to everyone', async () => {
    await router.push('/rema')
    expect(router.currentRoute.value.path).toBe('/rema')
  })

  it('does not let a wrong passphrase through', async () => {
    useSecretUnlock().tryPassword('x') // wrong phrase must not unlock
    await router.push('/inventory')
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('does not hide the other routes', async () => {
    await router.push('/charts')
    expect(router.currentRoute.value.path).toBe('/charts')
  })
})
