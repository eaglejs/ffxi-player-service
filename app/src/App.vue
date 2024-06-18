<template>
  <nav class="navbar">
    <div class="container-fluid">
      <a class="navbar-brand" href="#">FFXI Stats</a>
      <div class="d-flex">
        <button class="theme-btn me-1" @click="toggleTheme">
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
import { watch, onMounted, computed } from 'vue'
import { mdiWeatherSunny, mdiWeatherNight } from '@mdi/js'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const theme: any = computed(() => {
  return themeStore.theme
})

const toggleTheme: any = () => {
  themeStore.setTheme(theme.value === 'dark' ? 'light' : 'dark')
}

const themeColor = computed(() => {
  return theme.value === 'dark' ? '#fff' : '#000'
})

const getThemeIcon: any = computed(() => {
  return theme.value === 'dark' ? mdiWeatherSunny : mdiWeatherNight
})

watch(theme, (newTheme) => {
  document.documentElement.setAttribute('data-bs-theme', newTheme)
})

onMounted(() => {
  document.documentElement.setAttribute('data-bs-theme', theme.value)
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
