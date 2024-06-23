<template>
  <nav class="navbar">
    <div class="container-fluid ms-2 me-2">
      <h1>
        <a class="navbar-brand" href="#">FFXI Stats</a>
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
import { mdiWeatherSunny, mdiWeatherNight, mdiFullscreen, mdiFullscreenExit } from '@mdi/js'
import { useThemeStore } from '@/stores/theme'
import * as bootstrap from 'bootstrap'

const themeStore = useThemeStore()
const fullScreenElement = ref()
const themeElement = ref()

const theme: any = computed(() => {
  return themeStore.theme
})

const toggleTheme: any = () => {
  themeStore.setTheme(theme.value === 'dark' ? 'light' : 'dark')
}

const themeColor = computed(() => theme.value === 'dark' ? '#fff' : '#000')
const themeTitleText = computed(() => theme.value === 'dark' ? 'Set Light Theme' : 'Set Dark Theme')
const getThemeIcon: any = computed(() => theme.value === 'dark' ? mdiWeatherSunny : mdiWeatherNight)
const getFullScreenIcon: any = computed(() => isFullscreen.value ? mdiFullscreenExit : mdiFullscreen)
const fullScreenTitleText = computed(() => isFullscreen.value ? 'Exit Fullscreen' : 'Enter Fullscreen')
const isDesktop = computed(() => !(/iPhone|Android/i.test(navigator.userAgent)))

const isFullscreen = ref(false)
let fullScreenToolTip: any = null
let themeToolTip: any = null

const toggleFullscreen: any = () => {
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
  if (fullScreenToolTip) {
    fullScreenToolTip.dispose()
  }
  if (themeToolTip) {
    themeToolTip.dispose()
  }
})

onUnmounted(() => {
  fullScreenToolTip.dispose()
  themeToolTip.dispose()
})

</script>

<style scoped lang="scss">
.theme-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  width: 50px;
  height: 50px;
}
.fill {
  fill: v-bind(themeColor);
}
</style>
