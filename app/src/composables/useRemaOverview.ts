import { ref, shallowRef } from 'vue'
import { buildRemaOverview, categoryItemIds, type PlayerRema, type RemaData } from '@/helpers/rema'
import { useInventoryStore } from '@/stores/inventory'
import type { InventorySearchResponse } from '@/types/Inventory'

export type RemaStatus = 'loading' | 'done' | 'error'

interface Deps {
  store: Pick<ReturnType<typeof useInventoryStore>, 'listCharacters' | 'searchInventory'>
  loadData: () => Promise<RemaData>
}

const loadRemaData = () => import('@/assets/rema-data.json').then((m) => (m.default ?? m) as unknown as RemaData)

export function useRemaOverview(deps?: Partial<Deps>) {
  const store = deps?.store ?? useInventoryStore()
  const loadData = deps?.loadData ?? loadRemaData

  const status = ref<RemaStatus>('loading')
  const data = shallowRef<RemaData | null>(null)
  const players = ref<PlayerRema[]>([])
  const errorMessage = ref('')
  let requestId = 0

  async function refresh() {
    const thisRequest = ++requestId
    status.value = 'loading'
    errorMessage.value = ''
    try {
      const remaData = await loadData()
      data.value = remaData

      // One search per category keeps each query string short (the largest is ~230 ids).
      const [characters, ...responses] = await Promise.all([
        store.listCharacters(),
        ...remaData.categories.map((category) =>
          store.searchInventory({ itemIds: categoryItemIds(category) })
        )
      ])
      if (thisRequest !== requestId) return

      const byCategory: Record<string, InventorySearchResponse> = {}
      remaData.categories.forEach((category, i) => (byCategory[category.key] = responses[i]!))
      players.value = buildRemaOverview(remaData, characters, byCategory)
      status.value = 'done'
    } catch (error) {
      if (thisRequest !== requestId) return
      players.value = []
      errorMessage.value = error instanceof Error ? error.message : 'Could not load REMA weapons'
      status.value = 'error'
    }
  }

  return { status, data, players, errorMessage, refresh }
}
