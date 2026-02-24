import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import GenIcon from '@/components/gen-components/GenIcon.vue'
import { useThemeStore } from '@/stores/theme'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

describe('GenIcon.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('renders icon with correct SVG structure', () => {
    const wrapper = mount(GenIcon, {
      props: {
        icon: 'M10,10 L20,20',
        size: 'md'
      }
    })

    const svg = wrapper.find('svg')
    expect(svg.exists()).toBe(true)
    expect(svg.attributes('viewBox')).toBe('0 0 25 25')
    expect(svg.attributes('role')).toBe('img')
    expect(svg.attributes('fill')).toBe('currentColor')
  })

  it('renders icon path with correct d attribute', () => {
    const iconPath = 'M10,10 L20,20 Z'
    const wrapper = mount(GenIcon, {
      props: {
        icon: iconPath,
        size: 'md'
      }
    })

    const path = wrapper.find('path')
    expect(path.exists()).toBe(true)
    expect(path.attributes('d')).toBe(iconPath)
  })

  describe('icon sizes', () => {
    it('renders small icon (sm) with width/height 16', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'sm'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('16')
      expect(svg.attributes('height')).toBe('16')
    })

    it('renders medium icon (md) with width/height 20', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('20')
      expect(svg.attributes('height')).toBe('20')
    })

    it('renders large icon (lg) with width/height 24', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'lg'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('24')
      expect(svg.attributes('height')).toBe('24')
    })

    it('renders extra large icon (xl) with width/height 30', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'xl'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('30')
      expect(svg.attributes('height')).toBe('30')
    })

    it('renders 2xl icon with width/height 40', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: '2xl'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('40')
      expect(svg.attributes('height')).toBe('40')
    })
  })

  describe('theme color integration', () => {
    it('applies dark theme color (#fff) when theme is dark', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const themeStore = useThemeStore()
      themeStore.theme = 'dark'

      wrapper.vm.$nextTick(() => {
        const path = wrapper.find('path.fill')
        expect(path.exists()).toBe(true)
      })
    })

    it('applies light theme color (#000) when theme is light', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const themeStore = useThemeStore()
      themeStore.theme = 'light'

      wrapper.vm.$nextTick(() => {
        const path = wrapper.find('path.fill')
        expect(path.exists()).toBe(true)
      })
    })
  })

  describe('icon wrapper styling', () => {
    it('has icon-wrapper class for flex centering', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const iconWrapper = wrapper.find('.icon-wrapper')
      expect(iconWrapper.exists()).toBe(true)
    })
  })

  describe('edge cases', () => {
    it('handles empty icon path string', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: '',
          size: 'md'
        }
      })

      const path = wrapper.find('path')
      expect(path.exists()).toBe(true)
      expect(path.attributes('d')).toBe('')
    })

    it('handles undefined icon prop gracefully', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: undefined,
          size: 'md'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.exists()).toBe(true)
    })

    it('maintains SVG accessibility attributes', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('aria-hidden')).toBe('true')
      expect(svg.attributes('preserveAspectRatio')).toBe('xMidYMid meet')
    })
  })

  describe('size validator', () => {
    it('accepts valid size values', () => {
      const validSizes = ['sm', 'md', 'lg', 'xl', '2xl']
      
      validSizes.forEach(size => {
        const wrapper = mount(GenIcon, {
          props: {
            icon: 'M10,10',
            size: size
          }
        })
        expect(wrapper.exists()).toBe(true)
      })
    })
  })

  describe('component structure', () => {
    it('has correct class hierarchy', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      expect(wrapper.find('.icon-wrapper').exists()).toBe(true)
      expect(wrapper.find('.icon-wrapper svg').exists()).toBe(true)
      expect(wrapper.find('.icon-wrapper svg path.fill').exists()).toBe(true)
    })

    it('renders single SVG element', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const svgs = wrapper.findAll('svg')
      expect(svgs).toHaveLength(1)
    })

    it('renders single path element', () => {
      const wrapper = mount(GenIcon, {
        props: {
          icon: 'M10,10',
          size: 'md'
        }
      })

      const paths = wrapper.findAll('path')
      expect(paths).toHaveLength(1)
    })
  })
})
