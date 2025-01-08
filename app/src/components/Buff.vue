<template>
  <section class="buff-wrapper" :class="{'buff': isTimerPositive()}" :data-duration="durationIntTime">
    <img
      :class="{ 'buff-icon': isLowTimer() }"
      ref="buffElement"
      :src="buffIcon"
      :alt="buffName"
      data-bs-toggle="tooltip"
      data-bs-placement="bottom"
      :title="buffName"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onBeforeUpdate, onUpdated, onUnmounted } from 'vue'
import type { ComputedRef } from 'vue'
import * as bootstrap from 'bootstrap'
import { iconsPath } from '@/helpers/config'

let tooltip: bootstrap.Tooltip | null = null
const buffElement = ref()
const intervalId = ref()
const durationIntTime = ref()

const props = defineProps({
  buffId: Number,
  buffName: String,
  duration: Number,
  utcTime: String
})

const buffIcon: ComputedRef<string> = computed(() => {
  return `${iconsPath}${props.buffId}.webp`
})

function isLowTimer(): boolean {
  const now = new Date().getTime()
  const duration = (props.utcTime ? Date.parse(props.utcTime) : new Date().getTime())
  return (duration - now) <= 15000
}

function isTimerPositive(): boolean {
  const now = new Date().getTime()
  const duration = (props.utcTime ? Date.parse(props.utcTime) : new Date().getTime())
  return (duration - now) > 0
}

function durationReadable(): string {
  const now = new Date().getTime()
  const duration = (props.utcTime ? Date.parse(props.utcTime) : new Date().getTime()) - now

  let seconds = Math.floor((duration / 1000) % 60)
  let minutes = Math.floor((duration / (1000 * 60)) % 60)
  let hours = Math.floor((duration / (1000 * 60 * 60)) % 24)
  isLowTimer()
  isTimerPositive()
  if (hours >= 1) {
    return `${hours}h`
  } else if (minutes >= 1) {
    return `${minutes}m`
  } else {
    return `${seconds}s`
  }
}

onUpdated(() => {
  tooltip = new bootstrap.Tooltip(buffElement.value)
})

onMounted(() => {
  intervalId.value = setInterval(() => {
    durationIntTime.value = durationReadable()
  }, 1000)
  tooltip = new bootstrap.Tooltip(buffElement.value)
})

onBeforeUpdate(() => {
  tooltip = bootstrap.Tooltip.getInstance(buffElement.value)
  if (tooltip) {
    tooltip.dispose()
  }
})

onUnmounted(() => {
  clearInterval(intervalId.value)
  tooltip?.dispose()
})
</script>

<style scoped lang="scss">
@keyframes flash {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
.buff-wrapper {
  display: inline-block;
  position: relative;
}
.buff {
  &::after {
    color: #fff;
    content: attr(data-duration);
    display: block;
    position: absolute;
    bottom: -5px;
    left: 50%;
    transform: translateX(-50%);
    font-size: .75rem;
    font-weight: 700;
    text-shadow: 0 0 1px #000, 0 0 1px #000, 0 0 2px #000, 0 0 2px #000, 0 0 3px #000, 0 0 4px #000;
    z-index: 1;
  }
}

.buff-icon {
  animation: flash 1s infinite;
}
</style>
