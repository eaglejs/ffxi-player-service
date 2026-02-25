import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'dark')

  const setTheme = (value: string) => {
    theme.value = value
    localStorage.setItem('theme', value)
  }

  return { theme, setTheme }
})
