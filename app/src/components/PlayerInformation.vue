<template>
  <div>
    <div class="container-fluid mb-3">
      <div class="row">
        <div class="col-lg-12">
          <div class="card">
            <div class="card-header">
              <div class="d-flex justify-content-between">
                <h2 class="col-8 p-0 m-0">Title: ({{ player?.title }})</h2>
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
            <Player :player-id="playerId" />
            <div class="row mt-3 gx-3">
              <div class="col-lg-12 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-0">
                <div class="row gx-3">
                  <div class="col-xl-6 col-lg-12 col-md-12 col-sm-6 mt-xl-0 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-0">
                    <PlayerCurrencies :currencies="player?.currency1" :type="1" />
                  </div>
                  <div class="col-xl-6 col-lg-12 col-md-12 col-sm-6 mt-xl-0 mt-lg-3 mt-md-3 mt-sm-0 mt-3">
                    <PlayerCurrencies :currencies="player?.currency2" :type="2" />
                  </div>
                </div>
              </div>
            </div>
            <div class="row mt-3 gx-3">
              <div class="col-lg-12 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-0">
                <div class="row gx-3">
                  <div class="col-xl-6 col-lg-12 col-md-12 col-sm-6 mt-xl-0 mt-lg-0 mt-md-0 mt-sm-0 mt-xs-0 mt-0">
                    <PlayerStats :stats="player?.stats" />
                  </div>
                  <div class="col-xl-6 col-lg-12 col-md-12 col-sm-6 mt-xl-0 mt-lg-3 mt-md-3 mt-sm-0 mt-3">
                    <PlayerResistances :resistances="player?.stats" />
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
        <div class="col-md-6 mt-lg-0 mt-md-0 mt-3">
          <section class="mb-3">
            <ExperiencePoints :player="player" />
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
import { iconsPath } from '@/helpers/config'
import PlayerStats from '@/components/PlayerStats.vue'
import PlayerResistances from '@/components/PlayerResistances.vue'
import PlayerCurrencies from '@/components/PlayerCurrencies.vue'
import ExperiencePoints from './ExperiencePoints.vue'
import Player from '@/components/PlayerVitals.vue'
import ChatLog from './ChatLog.vue'
import { usePlayerStore } from '@/stores/player'
import type { Player as PlayerType } from '@/types/Player'

const playerStore = usePlayerStore()
const props = defineProps({
  playerId: Number
})
const player = computed<PlayerType>(() => playerStore.players.get(props.playerId ?? 0))

const formattedGil: ComputedRef<string> = computed(
  () => parseInt(String(player.value?.gil)).toLocaleString() || '0'
)
const gilIcon: ComputedRef<string> = computed(() => `${iconsPath}gil.webp`)
</script>

<style scoped lang="scss">
.gil {
  height: 20px;
  width: 20px;
}
</style>
