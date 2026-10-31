import { computed, onScopeDispose, ref, shallowRef, watch } from 'vue'
import axios from 'axios'
import {
  findItemIds,
  findKeyItemIds,
  loadItemData,
  MIN_NAME_QUERY,
  searchTerms,
  type IdMatches,
  type ItemData
} from '@/helpers/items'
import { bagsInResults, buildCharacterResults, filterByBags } from '@/helpers/inventoryRows'
import { useInventoryStore } from '@/stores/inventory'
import type { CharacterResult, InventorySearchResponse } from '@/types/Inventory'

export type SearchStatus = 'idle' | 'loading' | 'done' | 'error'

const DEBOUNCE_MS = 300
const EMPTY_RESPONSE: InventorySearchResponse = { totalMatches: 0, results: [] }

interface Deps {
  store: Pick<ReturnType<typeof useInventoryStore>, 'searchInventory' | 'searchKeyItems'>
  loadData: () => Promise<ItemData>
}

/** A character with no stored inventory answers 404; for a search that just means no matches. */
async function emptyOn404(request: Promise<InventorySearchResponse>) {
  try {
    return await request
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) return EMPTY_RESPONSE
    throw error
  }
}

export function useInventorySearch(deps?: Partial<Deps>) {
  const store = deps?.store ?? useInventoryStore()
  const loadData = deps?.loadData ?? loadItemData

  const query = ref('')
  const includeKeyItems = ref(false)
  const playerId = ref<number | null>(null)
  const bagFilter = ref<string[]>([])

  const status = ref<SearchStatus>('idle')
  const itemData = shallowRef<ItemData | null>(null)
  const characters = ref<CharacterResult[]>([])
  const truncated = ref<{ shown: number; total: number } | null>(null)
  const errorMessage = ref('')

  let timer: ReturnType<typeof setTimeout> | null = null
  let requestId = 0

  const results = computed(() => filterByBags(characters.value, bagFilter.value))
  const availableBags = computed(() => bagsInResults(characters.value))
  const needsMoreChars = computed(() => {
    return searchTerms(query.value).some((t) => t.length < MIN_NAME_QUERY && !/^\d+$/.test(t))
  })

  async function run() {
    const thisRequest = ++requestId
    const q = query.value.trim()

    if (!q || needsMoreChars.value) {
      status.value = 'idle'
      characters.value = []
      truncated.value = null
      return
    }

    status.value = 'loading'
    errorMessage.value = ''
    try {
      const data = await loadData()
      itemData.value = data

      const itemMatches: IdMatches = findItemIds(data, q)
      const keyMatches: IdMatches = includeKeyItems.value
        ? findKeyItemIds(data, q)
        : { ids: [], total: 0, truncated: false }
      const player = playerId.value ?? undefined

      const [items, keyItems] = await Promise.all([
        itemMatches.ids.length
          ? emptyOn404(store.searchInventory({ itemIds: itemMatches.ids, playerId: player }))
          : Promise.resolve(null),
        keyMatches.ids.length
          ? emptyOn404(store.searchKeyItems({ itemIds: keyMatches.ids, playerId: player }))
          : Promise.resolve(null)
      ])
      if (thisRequest !== requestId) return // a newer search superseded this one

      characters.value = buildCharacterResults(
        items ? { response: items, ids: itemMatches.ids } : null,
        keyItems ? { response: keyItems, ids: keyMatches.ids } : null,
        data
      )
      bagFilter.value = bagFilter.value.filter((b) => availableBags.value.includes(b))

      const total = itemMatches.total + keyMatches.total
      const shown = itemMatches.ids.length + keyMatches.ids.length
      truncated.value = itemMatches.truncated || keyMatches.truncated ? { shown, total } : null
      status.value = 'done'
    } catch (error) {
      if (thisRequest !== requestId) return
      characters.value = []
      truncated.value = null
      errorMessage.value = error instanceof Error ? error.message : 'Search failed'
      status.value = 'error'
    }
  }

  function schedule() {
    if (timer) clearTimeout(timer)
    timer = setTimeout(run, DEBOUNCE_MS)
  }

  function toggleBag(bag: string) {
    bagFilter.value = bagFilter.value.includes(bag)
      ? bagFilter.value.filter((b) => b !== bag)
      : [...bagFilter.value, bag]
  }

  function clear() {
    query.value = ''
  }

  watch([query, includeKeyItems, playerId], schedule)
  onScopeDispose(() => {
    if (timer) clearTimeout(timer)
  })

  return {
    query,
    includeKeyItems,
    playerId,
    bagFilter,
    status,
    itemData,
    results,
    availableBags,
    truncated,
    errorMessage,
    needsMoreChars,
    toggleBag,
    clear,
    run
  }
}
