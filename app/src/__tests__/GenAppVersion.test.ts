import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import GenAppVersion from '@/components/gen-components/GenAppVersion.vue'

// Mock the config helper
vi.mock('@/helpers/config', () => ({
  uiPackageVersion: '1.2.3'
}))

describe('GenAppVersion.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('component rendering', () => {
    it('renders the component with correct structure', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      expect(wrapper.find('.app-version').exists()).toBe(true)
      expect(wrapper.find('.version-text').exists()).toBe(true)
    })

    it('displays version text with correct format', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      const versionText = wrapper.find('.version-text')
      expect(versionText.text()).toBe('Version 1.2.3')
    })

    it('applies correct CSS classes', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      const versionText = wrapper.find('.version-text')
      expect(versionText.classes()).toContain('version-text')
      expect(versionText.classes()).toContain('my-2')
      expect(versionText.classes()).toContain('mx-0')
      expect(versionText.classes()).toContain('text-center')
    })
  })

  describe('props handling', () => {
    it('accepts themeColor prop', () => {
      const themeColor = '#e74c3c'
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor
        }
      })

      expect(wrapper.props('themeColor')).toBe(themeColor)
    })

    it('works with different theme colors', () => {
      const colors = ['#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6']
      
      colors.forEach(color => {
        const wrapper = mount(GenAppVersion, {
          props: {
            themeColor: color
          }
        })

        expect(wrapper.props('themeColor')).toBe(color)
        expect(wrapper.find('.version-text').exists()).toBe(true)
      })
    })

    it('handles theme color with rgba format', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: 'rgba(52, 152, 219, 0.8)'
        }
      })

      expect(wrapper.props('themeColor')).toBe('rgba(52, 152, 219, 0.8)')
    })

    it('handles theme color with hsl format', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: 'hsl(204, 70%, 53%)'
        }
      })

      expect(wrapper.props('themeColor')).toBe('hsl(204, 70%, 53%)')
    })
  })

  describe('computed properties', () => {
    it('version computed property returns uiPackageVersion', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      const versionText = wrapper.find('.version-text').text()
      expect(versionText).toContain('1.2.3')
    })
  })

  describe('edge cases', () => {
    it('renders correctly with empty theme color', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: ''
        }
      })

      expect(wrapper.find('.app-version').exists()).toBe(true)
      expect(wrapper.find('.version-text').text()).toBe('Version 1.2.3')
    })

    it('renders correctly with black theme color', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#000000'
        }
      })

      expect(wrapper.find('.version-text').exists()).toBe(true)
    })

    it('renders correctly with white theme color', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#ffffff'
        }
      })

      expect(wrapper.find('.version-text').exists()).toBe(true)
    })
  })

  describe('structure validation', () => {
    it('has correct parent-child hierarchy', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      const appVersion = wrapper.find('.app-version')
      const versionText = appVersion.find('.version-text')
      
      expect(appVersion.exists()).toBe(true)
      expect(versionText.exists()).toBe(true)
      expect(versionText.element.tagName.toLowerCase()).toBe('p')
    })

    it('version text is a paragraph element', () => {
      const wrapper = mount(GenAppVersion, {
        props: {
          themeColor: '#3498db'
        }
      })

      const versionText = wrapper.find('.version-text')
      expect(versionText.element.tagName).toBe('P')
    })
  })
})
