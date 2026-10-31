<template>
  <nav class="navbar">
    <div class="container-fluid ms-2 me-2 header-container">
      <h1 class="mb-0">
        <RouterLink class="navbar-brand" to="/" @click="onLogoClick"
          ><img :src="`${imagesPath}sam.webp`" alt="Samurai" class="me-2 logo" />FFXI
          Stats</RouterLink
        >
      </h1>
      <div class="d-flex align-items-center header-actions">
        <ul class="d-flex align-items-center list-unstyled app-menu m-0">
          <li>
            <RouterLink class="theme-btn btn btn-light me-1" to="/" aria-label="Home">
              <GenIcon :icon="mdiHome" size="xl" />
            </RouterLink>
          </li>
          <li>
            <RouterLink class="theme-btn btn btn-light me-1" to="/charts" aria-label="Charts">
              <GenIcon :icon="mdiChartTimelineVariant" size="xl" />
            </RouterLink>
          </li>
          <li v-if="unlocked" class="unlock-reveal">
            <RouterLink class="theme-btn btn btn-light me-1" to="/inventory" aria-label="Inventory search">
              <GenIcon :icon="mdiMagnify" size="xl" />
            </RouterLink>
          </li>
          <li>
            <RouterLink class="theme-btn btn btn-light me-1" to="/rema" aria-label="REMA weapons">
              <GenIcon :icon="mdiSwordCross" size="xl" />
            </RouterLink>
          </li>
        </ul>
        <GenTooltip v-if="isDesktop" :tip="fullScreenTitleText" placement="bottom">
          <template #main>
            <button
              ref="fullScreenElement"
              class="theme-btn btn btn-light me-1"
              :aria-label="fullScreenTitleText"
              @click="toggleFullscreen"
            >
              <GenIcon :icon="getFullScreenIcon" size="xl" />
            </button>
          </template>
        </GenTooltip>
        <GenTooltip :tip="themeTitleText" placement="bottom">
          <template #main>
            <button
              ref="themeElement"
              class="theme-btn btn btn-light"
              :aria-label="themeTitleText"
              @click="toggleTheme"
              data-bs-toggle="tooltip"
              data-bs-placement="bottom"
              :title="themeTitleText"
            >
              <GenIcon :icon="getThemeIcon" size="xl" />
            </button>
          </template>
        </GenTooltip>
      </div>
    </div>
  </nav>
  <RouterView />
  <PasswordPrompt v-if="showPrompt" @close="showPrompt = false" />
  <GenAppVersion :themeColor="themeColor" />
</template>

<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { watch, onMounted, computed, ref } from 'vue'
import type { ComputedRef, Ref } from 'vue'
import {
  mdiChartTimelineVariant,
  mdiFullscreen,
  mdiFullscreenExit,
  mdiHome,
  mdiMagnify,
  mdiSwordCross,
  mdiWeatherNight,
  mdiWeatherSunny
} from '@mdi/js'
import { useThemeStore } from '@/stores/theme'
import { imagesPath } from '@/helpers/config'
import GenTooltip from '@/components/gen-components/GenTooltip.vue'
import GenIcon from '@/components/gen-components/GenIcon.vue'
import GenAppVersion from '@/components/gen-components/GenAppVersion.vue'
import PasswordPrompt from '@/components/inventory/PasswordPrompt.vue'
import { useSecretUnlock } from '@/composables/useSecretUnlock'

const themeStore = useThemeStore()
const router = useRouter()
const { unlocked, handleKeydown: handleUnlockKey } = useSecretUnlock()

// Phones cannot type the passphrase blind, so 7 quick taps on the logo open a prompt instead.
const LOGO_TAPS = 7
const LOGO_TAP_WINDOW_MS = 3000
const showPrompt: Ref<boolean> = ref(false)
let logoTaps: number[] = []

function onLogoClick(): void {
  if (unlocked.value) return
  const now = Date.now()
  logoTaps = [...logoTaps.filter((t) => now - t < LOGO_TAP_WINDOW_MS), now]
  if (logoTaps.length >= LOGO_TAPS) {
    logoTaps = []
    showPrompt.value = true
  }
}

function isTyping(target: EventTarget | null): boolean {
  const el = target as HTMLElement | null
  return (
    !!el &&
    (el.tagName === 'INPUT' ||
      el.tagName === 'TEXTAREA' ||
      el.tagName === 'SELECT' ||
      el.isContentEditable)
  )
}

const theme: ComputedRef<string> = computed(() => themeStore.theme)
const themeColor: ComputedRef<string> = computed(() => (theme.value === 'dark' ? '#fff' : '#000'))
const themeTitleText: ComputedRef<string> = computed(() =>
  theme.value === 'dark' ? 'Set Light Theme' : 'Set Dark Theme'
)
const getThemeIcon: ComputedRef<string> = computed(() =>
  theme.value === 'dark' ? mdiWeatherSunny : mdiWeatherNight
)
const getFullScreenIcon: ComputedRef<string> = computed(() =>
  isFullscreen.value ? mdiFullscreenExit : mdiFullscreen
)
const fullScreenTitleText: ComputedRef<string> = computed(() =>
  isFullscreen.value ? 'Exit Fullscreen' : 'Enter Fullscreen'
)
const isDesktop: ComputedRef<boolean> = computed(() => !/iPhone|Android/i.test(navigator.userAgent))

const isFullscreen: Ref<boolean> = ref(false)

function toggleTheme(): void {
  themeStore.setTheme(theme.value === 'dark' ? 'light' : 'dark')
}

function toggleFullscreen(): void {
  if (!document.fullscreenElement && !isFullscreen.value) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    }
    isFullscreen.value = false
  }
}

watch(theme, (newTheme) => {
  document.documentElement.setAttribute('data-bs-theme', newTheme)
})

onMounted(() => {
  document.documentElement.setAttribute('data-bs-theme', theme.value)

  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })

  // add event listener that listens to 1 key press to toggle home page
  document.addEventListener('keydown', (e) => {
    handleUnlockKey(e)
    // Number shortcuts must not fire while typing (e.g. searching for "+1" items)
    if (isTyping(e.target) || e.ctrlKey || e.metaKey || e.altKey) return
    if (e.key === '1') {
      router.push('/')
    } else if (e.key === '2') {
      router.push('/charts')
    } else if (e.key === '3' && unlocked.value) {
      router.push('/inventory')
    } else if (e.key === '4') {
      router.push('/rema')
    }
  })
})
</script>

<style scoped lang="scss">
.header-container {
  flex-wrap: nowrap;
}

.navbar-brand {
  align-items: center;
  display: inline-flex;
}

.logo {
  height: calc(82px / 1.3);
  width: calc(49px / 1.3);
}

.theme-btn {
  align-items: center;
  background: none;
  border: none;
  display: inline-flex;
  font-size: 1.5rem;
  height: 45px;
  justify-content: center;
}

@media (max-width: 576px) {
  .header-container {
    margin-left: 0.25rem !important;
    margin-right: 0.25rem !important;
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }

  .navbar-brand {
    font-size: 1.05rem;
    margin-right: 0.25rem;
  }

  .logo {
    height: 44px;
    margin-right: 0.35rem !important;
    width: auto;
  }

  .header-actions,
  .app-menu {
    gap: 0.15rem;
  }

  .theme-btn {
    height: 36px;
    margin-right: 0 !important;
    padding: 0.25rem 0.35rem;

    :deep(.iconify) {
      height: 22px;
      width: 22px;
    }
  }
}

@media (max-width: 380px) {
  .navbar-brand {
    font-size: 0.95rem;
  }

  .logo {
    height: 38px;
  }

  .theme-btn {
    height: 32px;
    padding: 0.2rem 0.25rem;

    :deep(.iconify) {
      height: 20px;
      width: 20px;
    }
  }
}

.fill {
  fill: v-bind(themeColor);
}

.unlock-reveal {
  animation: reveal 0.6s ease-out;
}

@keyframes reveal {
  from {
    opacity: 0;
    transform: scale(0.6);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.router-link-active:not(.navbar-brand) {
  position: relative;

  &::after {
    animation: borderExpand 0.5s forwards;
    background-color: var(--menu-color);
    border-radius: 10px;
    bottom: 0;
    content: '';
    height: 3px;
    left: 50%;
    position: absolute;
    transform: translateX(-50%);
    width: 90%;
  }
}

@keyframes borderExpand {
  from {
    width: 0;
  }
  to {
    width: 90%;
  }
}
</style>
