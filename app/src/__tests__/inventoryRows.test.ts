import { describe, it, expect } from 'vitest'
import { bagsInResults, buildCharacterResults, filterByBags } from '@/helpers/inventoryRows'
import type { ItemData } from '@/helpers/items'
import type { InventorySearchResponse } from '@/types/Inventory'

const data: ItemData = {
  items: { '4096': ['Fire Crystal', 'Usable'], '4097': ['Ice Crystal', 'Usable'] },
  keyItems: { '10': 'Map of the Jeuno Area' }
}

const multi: InventorySearchResponse = {
  totalMatches: 2,
  results: [
    {
      playerId: 1,
      playerName: 'Alpha',
      totalCount: 15,
      updatedAt: '2026-10-25T00:00:00Z',
      locations: [
        { bag: 'satchel', itemId: 4096, count: 3 },
        { bag: 'inventory', itemId: 4096, count: 12 },
        { bag: 'safe', itemId: 4097, count: 1 }
      ]
    },
    {
      playerId: 2,
      playerName: 'Bravo',
      totalCount: 2,
      updatedAt: null,
      locations: [{ bag: 'wardrobe', itemId: 4097, count: 2 }]
    }
  ]
}

describe('buildCharacterResults', () => {
  it('groups locations into one row per item, sorted by total', () => {
    const [alpha, bravo] = buildCharacterResults({ response: multi, ids: [4096, 4097] }, null, data)
    expect(alpha!.playerName).toBe('Alpha')
    expect(alpha!.total).toBe(16)
    expect(alpha!.rows.map((r) => [r.name, r.total])).toEqual([
      ['Fire Crystal', 15],
      ['Ice Crystal', 1]
    ])
    expect(alpha!.rows[0]!.locations.map((l) => l.label)).toEqual(['Inventory', 'Mog Satchel'])
    expect(bravo!.playerName).toBe('Bravo')
  })

  it('uses the single searched id when the backend omits itemId', () => {
    const single: InventorySearchResponse = {
      totalMatches: 1,
      results: [
        {
          playerId: 1,
          playerName: 'Alpha',
          totalCount: 12,
          updatedAt: null,
          locations: [{ bag: 'inventory', count: 12 }]
        }
      ]
    }
    const [alpha] = buildCharacterResults({ response: single, ids: [4096] }, null, data)
    expect(alpha!.rows[0]).toMatchObject({ id: 4096, name: 'Fire Crystal', category: 'Usable', total: 12 })
  })

  it('merges key item results into the same character card', () => {
    const keys: InventorySearchResponse = {
      totalMatches: 1,
      results: [
        {
          playerId: 1,
          playerName: 'Alpha',
          totalCount: 1,
          updatedAt: null,
          locations: [{ bag: 'keyItems', count: 1 }]
        }
      ]
    }
    const results = buildCharacterResults({ response: multi, ids: [4096, 4097] }, { response: keys, ids: [10] }, data)
    const alpha = results.find((r) => r.playerId === 1)!
    expect(alpha.rows.map((r) => r.kind)).toContain('key')
    expect(alpha.rows.find((r) => r.kind === 'key')).toMatchObject({ id: 10, name: 'Map of the Jeuno Area' })
  })

  it('falls back to a placeholder name for ids missing from the table', () => {
    const response: InventorySearchResponse = {
      totalMatches: 1,
      results: [
        { playerId: 1, playerName: 'A', totalCount: 1, updatedAt: null, locations: [{ bag: 'safe', count: 1 }] }
      ]
    }
    const [a] = buildCharacterResults({ response, ids: [77777] }, null, data)
    expect(a!.rows[0]!.name).toBe('Item #77777')
  })
})

describe('filterByBags', () => {
  const built = buildCharacterResults({ response: multi, ids: [4096, 4097] }, null, data)

  it('returns everything when no bag is chosen', () => {
    expect(filterByBags(built, [])).toBe(built)
  })

  it('narrows locations, recomputes totals and drops empty characters', () => {
    const only = filterByBags(built, ['inventory'])
    expect(only).toHaveLength(1)
    expect(only[0]!.total).toBe(12)
    expect(only[0]!.rows).toHaveLength(1)
  })

  it('lists the bags present in display order', () => {
    expect(bagsInResults(built)).toEqual(['inventory', 'wardrobe', 'safe', 'satchel'])
  })
})
