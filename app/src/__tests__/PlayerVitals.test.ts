import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PlayerVitals from '@/components/PlayerVitals.vue'
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

// Mock window.location
Object.defineProperty(window, 'location', {
  value: { pathname: '/players/123' },
  writable: true
})

describe('PlayerVitals.vue', () => {
  let mockPlayer: Player

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockPlayer = {
      playerId: 123,
      playerName: 'testPlayer',
      masterLevel: 50,
      mainJob: 'WAR',
      mainJobLevel: 99,
      subJob: 'MNK',
      subJobLevel: 49,
      status: 0,
      hpp: 75,
      mpp: 50,
      tp: 1000,
      attack: 500,
      defense: 400,
      zone: 'Bastok Markets',
      currentExemplar: 5000,
      requiredExemplar: 10000,
      buffs: {
        buff1: {
          iconId: 1,
          name: 'Protect',
          duration: 300,
          timestamp: Date.now()
        }
      }
    } as Player

    const playerStore = usePlayerStore()
    playerStore.players.set(123, mockPlayer)
  })

  describe('rendering', () => {
    it('renders component with playerId prop', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('displays capitalized player name', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('TestPlayer')
    })

    it('displays master level', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('M. lvl: 50')
    })

    it('displays job levels', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('WAR99/MNK49')
    })

    it('displays HP percentage', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('75%')
    })

    it('displays MP percentage', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('50%')
    })

    it('displays TP value', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('1000')
    })

    it('displays attack and defense', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('500')
      expect(wrapper.text()).toContain('400')
    })

    it('displays zone', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('Bastok Markets')
    })

    it('displays exemplar progress', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('5,000')
      expect(wrapper.text()).toContain('10,000')
    })
  })

  describe('computed properties', () => {
    it('capitalizes player name', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.playerName).toBe('TestPlayer')
    })

    it('calculates TP percentage correctly', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      // 1000/3000 * 100 = 33.333...%
      expect(wrapper.vm.getTP).toContain('33.33')
    })

    it('formats current exemplar with commas', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.currentExemplar).toBe('5,000')
    })

    it('formats required exemplar with commas', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.requiredExemplar).toBe('10,000')
    })

    it('calculates exemplar progress percentage', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      // 5000/10000 * 100 = 50%
      expect(wrapper.vm.exemplarProgress).toBe(50)
    })

    it('caps exemplar progress at 100% when near completion', () => {
      mockPlayer.currentExemplar = 9999
      mockPlayer.requiredExemplar = 10000
      
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.exemplarProgress).toBe(100)
    })

    it('detects dead status', () => {
      mockPlayer.status = 2
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.dead).toBe(true)
    })

    it('detects alive status', () => {
      mockPlayer.status = 0
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.dead).toBe(false)
    })

    it('converts buffs object to Map', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.playerBuffs).toBeInstanceOf(Map)
      expect(wrapper.vm.playerBuffs.size).toBe(1)
      expect(wrapper.vm.playerBuffs.get('buff1')).toBeDefined()
    })
  })

  describe('progress bars', () => {
    it('sets HP progress bar width', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const hpBar = wrapper.find('.bg-danger')
      expect(hpBar.attributes('style')).toContain('width: 75%')
    })

    it('sets MP progress bar width', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const mpBar = wrapper.find('.bg-success')
      expect(mpBar.attributes('style')).toContain('width: 50%')
    })

    it('sets TP progress bar width', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const tpBar = wrapper.find('.bg-purple')
      expect(tpBar.attributes('style')).toContain('33.33')
    })
  })

  describe('child components', () => {
    it('renders GenOnlineDot component', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'GenOnlineDot' }).exists()).toBe(true)
    })

    it('renders BuffList component', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'BuffList' }).exists()).toBe(true)
    })

    it('passes correct props to BuffList', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const buffList = wrapper.findComponent({ name: 'BuffList' })
      expect(buffList.props('player')).toEqual(mockPlayer)
      expect(buffList.props('buffData')).toBeInstanceOf(Map)
    })
  })

  describe('RouterLink', () => {
    it('applies hover animation class on non-player page', () => {
      Object.defineProperty(window, 'location', {
        value: { pathname: '/dashboard' },
        writable: true
      })

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.isNotPlayerDetailsPage).toBe(true)
    })

    it('does not apply hover animation on player details page', () => {
      Object.defineProperty(window, 'location', {
        value: { pathname: '/players/123' },
        writable: true
      })

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.isNotPlayerDetailsPage).toBe(false)
    })
  })

  describe('edge cases', () => {
    it('handles missing playerId', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: undefined }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('handles player not found in store', () => {
      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 999 }
      })
      
      expect(wrapper.vm.player).toBeUndefined()
    })

    it('handles missing buffs', () => {
      mockPlayer.buffs = undefined as any
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.playerBuffs).toBeInstanceOf(Map)
      expect(wrapper.vm.playerBuffs.size).toBe(0)
    })

    it('handles TP at maximum', () => {
      mockPlayer.tp = 3000
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.getTP).toBe('100%')
    })
  })

  describe('death animation', () => {
    it('shows dead skull when player dies', () => {
      mockPlayer.status = 2
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const skull = wrapper.find('.dead')
      expect(skull.exists()).toBe(true)
    })

    it('applies dead-mask class when dead', () => {
      mockPlayer.status = 2
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const cardBody = wrapper.find('.card-body')
      expect(cardBody.classes()).toContain('dead-mask')
    })

    it('does not apply dead-mask class when alive', () => {
      mockPlayer.status = 0
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerVitals, {
        props: { playerId: 123 }
      })
      
      const cardBody = wrapper.find('.card-body')
      expect(cardBody.classes()).not.toContain('dead-mask')
    })
  })
})
