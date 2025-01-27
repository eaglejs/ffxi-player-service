<template>
  <main>
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center g-3">
        <div class="col-md-6" v-for="(id, $index) in playerIds" :key="id ?? $index">
          <ExperiencePoints v-if="id" :player="playerStore.getPlayerById(id)" />
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
import ExperiencePoints from '@/components/ExperiencePoints.vue'
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
    .map((player: Player) => {
      return getOnlinePlayers(player) ? player.playerId : null
    })
    .sort((a, b) => {
      if (a === null) return 1
      if (b === null) return -1
      return a - b
    })
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
