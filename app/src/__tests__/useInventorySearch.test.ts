import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { effectScope, nextTick } from 'vue'
import { AxiosError } from 'axios'
import { useInventorySearch } from '@/composables/useInventorySearch'
import type { ItemData } from '@/helpers/items'
import type { InventorySearchResponse } from '@/types/Inventory'

const data: ItemData = {
  items: { '4096': ['Fire Crystal', 'Usable'], '4097': ['Ice Crystal', 'Usable'] },
  keyItems: { '4096': 'Fire Key' }
}

const response = (playerId: number, bag: string, count: number, itemId?: number): InventorySearchResponse => ({
  totalMatches: 1,
  results: [
    { playerId, playerName: `P${playerId}`, totalCount: count, updatedAt: null, locations: [{ bag, count, itemId }] }
  ]
})

function setup(overrides: Record<string, unknown> = {}) {
  const store = {
    searchInventory: vi.fn(async () => response(1, 'inventory', 12)),
    searchKeyItems: vi.fn(async () => response(1, 'keyItems', 1)),
    ...overrides
  }
  const scope = effectScope()
  const search = scope.run(() => useInventorySearch({ store: store as never, loadData: async () => data }))!
  return { store, search, scope }
}

async function flush() {
  await vi.advanceTimersByTimeAsync(350)
  await nextTick()
}

describe('useInventorySearch', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('stays idle until there is something to search', async () => {
    const { search, store } = setup()
    search.query.value = 'f'
    await flush()
    expect(search.status.value).toBe('idle')
    expect(search.needsMoreChars.value).toBe(true)
    expect(store.searchInventory).not.toHaveBeenCalled()
  })

  it('debounces typing into a single request with the resolved ids', async () => {
    const { search, store } = setup()
    for (const text of ['fi', 'fir', 'fire']) {
      search.query.value = text
      await vi.advanceTimersByTimeAsync(100)
    }
    await flush()
    expect(store.searchInventory).toHaveBeenCalledTimes(1)
    expect(store.searchInventory).toHaveBeenCalledWith({ itemIds: [4096], playerId: undefined })
    expect(search.status.value).toBe('done')
    expect(search.results.value[0]!.rows[0]!.name).toBe('Fire Crystal')
  })

  it('searches key items separately only when asked', async () => {
    const { search, store } = setup()
    search.query.value = 'fire'
    await flush()
    expect(store.searchKeyItems).not.toHaveBeenCalled()

    search.includeKeyItems.value = true
    await flush()
    expect(store.searchKeyItems).toHaveBeenCalledWith({ itemIds: [4096], playerId: undefined })
    expect(search.results.value[0]!.rows.map((r) => r.kind).sort()).toEqual(['item', 'key'])
  })

  it('passes the chosen character through', async () => {
    const { search, store } = setup()
    search.playerId.value = 7
    search.query.value = 'fire'
    await flush()
    expect(store.searchInventory).toHaveBeenCalledWith({ itemIds: [4096], playerId: 7 })
  })

  it('treats a 404 for a character with no inventory as no matches', async () => {
    const notFound = new AxiosError('nope', '404', undefined, undefined, { status: 404 } as never)
    const { search } = setup({ searchInventory: vi.fn(async () => Promise.reject(notFound)) })
    search.query.value = 'fire'
    await flush()
    expect(search.status.value).toBe('done')
    expect(search.results.value).toEqual([])
  })

  it('reports other failures and recovers on retry', async () => {
    const searchInventory = vi.fn().mockRejectedValueOnce(new Error('boom')).mockResolvedValue(response(1, 'safe', 2))
    const { search } = setup({ searchInventory })
    search.query.value = 'fire'
    await flush()
    expect(search.status.value).toBe('error')
    expect(search.errorMessage.value).toBe('boom')

    await search.run()
    expect(search.status.value).toBe('done')
  })

  it('ignores a slow response that was superseded by a newer search', async () => {
    let releaseFirst: (r: InventorySearchResponse) => void = () => {}
    const searchInventory = vi
      .fn()
      .mockImplementationOnce(() => new Promise<InventorySearchResponse>((r) => (releaseFirst = r)))
      .mockResolvedValueOnce(response(2, 'safe', 5, undefined))
    const { search } = setup({ searchInventory })

    search.query.value = 'fire'
    await flush()
    search.query.value = 'ice'
    await flush()
    releaseFirst(response(1, 'inventory', 99))
    await nextTick()

    expect(search.results.value.map((r) => r.playerId)).toEqual([2])
  })

  it('flags truncation when a name matches more ids than one request carries', async () => {
    const many: ItemData = { items: {}, keyItems: {} }
    for (let i = 1; i <= 80; i++) many.items[String(i)] = [`Potion ${i}`, 'Usable']
    const store = { searchInventory: vi.fn(async () => response(1, 'safe', 1, 1)), searchKeyItems: vi.fn() }
    const search = effectScope().run(() => useInventorySearch({ store: store as never, loadData: async () => many }))!
    search.query.value = 'potion'
    await flush()
    expect(search.truncated.value).toEqual({ shown: 50, total: 80 })
  })

  it('toggles bag filters and drops filters that no longer apply', async () => {
    const { search } = setup()
    search.query.value = 'fire'
    await flush()
    search.toggleBag('inventory')
    expect(search.bagFilter.value).toEqual(['inventory'])
    search.toggleBag('inventory')
    expect(search.bagFilter.value).toEqual([])

    search.toggleBag('wardrobe')
    await search.run()
    expect(search.bagFilter.value).toEqual([])
  })
})
