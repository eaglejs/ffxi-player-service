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
import { ref, watch } from 'vue'
import User from '@/components/User.vue'
import { useUserStore } from '@/stores/user'
import moment from 'moment'

const userStore = useUserStore()
const players = ref(userStore.players.filter((user: any) => moment().diff(moment.unix(user?.lastOnline), 'minutes') < 5))

watch(() => userStore.players, () => {
  players.value = userStore.players.filter((user: any) => moment().diff(moment.unix(user?.lastOnline), 'minutes') < 5)
}, {deep: true})

</script>

<style scoped lang="scss">
[data-bs-theme="light"] {
  h2 {
    color: var(--bs-dark);
  }
}
</style>