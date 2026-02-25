<template>
  <div class="app-version">
    <p class="version-text m-0">Version {{ version }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed} from 'vue'
import { uiPackageVersion } from '@/helpers/config'

const props = defineProps<{
  themeColor: string
}>()

const version = computed(() => uiPackageVersion)

</script>

<style scoped lang="scss">
.app-version {
  .version-text {
    font-size: 0.75rem;
    color: v-bind('props.themeColor');
  }
}
</style>