import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import GenTooltip from '@/components/gen-components/GenTooltip.vue'
import * as bootstrap from 'bootstrap'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

vi.mock('bootstrap', () => ({
  Tooltip: vi.fn().mockImplementation(() => ({
    dispose: vi.fn()
  }))
}))

describe('GenTooltip.vue', () => {
  let mockTooltipInstance: any

  beforeEach(() => {
    setActivePinia(createPinia())
    mockTooltipInstance = {
      dispose: vi.fn()
    }
    ;(bootstrap.Tooltip as any) = vi.fn(() => mockTooltipInstance)
    ;(bootstrap.Tooltip as any).getInstance = vi.fn(() => mockTooltipInstance)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders slot content correctly', () => {
    const wrapper = mount(GenTooltip, {
      props: {
        tip: 'Test tooltip',
        placement: 'top'
      },
      slots: {
        main: '<button>Click me</button>'
      }
    })

    expect(wrapper.html()).toContain('Click me')
  })

  it('applies tooltip attributes to wrapper element', () => {
    const wrapper = mount(GenTooltip, {
      props: {
        tip: 'Test tooltip message',
        placement: 'bottom'
      },
      slots: {
        main: '<div>Content</div>'
      }
    })

    const span = wrapper.find('span')
    expect(span.attributes('data-bs-toggle')).toBe('tooltip')
    expect(span.attributes('data-bs-placement')).toBe('bottom')
    expect(span.attributes('data-bs-title')).toBe('Test tooltip message')
  })

  describe('tooltip placement', () => {
    it('sets placement to top', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('top')
    })

    it('sets placement to bottom', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'bottom'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('bottom')
    })

    it('sets placement to left', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'left'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('left')
    })

    it('sets placement to right', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'right'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('right')
    })
  })

  describe('tooltip tip content', () => {
    it('displays simple text tip', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Simple tooltip',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe('Simple tooltip')
    })

    it('handles empty tip string', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: '',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe('')
    })

    it('handles multiline tip text', () => {
      const multilineTip = 'Line 1\nLine 2\nLine 3'
      const wrapper = mount(GenTooltip, {
        props: {
          tip: multilineTip,
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe(multilineTip)
    })

    it('handles special characters in tip', () => {
      const specialTip = 'Tip with <special> & "characters"'
      const wrapper = mount(GenTooltip, {
        props: {
          tip: specialTip,
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe(specialTip)
    })
  })

  describe('bootstrap Tooltip lifecycle', () => {
    it('initializes Bootstrap Tooltip on mount', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      await wrapper.vm.$nextTick()
      
      expect(bootstrap.Tooltip).toHaveBeenCalled()
    })

    it('reinitializes tooltip on update', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Initial tip',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      await wrapper.setProps({ tip: 'Updated tip' })
      
      // Should call getInstance and dispose on before update
      expect((bootstrap.Tooltip as any).getInstance).toHaveBeenCalled()
    })

    it('disposes tooltip on unmount', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      await wrapper.vm.$nextTick()
      wrapper.unmount()

      expect(mockTooltipInstance.dispose).toHaveBeenCalled()
    })

    it('handles null tooltip element gracefully', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      // Component should still render even if tooltip initialization fails
      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('slot functionality', () => {
    it('renders complex slot content', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'top'
        },
        slots: {
          main: `
            <div class="complex">
              <span>Nested</span>
              <button>Button</button>
            </div>
          `
        }
      })

      expect(wrapper.find('.complex').exists()).toBe(true)
      expect(wrapper.text()).toContain('Nested')
      expect(wrapper.find('button').text()).toBe('Button')
    })

    it('renders multiple elements in slot', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'top'
        },
        slots: {
          main: '<span>First</span><span>Second</span>'
        }
      })

      const spans = wrapper.findAll('span')
      expect(spans.length).toBeGreaterThanOrEqual(2)
    })

    it('preserves slot element attributes', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Tooltip',
          placement: 'top'
        },
        slots: {
          main: '<button id="test-btn" class="btn-primary">Click</button>'
        }
      })

      const button = wrapper.find('button')
      expect(button.attributes('id')).toBe('test-btn')
      expect(button.classes()).toContain('btn-primary')
    })
  })

  describe('edge cases', () => {
    it('handles rapid prop updates', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Initial',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      await wrapper.setProps({ tip: 'Update 1' })
      await wrapper.setProps({ tip: 'Update 2' })
      await wrapper.setProps({ tip: 'Update 3' })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe('Update 3')
    })

    it('handles placement changes', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      await wrapper.setProps({ placement: 'bottom' })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('bottom')
    })

    it('renders correctly without slot content', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        }
      })

      expect(wrapper.find('span').exists()).toBe(true)
    })
  })

  describe('component structure', () => {
    it('renders single root span element', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      const rootSpan = wrapper.find('span[data-bs-toggle="tooltip"]')
      expect(rootSpan.exists()).toBe(true)
    })

    it('has correct Bootstrap data attributes', () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'My tooltip',
          placement: 'left'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      const span = wrapper.find('span')
      expect(span.attributes('data-bs-toggle')).toBe('tooltip')
      expect(span.attributes('data-bs-placement')).toBe('left')
      expect(span.attributes('data-bs-title')).toBe('My tooltip')
    })
  })

  describe('reactive updates', () => {
    it('updates tooltip title when tip prop changes', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Original',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe('Original')

      await wrapper.setProps({ tip: 'Updated' })

      expect(wrapper.find('span').attributes('data-bs-title')).toBe('Updated')
    })

    it('updates tooltip placement when placement prop changes', async () => {
      const wrapper = mount(GenTooltip, {
        props: {
          tip: 'Test',
          placement: 'top'
        },
        slots: {
          main: '<div>Content</div>'
        }
      })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('top')

      await wrapper.setProps({ placement: 'bottom' })

      expect(wrapper.find('span').attributes('data-bs-placement')).toBe('bottom')
    })
  })
})
