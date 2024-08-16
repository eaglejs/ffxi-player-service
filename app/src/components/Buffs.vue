<template>
  <div class="buffs-wrapper mt-1">
    <span v-for="(buffId, index) in buffIds" :key="index">
      <Buff :buff-id="buffId" :buff-name="formattedBuffNames.get(buffId)?.en" />
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ComputedRef } from 'vue'
import Buff from '@/components/Buff.vue'
import { BUFFS } from '@/constants/buffs'

type BuffType = {
  id: number;
  en: string;
};

const props = defineProps({
  buffData: String
})

const buffNames: ComputedRef<string[]> = computed(() => {
  if (!props?.buffData) {
    return []
  }
  return props?.buffData?.split(',').sort()
})

const buffIds: ComputedRef<(number | 'Not found')[]> = computed(() => {
  if (!buffNames.value) {
    return []
  }
  return buffNames.value.map((name: string) => {
    const buff: BuffType | undefined = Object.values(BUFFS).find((b: any): b is BuffType => b.en === name)
    return buff ? buff.id : 'Not found'
  }) || []
})

const formattedBuffNames: ComputedRef<Map<number, { en: string }>> = computed(() => {
  const result: Map<number, { en: string }> = new Map()
  buffIds.value.forEach((buffId: number | "Not found") => {
    if (typeof buffId === 'number') {
      const buff = BUFFS[buffId]
      if (buff) {
        result.set( buffId, { en: buff.en.charAt(0).toUpperCase() + buff.en.slice(1) })
      } else {
        result.set(buffId, { en: 'Not found' })
      }
    }
  });
  return result
})

</script>

<style scoped lang="scss">
@media screen and (max-width: 431px){
  .buffs-wrapper {
    min-height: 64px;
  }
}
</style>