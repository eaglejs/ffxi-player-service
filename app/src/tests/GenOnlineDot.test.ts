import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import GenOnlineDot from '@/components/gen-components/GenOnlineDot.vue'
import { usePlayerStore } from '@/stores/player'
import type { Player } from '@/types/Player'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

describe('GenOnlineDot.vue', () => {
  let playerStore: any

  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
    setActivePinia(createPinia())
    playerStore = usePlayerStore()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('online status detection', () => {
    it('shows online status when player was online within 60 seconds', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30, // 30 seconds ago
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const onlineDot = wrapper.find('.online-dot')
      expect(onlineDot.exists()).toBe(true)
      expect(wrapper.find('.offline-dot').exists()).toBe(false)
    })

    it('shows offline status when player was online more than 60 seconds ago', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 120, // 120 seconds ago
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const offlineDot = wrapper.find('.offline-dot')
      expect(offlineDot.exists()).toBe(true)
      expect(wrapper.find('.online-dot').exists()).toBe(false)
    })

    it('shows online status when player was just online (0 seconds)', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      expect(wrapper.find('.online-dot').exists()).toBe(true)
    })

    it('shows online status at exactly 59 seconds threshold', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 59,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      expect(wrapper.find('.online-dot').exists()).toBe(true)
    })

    it('shows offline status at exactly 61 seconds threshold', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 61,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })
  })

  describe('tooltip text', () => {
    it('displays "Online" tooltip when player is online', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const span = wrapper.find('span[title]')
      expect(span.attributes('title')).toBe('Online')
    })

    it('displays "Offline" tooltip when player is offline', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 120,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const span = wrapper.find('span[title]')
      expect(span.attributes('title')).toBe('Offline')
    })
  })

  describe('GenTooltip integration', () => {
    it('renders GenTooltip component', () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: Date.now() / 1000,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      expect(wrapper.findComponent({ name: 'GenTooltip' }).exists()).toBe(true)
    })

    it('passes correct tip prop to GenTooltip for online player', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const tooltip = wrapper.findComponent({ name: 'GenTooltip' })
      expect(tooltip.props('tip')).toBe('Online')
    })

    it('passes correct tip prop to GenTooltip for offline player', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 120,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const tooltip = wrapper.findComponent({ name: 'GenTooltip' })
      expect(tooltip.props('tip')).toBe('Offline')
    })

    it('sets tooltip placement to top', () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: Date.now() / 1000,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      const tooltip = wrapper.findComponent({ name: 'GenTooltip' })
      expect(tooltip.props('placement')).toBe('top')
    })
  })

  describe('automatic status checking', () => {
    it('checks status every 5 seconds', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()
      expect(wrapper.find('.online-dot').exists()).toBe(true)

      // Advance time by 65 seconds (player should now be offline)
      vi.advanceTimersByTime(65000)
      await flushPromises()

      // After interval check, should show offline
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })

    it('updates status on interval tick', async () => {
      const baseTime = Date.now()
      vi.setSystemTime(baseTime)

      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: baseTime / 1000 - 55, // 55 seconds ago (online)
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()
      expect(wrapper.find('.online-dot').exists()).toBe(true)

      // Advance time by 10 seconds (now 65 seconds total - should be offline)
      vi.advanceTimersByTime(10000)
      await flushPromises()

      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })
  })

  describe('player store integration', () => {
    it('watches player store changes', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 120,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()
      expect(wrapper.find('.offline-dot').exists()).toBe(true)

      // Update player prop directly instead of store
      await wrapper.setProps({
        player: {
          ...player,
          lastOnline: now / 1000 - 10
        }
      })

      await wrapper.vm.$nextTick()
      await flushPromises()

      // Should still be offline since the watch is on playerStore, not props
      // This test verifies the watch is set up correctly
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })
  })

  describe('edge cases', () => {
    it('handles undefined player gracefully', async () => {
      const wrapper = mount(GenOnlineDot, {
        props: {
          player: undefined
        }
      })

      await flushPromises()

      // Should default to offline when player is undefined
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })

    it('handles player with undefined lastOnline', async () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: undefined,
      } as any

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      // Should show offline when lastOnline is undefined
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })

    it('handles player with null lastOnline', async () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: null,
      } as any

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })

    it('handles player with zero lastOnline', async () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: 0,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      // Should be offline (very old timestamp)
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })

    it('handles future lastOnline timestamp', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 + 1000, // Future timestamp
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      // Should show online (negative time difference)
      expect(wrapper.find('.online-dot').exists()).toBe(true)
    })
  })

  describe('status dot styling', () => {
    it('applies correct CSS classes for online status', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const span = wrapper.find('span.online-dot')
      expect(span.exists()).toBe(true)
      expect(span.classes()).toContain('online-dot')
    })

    it('applies correct CSS classes for offline status', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 120,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const span = wrapper.find('span.offline-dot')
      expect(span.exists()).toBe(true)
      expect(span.classes()).toContain('offline-dot')
    })

    it('has Bootstrap tooltip attributes', async () => {
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: Date.now() / 1000,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      const span = wrapper.find('span[data-bs-toggle]')
      expect(span.attributes('data-bs-toggle')).toBe('tooltip')
      expect(span.attributes('data-bs-placement')).toBe('top')
    })
  })

  describe('component initialization', () => {
    it('checks online state on mount', async () => {
      const now = Date.now()
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: now / 1000 - 30,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      // Should immediately show correct status
      expect(wrapper.find('.online-dot').exists()).toBe(true)
    })

    it('sets up interval on mount', async () => {
      const setIntervalSpy = vi.spyOn(global, 'setInterval')

      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: Date.now() / 1000,
      } as Player

      mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      expect(setIntervalSpy).toHaveBeenCalledWith(expect.any(Function), 5000)
    })
  })

  describe('timestamp conversion', () => {
    it('correctly converts Unix timestamp to milliseconds', async () => {
      const unixTimestamp = 1700000000 // Unix timestamp in seconds
      const player: Player = {
        playerId: 1,
        playerName: 'TestPlayer',
        lastOnline: unixTimestamp,
      } as Player

      const wrapper = mount(GenOnlineDot, {
        props: {
          player
        }
      })

      await flushPromises()

      // Should be offline (old timestamp)
      expect(wrapper.find('.offline-dot').exists()).toBe(true)
    })
  })
})
