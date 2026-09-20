<template>
  <div class="card experience-wrapper" v-if="experienceGraph">
    <div class="card-header">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-2">
        <h2 v-if="isExperienceDashboard" class="mb-0">
          <GenOnlineDot :player="player" />
          {{ playerName }}
        </h2>
        <h2 v-else class="mb-0">Experience Points</h2>
        <section class="d-inline-flex flex-wrap align-items-center gap-3">
          <span class="experience-points font-weight-bold"
            >{{ averageExperiencePts.toLocaleString() }}k XP/hr</span
          >
          <span class="capacity-points font-weight-bold"
            >{{ averageCapacityPts.toLocaleString() }}k CP/hr</span
          >
          <span class="exemplar-points font-weight-bold"
            >{{ averageExemplarPts.toLocaleString() }}k EX/hr</span
          >
        </section>
      </div>

      <!-- Current Chains Display -->
      <div class="d-flex align-items-center gap-3 mt-2 text-muted small border-top pt-2" v-if="hasChainData">
        <span class="chain-badge xp-chain" v-if="latestExpChain !== null">
          <b>XP Chain</b>: {{ latestExpChain }}
        </span>
        <span class="chain-badge cp-chain" v-if="latestCapChain !== null">
          <b>CP Chain</b>: {{ latestCapChain }}
        </span>
        <span class="chain-badge ex-chain" v-if="latestExChain !== null">
          <b>EX Chain</b>: {{ latestExChain }}
        </span>
      </div>
    </div>
    <div class="card-body">
      <Line :data="experienceGraph" :options="options" />
    </div>
    <div class="card-footer">
      <section>
        <div class="d-flex justify-content-between">
          <span
            ><b>Merits</b>: <span class="experience-points">{{ totalMerits }}</span></span
          >
          <span
            ><b>Job Points</b>: <span class="capacity-points">{{ totalCapacityPoints }}</span></span
          >
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, type ComputedRef, onMounted, onUnmounted } from 'vue'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions
} from 'chart.js'
import { Line } from 'vue-chartjs'
import type { Player } from '@/types/Player'
import type { Experience } from '@/types/Experience'
import GenOnlineDot from '@/components/gen-components/GenOnlineDot.vue'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const experiencePointsRGB = 'rgb(74, 156, 88)'
const capacityPointsRGB = 'rgb(233 164 0)'
const exemplarPointsRGB = 'rgb(255, 99, 132)'

const props = defineProps<{
  player: Player | undefined
}>()

const averageExperiencePts = ref(0)
const averageCapacityPts = ref(0)
const averageExemplarPts = ref(0)
const isExperienceDashboard = ref<boolean>(window.location.pathname === '/charts')
let timerId: ReturnType<typeof setInterval> | null = null

const playerName = computed(() =>
  props.player && props.player.playerName
    ? props.player.playerName.charAt(0).toUpperCase() + props.player.playerName.slice(1)
    : ''
)
const totalMerits: ComputedRef<number> = computed(() => props.player?.merits?.total || 0)
const maxMerits: ComputedRef<number> = computed(() => props.player?.merits?.max || 0)
const totalCapacityPoints: ComputedRef<number> = computed(
  () => props?.player?.capacityPoints?.total || 0
)

const latestExpChain = computed(() => {
  const list = props.player?.expHistory?.experience
  if (!list || list.length === 0) return null
  const last = list[list.length - 1]
  return last && typeof last.chain === 'number' ? last.chain : null
})

const latestCapChain = computed(() => {
  const list = props.player?.expHistory?.capacity
  if (!list || list.length === 0) return null
  const last = list[list.length - 1]
  return last && typeof last.chain === 'number' ? last.chain : null
})

const latestExChain = computed(() => {
  const list = props.player?.expHistory?.exemplar
  if (!list || list.length === 0) return null
  const last = list[list.length - 1]
  return last && typeof last.chain === 'number' ? last.chain : null
})

const hasChainData = computed(
  () => latestExpChain.value !== null || latestCapChain.value !== null || latestExChain.value !== null
)

const experienceGraph = ref<ChartData<'line', any[]>>({
  labels: [],
  datasets: [
    {
      data: [],
      label: 'XP',
      fill: false,
      borderColor: experiencePointsRGB,
      backgroundColor: experiencePointsRGB,
      tension: 0.2,
      pointRadius: 4,
      pointHoverRadius: 6
    },
    {
      data: [],
      label: 'CP',
      fill: false,
      borderColor: capacityPointsRGB,
      backgroundColor: capacityPointsRGB,
      tension: 0.2,
      pointRadius: 4,
      pointHoverRadius: 6
    },
    {
      data: [],
      label: 'EX',
      fill: false,
      borderColor: exemplarPointsRGB,
      backgroundColor: exemplarPointsRGB,
      tension: 0.2,
      pointRadius: 4,
      pointHoverRadius: 6
    }
  ]
})

const options: ChartOptions<'line'> = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top',
      labels: {
        usePointStyle: true,
        boxWidth: 8
      }
    },
    tooltip: {
      callbacks: {
        title: (tooltipItems: any[]) => {
          if (!tooltipItems.length) return ''
          const item = tooltipItems[0]
          const raw = item.raw
          const sampleLabel = `Sample #${item.label}`
          if (raw && raw.timestamp) {
            try {
              const date = new Date(raw.timestamp)
              return `${sampleLabel} (${date.toLocaleTimeString()})`
            } catch {
              // ignore date parse errors
            }
          }
          return sampleLabel
        },
        label: (context: any) => {
          const label = context.dataset.label || ''
          const raw = context.raw
          const rateVal = typeof raw === 'object' && raw !== null ? raw.y : context.parsed.y
          const chain =
            typeof raw === 'object' && raw !== null && typeof raw.chain === 'number'
              ? raw.chain
              : null
          const rawPts =
            typeof raw === 'object' && raw !== null && typeof raw.rawPoints === 'number'
              ? raw.rawPoints
              : null

          let line = `${label} Rate: ${rateVal}k ${label}/hr`
          if (chain !== null) {
            line += ` (Chain ${chain})`
          }
          if (rawPts !== null) {
            line += ` [+${rawPts.toLocaleString()} pts]`
          }
          return line
        }
      }
    }
  },
  scales: {
    x: {
      title: {
        display: true,
        text: 'Sample #'
      },
      grid: {
        display: false
      }
    },
    y: {
      beginAtZero: true,
      title: {
        display: true,
        text: 'Rate (k/hr)'
      },
      grid: {
        color: 'rgba(200, 200, 200, 0.15)'
      }
    }
  }
}

function renderLatestData() {
  const expList: Experience[] = props.player?.expHistory?.experience || []
  const capList: Experience[] = props.player?.expHistory?.capacity || []
  const exList: Experience[] = props.player?.expHistory?.exemplar || []

  const maxLen = Math.max(expList.length, capList.length, exList.length)

  const labels: string[] = []
  for (let i = 0; i < maxLen; i++) {
    labels.push(`${i + 1}`)
  }

  const expData = expList.map((item: Experience, i: number) => ({
    x: labels[i] || `${i + 1}`,
    y: calculatePointRate(expList, i),
    rawPoints: item.points,
    chain: item.chain,
    timestamp: item.timestamp
  }))

  const capData = capList.map((item: Experience, i: number) => ({
    x: labels[i] || `${i + 1}`,
    y: calculatePointRate(capList, i),
    rawPoints: item.points,
    chain: item.chain,
    timestamp: item.timestamp
  }))

  const exData = exList.map((item: Experience, i: number) => ({
    x: labels[i] || `${i + 1}`,
    y: calculatePointRate(exList, i),
    rawPoints: item.points,
    chain: item.chain,
    timestamp: item.timestamp
  }))

  experienceGraph.value = {
    labels,
    datasets: [
      {
        data: expData,
        label: 'XP',
        fill: false,
        borderColor: experiencePointsRGB,
        backgroundColor: experiencePointsRGB,
        tension: 0.2,
        pointRadius: 4,
        pointHoverRadius: 6
      },
      {
        data: capData,
        label: 'CP',
        fill: false,
        borderColor: capacityPointsRGB,
        backgroundColor: capacityPointsRGB,
        tension: 0.2,
        pointRadius: 4,
        pointHoverRadius: 6
      },
      {
        data: exData,
        label: 'EX',
        fill: false,
        borderColor: exemplarPointsRGB,
        backgroundColor: exemplarPointsRGB,
        tension: 0.2,
        pointRadius: 4,
        pointHoverRadius: 6
      }
    ]
  }

  updateRates()
}

function updateRates() {
  const expList: Experience[] = props.player?.expHistory?.experience || []
  const capList: Experience[] = props.player?.expHistory?.capacity || []
  const exList: Experience[] = props.player?.expHistory?.exemplar || []

  averageExperiencePts.value = analyzePoints(expList)
  averageCapacityPts.value = analyzePoints(capList)
  averageExemplarPts.value = analyzePoints(exList)
}

function calculatePointRate(history: Experience[], index: number): number {
  if (!history || index < 0 || index >= history.length) return 0

  const subHistory = history.slice(0, index + 1)
  const validPoints = subHistory
    .filter((item) => item && typeof item.points === 'number')
    .map((item) => ({
      points: item.points ?? 0,
      timestamp: item.timestamp ? new Date(item.timestamp).getTime() : NaN
    }))
    .filter((item) => !isNaN(item.timestamp))

  if (validPoints.length <= 1) return 0

  validPoints.sort((a, b) => a.timestamp - b.timestamp)

  const currentTs = validPoints[validPoints.length - 1]?.timestamp ?? 0
  const WINDOW_MS = 10 * 60 * 1000 // 10-minute rolling window matching PointWatch
  const windowCutoff = currentTs - WINDOW_MS

  const windowPoints = validPoints.filter((p) => p.timestamp >= windowCutoff)
  if (windowPoints.length <= 1) {
    if (windowPoints.length === 1) {
      const pts = windowPoints[0]?.points ?? 0
      const ratePerSecond = pts / 60
      return parseFloat(((ratePerSecond * 3600) / 1000).toFixed(1)) || 0
    }
    return 0
  }

  const firstTs = windowPoints[0]?.timestamp ?? 0
  const lastTs = windowPoints[windowPoints.length - 1]?.timestamp ?? 0
  const timeSpanSeconds = (lastTs - firstTs) / 1000

  if (timeSpanSeconds === 0) return 0

  const totalPoints = windowPoints.reduce((sum, item) => sum + item.points, 0)
  const effectiveTimeSpan = Math.max(timeSpanSeconds, 60)
  const ratePerSecond = totalPoints / effectiveTimeSpan
  const ratePerHour = ratePerSecond * 3600

  return parseFloat((ratePerHour / 1000).toFixed(1)) || 0
}

function analyzePoints(experiencePoints: Experience[]): number {
  if (!experiencePoints || experiencePoints.length === 0) {
    return 0
  }

  const validPoints = experiencePoints
    .filter((item) => item && typeof item.points === 'number')
    .map((item) => ({
      points: item.points ?? 0,
      timestamp: item.timestamp ? new Date(item.timestamp).getTime() : NaN
    }))
    .filter((item) => !isNaN(item.timestamp))

  if (validPoints.length <= 1) {
    return 0
  }

  validPoints.sort((a, b) => a.timestamp - b.timestamp)

  const latestTs = validPoints[validPoints.length - 1]?.timestamp ?? 0
  const nowMs = Date.now()

  // Inactivity decay: if last gain was > 15 minutes ago in live play, rate drops to 0
  if (nowMs - latestTs > 15 * 60 * 1000 && latestTs > nowMs - 24 * 60 * 60 * 1000) {
    return 0
  }

  // 10-minute rolling window before latest entry (PointWatch standard)
  const WINDOW_MS = 10 * 60 * 1000 // 10 minutes
  const windowCutoff = latestTs - WINDOW_MS
  const windowPoints = validPoints.filter((p) => p.timestamp >= windowCutoff)

  if (windowPoints.length <= 1) {
    if (windowPoints.length === 1) {
      const pts = windowPoints[0]?.points ?? 0
      const ratePerSecond = pts / 60
      return parseFloat(((ratePerSecond * 3600) / 1000).toFixed(1)) || 0
    }
    return 0
  }

  const firstTs = windowPoints[0]?.timestamp ?? 0
  const lastTs = windowPoints[windowPoints.length - 1]?.timestamp ?? 0
  const timeSpanSeconds = (lastTs - firstTs) / 1000

  if (timeSpanSeconds === 0) {
    return 0
  }

  const totalPoints = windowPoints.reduce((sum, item) => sum + item.points, 0)
  const effectiveTimeSpan = Math.max(timeSpanSeconds, 60)
  const ratePerSecond = totalPoints / effectiveTimeSpan
  const ratePerHour = ratePerSecond * 3600

  return parseFloat((ratePerHour / 1000).toFixed(1)) || 0
}

onMounted(() => {
  renderLatestData()
  timerId = setInterval(() => {
    updateRates()
  }, 1000)
})

onUnmounted(() => {
  if (timerId) {
    clearInterval(timerId)
    timerId = null
  }
})

watch(
  () => props.player?.expHistory,
  () => renderLatestData(),
  { deep: true }
)
</script>

<style scoped lang="scss">
.experience-wrapper {
  height: 100%;
}

.experience-points {
  color: rgb(74, 156, 88);
}

.capacity-points {
  color: rgb(233 164 0);
}

.exemplar-points {
  color: rgb(255, 99, 132);
}

.chain-badge {
  font-size: 0.85rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  background-color: rgba(255, 255, 255, 0.05);

  &.xp-chain {
    border-left: 3px solid rgb(74, 156, 88);
  }

  &.cp-chain {
    border-left: 3px solid rgb(233, 164, 0);
  }

  &.ex-chain {
    border-left: 3px solid rgb(255, 99, 132);
  }
}
</style>

