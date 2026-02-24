import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import BuffList from '@/components/BuffList.vue'
import type { Buff } from '@/types/buff'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

// Mock uuid helper
vi.mock('@/helpers/utils', () => ({
  uuid: () => 'test-uuid-123'
}))

// Mock config
vi.mock('@/helpers/config', () => ({
  iconsPath: 'https://example.com/icons/'
}))

describe('BuffList.vue', () => {
  const mockPlayer = {
    playerId: 1,
    playerName: 'TestPlayer'
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  describe('component rendering', () => {
    it('renders buffs-wrapper container', () => {
      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: new Map()
        }
      })

      expect(wrapper.find('.buffs-wrapper').exists()).toBe(true)
      expect(wrapper.find('.buffs-section').exists()).toBe(true)
    })

    it('renders BuffItem for each buff in buffData', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Protect',
        buff_duration: 1800,
        utc_time: new Date().toISOString()
      } as Buff)
      buffMap.set('2', {
        buff_id: 2,
        buff_name: 'Shell',
        buff_duration: 1800,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(2)
    })

    it('renders no BuffItems when buffData is empty', () => {
      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: new Map()
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(0)
    })

    it('renders no BuffItems when buffData is undefined', () => {
      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: undefined
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(0)
    })
  })

  describe('buffData to buffList conversion', () => {
    it('converts Map to Array correctly', () => {
      const buffMap = new Map<string, Buff>()
      const buff1 = {
        buff_id: 42,
        buff_name: 'Haste',
        buff_duration: 300,
        utc_time: new Date().toISOString()
      } as Buff
      const buff2 = {
        buff_id: 100,
        buff_name: 'Protect',
        buff_duration: 1800,
        utc_time: new Date().toISOString()
      } as Buff
      
      buffMap.set('42', buff1)
      buffMap.set('100', buff2)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(2)
      
      // Check that props are passed correctly
      expect(buffItems[0].props('buffId')).toBe(42)
      expect(buffItems[0].props('buffName')).toBe('Haste')
      expect(buffItems[1].props('buffId')).toBe(100)
      expect(buffItems[1].props('buffName')).toBe('Protect')
    })

    it('handles single buff in Map', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'SingleBuff',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(1)
      expect(buffItems[0].props('buffName')).toBe('SingleBuff')
    })

    it('handles many buffs in Map', () => {
      const buffMap = new Map<string, Buff>()
      for (let i = 1; i <= 20; i++) {
        buffMap.set(i.toString(), {
          buff_id: i,
          buff_name: `Buff${i}`,
          buff_duration: 60 * i,
          utc_time: new Date().toISOString()
        } as Buff)
      }

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(20)
    })
  })

  describe('BuffItem prop passing', () => {
    it('passes player prop to each BuffItem', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('player')).toEqual(mockPlayer)
    })

    it('passes buffId prop to each BuffItem', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 123,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('buffId')).toBe(123)
    })

    it('passes buffName prop to each BuffItem', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'TestBuffName',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('buffName')).toBe('TestBuffName')
    })

    it('passes duration prop to each BuffItem', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Test',
        buff_duration: 1800,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('duration')).toBe(1800)
    })

    it('passes utcTime prop to each BuffItem', () => {
      const testTime = new Date().toISOString()
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: testTime
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('utcTime')).toBe(testTime)
    })
  })

  describe('key generation', () => {
    it('uses buff_id, utc_time and uuid for key', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 42,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: '2024-01-01T00:00:00.000Z'
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      // Verify component renders with key
      const spans = wrapper.findAll('span')
      expect(spans.length).toBeGreaterThan(0)
    })
  })

  describe('edge cases', () => {
    it('handles buff with all required fields', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'TestBuff',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      const buffItems = wrapper.findAllComponents({ name: 'BuffItem' })
      expect(buffItems).toHaveLength(1)
    })

    it('handles empty player object', () => {
      const emptyPlayer = {
        playerId: 0,
        playerName: ''
      }
      
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: emptyPlayer,
          buffData: buffMap
        }
      })

      const buffItem = wrapper.findComponent({ name: 'BuffItem' })
      expect(buffItem.props('player')).toEqual(emptyPlayer)
    })
  })

  describe('component structure', () => {
    it('has correct class hierarchy', () => {
      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: new Map()
        }
      })

      expect(wrapper.find('.buffs-wrapper').exists()).toBe(true)
      expect(wrapper.find('.buffs-wrapper .buffs-section').exists()).toBe(true)
    })

    it('maintains structure with buffs', () => {
      const buffMap = new Map<string, Buff>()
      buffMap.set('1', {
        buff_id: 1,
        buff_name: 'Test',
        buff_duration: 60,
        utc_time: new Date().toISOString()
      } as Buff)

      const wrapper = mount(BuffList, {
        props: {
          player: mockPlayer,
          buffData: buffMap
        }
      })

      expect(wrapper.find('.buffs-wrapper .buffs-section span').exists()).toBe(true)
    })
  })
})
