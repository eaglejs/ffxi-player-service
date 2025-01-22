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
                  <img
                    class="gil me-2"
                    :src="gilIcon"
                    alt="Gil"
                    data-bs-toggle="tooltip"
                    data-bs-placement="bottom"
                    title="Gil"
                  />
                  <span>{{ formattedGil }}</span>
                </section>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="container-fluid">
      <div class="row gx-3">
        <div class="col-md-6">
          <section>
            <User :player-id="playerId" />
            <div class="row mt-3 gx-3">
              <div class="col-lg-6 col-sm-12 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-0">
                <UserCurrencies :currencies="user?.currency1" :type="1" />
                <UserStats class="mt-3" :stats="user?.stats" />
              </div>
              <div class="col-lg-6 col-sm-12 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-3">
                <UserCurrencies class="mt-xs-3 mt-sm-3 mt-md-3 mt-lg-0" :currencies="user?.currency2" :type="2" />
                <UserResistances class="mt-3" :resistances="user?.stats" />
              </div>
            </div>
          </section>
        </div>
        <div class="col-md-6 mt-lg-0 mt-md-0 mt-3">
          <section class="mb-3">
            <ExperiencePoints :user="user" />
          </section>
          <div class="col-lg-12 col-sm-12 mt-lg-0 mt-3">
            <ChatLog />
          </div>
        </div>
      </div>
      <div class="row mt-3 gx-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, type ComputedRef } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { iconsPath } from '@/helpers/config'
import UserStats from '@/components/UserStats.vue'
import UserResistances from '@/components/UserResistances.vue'
import UserCurrencies from '@/components/UserCurrencies.vue'
import ExperiencePoints from './ExperiencePoints.vue'
import User from '@/components/User.vue'
import ChatLog from './ChatLog.vue'
import { useUserStore } from '@/stores/user'
import type { Player } from '@/types/Player'

const themeStore = useThemeStore()
const userStore = useUserStore()
const props = defineProps({
  playerId: Number
})
const user = computed<Player>(() => userStore.players.get(props.playerId ?? 0))

const theme: ComputedRef<'gray-dark' | 'gray-light'> = computed(() =>
  themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light'
)
const formattedGil: ComputedRef<string> = computed(
  () => parseInt(String(user.value?.gil)).toLocaleString() || '0'
)
const gilIcon: ComputedRef<string> = computed(() => `${iconsPath}gil.webp`)
</script>

<style scoped lang="scss">
.gil {
  height: 20px;
  width: 20px;
}
</style>
