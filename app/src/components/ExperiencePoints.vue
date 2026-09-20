<template>
  <div class="card experience-wrapper" v-if="experienceGraph">
    <div class="card-header">
      <div class="d-flex">
        <h2 v-if="isExperienceDashboard" class="mb-0">
          <GenOnlineDot :player="player" />
          {{ playerName }}
        </h2>
        <h2 v-else class="mb-0">Experience Points</h2>
        <section class="d-inline-flex flex-grow-1 justify-content-end">
          <span class="pe-2 experience-points"
            >{{ averageExperiencePts.toLocaleString() }}k XP/hr</span
          >
          <span class="ps-2 pe-2 capacity-points"
            >{{ averageCapacityPts.toLocaleString() }}k CP/hr</span
          >
          <span class="ps-2 exemplar-points">{{ averageExemplarPts.toLocaleString() }}k EX/hr</span>
        </section>
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
import { computed, ref, watch, type ComputedRef, onMounted } from 'vue'
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
          const chainText = raw && typeof raw.chain === 'number' ? `Chain ${raw.chain}` : item.label
          if (raw && raw.timestamp) {
            try {
              const date = new Date(raw.timestamp)
              const timeStr = date.toLocaleTimeString()
              return `${chainText} (${timeStr})`
            } catch {
              // ignore date parse errors
            }
          }
          return chainText
        },
        label: (context: any) => {
          const label = context.dataset.label || ''
          const raw = context.raw
          const val = typeof raw === 'object' && raw !== null ? raw.y : context.parsed.y
          const chain =
            typeof raw === 'object' && raw !== null && typeof raw.chain === 'number'
              ? raw.chain
              : null
          const formattedVal = val !== undefined && val !== null ? val.toLocaleString() : '0'
          if (chain !== null) {
            return `${label}: ${formattedVal} (Chain ${chain})`
          }
          return `${label}: ${formattedVal}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: {
        display: false
      }
    },
    y: {
      beginAtZero: true,
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
    const chain = expList[i]?.chain ?? capList[i]?.chain ?? exList[i]?.chain
    if (chain !== undefined && chain !== null) {
      labels.push(`Chain ${chain}`)
    } else {
      labels.push(`#${i + 1}`)
    }
  }

  const expData = expList.map((item: Experience) => ({
    y: item.points ?? 0,
    chain: item.chain,
    timestamp: item.timestamp
  }))
  const capData = capList.map((item: Experience) => ({
    y: item.points ?? 0,
    chain: item.chain,
    timestamp: item.timestamp
  }))
  const exData = exList.map((item: Experience) => ({
    y: item.points ?? 0,
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

  averageExperiencePts.value = analyzePoints(expList)
  averageCapacityPts.value = analyzePoints(capList)
  averageExemplarPts.value = analyzePoints(exList)
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

  // Filter out session gaps (> 1 hour inactivity)
  let startIdx = 0
  const INACTIVITY_THRESHOLD_MS = 60 * 60 * 1000 // 1 hour
  for (let i = validPoints.length - 1; i > 0; i--) {
    const currentTs = validPoints[i]?.timestamp
    const prevTs = validPoints[i - 1]?.timestamp
    if (currentTs !== undefined && prevTs !== undefined && currentTs - prevTs > INACTIVITY_THRESHOLD_MS) {
      startIdx = i
      break
    }
  }

  const sessionPoints = validPoints.slice(startIdx)
  if (sessionPoints.length <= 1) {
    return 0
  }

  const startTime = sessionPoints[0]?.timestamp ?? 0
  const endTime = sessionPoints[sessionPoints.length - 1]?.timestamp ?? 0
  const totalTimeSpanSeconds = (endTime - startTime) / 1000

  if (totalTimeSpanSeconds === 0) {
    return 0
  }

  const totalPoints = sessionPoints.reduce((sum, item) => sum + item.points, 0)
  const effectiveTimeSpan = Math.max(totalTimeSpanSeconds, 60)
  const ratePerSecond = totalPoints / effectiveTimeSpan
  const ratePerHour = ratePerSecond * 3600

  return parseFloat((ratePerHour / 1000).toFixed(1)) || 0
}

onMounted(() => {
  renderLatestData()
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
</style>
