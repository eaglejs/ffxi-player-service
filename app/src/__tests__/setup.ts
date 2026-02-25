import { config, RouterLinkStub } from '@vue/test-utils'
import { vi } from 'vitest'

config.global.stubs = {
  RouterLink: RouterLinkStub,
  RouterView: true,
}

// jsdom does not implement scrollTo and throws DATA_CLONE_ERR (code 25) when
// ScrollOptions are passed to a partially-detached element during test teardown.
window.scrollTo = vi.fn()
Element.prototype.scrollTo = vi.fn()

