<template>
  <div class="mt-1">
    <span v-for="(buffId, index) in buffIds" :key="index">
      <Buff :buff-id="buffId" :buff-name="formattedBuffNames[buffId].en" :url-path="urlPath" />
      <!-- <img :src="`${urlPath}${buffId}.png`" :alt="formattedBuffNames[buffId].en" data-bs-toggle="tooltip"
        data-bs-placement="bottom" :title="formattedBuffNames[buffId].en" /> -->
    </span>
  </div>
</template>

<script setup lang="ts">
import { defineProps, computed, ref } from 'vue'
import Buff from '@/components/Buff.vue'
import { BUFFS } from '@/constants/buffs'

const urlPath = ref(import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/icons/`);

const props = defineProps({
  buffData: String
})

const buffNames: any = computed(() => {
  return props?.buffData?.split(',').map(buff => buff.trim()) || []
})

const buffIds: any = computed(() => {
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

<style scoped lang="scss"></style>