<template>
  <div class="mt-1 text-center">
    <RadialProgress class="progress-circle" :diameter="35" :stroke-width="5" :innerStrokeWidth="5" :completed-steps="completedSteps" :total-steps="totalSteps">

    </RadialProgress>
    <div>
      {{ ability?.ability }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref} from 'vue'
import RadialProgress from "vue3-radial-progress";
import moment from 'moment'
import { useUserStore } from '@/stores/user'

interface Ability {
  ability: string;
  recast: number;
}
const props = defineProps({
  ability: {
    type: Object as () => Ability,
    required: true
  }
})

const userStore = useUserStore()
const completedSteps = ref(0)
const totalSteps = ref(Math.abs(moment().diff(moment.unix(props?.ability.recast), 'seconds')))

setInterval(() => {
  completedSteps.value = completedSteps.value + 1
  if (completedSteps.value >= totalSteps.value) {
    completedSteps.value = 0
  }
}, 1000)

</script>

<style scoped lang="scss">
.progress-circle {
  margin: 0 auto;
}
</style>