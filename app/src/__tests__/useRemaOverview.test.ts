import { describe, it, expect, vi } from 'vitest'
import { useRemaOverview } from '@/composables/useRemaOverview'
import type { RemaData } from '@/helpers/rema'

const data: RemaData = {
  categories: [
    { key: 'relic', label: 'Relic', weapons: [{ name: 'Spharai', type: 'Hand-to-Hand', stages: [{ id: 100, level: 75, itemLevel: 0 }, { id: 101, level: 80, itemLevel: 0 }] }] },
    { key: 'prime', label: 'Prime', weapons: [{ name: 'Prime Sword', type: 'Sword', stages: [{ id: 300, level: 99, itemLevel: 119 }] }] }
  ]
}

function setup(overrides: Record<string, unknown> = {}) {
  const store = {
    listCharacters: vi.fn(async () => [{ playerId: 1, playerName: 'Alpha', gil: 0, updatedAt: null }]),
    searchInventory: vi.fn(async ({ itemIds }: { itemIds: number[] }) =>
      itemIds.includes(100)
        ? { totalMatches: 1, results: [{ playerId: 1, playerName: 'Alpha', totalCount: 1, updatedAt: null, locations: [{ bag: 'safe', itemId: 100, count: 1 }] }] }
        : { totalMatches: 0, results: [] }
    ),
    ...overrides
  }
  return { store, rema: useRemaOverview({ store: store as never, loadData: async () => data }) }
}

describe('useRemaOverview', () => {
  it('runs one search per category plus the character list', async () => {
    const { store, rema } = setup()
    await rema.refresh()
    expect(store.listCharacters).toHaveBeenCalledTimes(1)
    expect(store.searchInventory).toHaveBeenCalledTimes(2)
    expect(store.searchInventory).toHaveBeenCalledWith({ itemIds: [100, 101] })
    expect(store.searchInventory).toHaveBeenCalledWith({ itemIds: [300] })
    expect(rema.status.value).toBe('done')
    expect(rema.players.value[0]!.ownedTotal).toBe(1)
  })

  it('reports failures and recovers on refresh', async () => {
    const listCharacters = vi
      .fn()
      .mockRejectedValueOnce(new Error('boom'))
      .mockResolvedValue([{ playerId: 1, playerName: 'Alpha', gil: 0, updatedAt: null }])
    const { rema } = setup({ listCharacters })
    await rema.refresh()
    expect(rema.status.value).toBe('error')
    expect(rema.errorMessage.value).toBe('boom')
    await rema.refresh()
    expect(rema.status.value).toBe('done')
  })

  it('ignores a slow refresh that was superseded', async () => {
    let release: (v: never[]) => void = () => {}
    const listCharacters = vi
      .fn()
      .mockImplementationOnce(() => new Promise<never[]>((r) => (release = r)))
      .mockResolvedValueOnce([{ playerId: 2, playerName: 'Bravo', gil: 0, updatedAt: null }])
    const { rema } = setup({ listCharacters })
    const first = rema.refresh()
    await rema.refresh()
    release([])
    await first
    expect(rema.players.value.map((p) => p.playerName)).toEqual(['Bravo'])
  })
})
