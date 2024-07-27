<template>
  <div>
    <div class="card p-2" v-if="stats">
      <p class="mb-0"><b>Offensive Stats</b></p>
      <section v-for="stat, index in baseStats" :key="stat.id">
        {{ stat.name }}: {{ stats[stat.id] }} + {{ stats[addedStats[index].id] }}
      </section>
    </div>
    <UserResistances class="mt-3" :resistances="stats" />
  </div>
</template>

<script setup lang="ts">
import UserResistances from '@/components/UserResistances.vue'
const baseStats = [
  {id: 'baseSTR', name: 'STR'},
  {id: 'baseDEX', name: 'DEX'},
  {id: 'baseINT', name: 'INT'},
  {id: 'baseVIT', name: 'VIT'},
  {id: 'baseAGI', name: 'AGI'},
  {id: 'baseMND', name: 'MND'},
  {id: 'baseCHR', name: 'CHR'},
]

const addedStats = [
  {id: 'addedSTR', name: 'Added STR'},
  {id: 'addedDEX', name: 'Added DEX'},
  {id: 'addedINT', name: 'Added INT'},
  {id: 'addedVIT', name: 'Added VIT'},
  {id: 'addedAGI', name: 'Added AGI'},
  {id: 'addedMND', name: 'Added MND'},
  {id: 'addedCHR', name: 'Added CHR'},
]

const props = defineProps({
  stats: Object,
})
</script>