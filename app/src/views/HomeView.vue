<template>
  <main>
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center g-3">
        <div class="col-md-6" v-for="(id, $index) in playerIds" :key="id ?? $index">
          <User v-if="id" :player-id="id" />
        </div>
        <div v-if="playerIds.length === 0" class="text-center">
          <h2>No players online</h2>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import User from '@/components/User.vue'
import { useUserStore } from '@/stores/user'
import type { Player } from '@/types/Player'

const userStore = useUserStore()
const playerIds = ref(getPlayersId())

watch(() => userStore.players, () => {
  playerIds.value = getPlayersId()
}, {deep: true})

onMounted(() => {
  userStore.fetchUsers();
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      userStore.fetchUsers();
      playerIds.value = getPlayersId();
    }
  });
})

function getOnlinePlayers(user: Player): boolean{
  const now = Date.now();
  const lastOnline = user?.lastOnline * 1000; // Convert to milliseconds
  const diffInMinutes = (now - lastOnline) / (1000 * 60); // Convert milliseconds to minutes
  return diffInMinutes < 5;
}

function getPlayersId() {
  return Array.from(userStore.players.values())?.map((user: Player) => {
    return getOnlinePlayers(user) ? user.playerId : null;
  }).sort((a, b) => {
    if (a === null) return 1;
    if (b === null) return -1;
    return a - b;
  });
}

</script>

<style scoped lang="scss">
[data-bs-theme="light"] {
  h2 {
    color: var(--bs-dark);
  }
}
</style>