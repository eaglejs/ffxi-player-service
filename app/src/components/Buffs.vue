<template>
  <div class="buffs-wrapper mt-1">
    <span v-for="(buff, index) in buffList" :key="index">
      <Buff :buff-id="buff.buff_id" :buff-name="buff.buff_name" :duration="buff.buff_duration" :utc-time="buff.utc_time" />
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ComputedRef } from 'vue'
import Buff from '@/components/Buff.vue'
import type { Buff as BuffData} from '@/types/buff'

const props = defineProps({
  buffData: Object as () => Map<string, BuffData>
})

const buffList: ComputedRef<BuffData[]> = computed(() => {
  return Array.from((props.buffData ?? new Map()).values())
})

</script>

<style scoped lang="scss">
@media screen and (max-width: 431px){
  .buffs-wrapper {
    min-height: 64px;
  }
}
</style>