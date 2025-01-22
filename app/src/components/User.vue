<template>
  <div class="card user-card" :data-bs-theme="theme">
    <svg ref="deadElement" class="dead" viewBox="0 0 25 25" width="30" height="30">
      <path class="skull-fill" :d="mdiSkullCrossbones"></path>
    </svg>
    <div class="card-header">
      <div class="d-flex justify-content-between">
        <h2 class="mb-0">
          <span
            ref="titleElement"
            :class="onlineStatusDot"
            :title="onlineTitleText"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
          ></span
          ><RouterLink :to="`/users/${user?.playerId}`">{{ playerName }}</RouterLink> M. lvl:
          {{ user?.masterLevel }} ({{ user?.mainJob }}{{ user?.mainJobLevel }}/{{ user?.subJob
          }}{{ user?.subJobLevel }}) -
          <span>
            {{ user?.zone }}
          </span>
        </h2>
      </div>
    </div>
    <div class="card-body" :class="{ 'dead-mask': dead }">
      <div class="row mb-2">
        <section class="col-9">
          <section class="row">
            <section class="col-9">
              <p class="mb-0"><b>Exemplar</b></p>
              <div class="progress mt-1" :data-bs-theme="theme">
                <div
                  class="progress-bar"
                  :class="{
                    'progress-bar-animated': exemplarProgressRounded < 100,
                    'progress-bar-striped': exemplarProgressRounded < 100
                  }"
                  role="progressbar"
                  :style="{ width: exemplarProgress + '%' }"
                  aria-valuenow="25"
                  aria-valuemin="0"
                  aria-valuemax="100"
                >
                  {{ exemplarProgressRounded }}%
                </div>
              </div>
              <p class="mb-2 text-center">{{ currentExemplar }} / {{ requiredExemplar }}</p>
            </section>
            <section class="col-3">
              <div class="row">
                <section class="col-12 d-flex p-0">
                  <p class="me-2"><b>Atk:</b></p>
                  <p>{{ user?.attack }}</p>
                </section>
                <section class="col-12 d-flex p-0">
                  <p class="me-2"><b>Def:</b></p>
                  <p>{{ user?.defense }}</p>
                </section>
              </div>
            </section>
          </section>
          <div v-if="playerBuffs.size">
            <Buffs :player="player" :buff-data="playerBuffs" />
          </div>
        </section>
        <section class="col-3">
          <p class="mb-0"><b>HP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div
              class="progress-bar bg-danger"
              role="progressbar"
              :style="{ width: user?.hpp + '%' }"
              :aria-valuenow="user?.tp"
              aria-valuemin="0"
              aria-valuemax="100"
            >
              {{ user?.hpp }}%
            </div>
          </div>
          <p class="mb-0"><b>MP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div
              class="progress-bar bg-success"
              role="progressbar"
              :style="{ width: user?.mpp + '%' }"
              :aria-valuenow="user?.tp"
              aria-valuemin="0"
              aria-valuemax="100"
            >
              {{ user?.mpp }}%
            </div>
          </div>
          <p class="mb-0"><b>TP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div
              class="progress-bar bg-purple"
              role="progressbar"
              :style="{ width: getTP }"
              :aria-valuenow="user?.tp"
              aria-valuemin="0"
              aria-valuemax="3000"
            >
              {{ user?.tp }}
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import { computed, onMounted, onUpdated, onUnmounted, onBeforeUpdate, ref, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { mdiSkullCrossbones } from '@mdi/js'
import * as bootstrap from 'bootstrap'
import Buffs from '@/components/Buffs.vue'
import type { ComputedRef } from 'vue'
import type { Ability } from '@/types/Ability'
import type { Buff } from '@/types/buff'
import { useUserStore } from '@/stores/user'
import type { Player } from '@/types/Player'

const props = defineProps({
  playerId: Number,
})

const userStore = useUserStore()
const user = computed<Player>(() => userStore.players.get(props.playerId ?? 0));
const currentExemplar = computed(() => user.value?.currentExemplar.toLocaleString())
const exemplarProgressRounded = computed(() => Math.floor(exemplarProgress.value))
const playerBuffs: ComputedRef<Map<string, Buff>> = computed(() => {
  const buffs = user?.value?.buffs
  return buffs ? new Map(Object.entries(buffs)) : new Map()
})
const player = computed(() => {
  return {
    playerId: user?.value?.playerId,
    playerName: user?.value?.playerName
  }
})
const playerName = computed(
  () => user?.value?.playerName.charAt(0).toUpperCase() + user?.value?.playerName.slice(1)
)
const requiredExemplar = computed(() => user?.value?.requiredExemplar.toLocaleString())
const theme: ComputedRef<string> = computed(() =>
  themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light'
)
const themeColor = computed(() => (themeStore.theme === 'dark' ? '#fff' : '#000'))
const themeStore = useThemeStore()
const getTP = computed(() => (user?.value?.tp / 3000) * 100 + '%')
const playerAbilities = ref([] as Ability[])
const exemplarProgress = computed(() => {
  if (user?.value?.requiredExemplar - user?.value?.currentExemplar <= 1) {
    return 100
  } else {
    return (user?.value?.currentExemplar / user?.value?.requiredExemplar) * 100
  }
})

const filterAbilties = () => {
  let abilities =
    typeof user?.value?.abilities === 'string'
      ? JSON.parse(user?.value?.abilities)
      : user?.value?.abilities
  let filteredAbilities: Ability[] = []

  for (const ability of abilities) {
    const recastTime: Date = new Date(ability?.recast * 1000)
    if (new Date().getTime() - recastTime.getTime() <= 0) {
      filteredAbilities.push(ability)
    }
  }

  return filteredAbilities
}

function renderDeathAnimation (dead: number) {
  if (dead == 2) {
    deadElement.value.classList.add('transition')
    setTimeout(() => {
      deadElement.value.classList.add('fade-in')
    }, 100)
  } else {
    deadElement.value.classList.remove('fade-in')
    setTimeout(() => {
      deadElement.value.classList.remove('transition')
    }, 100)
  }
}

const dead = computed(() => user?.value?.status == 2)

watch(userStore.players, (players: any) => {
  const player = players.get(props.playerId)
  renderDeathAnimation(player.status)
})

let tooltip: bootstrap.Tooltip | null = null
const titleElement = ref()
const deadElement = ref()
const isOnline = ref(Date.now() - user?.value?.lastOnline * 1000 < 60000)
const onlineTitleText = ref('Offline')
const onlineStatusDot = ref('offline-dot')

const checkOnlineState = () => {
  isOnline.value = Date.now() - user?.value?.lastOnline * 1000 < 60000
  onlineTitleText.value = isOnline.value ? 'Online' : 'Offline'
  onlineStatusDot.value = isOnline.value ? 'online-dot' : 'offline-dot'
}

onUpdated(() => {
  tooltip = new bootstrap.Tooltip(titleElement.value)
})

onMounted(() => {
  tooltip = new bootstrap.Tooltip(titleElement.value)
  setInterval(checkOnlineState, 5000)
})

onBeforeUpdate(() => {
  tooltip = bootstrap.Tooltip.getInstance(titleElement.value)
  if (tooltip) {
    tooltip?.dispose()
  }
})

onUnmounted(() => {
  if (tooltip) {
    tooltip?.dispose()
  }
})

watch( userStore?.players, () => {
  checkOnlineState()
})

</script>

<style scoped lang="scss">
.card {
  &.user-card {
    height: 100%;
  }

  %status-dot {
    content: '';
    display: inline-block;
    width: 15px;
    height: 15px;
    border-radius: 50%;
    margin-right: 10px;
  }

  .online-dot {
    &::before {
      background-color: green;
      @extend %status-dot;
    }
  }

  .offline-dot {
    &::before {
      background-color: red;
      @extend %status-dot;
    }
  }

  .dead-mask:before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1;
  }

  .dead {
    position: absolute;
    top: 50%;
    right: 50%;
    transform: translate(50%, -50%) scale(1.2);
    z-index: 2;
    opacity: 0;
    transition:
      transform 0.3s ease-in-out,
      opacity 0.3s ease-in-out;
  }

  .dead.transition {
    width: 250px;
    height: 250px;
  }

  .dead.fade-in {
    transform: translate(50%, -50%) scale(1);
    width: 250px;
    height: 250px;
    opacity: 1;
  }

  .fill {
    fill: v-bind(themeColor);
  }

  .skull-fill {
    fill: darkred;
    stroke: white;
    stroke-width: 0.2px;
  }

  .progress-bar {
    font-size: 0.95rem;
    font-weight: bold;
    &.bg-purple {
      background-color: #8a19bc;
    }
  }
}
</style>
