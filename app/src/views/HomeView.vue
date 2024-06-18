<template>
  <main>
    <div class="container-fluid">
      <div class="row">
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

const userStore = useUserStore()
const players = computed(() => userStore.players)

</script>
