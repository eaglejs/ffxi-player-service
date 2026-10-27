<template>
  <div class="backdrop" @click.self="emit('close')">
    <aside
      class="drawer card"
      role="dialog"
      aria-modal="true"
      :aria-label="`${playerName} inventory`"
      @keydown.esc="emit('close')"
    >
      <header class="card-header drawer-head">
        <h3 class="mb-0">{{ playerName }}</h3>
        <button
          ref="closeButton"
          type="button"
          class="btn btn-sm btn-outline-secondary close-btn"
          aria-label="Close"
          @click="emit('close')"
        >
          <McIcon :path="mdiClose" :size="20" />
        </button>
      </header>

      <div class="card-body drawer-content">
        <p v-if="state === 'loading'" class="note">Loading inventory…</p>
        <p v-else-if="state === 'error'" class="note">Could not load this inventory.</p>
        <p v-else-if="state === 'empty'" class="note">
          No inventory has been synced for this character yet.
        </p>

        <div v-else-if="inventory && names" class="drawer-body">
          <div class="gil">
            <ItemIcon :id="GIL_ITEM_ID" :tile="40" />
            <div class="d-flex align-items-baseline gap-2">
              <span class="gil-amount">{{ inventory.gil.toLocaleString() }}</span>
              <span class="gil-label">gil</span>
            </div>
          </div>

          <details v-for="bag in bagNames" :key="bag" class="bag" :open="bag === 'inventory'">
            <summary>
              <span>{{ bagLabel(bag) }}</span>
              <span class="bag-count">{{ itemsIn(bag).length }}</span>
            </summary>
            <ul class="grid">
              <li
                v-for="item in itemsIn(bag)"
                :key="item.id"
                class="cell"
                :title="hoveredCellKey === `${bag}-${item.id}` ? undefined : `${itemName(names, item.id)} × ${item.count}`"
                @mouseenter="onHoverItem(bag, item, $event)"
                @mousemove="onMouseMove"
                @mouseleave="onMouseLeave"
              >
                <ItemIcon :id="item.id" :tile="42" />
                <span v-if="item.count > 1" class="badge-count">{{ item.count }}</span>
              </li>
            </ul>
          </details>

          <details v-if="inventory.keyItems.length" class="bag">
            <summary>
              <span>Key Items</span>
              <span class="bag-count">{{ inventory.keyItems.length }}</span>
            </summary>
            <ul class="key-list">
              <li v-for="id in inventory.keyItems" :key="id">{{ keyItemName(names, id) }}</li>
            </ul>
          </details>

          <p class="note small">Synced {{ timeAgo(inventory.updatedAt) }}</p>
        </div>
      </div>

      <ItemHoverTooltip :tooltip="activeTooltip" :x="mouseX" :y="mouseY" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, shallowRef } from 'vue'
import { mdiClose } from '@mdi/js'
import { bagLabel, compareBags } from '@/helpers/bags'
import {
  buildItemTooltipView,
  itemCategory,
  itemName,
  keyItemName,
  loadItemData,
  loadItemDetails,
  type ItemData,
  type ItemDetailsMap
} from '@/helpers/items'
import { timeAgo } from '@/helpers/time'
import { useInventoryStore } from '@/stores/inventory'
import ItemHoverTooltip from '@/components/inventory/ItemHoverTooltip.vue'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'
import type { InventoryItem, PlayerInventory } from '@/types/Inventory'

const GIL_ITEM_ID = 0xffff

const props = defineProps<{ playerId: number; playerName: string }>()
const emit = defineEmits<{ close: [] }>()

const store = useInventoryStore()
const inventory = ref<PlayerInventory | null>(null)
const names = ref<ItemData | null>(null)
const details = shallowRef<ItemDetailsMap | null>(null)
const state = ref<'loading' | 'ready' | 'empty' | 'error'>('loading')
const closeButton = ref<HTMLButtonElement | null>(null)

const hoveredItem = ref<{ bag: string; item: InventoryItem } | null>(null)
const hoveredCellKey = computed(() =>
  hoveredItem.value ? `${hoveredItem.value.bag}-${hoveredItem.value.item.id}` : null
)
const mouseX = ref(0)
const mouseY = ref(0)

const activeTooltip = computed(() => {
  if (!hoveredItem.value || !names.value) return null
  const { bag, item } = hoveredItem.value
  const name = itemName(names.value, item.id)
  const category = itemCategory(names.value, item.id)
  const detail = details.value?.[item.id]
  const loc = item.count > 1 ? `${bagLabel(bag)} (×${item.count})` : bagLabel(bag)
  return buildItemTooltipView(item.id, name, detail, {
    category,
    locations: [loc]
  })
})

function onHoverItem(bag: string, item: InventoryItem, e: MouseEvent) {
  hoveredItem.value = { bag, item }
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  if (!details.value) {
    loadItemDetails()
      .then((d) => {
        details.value = d
      })
      .catch(() => {
        // Basic tooltip still works
      })
  }
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function onMouseLeave() {
  hoveredItem.value = null
}

function itemsIn(bag: string): InventoryItem[] {
  return inventory.value?.bags[bag] ?? []
}

const bagNames = computed(() =>
  Object.keys(inventory.value?.bags ?? {})
    .filter((bag) => itemsIn(bag).length > 0)
    .sort(compareBags)
)

onMounted(async () => {
  await nextTick()
  closeButton.value?.focus()
  try {
    const [data, result] = await Promise.all([loadItemData(), store.getInventory(props.playerId)])
    names.value = data
    inventory.value = result
    state.value = result ? 'ready' : 'empty'
  } catch {
    state.value = 'error'
  }
})
</script>

<style scoped lang="scss">
.backdrop {
  background: rgba(0, 0, 0, 0.55);
  inset: 0;
  position: fixed;
  z-index: 1050;
}

.drawer {
  animation: slideIn 0.2s ease-out;
  background-color: var(--bs-body-bg);
  border-bottom: 0;
  border-left: 1px solid var(--bs-border-color);
  border-radius: 0;
  border-right: 0;
  border-top: 0;
  bottom: 0;
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.25);
  color: var(--bs-body-color);
  display: flex;
  flex-direction: column;
  max-width: 100%;
  position: absolute;
  right: 0;
  top: 0;
  width: 480px;
}

@keyframes slideIn {
  from {
    transform: translateX(40px);
    opacity: 0;
  }
  to {
    transform: none;
    opacity: 1;
  }
}

.drawer-head {
  align-items: center;
  border-radius: 0;
  display: flex;
  justify-content: space-between;
  padding: 0.85rem 1.25rem;

  h3 {
    font-size: 1.25rem;
    font-weight: 600;
  }
}

.close-btn {
  align-items: center;
  display: inline-flex;
  justify-content: center;
  padding: 0.25rem 0.4rem;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.25rem 2rem;
}

.note {
  color: var(--bs-secondary-color);
  margin-top: 1rem;

  &.small {
    font-size: 0.8rem;
    text-align: right;
  }
}

.gil {
  align-items: center;
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
  padding: 0.65rem 0.85rem;
}

.gil-amount {
  color: var(--bs-body-color);
  font-size: 1.35rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.gil-label {
  color: var(--bs-secondary-color);
  font-size: 0.9rem;
  font-weight: 500;
}

.bag {
  background-color: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  margin-bottom: 0.6rem;
  padding: 0.55rem 0.85rem;

  summary {
    align-items: center;
    cursor: pointer;
    display: flex;
    font-weight: 600;
    justify-content: space-between;
    list-style: none;
    user-select: none;

    &::-webkit-details-marker {
      display: none;
    }
  }
}

.bag-count {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.75rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  margin-left: 0.4rem;
  padding: 0.05rem 0.55rem;
}

.grid {
  border-top: 1px solid var(--bs-border-color);
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  list-style: none;
  margin: 0.6rem 0 0.2rem;
  padding: 0.65rem 0 0;
}

.cell {
  position: relative;
}

.badge-count {
  background: rgba(0, 0, 0, 0.78);
  border-radius: 4px;
  bottom: 2px;
  color: #fff;
  font-size: 0.68rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  line-height: 1.2;
  padding: 0 4px;
  position: absolute;
  right: 2px;
}

.key-list {
  border-top: 1px solid var(--bs-border-color);
  columns: 1;
  font-size: 0.85rem;
  list-style: none;
  margin: 0.6rem 0 0;
  max-height: 260px;
  overflow-y: auto;
  padding: 0.5rem 0 0;

  li {
    padding: 0.2rem 0;

    & + li {
      border-top: 1px dashed var(--bs-border-color);
    }
  }
}
</style>
