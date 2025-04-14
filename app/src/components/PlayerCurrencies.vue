<template>
  <div class="card">
    <div class="card-header">
      <h2 class="p-0 m-0">Currency {{ type }}</h2>
    </div>
    <div class="card-body">
      <section class="container-fluid currency-wrapper" v-if="currencies">
        <div class="row" v-for="currency in currencyData" :key="currency.id">
          <div class="col-7 p-0">
            {{ currency.name }}:
          </div>
          <div class="col-5 ms-auto p-0 text-end">
            <b>{{ formattedCurrency(parseInt(currencies[currency.id])) }}</b>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
const currencyData1 = [
  { id: 'conquestPointsBastok', name: 'CP. Bastok' },
  { id: 'conquestPointsSandoria', name: 'CP. San d\'Oria' },
  { id: 'conquestPointsWindurst', name: 'CP. Windurst' },
  { id: 'deeds', name: 'Deeds' },
  { id: 'dominionNotes', name: 'Dominion' },
  { id: 'imperialStanding', name: 'Imp. Standing' },
  { id: 'loginPoints', name: 'Login Points' },
  { id: 'nyzulTokens', name: 'Nyzul Tokens' },
  { id: 'sparksOfEminence', name: 'Sparks' },
  { id: 'therionIchor', name: 'Therion Ichor' },
  { id: 'unityAccolades', name: 'Un. Accolades' },
  { id: 'voidstones', name: 'Voidstones' },
];
const currencyData2 = [
  { id: 'coalitionImprimaturs', name: 'Col. Imprimaturs'},
  { id: 'domainPoints', name: 'Domain Points' },
  { id: 'eschaBeads', name: 'Escha Beads' },
  { id: 'eschaSilt', name: 'Escha Silt' },
  { id: 'gallantry', name: 'Gallantry' },
  { id: 'gallimaufry', name: 'Gallimaufry' },
  { id: 'hallmarks', name: 'Hallmarks' },
  { id: 'mogSegments', name: 'Mog Segments' },
  { id: 'mweyaPlasmCorpuscles', name: 'Mweya Plasm' },
  { id: 'potpourri', name: 'Potpourri' },
];

const props = defineProps({
  currencies: Object,
  type: Number,
})

const currencyData = props.type === 1 ? currencyData1 : currencyData2

const formattedCurrency = (value: number) => {
  return value.toLocaleString()
}
</script>

<style scoped lang="scss">
.currency-wrapper {
  overflow-y: auto;
  max-height: 185px;
}
</style>