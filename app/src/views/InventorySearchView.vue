<template>
  <main class="inventory">
    <div class="shell container-fluid">
      <header class="page-head">
        <h2>
          <McIcon :path="mdiMagnify" :size="26" />
          Inventory Search
        </h2>
        <button type="button" class="btn btn-sm btn-outline-secondary lock-btn" @click="relock">
          <McIcon :path="mdiLockOutline" :size="16" />
          Lock
        </button>
      </header>

      <div class="search-bar">
        <McIcon :path="mdiMagnify" :size="22" class="search-icon" />
        <input
          ref="searchInput"
          v-model="search.query.value"
          type="search"
          class="search-input"
          placeholder="Search items by name or id…"
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
          <span>Character</span>
          <select v-model="search.playerId.value" class="form-select form-select-sm">
            <option :value="null">All characters</option>
            <option v-for="c in characterOptions" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </label>
        <div class="form-check form-switch filter">
          <input id="key-items" v-model="search.includeKeyItems.value" type="checkbox" class="form-check-input" role="switch" />
          <label class="form-check-label" for="key-items">Include key items</label>
        </div>
      </div>

      <div v-if="search.availableBags.value.length > 1" class="chips" role="group" aria-label="Filter by bag">
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

      <div class="status" aria-live="polite">
        <p v-if="search.status.value === 'idle'" class="hint">
          <McIcon :path="mdiMagnify" :size="56" class="hint-icon" />
          <span v-if="search.needsMoreChars.value">Keep typing…</span>
          <span v-else>Type an item name, or an item id, to see who is carrying it.</span>
        </p>
        <p v-else-if="search.status.value === 'loading'" class="hint">Searching…</p>
        <div v-else-if="search.status.value === 'error'" class="hint">
          <span>Search failed: {{ search.errorMessage.value }}</span>
          <button type="button" class="btn btn-sm btn-outline-secondary ms-2" @click="search.run()">Retry</button>
        </div>
        <p v-else-if="!search.results.value.length" class="hint">
          Nobody is carrying “{{ search.query.value.trim() }}”.
        </p>
        <p v-if="search.truncated.value && search.status.value === 'done'" class="notice">
          {{ search.truncated.value.total }} items match; showing the best
          {{ search.truncated.value.shown }}. Refine your search to narrow it.
        </p>
      </div>

      <div class="results">
        <ResultCard
          v-for="character in search.results.value"
          :key="character.playerId"
          :character="character"
          @browse="openBrowser"
        />
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

.shell {
  margin: 0 auto;
  max-width: 880px;
  padding-bottom: 3rem;
}

.page-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin: 0.5rem 0 1rem;

  h2 {
    align-items: center;
    display: flex;
    gap: 0.5rem;
    margin: 0;
  }
}

.lock-btn {
  align-items: center;
  display: inline-flex;
  gap: 0.3rem;
}

.search-bar {
  align-items: center;
  background: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  display: flex;
  gap: 0.5rem;
  padding: 0.35rem 0.9rem;
  transition:
    border-color 0.15s,
    box-shadow 0.15s;

  &:focus-within {
    border-color: #c9b27c;
    box-shadow: 0 0 0 0.2rem rgb(201 178 124 / 25%);
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
  font-size: 1.1rem;
  min-width: 0;
  outline: none;
  padding: 0.35rem 0;

  &::-webkit-search-cancel-button {
    display: none;
  }
}

.clear-btn {
  background: none;
  border: 0;
  color: var(--bs-secondary-color);
  display: inline-flex;
  padding: 0.2rem;
}

.filters {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1.5rem;
  margin: 0.9rem 0 0.25rem;
}

.filter {
  align-items: center;
  display: inline-flex;
  gap: 0.5rem;
  margin: 0;

  select {
    min-width: 12rem;
  }
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-top: 0.75rem;
}

.chip {
  background: none;
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-body-color);
  font-size: 0.8rem;
  padding: 0.15rem 0.75rem;
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

.status {
  min-height: 1rem;
}

.hint {
  align-items: center;
  color: var(--bs-secondary-color);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  justify-content: center;
  margin: 2.5rem 0;
  text-align: center;
}

.hint-icon {
  opacity: 0.3;
}

.notice {
  background: var(--bs-warning-bg-subtle);
  border-radius: 8px;
  color: var(--bs-warning-text-emphasis);
  font-size: 0.85rem;
  margin: 1rem 0 0;
  padding: 0.5rem 0.9rem;
}

.results {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}
</style>
