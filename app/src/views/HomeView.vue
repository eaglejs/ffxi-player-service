<template>
  <main>
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center">
        <div class="col-md-6" v-for="user in players" :key="user.playerName">
          <User class="ms-1 me-1" :user="user" />
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
const players = ref(userStore.players.filter((user: Player) => getOnlinePlayers(user)))

watch(() => userStore.players, () => {
  players.value = userStore.players.filter((user: Player) => getOnlinePlayers(user))
}, {deep: true})

onMounted(() => {
  window.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      players.value = userStore.players.filter((user: Player) => getOnlinePlayers(user))
    }
  })
})

function getOnlinePlayers(user: Player): boolean{
  const now = Date.now();
  const lastOnline = user?.lastOnline * 1000; // Convert to milliseconds
  const diffInMinutes = (now - lastOnline) / (1000 * 60); // Convert milliseconds to minutes
  return diffInMinutes < 5;
}
</script>

<style scoped lang="scss">
[data-bs-theme="light"] {
  h2 {
    color: var(--bs-dark);
  }
}
</style>