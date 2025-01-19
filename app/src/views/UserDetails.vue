<template>
  <main class="user-details">
    <UserInformation class="ms-1 me-1" v-if="playerId" :player-id="playerId" />
  </main>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import UserInformation from '@/components/UserInformation.vue'

const playerId = parseInt(window.location.pathname.split('/').pop() || '')

const userStore = useUserStore()

onMounted(() => {
  if (playerId) {
    userStore.fetchUser(playerId)
  }
})
</script>

<style scoped lang="scss">
.user-details {
  margin: 0 auto;
  max-width: 1920px;
  width: 100%;
}
</style>
