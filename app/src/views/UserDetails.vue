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

const playerId = parseInt(window.location.pathname.split('/').pop() || '')

const userStore = useUserStore();
const user = ref(userStore.players.get(playerId));

watch(() => userStore.players, () => {
  user.value = userStore.players.get(playerId)
}, { deep: true })

onMounted(() => {
  if (playerId) {
    userStore.fetchUser(playerId)
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