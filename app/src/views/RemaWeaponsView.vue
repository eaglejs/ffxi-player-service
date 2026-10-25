<template>
  <main class="rema">
    <div class="shell container-fluid">
      <header class="page-head">
        <h2>
          <McIcon :path="mdiSwordCross" :size="26" />
          REMA Weapons
        </h2>
        <button type="button" class="btn btn-sm btn-outline-secondary refresh-btn" :disabled="rema.status.value === 'loading'" @click="rema.refresh()">
          <McIcon :path="mdiRefresh" :size="16" />
          Refresh
        </button>
      </header>
      <p class="lede">Relic, Empyrean, Mythic, Aeonic and Prime weapons across every character with a synced inventory. The badge shows the highest upgrade stage held.</p>

      <div v-if="rema.data.value" class="chips" role="group" aria-label="Show categories">
        <button
          type="button"
          class="chip"
          :class="{ on: selected.length === 0 }"
          :aria-pressed="selected.length === 0"
          @click="selected = []"
        >
          All
        </button>
        <button
          v-for="category in rema.data.value.categories"
          :key="category.key"
          type="button"
          class="chip"
          :class="{ on: selected.includes(category.key) }"
          :aria-pressed="selected.includes(category.key)"
          @click="toggle(category.key)"
        >
          {{ category.label }}
          <span class="chip-count">{{ ownedAcrossPlayers(category.key) }}</span>
        </button>
      </div>

      <div class="status" aria-live="polite">
        <p v-if="rema.status.value === 'loading' && !rema.players.value.length" class="hint">Loading weapons…</p>
        <div v-else-if="rema.status.value === 'error'" class="hint">
          <span>Could not load REMA weapons: {{ rema.errorMessage.value }}</span>
          <button type="button" class="btn btn-sm btn-outline-secondary ms-2" @click="rema.refresh()">Retry</button>
        </div>
        <p v-else-if="rema.status.value === 'done' && !rema.players.value.length" class="hint">
          No character has synced an inventory yet.
        </p>
      </div>

      <div class="grid">
        <RemaPlayerCard
          v-for="player in rema.players.value"
          :key="player.playerId"
          :player="player"
          :category-keys="selected"
        />
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { mdiRefresh, mdiSwordCross } from '@mdi/js'
import { useRemaOverview } from '@/composables/useRemaOverview'
import McIcon from '@/components/inventory/McIcon.vue'
import RemaPlayerCard from '@/components/rema/RemaPlayerCard.vue'

const rema = useRemaOverview()
const selected = ref<string[]>([])

function toggle(key: string) {
  selected.value = selected.value.includes(key)
    ? selected.value.filter((k) => k !== key)
    : [...selected.value, key]
}

/** Weapons of this category held across all characters, for the chip counts. */
function ownedAcrossPlayers(key: string): number {
  return rema.players.value.reduce(
    (n, p) => n + (p.categories.find((c) => c.key === key)?.owned.length ?? 0),
    0
  )
}

onMounted(() => rema.refresh())
</script>

<style scoped lang="scss">
// main.scss sets body text to white, so the page restores the themed text color itself.
.rema {
  color: var(--bs-body-color);
}

.shell {
  margin: 0 auto;
  max-width: 1200px;
  padding-bottom: 3rem;
}

.page-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin: 0.5rem 0 0.25rem;

  h2 {
    align-items: center;
    display: flex;
    gap: 0.5rem;
    margin: 0;
  }
}

.refresh-btn {
  align-items: center;
  display: inline-flex;
  gap: 0.3rem;
}

.lede {
  color: var(--bs-secondary-color);
  font-size: 0.9rem;
  margin: 0 0 0.9rem;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}

.chip {
  background: none;
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-body-color);
  font-size: 0.85rem;
  padding: 0.15rem 0.85rem;
  transition:
    background-color 0.15s,
    border-color 0.15s;

  &:hover {
    border-color: #c9b27c;
  }

  &.on {
    background: #0c2d66;
    border-color: #c9b27c;
    color: #f3e3b3;
  }
}

.chip-count {
  font-size: 0.75rem;
  font-variant-numeric: tabular-nums;
  margin-left: 0.3rem;
  opacity: 0.7;
}

.hint {
  align-items: center;
  color: var(--bs-secondary-color);
  display: flex;
  justify-content: center;
  margin: 2.5rem 0;
  text-align: center;
}

.grid {
  align-items: start;
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 520px), 1fr));
  margin-top: 0.75rem;
}
</style>
