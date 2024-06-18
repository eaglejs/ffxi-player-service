<template>
  <div class="card mb-3 mt-3 p-0" :data-bs-theme="theme">
    <div class="card-body">
      <h2 class="card-title" :class="onlineStatusDot" :title="online">
        {{ playerName }}
        M. lvl: {{ user?.masterLevel }}
        ({{ user?.mainJob }}{{ user?.mainJobLevel }}/{{ user?.subJob }}{{ user?.subJobLevel }}) 
        - <span>
          {{ user?.zone }}
        </span>
      </h2>
      <div class="row">
        <section class="col-6">
          <p class="mb-0"><b>Exemplar</b></p>
          <div class="progress" :data-bs-theme="theme">
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
          <p><b>HP</b>
          <div class="progress" :data-bs-theme="theme">
            <div class="progress-bar bg-danger" role="progressbar" :style="{ width: user?.hpp + '%' }"
              aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">{{ user?.hpp }}%</div>
          </div>
          </p>
          <p><b>MP</b>
          <div class="progress" :data-bs-theme="theme">
            <div class="progress-bar bg-success" role="progressbar" :style="{ width: user?.mpp + '%' }"
              aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">{{ user?.mpp }}%</div>
          </div>
          </p>
        </section>
      </div>
      <div class="row">
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
import { computed, defineProps, watch, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import moment from 'moment'
import Buffs from '@/components/Buffs.vue'

const themeStore = useThemeStore()
const playerName = computed(() => props?.user?.playerName.charAt(0).toUpperCase() + props?.user?.playerName.slice(1))
const currentExemplar = computed(() => props?.user?.currentExemplar.toLocaleString())
const requiredExemplar = computed(() => props?.user?.requiredExemplar.toLocaleString())
const playerBuffs = computed(() => props?.user?.buffs)
const exemplarProgress = computed(() => (props?.user?.currentExemplar / props?.user?.requiredExemplar) * 100)
const exemplarProgressRounded = computed(() => Math.round(exemplarProgress.value))
const isOnline = computed(() => moment().diff(moment.unix(props?.user?.lastOnline), 'minutes') < 1)
const onlineStatusDot = computed(() => isOnline.value ? 'online-dot' : 'offline-dot')
const online = computed(() => isOnline.value ? 'Online' : 'Offline')

const props = defineProps({
  user: Object,
})

const theme: any = computed(() => {
  return themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light'
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
}
</style>