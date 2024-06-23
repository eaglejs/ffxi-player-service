<template>
  <div class="container-fluid mt-1">
    <div class="row">
      <AbilityVue class="col-2" v-for="(ability, index) in filteredAbilities" :key="ability.ability" :ability="ability" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, ref } from 'vue'
import AbilityVue from '@/components/AbilityVue.vue'
import moment from 'moment'
import type { Ability } from '@/types/Ability'

const props = defineProps({
  abilities: Array<Ability>
})

const allAbilities = ref(props?.abilities || [])

// Computed property to filter abilities
const filteredAbilities = computed(() => {
  return allAbilities.value.filter(ability => {
    // Calculate the difference between the current time and the recast time
    const diffInSeconds = Math.abs(moment().diff(moment.unix(ability.recast), 'seconds'))
    // Filter out abilities where the difference is less than or equal to 0
    return diffInSeconds > 0
  });
})


</script>

<style scoped lang="scss"></style>