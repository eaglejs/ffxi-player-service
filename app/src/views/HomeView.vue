<template>
  <main>
    <div class="container-fluid">
      <div class="row justify-content-md-center">
        <div class="col-md-6" v-for="user in players" :key="user.playerName">
          <User class="ms-1 me-1" :user="user" />
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import User from '@/components/User.vue'
import { useUserStore } from '@/stores/user'
import moment from 'moment'

const userStore = useUserStore()
const players = computed(() => userStore.players.filter((user: any) => moment().diff(moment.unix(user?.lastOnline), 'minutes') < 5))

</script>
