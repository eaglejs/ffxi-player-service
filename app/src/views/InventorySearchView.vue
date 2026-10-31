<template>
  <main class="inventory">
    <div class="shell container-fluid mb-5">
      <div class="card mb-3">
        <div class="card-header">
          <header class="page-head">
            <h2 class="mb-0">
              <McIcon :path="mdiMagnify" :size="24" />
              Inventory Search
            </h2>
            <div class="d-flex align-items-center gap-2">
              <span
                v-if="search.status.value === 'done' && search.results.value.length"
                class="summary-badge d-none d-sm-inline-flex"
              >
                <b>Matches:</b> {{ search.results.value.length }}
                {{ search.results.value.length === 1 ? 'character' : 'characters' }}
              </span>
              <button
                type="button"
                class="btn btn-sm btn-secondary lock-btn"
                @click="relock"
              >
                <McIcon :path="mdiLockOutline" :size="16" />
                Lock
              </button>
            </div>
          </header>
        </div>

        <div class="card-body">
          <div class="search-bar">
            <McIcon :path="mdiMagnify" :size="22" class="search-icon" />
            <input
              ref="searchInput"
              v-model="search.query.value"
              type="search"
              class="search-input"
              placeholder="Search items by name or id; separate several with commas…"
              aria-label="Search inventory"
              autocomplete="off"
              spellcheck="false"
            />
            <button
              v-if="search.query.value"
              type="button"
              class="clear-btn"
              aria-label="Clear search"
              @click="clearSearch"
            >
              <McIcon :path="mdiClose" :size="18" />
            </button>
          </div>

          <div class="filters">
            <label class="filter">
              <span class="filter-label"><b>Character:</b></span>
              <select v-model="search.playerId.value" class="form-select form-select-sm">
                <option :value="null">All characters</option>
                <option v-for="c in characterOptions" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </label>
            <div class="form-check form-switch filter">
              <input
                id="key-items"
                v-model="search.includeKeyItems.value"
                type="checkbox"
                class="form-check-input"
                role="switch"
              />
              <label class="form-check-label" for="key-items">Include key items</label>
            </div>
          </div>

          <div
            v-if="search.availableBags.value.length > 1"
            class="chips"
            role="group"
            aria-label="Filter by bag"
          >
            <button
              v-for="bag in search.availableBags.value"
              :key="bag"
              type="button"
              class="chip"
              :class="{ on: search.bagFilter.value.includes(bag) }"
              :aria-pressed="search.bagFilter.value.includes(bag)"
              @click="search.toggleBag(bag)"
            >
              {{ bagLabel(bag) }}
            </button>
          </div>
        </div>
      </div>

      <div class="status" aria-live="polite">
        <div v-if="search.status.value === 'idle'" class="card status-card">
          <div class="card-body hint">
            <McIcon :path="mdiMagnify" :size="48" class="hint-icon" />
            <span v-if="search.needsMoreChars.value">Keep typing…</span>
            <span v-else>Type an item name, or an item id, to see who is carrying it.</span>
          </div>
        </div>
        <div v-else-if="search.status.value === 'loading'" class="card status-card">
          <div class="card-body hint flex-row">
            <div class="spinner-border spinner-border-sm text-secondary me-2" role="status">
              <span class="visually-hidden">Searching...</span>
            </div>
            <span>Searching…</span>
          </div>
        </div>
        <div v-else-if="search.status.value === 'error'" class="card status-card">
          <div class="card-body hint flex-row">
            <span>Search failed: {{ search.errorMessage.value }}</span>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary ms-2"
              @click="search.run()"
            >
              Retry
            </button>
          </div>
        </div>
        <div v-else-if="!search.results.value.length" class="card status-card">
          <div class="card-body hint">
            <span>Nobody is carrying “{{ search.query.value.trim() }}”.</span>
          </div>
        </div>
        <p v-if="search.truncated.value && search.status.value === 'done'" class="notice">
          {{ search.truncated.value.total }} items match; showing the best
          {{ search.truncated.value.shown }}. Refine your search to narrow it.
        </p>
      </div>

      <div
        v-if="search.results.value.length"
        class="row justify-content-md-center g-3 results"
      >
        <div
          v-for="character in search.results.value"
          :key="character.playerId"
          class="col-lg-6 d-flex align-items-stretch"
        >
          <ResultCard
            class="w-100"
            :character="character"
            @browse="openBrowser"
          />
        </div>
      </div>
    </div>

    <InventoryBrowser
      v-if="browsing"
      :player-id="browsing.playerId"
      :player-name="browsing.playerName"
      @close="browsing = null"
    />
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { mdiClose, mdiLockOutline, mdiMagnify } from '@mdi/js'
import { bagLabel } from '@/helpers/bags'
import { useInventorySearch } from '@/composables/useInventorySearch'
import { useSecretUnlock } from '@/composables/useSecretUnlock'
import { usePlayerStore } from '@/stores/player'
import InventoryBrowser from '@/components/inventory/InventoryBrowser.vue'
import McIcon from '@/components/inventory/McIcon.vue'
import ResultCard from '@/components/inventory/ResultCard.vue'
import type { CharacterResult } from '@/types/Inventory'

const CHARACTERS_KEY = 'inventoryCharacters'

const router = useRouter()
const { lock } = useSecretUnlock()
const playerStore = usePlayerStore()
const search = useInventorySearch()

const searchInput = ref<HTMLInputElement | null>(null)
const browsing = ref<{ playerId: number; playerName: string } | null>(null)
const remembered = ref<Record<number, string>>(readRemembered())

function readRemembered(): Record<number, string> {
  try {
    return JSON.parse(localStorage.getItem(CHARACTERS_KEY) || '{}')
  } catch {
    return {}
  }
}

// Characters can only be picked from those the UI has seen: online now, or in an earlier search.
const characterOptions = computed(() => {
  const byId = new Map<number, string>()
  for (const [id, name] of Object.entries(remembered.value)) byId.set(Number(id), name)
  for (const player of playerStore.players.values()) {
    if (player?.playerId !== undefined && player.playerName) {
      byId.set(player.playerId, player.playerName)
    }
  }
  return [...byId].map(([id, name]) => ({ id, name })).sort((a, b) => a.name.localeCompare(b.name))
})

watch(
  () => search.results.value,
  (results) => {
    let changed = false
    for (const c of results) {
      if (remembered.value[c.playerId] !== c.playerName) {
        remembered.value = { ...remembered.value, [c.playerId]: c.playerName }
        changed = true
      }
    }
    if (changed) {
      try {
        localStorage.setItem(CHARACTERS_KEY, JSON.stringify(remembered.value))
      } catch {
        // storage unavailable; the picker just forgets between visits
      }
    }
  }
)

function clearSearch() {
  search.clear()
  searchInput.value?.focus()
}

function openBrowser(character: CharacterResult) {
  browsing.value = { playerId: character.playerId, playerName: character.playerName }
}

function relock() {
  lock()
  router.push('/')
}

onMounted(() => {
  searchInput.value?.focus()
  playerStore.fetchPlayers().catch(() => {
    // the picker still works from remembered characters
  })
})
</script>

<style scoped lang="scss">
// main.scss sets body text to white, so the page restores the themed text color itself.
.inventory {
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

.lock-btn {
  align-items: center;
  display: inline-flex;
  gap: 0.35rem;
}

.search-bar {
  align-items: center;
  background-color: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  display: flex;
  gap: 0.6rem;
  padding: 0.35rem 0.85rem;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;

  &:focus-within {
    border-color: var(--bs-primary);
    box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.2);
  }
}

.search-icon {
  color: var(--bs-secondary-color);
}

.search-input {
  background: none;
  border: 0;
  color: var(--bs-body-color);
  flex: 1;
  font-size: 1.05rem;
  min-width: 0;
  outline: none;
  padding: 0.35rem 0;

  &::-webkit-search-cancel-button {
    display: none;
  }
}

.clear-btn {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 0;
  border-radius: 50%;
  color: var(--bs-secondary-color);
  display: inline-flex;
  height: 26px;
  justify-content: center;
  padding: 0;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;
  width: 26px;

  &:hover {
    background-color: var(--bs-border-color);
    color: var(--bs-body-color);
  }
}

.filters {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1.5rem;
  margin-top: 0.85rem;
}

.filter {
  align-items: center;
  display: inline-flex;
  gap: 0.5rem;
  margin: 0;

  select {
    min-width: 12.5rem;
  }
}

.filter-label {
  font-size: 0.9rem;
}

.chips {
  border-top: 1px solid var(--bs-border-color);
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-top: 0.85rem;
  padding-top: 0.85rem;
}

.chip {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-body-color);
  display: inline-flex;
  font-size: 0.82rem;
  font-weight: 500;
  padding: 0.2rem 0.8rem;
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
  }
}

.status-card {
  margin-bottom: 1rem;
}

.hint {
  align-items: center;
  color: var(--bs-secondary-color);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  justify-content: center;
  padding: 2rem 1rem;
  text-align: center;
}

.hint-icon {
  opacity: 0.35;
}

.notice {
  background: var(--bs-warning-bg-subtle);
  border: 1px solid var(--bs-warning-border-subtle);
  border-radius: 8px;
  color: var(--bs-warning-text-emphasis);
  font-size: 0.85rem;
  margin: 0 0 1rem;
  padding: 0.55rem 0.9rem;
}
</style>
