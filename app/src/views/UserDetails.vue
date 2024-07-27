<template>
  <main class="user-details">
    <div class="container-fluid mb-5">
      <div class="row justify-content-md-center">
        <div class="col-md-12">
          <User class="ms-1 me-1" v-if="user" :user="user" />
        </div>
      </div>
      <div class="row justify-content-md-center">
        <div class="col-md-12">
          <UserInformation class="ms-1 me-1" v-if="user" :user="user" />
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, watch, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import User from '@/components/User.vue'
import UserInformation from '@/components/UserInformation.vue'

const username = window.location.pathname.split('/').pop()

const userStore = useUserStore()
const user = ref(userStore.players.find((player: any) => player.playerName === username))

watch(() => userStore.players, () => {
  user.value = userStore.players.find((player: any) => player.playerName === username)
}, {deep: true})

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