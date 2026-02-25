import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PlayerInformation from '@/components/PlayerInformation.vue'
import { usePlayerStore } from '@/stores/player'
import type { Player } from '@/types/Player'

// Mock config
vi.mock('@/helpers/config', () => ({
  iconsPath: 'https://example.com/icons/',
  fullWsUrl: 'ws://localhost:8080',
  fullUrl: 'http://localhost:8080',
  gilIcon: 'https://example.com/gil.png'
}))

describe('PlayerInformation.vue', () => {
  let mockPlayer: Player

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockPlayer = {
      playerId: 123,
      playerName: 'testPlayer',
      title: 'Test Title',
      gil: 1000000,
      currency1: {
        conquestPointsBastok: 100,
        sparksOfEminence: 200
      },
      currency2: {
        bayld: 300,
        hallmarks: 400
      },
      stats: {
        baseSTR: 50,
        baseAGI: 50,
        baseDEX: 50,
        baseVIT: 50,
        baseINT: 50,
        baseMND: 50,
        baseCHR: 50,
        fireResistance: 10,
        iceResistance: 10,
        windResistance: 10,
        earthResistance: 10,
        lightningResistance: 10,
        waterResistance: 10,
        lightResistance: 10,
        darkResistance: 10
      }
    } as Player

    const playerStore = usePlayerStore()
    playerStore.players.set(123, mockPlayer)
  })

  describe('rendering', () => {
    it('renders component with playerId prop', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('displays player title', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('Test Title')
    })

    it('displays formatted gil amount', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.text()).toContain('1,000,000')
    })

    it('displays gil icon with correct src', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const img = wrapper.find('img.gil')
      expect(img.exists()).toBe(true)
      expect(img.attributes('src')).toContain('gil.webp')
    })
  })

  describe('computed properties', () => {
    it('formats gil with commas', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.formattedGil).toBe('1,000,000')
    })

    it('handles zero gil', () => {
      mockPlayer.gil = 0
      const playerStore = usePlayerStore()
      playerStore.players.set(123, mockPlayer)

      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.formattedGil).toBe('0')
    })

    it('retrieves player from store', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.player.playerId).toBe(123)
      expect(wrapper.vm.player.playerName).toBe('testPlayer')
    })

    it('generates correct gilIcon path', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.gilIcon).toBe('https://example.com/icons/gil.webp')
    })
  })

  describe('child components', () => {
    it('renders PlayerVitals component', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'Player' }).exists()).toBe(true)
    })

    it('renders PlayerCurrencies components', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const currencies = wrapper.findAllComponents({ name: 'PlayerCurrencies' })
      expect(currencies).toHaveLength(2)
    })

    it('renders PlayerStats component', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'PlayerStats' }).exists()).toBe(true)
    })

    it('renders PlayerResistances component', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'PlayerResistances' }).exists()).toBe(true)
    })

    it('renders ExperiencePoints component', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'ExperiencePoints' }).exists()).toBe(true)
    })

    it('renders ChatLog component', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.findComponent({ name: 'ChatLog' }).exists()).toBe(true)
    })
  })

  describe('edge cases', () => {
    it('handles missing playerId', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: undefined }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('handles player not found in store', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 999 }
      })
      
      expect(wrapper.vm.player).toBeUndefined()
    })

    it('handles undefined gil', () => {
      const playerStore = usePlayerStore()
      playerStore.players.set(123, { ...mockPlayer, gil: undefined } as any)

      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      expect(wrapper.vm.formattedGil).toBe('NaN')
    })
  })

  describe('props', () => {
    it('passes playerId to PlayerVitals', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const vitals = wrapper.findComponent({ name: 'Player' })
      expect(vitals.props('playerId')).toBe(123)
    })

    it('passes currency1 to first PlayerCurrencies', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const currencies = wrapper.findAllComponents({ name: 'PlayerCurrencies' })
      expect(currencies[0].props('currencies')).toEqual(mockPlayer.currency1)
      expect(currencies[0].props('type')).toBe(1)
    })

    it('passes currency2 to second PlayerCurrencies', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const currencies = wrapper.findAllComponents({ name: 'PlayerCurrencies' })
      expect(currencies[1].props('currencies')).toEqual(mockPlayer.currency2)
      expect(currencies[1].props('type')).toBe(2)
    })

    it('passes player to ExperiencePoints', () => {
      const wrapper = shallowMount(PlayerInformation, {
        props: { playerId: 123 }
      })
      
      const exp = wrapper.findComponent({ name: 'ExperiencePoints' })
      expect(exp.props('player')).toEqual(mockPlayer)
    })
  })
})
