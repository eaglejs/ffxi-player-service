<template>
  <main class="rema">
    <div class="shell container-fluid mb-5">
      <div class="card mb-3">
        <div class="card-header">
          <header class="page-head">
            <h2 class="mb-0">
              <McIcon :path="mdiSwordCross" :size="24" />
              REMA Weapons
            </h2>
            <div class="d-flex align-items-center gap-2">
              <span v-if="rema.players.value.length" class="summary-badge d-none d-sm-inline-flex">
                <b>Characters:</b> {{ rema.players.value.length }}
              </span>
              <button
                type="button"
                class="btn btn-sm btn-secondary refresh-btn"
                :disabled="rema.status.value === 'loading'"
                @click="rema.refresh()"
              >
                <McIcon :path="mdiRefresh" :size="16" />
                Refresh
              </button>
            </div>
          </header>
        </div>
        <div class="card-body">
          <p class="lede">
            Relic, Empyrean, Mythic, Aeonic and Prime weapons across every character with a synced
            inventory. The badge shows the highest upgrade stage held.
          </p>

          <div v-if="rema.data.value" class="chips" role="group" aria-label="Show categories">
            <button
              type="button"
              class="chip"
              :class="{ on: selected.length === 0 }"
              :aria-pressed="selected.length === 0"
              @click="selected = []"
            >
              All
              <span class="chip-count">{{ totalOwnedAcrossAll }}</span>
            </button>
            <button
              v-for="category in rema.data.value.categories"
              :key="category.key"
              type="button"
              class="chip"
              :class="[`chip-${category.key}`, { on: selected.includes(category.key) }]"
              :aria-pressed="selected.includes(category.key)"
              @click="toggle(category.key)"
            >
              <span class="chip-dot" :class="`dot-${category.key}`" />
              {{ category.label }}
              <span class="chip-count">{{ ownedAcrossPlayers(category.key) }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="status" aria-live="polite">
        <div
          v-if="rema.status.value === 'loading' && !rema.players.value.length"
          class="card status-card"
        >
          <div class="card-body hint">
            <div class="spinner-border spinner-border-sm text-secondary me-2" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
            <span>Loading weapons…</span>
          </div>
        </div>
        <div v-else-if="rema.status.value === 'error'" class="card status-card">
          <div class="card-body hint">
            <span>Could not load REMA weapons: {{ rema.errorMessage.value }}</span>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary ms-2"
              @click="rema.refresh()"
            >
              Retry
            </button>
          </div>
        </div>
        <div
          v-else-if="rema.status.value === 'done' && !rema.players.value.length"
          class="card status-card"
        >
          <div class="card-body hint">
            <p class="mb-0">No character has synced an inventory yet.</p>
          </div>
        </div>
      </div>

      <div v-if="rema.players.value.length" class="row justify-content-md-center g-3 grid">
        <div
          v-for="player in rema.players.value"
          :key="player.playerId"
          class="col-lg-6 d-flex align-items-stretch"
        >
          <RemaPlayerCard
            class="w-100"
            :player="player"
            :category-keys="selected"
          />
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
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

const totalOwnedAcrossAll = computed(() =>
  rema.players.value.reduce((n, p) => n + p.ownedTotal, 0)
)

onMounted(() => rema.refresh())
</script>

<style scoped lang="scss">
// main.scss sets body text to white, so the page restores the themed text color itself.
.rema {
  color: var(--bs-body-color);
}

.page-head {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: space-between;

  h2 {
    align-items: center;
    display: flex;
    gap: 0.5rem;
  }
}

.summary-badge {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 6px;
  font-size: 0.85rem;
  gap: 0.35rem;
  padding: 0.2rem 0.65rem;
}

.refresh-btn {
  align-items: center;
  display: inline-flex;
  gap: 0.35rem;
}

.lede {
  color: var(--bs-secondary-color);
  font-size: 0.92rem;
  margin: 0 0 0.85rem;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
}

.chip {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-body-color);
  display: inline-flex;
  font-size: 0.85rem;
  font-weight: 500;
  gap: 0.4rem;
  padding: 0.25rem 0.85rem;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease,
    transform 0.15s ease;

  &:hover {
    border-color: var(--bs-primary);
    transform: translateY(-1px);
  }

  &.on {
    background-color: var(--bs-primary);
    border-color: var(--bs-primary);
    color: #fff;

    .chip-count {
      background-color: rgba(255, 255, 255, 0.22);
      color: #fff;
    }

    .chip-dot {
      box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.45);
    }
  }
}

.chip-dot {
  border-radius: 50%;
  display: inline-block;
  height: 8px;
  width: 8px;

  &.dot-relic {
    background-color: #e9a400;
  }
  &.dot-empyrean {
    background-color: #3984fa;
  }
  &.dot-mythic {
    background-color: #8a19bc;
  }
  &.dot-aeonic {
    background-color: #4a9c58;
  }
  &.dot-prime {
    background-color: #ff6384;
  }
}

.chip-count {
  background-color: var(--bs-body-bg);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.75rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  line-height: 1.3;
  padding: 0.05rem 0.45rem;
}

.status-card {
  margin-bottom: 1rem;
}

.hint {
  align-items: center;
  color: var(--bs-secondary-color);
  display: flex;
  justify-content: center;
  padding: 2rem 1rem;
  text-align: center;
}
</style>
