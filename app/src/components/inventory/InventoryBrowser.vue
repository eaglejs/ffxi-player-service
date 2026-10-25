<template>
  <div class="backdrop" @click.self="emit('close')">
    <aside
      class="drawer"
      role="dialog"
      aria-modal="true"
      :aria-label="`${playerName} inventory`"
      @keydown.esc="emit('close')"
    >
      <header class="drawer-head">
        <h3>{{ playerName }}</h3>
        <button ref="closeButton" type="button" class="close-btn" aria-label="Close" @click="emit('close')">
          <McIcon :path="mdiClose" :size="22" />
        </button>
      </header>

      <p v-if="state === 'loading'" class="note">Loading inventory…</p>
      <p v-else-if="state === 'error'" class="note">Could not load this inventory.</p>
      <p v-else-if="state === 'empty'" class="note">
        No inventory has been synced for this character yet.
      </p>

      <div v-else-if="inventory && names" class="drawer-body">
        <div class="gil">
          <ItemIcon :id="GIL_ITEM_ID" :tile="40" />
          <span class="gil-amount">{{ inventory.gil.toLocaleString() }}</span>
          <span class="gil-label">gil</span>
        </div>

        <details v-for="bag in bagNames" :key="bag" class="bag" :open="bag === 'inventory'">
          <summary>
            {{ bagLabel(bag) }}
            <span class="bag-count">{{ itemsIn(bag).length }}</span>
          </summary>
          <ul class="grid">
            <li v-for="item in itemsIn(bag)" :key="item.id" class="cell" :title="`${itemName(names, item.id)} × ${item.count}`">
              <ItemIcon :id="item.id" :tile="40" />
              <span v-if="item.count > 1" class="badge-count">{{ item.count }}</span>
            </li>
          </ul>
        </details>

        <details v-if="inventory.keyItems.length" class="bag">
          <summary>
            Key Items
            <span class="bag-count">{{ inventory.keyItems.length }}</span>
          </summary>
          <ul class="key-list">
            <li v-for="id in inventory.keyItems" :key="id">{{ keyItemName(names, id) }}</li>
          </ul>
        </details>

        <p class="note small">Synced {{ timeAgo(inventory.updatedAt) }}</p>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { mdiClose } from '@mdi/js'
import { bagLabel, compareBags } from '@/helpers/bags'
import { itemName, keyItemName, loadItemData, type ItemData } from '@/helpers/items'
import { timeAgo } from '@/helpers/time'
import { useInventoryStore } from '@/stores/inventory'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'
import type { InventoryItem, PlayerInventory } from '@/types/Inventory'

const GIL_ITEM_ID = 0xffff

const props = defineProps<{ playerId: number; playerName: string }>()
const emit = defineEmits<{ close: [] }>()

const store = useInventoryStore()
const inventory = ref<PlayerInventory | null>(null)
const names = ref<ItemData | null>(null)
const state = ref<'loading' | 'ready' | 'empty' | 'error'>('loading')
const closeButton = ref<HTMLButtonElement | null>(null)

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
  background: rgb(0 0 0 / 55%);
  inset: 0;
  position: fixed;
  z-index: 1050;
}

.drawer {
  animation: slideIn 0.2s ease-out;
  background: linear-gradient(180deg, #0c2d66 0%, #031633 100%);
  border-left: 1px solid #c9b27c;
  bottom: 0;
  color: #fff;
  max-width: 100%;
  overflow-y: auto;
  padding: 1rem 1.25rem 2rem;
  position: absolute;
  right: 0;
  top: 0;
  width: 460px;
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
  display: flex;
  justify-content: space-between;

  h3 {
    color: #f3e3b3;
    margin: 0;
  }
}

.close-btn {
  background: none;
  border: 0;
  color: #fff;
  padding: 0.25rem;
}

.note {
  color: rgb(255 255 255 / 70%);
  margin-top: 1rem;

  &.small {
    font-size: 0.8rem;
  }
}

.gil {
  align-items: center;
  display: flex;
  gap: 0.6rem;
  margin: 1rem 0;
}

.gil-amount {
  color: #f3e3b3;
  font-size: 1.4rem;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
}

.gil-label {
  color: rgb(255 255 255 / 55%);
}

.bag {
  border-top: 1px solid rgb(255 255 255 / 10%);
  padding: 0.5rem 0;

  summary {
    cursor: pointer;
    font-weight: 500;
  }
}

.bag-count {
  background: rgb(255 255 255 / 12%);
  border-radius: 999px;
  font-size: 0.75rem;
  margin-left: 0.4rem;
  padding: 0 0.5rem;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  list-style: none;
  margin: 0.6rem 0 0.25rem;
  padding: 0;
}

.cell {
  position: relative;
}

.badge-count {
  background: rgb(0 0 0 / 75%);
  border-radius: 4px;
  bottom: 1px;
  font-size: 0.7rem;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
  padding: 0 4px;
  position: absolute;
  right: 1px;
}

.key-list {
  columns: 1;
  font-size: 0.85rem;
  list-style: none;
  margin: 0.5rem 0 0;
  max-height: 260px;
  overflow-y: auto;
  padding: 0;
}
</style>
