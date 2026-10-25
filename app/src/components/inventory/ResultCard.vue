<template>
  <section class="window" :aria-label="`${character.playerName} results`">
    <header class="window-head">
      <button type="button" class="char-name" @click="emit('browse', character)">
        <McIcon :path="mdiBagPersonalOutline" :size="18" />
        {{ character.playerName }}
        <span class="browse-hint">browse bags</span>
      </button>
      <span class="meta">
        <strong>{{ character.total.toLocaleString() }}</strong> total
        <template v-if="updated"> · synced {{ updated }}</template>
      </span>
    </header>

    <ul class="rows">
      <li v-for="row in character.rows" :key="`${row.kind}-${row.id}`" class="row-item">
        <ItemIcon :id="row.id" :kind="row.kind" />
        <div class="row-body">
          <div class="row-title">
            <a
              v-if="row.kind === 'item'"
              :href="ffxiahUrl(row.id)"
              target="_blank"
              rel="noopener noreferrer"
              class="item-link"
              :title="`Open ${row.name} on FFXIAH`"
            >
              {{ row.name }}
              <McIcon :path="mdiOpenInNew" :size="13" />
            </a>
            <span v-else class="item-link">{{ row.name }}</span>
            <span v-if="row.category" class="category">{{ row.category }}</span>
            <span class="total">× {{ row.total.toLocaleString() }}</span>
          </div>
          <ul class="pills" aria-label="Locations">
            <li v-for="loc in row.locations" :key="loc.bag" class="pill">
              {{ loc.label }}
              <b>{{ loc.count.toLocaleString() }}</b>
            </li>
          </ul>
        </div>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mdiBagPersonalOutline, mdiOpenInNew } from '@mdi/js'
import { ffxiahUrl } from '@/helpers/items'
import { timeAgo } from '@/helpers/time'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'
import type { CharacterResult } from '@/types/Inventory'

const props = defineProps<{ character: CharacterResult }>()
const emit = defineEmits<{ browse: [character: CharacterResult] }>()

const updated = computed(() => timeAgo(props.character.updatedAt))
</script>

<style scoped lang="scss">
.window {
  background: linear-gradient(180deg, #0c2d66 0%, #031633 100%);
  border: 1px solid #c9b27c;
  border-radius: 12px;
  box-shadow:
    inset 0 0 0 3px #031633,
    inset 0 0 0 4px rgb(201 178 124 / 35%),
    0 6px 18px rgb(0 0 0 / 28%);
  color: #fff;
  padding: 0.5rem 1rem 0.75rem;
}

.window-head {
  align-items: baseline;
  border-bottom: 1px solid rgb(201 178 124 / 30%);
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 1rem;
  justify-content: space-between;
  padding: 0.5rem 0.25rem;
}

.char-name {
  background: none;
  border: 0;
  color: #f3e3b3;
  font-size: 1.15rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  padding: 0;

  &:hover .browse-hint,
  &:focus-visible .browse-hint {
    opacity: 1;
  }
}

.browse-hint {
  color: rgb(255 255 255 / 60%);
  font-size: 0.75rem;
  font-weight: 400;
  margin-left: 0.5rem;
  opacity: 0;
  transition: opacity 0.15s;
}

.meta {
  color: rgb(255 255 255 / 65%);
  font-size: 0.85rem;
}

.rows {
  list-style: none;
  margin: 0;
  padding: 0;
}

.row-item {
  align-items: flex-start;
  display: flex;
  gap: 0.75rem;
  padding: 0.65rem 0.25rem;

  & + & {
    border-top: 1px solid rgb(255 255 255 / 8%);
  }
}

.row-body {
  flex: 1;
  min-width: 0;
}

.row-title {
  align-items: baseline;
  display: flex;
  flex-wrap: wrap;
  gap: 0.15rem 0.6rem;
}

.item-link {
  color: #fff;
  font-weight: 500;
  text-decoration: none;

  &:hover {
    color: #f3e3b3;
    text-decoration: underline;
  }
}

.category {
  color: rgb(255 255 255 / 50%);
  font-size: 0.75rem;
}

.total {
  color: #f3e3b3;
  font-variant-numeric: tabular-nums;
  margin-left: auto;
}

.pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  list-style: none;
  margin: 0.4rem 0 0;
  padding: 0;
}

.pill {
  background: rgb(255 255 255 / 9%);
  border-radius: 999px;
  font-size: 0.8rem;
  padding: 0.1rem 0.65rem;

  b {
    color: #f3e3b3;
    font-variant-numeric: tabular-nums;
    margin-left: 0.25rem;
  }
}
</style>
