<template>
  <div class="card mb-3 mt-3 p-0" :data-bs-theme="theme">
    <div class="card-header">
      <div class="d-flex justify-content-between">
        <h2 class="">Player Status - ({{ user?.title }})</h2>
      </div>
    </div>
    <div class="card-body">
      <div class="row">
        <div class="col-md-3 col-lg-2">
          <p class="mb-0"><b>Offensive Stats</b></p>
          <section v-for="stat, index in baseStats" :key="stat.id">
            {{ stat.name }}: {{ user?.stats[stat.id] }} + {{ user?.stats[addedStats[index].id] }}
          </section>
        </div>
        <div class="col-md-4 col-lg-3">
          <div class="row">
            <p class="mb-0"><b>Elemental Resistance</b></p>
            <section class="col-12">
              <span v-for="resistance in resistancesGroupLight" :key="resistance.id">
                <img :src="`${iconsPath}${resistance.id}.webp`" class="me-2"/>
                <span class="me-2">
                  {{ getValue(resistance.id) }}
                </span>
              </span>
            </section>
          </div>
          <div class="row">
            <section class="col-12">
              <span v-for="resistance in resistancesGroupDark" :key="resistance.id">
                <img :src="`${iconsPath}${resistance.id}.webp`" class="me-2"/>
                <span class="me-2">
                  {{ getValue(resistance.id) }}
                </span>
              </span>
            </section>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { iconsPath } from '@/helpers/config'

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

const resistancesGroupLight = [
  {id: 'fire', name: 'Fire'},
  {id: 'wind', name: 'Wind'},
  {id: 'lightning', name: 'Lightning'},
  {id: 'light', name: 'Light'},
]

const resistancesGroupDark = [
  {id: 'ice', name: 'Ice'},
  {id: 'earth', name: 'Earth'},
  {id: 'water', name: 'Water'},
  {id: 'dark', name: 'Dark'},
]

const props = defineProps({
  user: Object,
})

const theme: any = computed(() => themeStore.theme === 'dark' ? 'gray-dark' : 'gray-light')
const themeStore = useThemeStore()

const getValue = (resistance: string) => {
  return `${props.user?.stats?.[resistance + 'Resistance']}`
}

</script>

<style scoped lang="scss"></style>