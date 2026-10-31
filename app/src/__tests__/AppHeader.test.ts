import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import App from '@/App.vue'
import { useSecretUnlock } from '@/composables/useSecretUnlock'

const store = new Map<string, string>()
vi.stubGlobal('localStorage', {
  getItem: (k: string) => store.get(k) ?? null,
  setItem: (k: string, v: string) => void store.set(k, v),
  removeItem: (k: string) => void store.delete(k),
  clear: () => store.clear()
})

vi.mock('bootstrap', () => ({
  Tooltip: Object.assign(
    vi.fn().mockImplementation(function () {
      return { dispose: vi.fn() }
    }),
    { getInstance: vi.fn(() => ({ dispose: vi.fn() })) }
  )
}))

function makeRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/charts', component: { template: '<div />' } },
      { path: '/inventory', component: { template: '<div />' } },
      { path: '/rema', component: { template: '<div />' } }
    ]
  })
}

describe('App.vue header mobile layout', () => {
  beforeEach(() => {
    store.clear()
    setActivePinia(createPinia())
    useSecretUnlock().lock()
  })

  it('uses a single-row flex header container and zero-margin brand heading', async () => {
    const router = makeRouter()
    await router.push('/')
    const wrapper = mount(App, {
      global: { plugins: [router] }
    })

    const headerContainer = wrapper.find('nav.navbar > .header-container')
    expect(headerContainer.exists()).toBe(true)

    const heading = headerContainer.find('h1')
    expect(heading.classes()).toContain('mb-0')

    const actions = headerContainer.find('.header-actions')
    expect(actions.exists()).toBe(true)
  })

  it('provides aria-labels on all header navigation links and buttons', async () => {
    useSecretUnlock().tryPassword('ffxi')
    const router = makeRouter()
    await router.push('/')
    const wrapper = mount(App, {
      global: { plugins: [router] }
    })

    const navLinks = wrapper.findAll('.app-menu a.theme-btn')
    expect(navLinks.length).toBeGreaterThanOrEqual(3)
    for (const link of navLinks) {
      expect(link.attributes('aria-label')).toBeTruthy()
    }
  })

  it('omits the fullscreen tooltip wrapper completely on mobile user agents', async () => {
    const uaSpy = vi
      .spyOn(window.navigator, 'userAgent', 'get')
      .mockReturnValue('Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)')

    const router = makeRouter()
    await router.push('/')
    const wrapper = mount(App, {
      global: { plugins: [router] }
    })

    const tooltips = wrapper.findAll('[data-bs-toggle="tooltip"]')
    const titles = tooltips.map((t) => t.attributes('data-bs-title') || t.attributes('title'))
    expect(titles).not.toContain('Enter Fullscreen')
    expect(titles).not.toContain('Exit Fullscreen')

    uaSpy.mockRestore()
  })
})
