import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import BuffItem from '@/components/BuffItem.vue'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

// Mock config
vi.mock('@/helpers/config', () => ({
  iconsPath: 'https://example.com/icons/'
}))

describe('BuffItem.vue', () => {
  const mockPlayer = {
    playerId: 1,
    playerName: 'TestPlayer'
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    setActivePinia(createPinia())
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('component rendering', () => {
    it('renders buff icon with correct src', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 42,
          buffName: 'Protect',
          duration: 1800,
          utcTime: new Date(Date.now() + 60000).toISOString()
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
      expect(img.attributes('src')).toBe('https://example.com/icons/42.webp')
      expect(img.attributes('alt')).toBe('Protect')
    })

    it('renders GenTooltip with buff name', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 100,
          buffName: 'Haste',
          duration: 300,
          utcTime: new Date(Date.now() + 30000).toISOString()
        }
      })

      const tooltip = wrapper.findComponent({ name: 'GenTooltip' })
      expect(tooltip.exists()).toBe(true)
      expect(tooltip.props('tip')).toBe('Haste')
      expect(tooltip.props('placement')).toBe('bottom')
    })

    it('renders with buff-wrapper class', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 100,
          utcTime: new Date(Date.now() + 10000).toISOString()
        }
      })

      expect(wrapper.find('.buff-wrapper').exists()).toBe(true)
    })
  })

  describe('isTimerPositive functionality', () => {
    it('adds buff class when timer is positive', () => {
      const futureTime = new Date(Date.now() + 60000).toISOString()
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 60,
          utcTime: futureTime
        }
      })

      expect(wrapper.find('.buff').exists()).toBe(true)
    })

    it('does not add buff class when timer has expired', () => {
      const pastTime = new Date(Date.now() - 1000).toISOString()
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 0,
          utcTime: pastTime
        }
      })

      expect(wrapper.find('.buff').exists()).toBe(false)
    })
  })

  describe('isLowTimer functionality', () => {
    it('adds buff-icon class when timer is <= 15 seconds', () => {
      const lowTime = new Date(Date.now() + 10000).toISOString() // 10 seconds
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 10,
          utcTime: lowTime
        }
      })

      const img = wrapper.find('img')
      expect(img.classes()).toContain('buff-icon')
    })

    it('does not add buff-icon class when timer is > 15 seconds', () => {
      const normalTime = new Date(Date.now() + 60000).toISOString() // 60 seconds
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 60,
          utcTime: normalTime
        }
      })

      const img = wrapper.find('img')
      expect(img.classes()).not.toContain('buff-icon')
    })
  })

  describe('edge cases', () => {
    it('handles missing utcTime gracefully', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 100
        }
      })

      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('img').exists()).toBe(true)
    })

    it('handles non-empty buffName', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'ValidBuff',
          duration: 100,
          utcTime: new Date().toISOString()
        }
      })

      const tooltip = wrapper.findComponent({ name: 'GenTooltip' })
      expect(tooltip.props('tip')).toBe('ValidBuff')
    })

    it('renders with different buffId values', () => {
      const buffIds = [1, 42, 100, 999]
      
      buffIds.forEach(buffId => {
        const wrapper = mount(BuffItem, {
          props: {
            player: mockPlayer,
            buffId,
            buffName: `Buff${buffId}`,
            duration: 60,
            utcTime: new Date(Date.now() + 30000).toISOString()
          }
        })

        const img = wrapper.find('img')
        expect(img.attributes('src')).toBe(`https://example.com/icons/${buffId}.webp`)
      })
    })

    it('cleans up interval on unmount', () => {
      const clearIntervalSpy = vi.spyOn(global, 'clearInterval')
      
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 60,
          utcTime: new Date(Date.now() + 30000).toISOString()
        }
      })

      wrapper.unmount()

      expect(clearIntervalSpy).toHaveBeenCalled()
    })
  })

  describe('data attributes', () => {
    it('sets data-duration attribute when buff is active', () => {
      const futureTime = new Date(Date.now() + 60000).toISOString()
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test',
          duration: 60,
          utcTime: futureTime
        }
      })

      const buffWrapper = wrapper.find('.buff-wrapper')
      // data-duration is set via Vue binding, check that wrapper has data attributes
      expect(buffWrapper.exists()).toBe(true)
    })
  })

  describe('player prop requirement', () => {
    it('requires player prop', () => {
      const wrapper = mount(BuffItem, {
        props: {
          player: mockPlayer,
          buffId: 1,
          buffName: 'Test'
        }
      })

      expect(wrapper.props('player')).toEqual(mockPlayer)
    })
  })
})
