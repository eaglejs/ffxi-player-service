<template>
  <div class="card p-2">
    <p class="mb-0"><b>Currency 2</b></p>
    <section class="container-fluid" v-if="currencies">
      <div class="row" v-for="currency in currencyData" :key="currency.id">
        <div class="col-7">
          {{ currency.name }}:
        </div> 
        <div class="col-5 ms-auto text-end">
          <b>{{ formattedCurrency(parseInt(currencies[currency.id])) }}</b>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const currencyData = [
  {id: 'domainPoints', name: 'Domain Points'},
  {id: 'eschaBeads', name: 'Escha Beads'},
  {id: 'eschaSilt', name: 'Escha Silt'},
  {id: 'gallantry', name: 'Gallantry'},
  {id: 'gallimaufry', name: 'Gallimaufry'},
  {id: 'hallmarks', name: 'Hallmarks'},
  {id: 'mogSegments', name: 'Mog Segments'},
  {id: 'mweyaPlasmCorpuscles', name: 'Mweya Plasm'},
  {id: 'potpourri', name: 'Potpourri'},
]

const props = defineProps({
  currencies: Object,
})

const formattedCurrency = (value: number) => {
  return value.toLocaleString()
}
</script>