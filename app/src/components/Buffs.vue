<template>
  <div class="buffs-wrapper">
    <div class="buffs-section">
      <span v-for="(buff) in buffList" :key="buff.utc_time+buff.buff_id">
        <Buff :player="player" :buff-id="buff.buff_id" :buff-name="buff.buff_name" :duration="buff.buff_duration" :utc-time="buff.utc_time" />
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ComputedRef } from 'vue'
import Buff from '@/components/Buff.vue'
import type { Buff as BuffData} from '@/types/buff'

interface Player {
  playerId: number
  playerName: string
}

const props = defineProps({
  player: {
    type: Object as () => Player,
    required: true
  },
  buffData: Object as () => Map<string, BuffData>
})

const buffList: ComputedRef<BuffData[]> = computed(() => {
  return Array.from((props.buffData ?? new Map()).values())
})

</script>

<style scoped lang="scss">
.buffs-wrapper {
  container: buffs / inline-size;
}

.buffs-section {
  height: 68px; // 2 rows
  background-color: var(--buff-bg);
  border-radius: 7px;
}

@container buffs (width < 237px) {
  .buffs-section {
    height: 204px; //6 rows
  }
}

@container buffs (width >= 237px) and (width < 272px) {
  .buffs-section {
    height: 170px; //5 rows
  }
}

@container buffs (width >= 272px) and (width < 374px) {
  .buffs-section {
    height: 136px; // 4 rows
  }
}

@container buffs (width >= 374px) and (width < 509px) {
  .buffs-section {
    height: 102px; // 3 rows
  }
}
</style>