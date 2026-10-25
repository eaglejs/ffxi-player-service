import { ref } from 'vue'
import { sha256Hex } from '@/helpers/sha256'

// Only the hash of the passphrase ships in the bundle. This hides an unfinished feature; it is
// not access control (the API endpoints themselves are open).
const UNLOCK_HASH = '9b2808465ae008a6824924d4611f1e2b610c6767a21188adeaede015cb31f2fc'
const UNLOCK_LENGTH = 14
const STORAGE_KEY = 'inventorySearchUnlocked'

function readStored(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === '1'
  } catch {
    return false
  }
}

function writeStored(value: boolean) {
  try {
    if (value) localStorage.setItem(STORAGE_KEY, '1')
    else localStorage.removeItem(STORAGE_KEY)
  } catch {
    // storage unavailable (private mode); the unlock then lasts until reload
  }
}

// Shared by App.vue, the router guard and the search view.
const unlocked = ref(readStored())
let typed = ''

export function isUnlockPassword(candidate: string): boolean {
  return sha256Hex(candidate) === UNLOCK_HASH
}

function isTypingTarget(target: EventTarget | null): boolean {
  const el = target as HTMLElement | null
  if (!el || !el.tagName) return false
  return el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT' || el.isContentEditable
}

export function useSecretUnlock() {
  function unlock() {
    unlocked.value = true
    writeStored(true)
  }

  function lock() {
    unlocked.value = false
    writeStored(false)
  }

  /** For the logo-tap prompt (phones cannot type the passphrase blind). */
  function tryPassword(candidate: string): boolean {
    if (!isUnlockPassword(candidate.trim())) return false
    unlock()
    return true
  }

  /** Feed every keydown; unlocks when the last typed characters hash to the passphrase. */
  function handleKeydown(e: KeyboardEvent): boolean {
    if (unlocked.value || e.ctrlKey || e.metaKey || e.altKey || isTypingTarget(e.target)) {
      return false
    }
    if (e.key.length !== 1) return false

    typed = (typed + e.key).slice(-UNLOCK_LENGTH)
    if (typed.length === UNLOCK_LENGTH && isUnlockPassword(typed)) {
      typed = ''
      unlock()
      return true
    }
    return false
  }

  return { unlocked, tryPassword, handleKeydown, lock }
}

/** Test hook: clears the typed-key buffer. */
export function resetUnlockBuffer() {
  typed = ''
}
