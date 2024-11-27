<template>
  <nav>
    <ul>
      <li class="col-md-6" v-for="user in players" :key="user.playerName">
        <User :user="user" />
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import User from '@/components/User.vue'
import { onMounted } from 'vue';

const props = defineProps({
  players: Object,
})

onMounted( () => {
  console.log(props.players);
});

</script>

<style scoped lang="scss"></style>
