<template>
  <div :data-bs-theme="theme">
    <div class="card mb-3 mt-3 p-0">
      <div class="card-header">
        <div class="d-flex justify-content-between">
          <h2 class="p-0 m-0">Title: ({{ user?.title }})</h2>
        </div>
      </div>
    </div>
    <div class="container-fluid">
      <div class="row">
        <UserStats class="col-lg-3 p-0" :stats="user?.stats" />
        <UserCurrency2 class="col-lg-3 ms-lg-3 mt-lg-0 mt-3" :currencies="user?.currency2" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import UserStats from '@/components/UserStats.vue'
import UserCurrency2 from '@/components/UserCurrency2.vue'

const props = defineProps({
  user: Object,
})

const theme: any = computed(() => themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light')
const themeStore = useThemeStore()

</script>

<style scoped lang="scss"></style>