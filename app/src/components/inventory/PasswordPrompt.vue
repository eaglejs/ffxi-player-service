<template>
  <div class="backdrop" @click.self="emit('close')">
    <form
      class="prompt"
      :class="{ shake }"
      role="dialog"
      aria-modal="true"
      aria-label="Enter passphrase"
      @submit.prevent="submit"
      @keydown.esc="emit('close')"
    >
      <input
        ref="input"
        v-model="value"
        type="password"
        class="form-control"
        autocomplete="off"
        aria-label="Passphrase"
        placeholder="Passphrase"
      />
      <p v-if="failed" class="error" role="alert">Incorrect passphrase</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useSecretUnlock } from '@/composables/useSecretUnlock'

const emit = defineEmits<{ close: [] }>()
const { tryPassword } = useSecretUnlock()

const value = ref('')
const failed = ref(false)
const shake = ref(false)
const input = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  await nextTick()
  input.value?.focus()
})

function submit() {
  if (tryPassword(value.value)) {
    emit('close')
    return
  }
  failed.value = true
  shake.value = true
  value.value = ''
  setTimeout(() => (shake.value = false), 400)
}
</script>

<style scoped lang="scss">
.backdrop {
  align-items: flex-start;
  background: rgb(0 0 0 / 55%);
  display: flex;
  inset: 0;
  justify-content: center;
  padding-top: 20vh;
  position: fixed;
  z-index: 1060;
}

.prompt {
  max-width: 320px;
  width: 90%;
}

.error {
  color: #ff8d8d;
  font-size: 0.85rem;
  margin: 0.5rem 0 0;
}

.shake {
  animation: shake 0.4s;
}

@keyframes shake {
  20%,
  60% {
    transform: translateX(-6px);
  }
  40%,
  80% {
    transform: translateX(6px);
  }
}
</style>
