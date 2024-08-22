<template>
  <main class="user-details">
    <div class="container-fluid mb-5 p-0">
      <UserInformation class="ms-1 me-1" v-if="user" :user="user" />
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, watch, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import UserInformation from '@/components/UserInformation.vue'
import type { Player } from '@/types/Player'

const username = window.location.pathname.split('/').pop()

const userStore = useUserStore()
const user = ref(userStore.players.find((player: Player) => player.playerName === username))

watch(() => userStore.players, () => {
  user.value = userStore.players.find((player: Player) => player.playerName === username)
}, { deep: true })

onMounted(() => {
  if (username) {
    userStore.fetchUser(username)
  }
})

</script>

<style scoped lang="scss">
.user-details {
  margin: 0 auto;
  max-width: 1280px;
  width: 100%;
}
</style>