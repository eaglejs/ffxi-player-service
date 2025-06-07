<template>
  <section
    class="buff-wrapper"
    :class="{ buff: isTimerPositive() }"
    :data-duration="durationIntTime"
  >
    <GenTooltip :tip="buffName" placement="bottom">
      <template #main>
        <img
          :class="{ 'buff-icon': isLowTimer() }"
          ref="buffElement"
          :src="buffIcon"
          :alt="buffName"
        />
      </template>
    </GenTooltip>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, onUnmounted } from 'vue'
import type { ComputedRef } from 'vue'
import { iconsPath } from '@/helpers/config'
import GenTooltip from '@/components/gen-components/GenTooltip.vue'

interface Player {
  playerId: number
  playerName: string
}

const intervalId = ref()
const durationIntTime = ref()

const props = defineProps({
  player: {
    type: Object as () => Player,
    required: true
  },
  buffId: Number,
  buffName: {
    type: String,
    default: ''
  },
  duration: Number,
  utcTime: String
})

const buffIcon: ComputedRef<string> = computed(() => {
  return `${iconsPath}${props.buffId}.webp`
})

function isLowTimer(): boolean {
  const now = new Date().getTime()
  const duration = props.utcTime ? Date.parse(props.utcTime) : new Date().getTime()
  return duration - now <= 15000
}

function isTimerPositive(): boolean {
  const now = new Date().getTime()
  const duration = props.utcTime ? Date.parse(props.utcTime) : new Date().getTime()
  return duration - now > 0
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

onMounted(() => {
  intervalId.value = setInterval(() => {
    durationIntTime.value = durationReadable()
    // if (!isTimerPositive()) {
    //   playerStore.refreshBuffs(props?.player)
    // }
  }, 1000)
})

onUnmounted(() => {
  clearInterval(intervalId.value)
})
</script>

<style scoped lang="scss">
@keyframes flash {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
.buff-wrapper {
  display: inline-block;
  margin: 1px;
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
    font-size: 0.75rem;
    font-weight: 700;
    text-shadow:
      0 0 1px #000,
      0 0 1px #000,
      0 0 2px #000,
      0 0 2px #000,
      0 0 3px #000,
      0 0 4px #000;
    z-index: 1;
  }
}

.buff-icon {
  animation: flash 1s infinite;
}
</style>
