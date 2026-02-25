import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PlayerCurrencies from '@/components/PlayerCurrencies.vue'
import PlayerNavigation from '@/components/PlayerNavigation.vue'
import PlayerResistances from '@/components/PlayerResistances.vue'
import PlayerStats from '@/components/PlayerStats.vue'

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
  value: {
    pathname: '/players/123'
  },
  writable: true
})

// Mock config
vi.mock('@/helpers/config', () => ({
  iconsPath: 'https://example.com/icons/',
  fullWsUrl: 'ws://localhost:8080',
  gilIcon: 'https://example.com/gil.png'
}))

describe('PlayerCurrencies.vue', () => {
  const mockCurrencies = {
    conquestPointsBastok: 0,
    sparksOfEminence: 0
  }

  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders component', () => {
    const wrapper = shallowMount(PlayerCurrencies, {
      props: { currencies: mockCurrencies, type: 1 }
    })
    expect(wrapper.exists()).toBe(true)
  })
})

describe('PlayerNavigation.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders component', () => {
    const wrapper = shallowMount(PlayerNavigation)
    expect(wrapper.exists()).toBe(true)
  })
})

describe('PlayerResistances.vue', () => {
  const mockPlayer = {
    stats: {
      fireResistance: 0,
      iceResistance: 0,
      windResistance: 0,
      earthResistance: 0,
      lightningResistance: 0,
      waterResistance: 0,
      lightResistance: 0,
      darkResistance: 0
    }
  }

  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders component', () => {
    const wrapper = shallowMount(PlayerResistances, {
      props: { player: mockPlayer }
    })
    expect(wrapper.exists()).toBe(true)
  })
})

describe('PlayerStats.vue', () => {
  const mockPlayer = {
    attack: 100,
    defense: 100,
    stats: {
      baseSTR: 50,
      baseAGI: 50,
      baseDEX: 50,
      baseVIT: 50,
      baseINT: 50,
      baseMND: 50,
      baseCHR: 50,
      addedSTR: 0,
      addedAGI: 0,
      addedDEX: 0,
      addedVIT: 0,
      addedINT: 0,
      addedMND: 0,
      addedCHR: 0
    }
  }

  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders component', () => {
    const wrapper = shallowMount(PlayerStats, {
      props: { player: mockPlayer }
    })
    expect(wrapper.exists()).toBe(true)
  })
})
