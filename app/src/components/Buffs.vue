<template>
  <div class="buffs-wrapper mt-1">
    <span v-for="(buffId, index) in buffIds" :key="index">
      <Buff :buff-id="buffId" :buff-name="formattedBuffNames[buffId].en" />
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import Buff from '@/components/Buff.vue'
import { BUFFS } from '@/constants/buffs'

const props = defineProps({
  buffData: String
})

const buffNames: any = computed(() => {
  if (!props?.buffData) {
    return []
  }
  return props?.buffData?.split(',').sort()
})

const buffIds: any = computed(() => {
  if (!buffNames.value) {
    return []
  }
  return buffNames.value.map((name: string) => {
    const buff: any = Object.values(BUFFS).find((b: any) => b.en === name)
    return buff ? buff.id : 'Not found'
  }) || []
})

const formattedBuffNames: any = computed(() => {
  const result: any = {}
  buffIds.value.forEach((buffId: string) => {
    const buff = BUFFS[buffId]
    if (buff) {
      result[buffId] = { en: buff.en.charAt(0).toUpperCase() + buff.en.slice(1) }
    } else {
      result[buffId] = { en: 'Not found' }
    }
  })
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