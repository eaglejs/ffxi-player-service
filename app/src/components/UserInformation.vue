<template>
  <div :data-bs-theme="theme">
    <div class="container-fluid mb-3">
      <div class="row">
        <div class="col-lg-12">
          <div class="card">
            <div class="card-header">
              <div class="d-flex justify-content-between">
                <h2 class="col-8 p-0 m-0">Title: ({{ user?.title }})</h2>
                <section class="col-4 text-end">
                  <img class="gil me-2" :src="gilIcon" alt="Gil" data-bs-toggle="tooltip" data-bs-placement="bottom" title="Gil">
                  <span>{{ formattedGil }}</span>
                </section>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container-fluid">
      <div class="row">
        <div class="col-md-12">
          <User :user="user" />
        </div>
      </div>
      <div class="row">
        <div class="col-lg-3 col-sm-6">
          <UserStats :stats="user?.stats" />
        </div>
        <div class="col-lg-3 col-sm-6 mt-sm-0 mt-3">
          <UserCurrency2 :currencies="user?.currency2" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type ComputedRef } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { iconsPath } from '@/helpers/config'
import UserStats from '@/components/UserStats.vue'
import UserCurrency2 from '@/components/UserCurrency2.vue'
import User from '@/components/User.vue'

const themeStore = useThemeStore()
const props = defineProps({
  user: Object,
})

const theme: ComputedRef<"gray-dark" | "gray-light"> = computed(() => themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light')
const formattedGil: ComputedRef<string> = computed(() => parseInt(props.user?.gil).toLocaleString() || "0")
const gilIcon: ComputedRef<string> = computed(() =>`${iconsPath}gil.webp`)
</script>

<style scoped lang="scss">
.gil {
  height: 20px;
  width: 20px;
}
</style>