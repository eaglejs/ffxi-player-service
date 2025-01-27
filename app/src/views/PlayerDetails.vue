<template>
  <main class="player-details">
    <PlayerInformation class="ms-1 me-1" v-if="playerId" :player-id="playerId" />
  </main>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePlayerStore } from '@/stores/player'
import PlayerInformation from '@/components/PlayerInformation.vue'

const playerId = parseInt(window.location.pathname.split('/').pop() || '')

const playerStore = usePlayerStore()

onMounted(() => {
  if (playerId) {
    playerStore.fetchPlayer(playerId)
  }
})
</script>

<style scoped lang="scss">
.player-details {
  margin: 0 auto;
  max-width: 1920px;
  width: 100%;
}
</style>
