<template>
  <div class="card user-card" :data-bs-theme="theme">
    <svg ref="deadElement" class="dead" viewBox="0 0 25 25" width="30" height="30">
      <path class="skull-fill" :d="mdiSkullCrossbones"></path>
    </svg>
    <div class="card-header">
      <div class="d-flex justify-content-between">
        <h2 class="mb-0">
          <span ref="titleElement" :class="onlineStatusDot" :title="onlineTitleText" data-bs-toggle="tooltip"
            data-bs-placement="top"></span><RouterLink :to="`/users/${user?.playerId}`">{{ playerName }}</RouterLink>
          M. lvl: {{ user?.masterLevel }}
          ({{ user?.mainJob }}{{ user?.mainJobLevel }}/{{ user?.subJob }}{{ user?.subJobLevel }})
          - <span>
            {{ user?.zone }}
          </span>
        </h2>
      </div>
    </div>
    <div class="card-body" :class="{ 'dead-mask': dead }">
      <div class="row mb-2">
        <section class="col-6">
          <p class="mb-0"><b>Exemplar</b></p>
          <div class="progress mt-1" :data-bs-theme="theme">
            <div class="progress-bar"
              :class="{ 'progress-bar-animated': exemplarProgressRounded < 100, 'progress-bar-striped': exemplarProgressRounded < 100 }"
              role="progressbar" :style="{ width: exemplarProgress + '%' }" aria-valuenow="25" aria-valuemin="0"
              aria-valuemax="100">
              {{ exemplarProgressRounded }}%
            </div>
          </div>
          <p class="mb-2 text-center">{{ currentExemplar }} / {{ requiredExemplar }}</p>
          <div v-if="playerBuffs.length">
            <Buffs :buff-data="playerBuffs" />
          </div>
        </section>
        <section class="col-3">
          <div class="row">
            <section class="col-12">
              <p class="m-0"><b>Attack</b></p>
              <p class="m-0">{{ user?.attack }}</p>
              <p class="m-0"><b>Defense</b></p>
              <p class="m-0">{{ user?.defense }}</p>
            </section>
          </div>
        </section>
        <section class="col-3">
          <p class="mb-0"><b>HP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div class="progress-bar bg-danger" role="progressbar" :style="{ width: user?.hpp + '%' }"
              :aria-valuenow="user?.tp" aria-valuemin="0" aria-valuemax="100">{{ user?.hpp }}%</div>
          </div>
          <p class="mb-0"><b>MP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div class="progress-bar bg-success" role="progressbar" :style="{ width: user?.mpp + '%' }"
              :aria-valuenow="user?.tp" aria-valuemin="0" aria-valuemax="100">{{ user?.mpp }}%</div>
          </div>
          <p class="mb-0"><b>TP</b></p>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div class="progress-bar bg-purple" role="progressbar" :style="{ width:  getTP }"
              :aria-valuenow="user?.tp" aria-valuemin="0" aria-valuemax="3000">{{ user?.tp }}</div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"
import { computed, onMounted, onUpdated, onUnmounted, onBeforeUpdate, ref, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { mdiSkullCrossbones } from '@mdi/js'
import * as bootstrap from 'bootstrap'
import Buffs from '@/components/Buffs.vue'
import type { ComputedRef } from 'vue'
import type { Ability } from '@/types/Ability'

const userStore = useUserStore()
const props = defineProps({
  user: Object,
})

const currentExemplar = computed(() => parseInt(props?.user?.currentExemplar).toLocaleString())
const exemplarProgressRounded = computed(() => Math.floor(exemplarProgress.value))
const playerBuffs = computed(() => props?.user?.buffs ?? [])
const playerName = computed(() => props?.user?.playerName.charAt(0).toUpperCase() + props?.user?.playerName.slice(1))
const requiredExemplar = computed(() => parseInt(props?.user?.requiredExemplar).toLocaleString())
const theme: ComputedRef<string> = computed(() => themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light')
const themeColor = computed(() => themeStore.theme === 'dark' ? '#fff' : '#000')
const themeStore = useThemeStore()
const getTP = computed(() => (props?.user?.tp / 3000) * 100 + '%')
const playerAbilities = ref([] as Ability[]) 
const exemplarProgress = computed(() => { 
  if (props?.user?.requiredExemplar - props?.user?.currentExemplar <= 1) {
    return 100
  }  else {
    return (props?.user?.currentExemplar / props?.user?.requiredExemplar) * 100
  }
})

const filterAbilties = () => {
  let abilities = typeof props?.user?.abilities === 'string' ? JSON.parse(props?.user?.abilities) : props?.user?.abilities
  let filteredAbilities:  Ability[] = []

  for (const ability of abilities) {
    const recastTime: Date = new Date(ability?.recast * 1000);
    if ((new Date().getTime() - recastTime.getTime()) <= 0){
      filteredAbilities.push(ability)
    }
  }

  return filteredAbilities
}

const dead = computed(() => {
  return props?.user?.status == 2
})

watch(dead, (newVal) => {
  if (newVal && deadElement.value) {
    deadElement.value.classList.add('transition')
    setTimeout(() => {
      deadElement.value.classList.add('fade-in')
    }, 50)
  } else {
    if (!deadElement.value) {
      return
    }
    setTimeout(() => {
      if (!deadElement.value) {
        return
      }
      deadElement.value.classList.remove('transition')
    }, 350)
    if (deadElement.value) {
      deadElement.value.classList.remove('fade-in')
    }
  }
})

let tooltip: bootstrap.Tooltip | null = null
const titleElement = ref()
const deadElement = ref()
const isOnline = ref((Date.now() - (props?.user?.lastOnline * 1000)) < 60000)
const onlineTitleText = ref('Offline')
const onlineStatusDot = ref('offline-dot')

const checkOnlineState = () => {
  isOnline.value = (Date.now() - (props?.user?.lastOnline * 1000)) < 60000
  onlineTitleText.value = isOnline.value ? 'Online' : 'Offline'
  onlineStatusDot.value = isOnline.value ? 'online-dot' : 'offline-dot'
}

onUpdated(() => {
  tooltip = new bootstrap.Tooltip(titleElement.value)
})

onMounted(() => {
  tooltip = new bootstrap.Tooltip(titleElement.value)
  checkOnlineState()
  setInterval(checkOnlineState, 5000)
})

onBeforeUpdate(() => {
  tooltip = bootstrap.Tooltip.getInstance(titleElement.value)
  if (tooltip) {
    tooltip.dispose()
  }
})

onUnmounted(() => {
  if (tooltip) {
    tooltip.dispose()
  }
})

// watch( userStore?.players, () => {
//   playerAbilities.value = filterAbilties()
// })

// watch( onlineStatusDot, (newValue: string) => {
//   if (newValue == 'offline-dot') {
//     serverStore.connectWebSocket()
//   }
// })

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
    transition: transform 0.3s ease-in-out, opacity 0.3s ease-in-out;
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
    stroke-width: .2px;
  }

  .progress-bar {
    font-size: .95rem;
    font-weight: bold;
    &.bg-purple {
      background-color: #8a19bc;
    }
  }
}
</style>