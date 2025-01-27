<template>
  <main>
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center g-3">
        <div class="col-md-6" v-for="(id, $index) in playerIds" :key="id ?? $index">
          <PlayerVitals v-if="id" :player-id="id" />
        </div>
        <div v-if="playerIds.length === 0" class="text-center">
          <h2>No players online</h2>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import PlayerVitals from '@/components/PlayerVitals.vue'
import { usePlayerStore } from '@/stores/player'
import type { Player } from '@/types/Player'

const playerStore = usePlayerStore()
const playerIds = ref(getPlayersId())

function getOnlinePlayers(player: Player): boolean {
  const now = Date.now()
  const lastOnline = player?.lastOnline * 1000 // Convert to milliseconds
  const diffInMinutes = (now - lastOnline) / (1000 * 60) // Convert milliseconds to minutes
  return diffInMinutes < 5
}

function getPlayersId() {
  return Array.from(playerStore.players.values() as Iterable<Player>)
    .reduce((acc: number[], player: Player) => {
      if (getOnlinePlayers(player)) {
        acc.push(player.playerId)
      }
      return acc
    }, [])
    .sort((a: number, b: number) => a - b)
}

watch(
  () => playerStore.players,
  () => {
    playerIds.value = getPlayersId()
  },
  { deep: true }
)

onMounted(() => {
  playerStore.fetchPlayers()
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      playerStore.fetchPlayers()
      playerIds.value = getPlayersId()
    }
  })
})
</script>

<style scoped lang="scss">
[data-bs-theme='light'] {
  h2 {
    color: var(--bs-dark);
  }
}
</style>
