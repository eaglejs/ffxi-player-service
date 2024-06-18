import { ref, onMounted } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
	const theme = ref('dark')

  const setTheme = (value: string) => {
    theme.value = value
    localStorage.setItem('theme', value)
  }

  onMounted(() => {
    theme.value = localStorage.getItem('theme') || 'dark'
  })

	return { theme, setTheme }
})
