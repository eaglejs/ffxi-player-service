import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import ChatLog from '@/components/ChatLog.vue'
import { usePlayerStore } from '@/stores/player'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn((key) => {
    if (key === 'timeStampsEnabled') return 'false'
    return '{}'
  }),
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

// Mock uuid helper
vi.mock('@/helpers/utils', () => ({
  uuid: () => 'test-uuid',
  isIPhone: () => false,
  isAndroid: () => false
}))

describe('ChatLog.vue', () => {
  let router: any
  let playerStore: any

  beforeEach(async () => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    playerStore = usePlayerStore()
    playerStore.chatLog = []
    playerStore.fetchChatLog = vi.fn().mockResolvedValue([])
    playerStore.fetchChatLogByMessageType = vi.fn().mockResolvedValue([])

    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: { template: 'Home' } },
        { path: '/players/:id', component: { template: 'Player' } }
      ]
    })

    await router.push('/players/123')
    await router.isReady()
  })

  describe('component rendering', () => {
    it('renders card with header and body', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      expect(wrapper.find('.card').exists()).toBe(true)
      expect(wrapper.find('.card-header').exists()).toBe(true)
      expect(wrapper.find('.card-body.chat-log').exists()).toBe(true)
    })

    it('renders dropdown for chat filter', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const dropdown = wrapper.find('.dropdown-toggle')
      expect(dropdown.exists()).toBe(true)
    })

    it('renders timestamp toggle switch', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const toggle = wrapper.find('input[type="checkbox"]')
      expect(toggle.exists()).toBe(true)
      expect(toggle.attributes('id')).toBe('flexSwitchCheckChecked')
    })

    it('renders scroll buttons', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const buttons = wrapper.findAll('.arrow-btns')
      expect(buttons.length).toBe(2)
    })
  })

  describe('chat log display', () => {
    it('renders loading state initially', async () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      expect(wrapper.find('.spinner-grow').exists()).toBe(true)
    })

    it('displays "No messages" when chatLog is empty and not loading', async () => {
      playerStore.chatLog = []

      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      // Wait for mount and set not loading
      await wrapper.vm.$nextTick()
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('No')
      expect(wrapper.text()).toContain('messages found')
    })
  })

  describe('chat filter functionality', () => {
    it('displays current chat filter value', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const filterButton = wrapper.find('.dropdown-toggle')
      expect(filterButton.text()).toBeTruthy()
    })

    it('has computed chatLog property', async () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      await wrapper.vm.$nextTick()
      
      // chatLog is computed from playerStore
      expect(wrapper.vm.chatLog).toBeDefined()
      expect(Array.isArray(wrapper.vm.chatLog)).toBe(true)
    })
  })

  describe('message formatting', () => {
    it('formats messages correctly', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const result = wrapper.vm.formatMessage('Simple message')
      expect(result).toEqual([{ text: 'Simple message', style: '' }])
    })

    it('handles messages with translation markers', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const result = wrapper.vm.formatMessage('Text・translation・more')
      expect(result.length).toBeGreaterThan(1)
    })

    it('handles empty message', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const result = wrapper.vm.formatMessage('')
      expect(result).toEqual([{ text: '', style: '' }])
    })
  })

  describe('timestamp functionality', () => {
    it('toggles timestamp display', async () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const initialValue = wrapper.vm.timeStampsEnabled
      wrapper.vm.toggleTimeStamp()
      
      expect(wrapper.vm.timeStampsEnabled).toBe(!initialValue)
    })

    it('converts UTC to local time', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      const result = wrapper.vm.toLocalTime('2024-01-01T00:00:00.000Z')
      expect(typeof result).toBe('string')
      expect(result.length).toBeGreaterThan(0)
    })
  })

  describe('chat color mapping', () => {
    it('maps message types to colors correctly', () => {
      const wrapper = mount(ChatLog, {
        global: {
          plugins: [router]
        }
      })

      expect(wrapper.vm.chatColor('party')).toBe('party')
      expect(wrapper.vm.chatColor('tell')).toBe('tell')
      expect(wrapper.vm.chatColor('linkshell1')).toBe('linkshell1')
      expect(wrapper.vm.chatColor('say')).toBe('say')
      expect(wrapper.vm.chatColor('unknown')).toBe('say') // default
    })
  })
})
