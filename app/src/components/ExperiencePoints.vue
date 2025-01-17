<template>
  <div class="card experience-wrapper" v-if="experienceGraph">
    <div class="card-header">
      <div class="d-flex">
        <h3 class="mb-0">Experience Points</h3>
        <section class="d-inline-flex flex-grow-1 justify-content-end">
          <span class="pe-2 experience-points">{{ averageExperiencePts.toLocaleString() }}k XP/hr</span>
          <span class="ps-2 pe-2 capacity-points">{{ averageCapacityPts.toLocaleString() }}k CP/hr</span>
          <span class="ps-2 exemplar-points">{{ averageExemplarPts.toLocaleString() }}k EX/hr</span>
        </section>
      </div>
    </div>
    <div class="card-body">
      <Line :data="experienceGraph" :options="options" />
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
  type ChartData
} from 'chart.js'
import { Line } from 'vue-chartjs'
import type { Player } from '@/types/Player'
import type { Experience } from '@/types/Experience'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const props = defineProps<{
  user: Player | undefined
}>()
const averageExperiencePts = ref(0)
const averageCapacityPts = ref(0)
const averageExemplarPts = ref(0)
const experiencePoints: ComputedRef<number[]> = computed(
  () => props.user?.expHistory?.experience?.map((exp: Experience) => exp.points) || []
)
const capacityPoints: ComputedRef<number[]> = computed(
  () => props.user?.expHistory?.capacity?.map((exp: Experience) => exp.points) || []
)
const exemplarPoints: ComputedRef<number[]> = computed(
  () => props.user?.expHistory?.exemplar?.map((exp: Experience) => exp.points) || []
)

const experienceGraph = ref<ChartData<'line', (number | null)[]>>({
  labels: (function () {
    let labels = []
    for (let i = 0; i < 50; i++) {
      labels.push(i.toString())
    }
    return labels
  })(),
  datasets: [
    {
      data: experiencePoints.value || [],
      label: 'XP',
      fill: false,
      borderColor: 'rgb(74, 156, 88)',
      tension: 0.1
    },
    {
      data: capacityPoints.value || [],
      label: 'CP',
      fill: false,
      borderColor: 'rgb(255, 205, 86)',
      tension: 0.1
    },
    {
      data: exemplarPoints.value || [],
      label: 'EX',
      fill: false,
      borderColor: 'rgb(255, 99, 132)',
      tension: 0.1
    }
  ]
})
const options = {
  responsive: true,
  maintainAspectRatio: false
}

function renderLatestData() {
  experienceGraph.value = {
    labels: (function () {
      let labels = []
      for (let i = 0; i < 50; i++) {
        labels.push(i.toString())
      }
      return labels
    })(),
    datasets: [
      {
        data: experiencePoints.value || [],
        label: 'XP',
        fill: false,
        borderColor: 'rgb(74, 156, 88)',
        tension: 0.1
      },
      {
        data: capacityPoints.value || [],
        label: 'CP',
        fill: false,
        borderColor: 'rgb(255, 205, 86)',
        tension: 0.1
      },
      {
        data: exemplarPoints.value || [],
        label: 'EX',
        fill: false,
        borderColor: 'rgb(255, 99, 132)',
        tension: 0.1
      }
    ]
  }
  averageExperiencePts.value = analyzePoints(props?.user?.expHistory?.experience || [])
  averageCapacityPts.value = analyzePoints(props?.user?.expHistory?.capacity || [])
  averageExemplarPts.value = analyzePoints(props?.user?.expHistory?.exemplar || [])
}

function analyzePoints(experiencePoints: Experience[]): number {
  if (!experiencePoints || experiencePoints.length === 0) {
    return 0;
  }

  // Convert timestamps to Date objects and sort by timestamp
  const points = experiencePoints.map(item => ({
    points: item.points ?? 0,
    timestamp: new Date(item.timestamp).getTime()
  }));

  // Calculate the total time span of the given data points
  const startTime = points[0].timestamp;
  const endTime = points[points.length - 1].timestamp;
  const totalTimeSpan = (endTime - startTime) / 1000; // in seconds

  // Calculate total points accumulated
  const totalPoints = points.reduce((sum, item) => sum + item.points, 0);

  // Calculate the rate of points per second
  const ratePerSecond = totalPoints / totalTimeSpan;

  // Extrapolate the rate to an hour
  const ratePerHour = ratePerSecond * 3600;


  // Format the rate to one decimal point
  return parseFloat((ratePerHour / 1000).toFixed(1)) || 0;
}

onMounted(() => {
  renderLatestData()
})

watch(
  () => props.user?.expHistory?.experience,
  () => renderLatestData()
)
</script>

<style scoped lang="scss">
.experience-wrapper {
  height: 100%;
}

.experience-points {
  color: rgb(74, 156, 88);
  // color: rgb(86, 156, 86);
  // color: rgb(86, 130, 47);

}

.capacity-points {
  color: rgb(255, 205, 86);
}

.exemplar-points {
  color: rgb(255, 99, 132);
}
</style>
