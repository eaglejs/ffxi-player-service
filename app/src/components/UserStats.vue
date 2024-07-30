<template>
  <div>
    <div class="card" v-if="stats">
      <div class="card-header">
        <h3 class="mb-0">Offensive Stats</h3>
      </div>
      <div class="card-body">
        <section class="container-fluid">
          <section class="row" v-for="stat, index in baseStats" :key="stat.id">
            <div class="col-7 p-0">
              {{ stat.name }}:
            </div>
            <div class="col-5 ms-auto p-0 text-end">
              <b>{{ stats[stat.id] }} + <span class="text-success">{{ stats[addedStats[index].id] }}</span></b>
            </div>
          </section>
        </section>
      </div>
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