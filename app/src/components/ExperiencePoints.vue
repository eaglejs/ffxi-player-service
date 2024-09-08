<template>
  <div class="card experience-wrapper" v-if="data">
    <div class="card-header">
      <div class="d-flex">
        <h3 class="mb-0">Experience Points</h3>
        <section class="flex-grow-1">
          <span class="float-end">{{ analyzePts }} Ex/hr</span>
        </section>
      </div>
    </div>
    <div class="card-body">
      <Line :data="data" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { random } from 'mathjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'
import type { Player } from '@/types/Player'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const props = defineProps<{
  user: Player | undefined
}>()
const previousCurrentExemplar = ref<number>(0);
const exemplarHistory: number[] = [];
const analyzePts = ref(0);
const experience = ref({
  labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'], // 1st through 5th kill of an enemy
  datasets: [
    {
      label: 'Experience Points',
      data: [],
      fill: false,
      borderColor: 'rgb(75, 192, 192)',
      tension: 0.1
    }
  ]
});
const options = {
  responsive: true,
  maintainAspectRatio: false,
  legend: {
    display: false
  }
}

const data = computed(() => experience.value);
const justExemplarPoints = computed(() => exemplarHistory.map((points, index) => points));

function renderLatestData (debug = false) {
  let currentExemplarPoints: number;
  if (debug) {
    const min = 750;
    const max = 1100;
    const randomPoints = Math.floor(random(min, max));
    exemplarHistory.push(randomPoints)
  } else if (previousCurrentExemplar.value === 0) {
    exemplarHistory.push({points: 0, ts: new Date().getTime()/1000})
  } else {
    currentExemplarPoints = (props.user?.currentExemplar ?? 0) - (previousCurrentExemplar.value ?? 0)
    if (currentExemplarPoints < 0) {
      currentExemplarPoints = 0
    }
    exemplarHistory.push(currentExemplarPoints)
  }

  if (exemplarHistory.length > 10) {
    exemplarHistory.shift()
  }

  experience.value = {
    labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'], // 1st through 5th kill of an enemy
    datasets: [
      {
        label: 'Exemplar Points',
        data: justExemplarPoints.value,
        fill: false,
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1
      }
    ]
  }
  analyzePts.value = analyzePoints(exemplarHistory);
  previousCurrentExemplar.value = props.user?.currentExemplar || 0;
}

function analyzePoints(experiencePoints: Array<number>) {
  let t = new Date().getTime()/1000
  let running_total = 0
  let maximum_timestamp = 29
  experiencePoints.forEach((points) => { 
    let time_diff = t - points.ts
    if (t - points.ts > 600) {
      points.ts = null
    } else {
      running_total += points
      if (time_diff > maximum_timestamp) {
        maximum_timestamp = time_diff
  
      }
    }
  })

  let rate
  if (maximum_timestamp == 29) {
    rate = 0
  } else {
    rate = Math.floor((running_total/maximum_timestamp)*3600)
  }

  return rate
}

// setInterval(() => {
//   renderLatestData(true)
// }, 3000)

watch(() => props.user?.currentExemplar, () => renderLatestData() )
</script>

<style scoped lang="scss">
.experience-wrapper {
  height: 100%;
}
</style>
