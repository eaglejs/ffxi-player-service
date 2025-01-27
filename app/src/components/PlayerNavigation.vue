<template>
  <nav>
    <ul>
      <li class="col-md-6" v-for="player in players" :key="player.playerName">
        <Player :player="player" />
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import Player from '@/components/PlayerVitals.vue'
import { onMounted } from 'vue';

const props = defineProps({
  players: Object,
})

onMounted( () => {
  // console.log(props.players);
});

</script>

<style scoped lang="scss"></style>
