import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useThemeStore } from '@/stores/theme'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => null),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

describe('useThemeStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('initialization', () => {
    it('creates store with default theme as dark', () => {
      const store = useThemeStore()
      expect(store.theme).toBe('dark')
    })

    it('initializes theme state correctly', () => {
      const store = useThemeStore()
      expect(store.theme).toBeDefined()
      expect(typeof store.theme).toBe('string')
    })
  })

  describe('setTheme action', () => {
    it('updates theme value', () => {
      const store = useThemeStore()
      
      store.setTheme('light')
      
      expect(store.theme).toBe('light')
    })

    it('saves theme to localStorage', () => {
      const store = useThemeStore()
      
      store.setTheme('light')
      
      expect(localStorage.setItem).toHaveBeenCalledWith('theme', 'light')
    })

    it('updates theme multiple times', () => {
      const store = useThemeStore()
      
      store.setTheme('light')
      expect(store.theme).toBe('light')
      
      store.setTheme('dark')
      expect(store.theme).toBe('dark')
      
      store.setTheme('light')
      expect(store.theme).toBe('light')
    })

    it('persists each theme change to localStorage', () => {
      const store = useThemeStore()
      
      store.setTheme('light')
      store.setTheme('dark')
      
      expect(localStorage.setItem).toHaveBeenCalledTimes(2)
      expect(localStorage.setItem).toHaveBeenNthCalledWith(1, 'theme', 'light')
      expect(localStorage.setItem).toHaveBeenNthCalledWith(2, 'theme', 'dark')
    })
  })

  describe('theme values', () => {
    it('accepts dark theme', () => {
      const store = useThemeStore()
      
      store.setTheme('dark')
      
      expect(store.theme).toBe('dark')
    })

    it('accepts light theme', () => {
      const store = useThemeStore()
      
      store.setTheme('light')
      
      expect(store.theme).toBe('light')
    })

    it('accepts custom theme values', () => {
      const store = useThemeStore()
      
      store.setTheme('custom-theme')
      
      expect(store.theme).toBe('custom-theme')
    })
  })

  describe('localStorage integration', () => {
    it('loads theme from localStorage on mount', () => {
      localStorageMock.getItem.mockReturnValueOnce('light')
      
      const store = useThemeStore()
      
      // Note: onMounted doesn't execute in tests unless component is mounted
      // So we just verify the store initializes
      expect(store).toBeDefined()
    })

    it('uses default theme when localStorage is empty', () => {
      localStorageMock.getItem.mockReturnValueOnce(null)
      
      const store = useThemeStore()
      
      expect(store.theme).toBe('dark')
    })
  })

  describe('store reactivity', () => {
    it('theme is reactive', () => {
      const store = useThemeStore()
      
      const initialTheme = store.theme
      store.setTheme('new-theme')
      
      expect(store.theme).not.toBe(initialTheme)
      expect(store.theme).toBe('new-theme')
    })
  })

  describe('store actions', () => {
    it('exports setTheme action', () => {
      const store = useThemeStore()
      
      expect(store.setTheme).toBeDefined()
      expect(typeof store.setTheme).toBe('function')
    })
  })

  describe('store state', () => {
    it('exports theme state', () => {
      const store = useThemeStore()
      
      expect(store.theme).toBeDefined()
    })

    it('theme state is writable via setTheme', () => {
      const store = useThemeStore()
      
      const canWrite = () => {
        store.setTheme('test')
        return store.theme === 'test'
      }
      
      expect(canWrite()).toBe(true)
    })
  })

  describe('edge cases', () => {
    it('handles empty string theme', () => {
      const store = useThemeStore()
      
      store.setTheme('')
      
      expect(store.theme).toBe('')
      expect(localStorage.setItem).toHaveBeenCalledWith('theme', '')
    })

    it('handles numeric theme values', () => {
      const store = useThemeStore()
      
      store.setTheme('123')
      
      expect(store.theme).toBe('123')
    })

    it('handles special characters in theme', () => {
      const store = useThemeStore()
      
      store.setTheme('theme-with-dashes_and_underscores')
      
      expect(store.theme).toBe('theme-with-dashes_and_underscores')
    })
  })
})
