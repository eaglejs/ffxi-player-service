<template>
  <GenTooltip
    :tip="onlineTitleText"
    placement="top"
  >
    <template #main>
      <span
        :class="onlineStatusDot"
        :title="onlineTitleText"
        data-bs-toggle="tooltip"
        data-bs-placement="top"
      ></span>
    </template>
  </GenTooltip>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { Player } from '@/types/Player';
import { useUserStore } from '@/stores/user'
import GenTooltip from '@/components/gen-components/GenTooltip.vue'

const props = defineProps<{
  user: Player | undefined
}>()

const userStore = useUserStore()
const onlineTitleText = ref('Offline')
const onlineStatusDot = ref('offline-dot')
const isOnline = ref(Date.now() - (props.user?.lastOnline ?? 0) * 1000 < 60000)

const checkOnlineState = () => {
  isOnline.value = Date.now() - (props.user?.lastOnline ?? 0) * 1000 < 60000
  onlineTitleText.value = isOnline.value ? 'Online' : 'Offline'
  onlineStatusDot.value = isOnline.value ? 'online-dot' : 'offline-dot'
}

onMounted(() => {
  setInterval(checkOnlineState, 5000)
})

watch(userStore?.players, () => {
  checkOnlineState()
})
</script>

<style scoped lang="scss">
%status-dot {
  content: '';
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 50%;
  margin-right: 10px;
}

.online-dot {
  &::before {
    background-color: green;
    @extend %status-dot;
  }
}

.offline-dot {
  &::before {
    background-color: red;
    @extend %status-dot;
  }
}
</style>
