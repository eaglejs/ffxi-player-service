<template>
  <GameWindow :label="`${character.playerName} results`">
    <template #header>
      <button type="button" class="char-name" @click="emit('browse', character)">
        <McIcon :path="mdiBagPersonalOutline" :size="20" class="char-icon" />
        <span>{{ character.playerName }}</span>
        <span class="browse-hint">browse bags</span>
      </button>
      <span class="meta">
        <strong>{{ character.total.toLocaleString() }}</strong> total
        <template v-if="updated"> · synced {{ updated }}</template>
      </span>
    </template>

    <ul class="rows">
      <li
        v-for="row in character.rows"
        :key="`${row.kind}-${row.id}`"
        class="row-item"
        @mouseenter="onHoverRow(row, $event)"
        @mousemove="onMouseMove"
        @mouseleave="onMouseLeave"
      >
        <ItemIcon :id="row.id" :kind="row.kind" />
        <div class="row-body">
          <div class="row-title">
            <a
              v-if="row.kind === 'item'"
              :href="ffxiahUrl(row.id)"
              target="_blank"
              rel="noopener noreferrer"
              class="item-link"
              :title="hoveredRowKey === `${row.kind}-${row.id}` ? undefined : `Open ${row.name} on FFXIAH`"
            >
              {{ row.name }}
              <McIcon :path="mdiOpenInNew" :size="13" class="ext-icon" />
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

    <template #footer>
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 small">
        <span>
          <b class="me-1">Matched Items:</b> {{ character.rows.length }}
        </span>
        <span v-if="updated" class="text-secondary">Synced {{ updated }}</span>
      </div>
    </template>

    <ItemHoverTooltip :tooltip="activeTooltip" :kind="hoveredRow?.kind" :x="mouseX" :y="mouseY" />
  </GameWindow>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef } from 'vue'
import { mdiBagPersonalOutline, mdiOpenInNew } from '@mdi/js'
import {
  buildItemTooltipView,
  ffxiahUrl,
  loadItemDetails,
  type ItemDetailsMap
} from '@/helpers/items'
import { timeAgo } from '@/helpers/time'
import GameWindow from '@/components/inventory/GameWindow.vue'
import ItemHoverTooltip from '@/components/inventory/ItemHoverTooltip.vue'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'
import type { CharacterResult, ResultRow } from '@/types/Inventory'

const props = defineProps<{ character: CharacterResult }>()
const emit = defineEmits<{ browse: [character: CharacterResult] }>()

const updated = computed(() => timeAgo(props.character.updatedAt))

const itemDetails = shallowRef<ItemDetailsMap | null>(null)
const hoveredRow = ref<ResultRow | null>(null)
const hoveredRowKey = computed(() =>
  hoveredRow.value ? `${hoveredRow.value.kind}-${hoveredRow.value.id}` : null
)
const mouseX = ref(0)
const mouseY = ref(0)

const activeTooltip = computed(() => {
  const row = hoveredRow.value
  if (!row) return null
  const detail = row.kind === 'item' ? itemDetails.value?.[row.id] : undefined
  return buildItemTooltipView(row.id, row.name, detail, {
    category: row.category,
    locations: row.locations.map((l) => (l.count > 1 ? `${l.label} (×${l.count})` : l.label))
  })
})

function onHoverRow(row: ResultRow, e: MouseEvent) {
  hoveredRow.value = row
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  if (row.kind === 'item' && !itemDetails.value) {
    loadItemDetails()
      .then((d) => {
        itemDetails.value = d
      })
      .catch(() => {
        // Tooltip still renders basic item info if details fail to load
      })
  }
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function onMouseLeave() {
  hoveredRow.value = null
}
</script>

<style scoped lang="scss">
.char-name {
  align-items: center;
  background: none;
  border: 0;
  color: var(--bs-body-color);
  display: inline-flex;
  font-size: 1.2rem;
  font-weight: 600;
  gap: 0.45rem;
  padding: 0;
  transition: color 0.15s ease;

  &:hover,
  &:focus-visible {
    color: var(--bs-primary);

    .browse-hint {
      background-color: var(--bs-primary);
      border-color: var(--bs-primary);
      color: #fff;
    }
  }
}

.char-icon {
  color: var(--bs-secondary-color);
}

.browse-hint {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.72rem;
  font-weight: 500;
  margin-left: 0.25rem;
  padding: 0.1rem 0.55rem;
  transition:
    background-color 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.meta {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.8rem;
  padding: 0.15rem 0.65rem;

  strong {
    color: var(--bs-body-color);
  }
}

.rows {
  list-style: none;
  margin: 0;
  padding: 0;
}

.row-item {
  align-items: flex-start;
  display: flex;
  gap: 0.8rem;
  padding: 0.7rem 0.25rem;

  & + & {
    border-top: 1px solid var(--bs-border-color);
  }
}

.row-body {
  flex: 1;
  min-width: 0;
}

.row-title {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.6rem;
}

.item-link {
  align-items: center;
  color: var(--bs-body-color);
  display: inline-flex;
  font-weight: 600;
  gap: 0.25rem;
  text-decoration: none;
  transition: color 0.15s ease;

  &:hover {
    color: var(--bs-primary);
    text-decoration: underline;
  }
}

.ext-icon {
  opacity: 0.65;
}

.category {
  background-color: var(--bs-secondary-bg);
  border-radius: 4px;
  color: var(--bs-secondary-color);
  font-size: 0.72rem;
  font-weight: 500;
  padding: 0.05rem 0.45rem;
}

.total {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-body-color);
  font-size: 0.8rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  margin-left: auto;
  padding: 0.05rem 0.55rem;
}

.pills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  list-style: none;
  margin: 0.45rem 0 0;
  padding: 0;
}

.pill {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.78rem;
  padding: 0.1rem 0.65rem;

  b {
    color: var(--bs-body-color);
    font-variant-numeric: tabular-nums;
    font-weight: 600;
    margin-left: 0.25rem;
  }
}
</style>
