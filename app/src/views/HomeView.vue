<template>
  <main>
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center g-3">
        <div class="col-md-6" v-for="user in players" :key="user.playerName">
          <User v-if="user" :user="user" />
        </div>
        <div v-if="players.length === 0" class="text-center">
          <h2>No players online</h2>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import User from '@/components/User.vue'
import { useUserStore } from '@/stores/user'
import type { Player } from '@/types/Player'

const userStore = useUserStore()
const players = ref(getPlayers())

watch(() => userStore.players, () => {
  players.value = getPlayers()
}, {deep: true})

onMounted(() => {
  userStore.fetchUsers();
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      userStore.fetchUsers();
      players.value = getPlayers();
    }
  });
})

function getOnlinePlayers(user: Player): boolean{
  const now = Date.now();
  const lastOnline = user?.lastOnline * 1000; // Convert to milliseconds
  const diffInMinutes = (now - lastOnline) / (1000 * 60); // Convert milliseconds to minutes
  return diffInMinutes < 5;
}

function getPlayers() {
  return Array.from(userStore.players, ([playerId, value]) => (value)).filter((user: Player) => getOnlinePlayers(user)).sort((a: Player, b: Player) => a.playerName.localeCompare(b.playerName));
}

</script>

<style scoped lang="scss">
[data-bs-theme="light"] {
  h2 {
    color: var(--bs-dark);
  }
}
</style>