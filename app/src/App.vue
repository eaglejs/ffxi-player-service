<template>
  <nav class="navbar">
    <div class="container-fluid ms-2 me-2">
      <h1>
        <RouterLink class="navbar-brand" to="/"><img :src="`${imagesPath}sam.webp`" alt="Samurai" class="me-2 logo" />FFXI Stats</RouterLink>
      </h1>
      <div class="d-flex">
        <button ref="fullScreenElement" class="theme-btn btn btn-light me-1" v-if="isDesktop" @click="toggleFullscreen"  data-bs-toggle="tooltip"
          data-bs-placement="bottom" :title="fullScreenTitleText">
          <svg viewBox="0 0 25 25" width="30" height="30">
            <path class="fill" :d="getFullScreenIcon"></path>
          </svg>
        </button>
        <button ref="themeElement" class="theme-btn btn btn-light" @click="toggleTheme"  data-bs-toggle="tooltip"
          data-bs-placement="bottom" :title="themeTitleText">
          <svg viewBox="0 0 25 25" width="30" height="30">
            <path class="fill" :d="getThemeIcon"></path>
          </svg>
        </button>
      </div>
    </div>
  </nav>
  <RouterView />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import { watch, onMounted, computed, ref, onUpdated, onBeforeUpdate, onUnmounted } from 'vue'
import type { ComputedRef, Ref } from 'vue'
import { mdiWeatherSunny, mdiWeatherNight, mdiFullscreen, mdiFullscreenExit } from '@mdi/js'
import { useThemeStore } from '@/stores/theme'
import { imagesPath } from '@/helpers/config'
import * as bootstrap from 'bootstrap'

const themeStore = useThemeStore()
const fullScreenElement = ref()
const themeElement = ref()

const theme: ComputedRef<string> = computed(() => themeStore.theme)
const themeColor: ComputedRef<string> = computed(() => theme.value === 'dark' ? '#fff' : '#000')
const themeTitleText: ComputedRef<string> = computed(() => theme.value === 'dark' ? 'Set Light Theme' : 'Set Dark Theme')
const getThemeIcon: ComputedRef<string> = computed(() => theme.value === 'dark' ? mdiWeatherSunny : mdiWeatherNight)
const getFullScreenIcon: ComputedRef<string> = computed(() => isFullscreen.value ? mdiFullscreenExit : mdiFullscreen)
const fullScreenTitleText: ComputedRef<string> = computed(() => isFullscreen.value ? 'Exit Fullscreen' : 'Enter Fullscreen')
const isDesktop: ComputedRef<boolean> = computed(() => !(/iPhone|Android/i.test(navigator.userAgent)))

const isFullscreen: Ref<boolean> = ref(false)
let fullScreenToolTip: bootstrap.Tooltip | null = null
let themeToolTip: bootstrap.Tooltip | null = null

function toggleTheme(): void {
  themeStore.setTheme(theme.value === 'dark' ? 'light' : 'dark')
  if (theme.value === 'dark') {
    document.querySelector(':root')?.classList.add('dark-theme')
  } else if (theme.value === '' || theme.value === 'light') {
    document.querySelector(':root')?.classList.remove('dark-theme')
  }
}

function toggleFullscreen(): void {
  if (!document.fullscreenElement && !isFullscreen.value) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

watch(theme, (newTheme) => {
  document.documentElement.setAttribute('data-bs-theme', newTheme)
})

onMounted(() => {
  document.documentElement.setAttribute('data-bs-theme', theme.value)
  if (fullScreenElement.value) {
    fullScreenToolTip = new bootstrap.Tooltip(fullScreenElement.value)
  }
  if (themeElement.value) {
    themeToolTip = new bootstrap.Tooltip(themeElement.value)
  }
  if (theme.value === 'dark') {
    document.querySelector(':root')?.classList.add('dark-theme')
  } else if (theme.value === '' || theme.value === 'light') {
    document.querySelector(':root')?.classList.remove('dark-theme')
  }
})

onUpdated(() => {
  if (fullScreenElement.value) {
    fullScreenToolTip = new bootstrap.Tooltip(fullScreenElement.value)
  }
  if (themeElement.value) {
    themeToolTip = new bootstrap.Tooltip(themeElement.value)
  }
})

onBeforeUpdate(() => {
  fullScreenToolTip = bootstrap.Tooltip.getInstance(fullScreenElement.value)
  themeToolTip = bootstrap.Tooltip.getInstance(themeElement.value)
  fullScreenToolTip?.dispose()
  themeToolTip?.dispose()
})

onUnmounted(() => {
  fullScreenToolTip?.dispose()
  themeToolTip?.dispose()
})

</script>

<style scoped lang="scss">
.logo {
  height: calc(82px / 1.3);
  width: calc(49px / 1.3);
}
.theme-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  height: 50px;
  width: 50px;
}
.fill {
  fill: v-bind(themeColor);
}
</style>
