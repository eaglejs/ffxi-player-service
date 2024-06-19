<template>
  <div class="card mb-3 mt-3 p-0" :data-bs-theme="theme">
    <svg ref="deadElement" class="dead" viewBox="0 0 25 25" width="30" height="30">
      <path class="skull-fill" :d="mdiSkullCrossbones"></path>
    </svg>
    <div class="card-body" :class="{ 'dead-mask': dead }">
      <h2 class="card-title">
        <span ref="titleElement" :class="onlineStatusDot" :title="onlineTitleText" data-bs-toggle="tooltip"
          data-bs-placement="top"></span>{{ playerName }}
        M. lvl: {{ user?.masterLevel }}
        ({{ user?.mainJob }}{{ user?.mainJobLevel }}/{{ user?.subJob }}{{ user?.subJobLevel }})
        - <span>
          {{ user?.zone }}
        </span>
      </h2>
      <div class="row">
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
        </section>
        <section class="col-3">
          <p class="m-0"><b>Attack</b></p>
          <p class="m-0">{{ user?.attack }}</p>
          <p class="m-0"><b>Defense</b></p>
          <p class="m-0">{{ user?.defense }}</p>
        </section>
        <section class="col-3">
          <p class="mb-0"><b>HP</b>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div class="progress-bar bg-danger" role="progressbar" :style="{ width: user?.hpp + '%' }"
              aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">{{ user?.hpp }}%</div>
          </div>
          </p>
          <p class="mb-0"><b>MP</b>
          <div class="progress mb-1 mt-1" :data-bs-theme="theme">
            <div class="progress-bar bg-success" role="progressbar" :style="{ width: user?.mpp + '%' }"
              aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">{{ user?.mpp }}%</div>
          </div>
          </p>
        </section>
      </div>
      <div class="row mt-4">
        <section class="col-12">
          <Buffs :buff-data="playerBuffs" />
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"
import { computed, defineProps, onMounted, onUpdated, onUnmounted, onBeforeUpdate, ref, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { mdiSkullCrossbones } from "@mdi/js"
import * as bootstrap from 'bootstrap'
import moment from 'moment'
import Buffs from '@/components/Buffs.vue'

const currentExemplar = computed(() => props?.user?.currentExemplar.toLocaleString())
const dead = computed(() => props?.user?.hpp === 0)
const exemplarProgress = computed(() => (props?.user?.currentExemplar / props?.user?.requiredExemplar) * 100)
const exemplarProgressRounded = computed(() => Math.round(exemplarProgress.value))
const playerBuffs = computed(() => props?.user?.buffs)
const playerName = computed(() => props?.user?.playerName.charAt(0).toUpperCase() + props?.user?.playerName.slice(1))
const requiredExemplar = computed(() => props?.user?.requiredExemplar.toLocaleString())
const theme: any = computed(() => themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light')
const themeColor = computed(() => themeStore.theme === 'dark' ? '#fff' : '#000')
const themeStore = useThemeStore()

const props = defineProps({
  user: Object,
})

let tooltip: any = null
const titleElement = ref()
const deadElement = ref()
const isOnline = ref(moment().diff(moment.unix(props?.user?.lastOnline), 'minutes') < 1)
const onlineTitleText = ref('Offline')
const onlineStatusDot = ref('offline-dot')

const checkOnlineState = () => {
  isOnline.value = moment().diff(moment.unix(props?.user?.lastOnline), 'minutes') < 1
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

watch(dead, (newDead) => {
  if (newDead) {
    deadElement.value.classList.add('transition')
    setTimeout(() => {
      deadElement.value.classList.add('fade-in')
    }, 100)
  } else {
    deadElement.value.classList.remove('transition')
    deadElement.value.classList.remove('fade-in')
  }
})

</script>

<style scoped lang="scss">
.card {
  height: calc(100% - 15px);

  h2 {
    font-size: 1.5rem;
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
}
</style>