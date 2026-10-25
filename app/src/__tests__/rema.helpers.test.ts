import { describe, it, expect } from 'vitest'
import { buildRemaOverview, categoryItemIds, stageBadge, stageLabel, type RemaData } from '@/helpers/rema'
import type { InventorySearchResponse } from '@/types/Inventory'
import realData from '@/assets/rema-data.json'

const data: RemaData = {
  categories: [
    {
      key: 'relic',
      label: 'Relic',
      weapons: [
        {
          name: 'Spharai',
          type: 'Hand-to-Hand',
          stages: [
            { id: 100, level: 75, itemLevel: 0 },
            { id: 101, level: 99, itemLevel: 0 },
            { id: 102, level: 99, itemLevel: 0 },
            { id: 103, level: 99, itemLevel: 119 }
          ]
        },
        { name: 'Mandau', type: 'Dagger', stages: [{ id: 200, level: 75, itemLevel: 0 }] }
      ]
    },
    {
      key: 'prime',
      label: 'Prime',
      weapons: [{ name: 'Prime Sword', type: 'Sword', stages: [{ id: 300, level: 99, itemLevel: 119 }] }]
    }
  ]
}

const characters = [
  { playerId: 1, playerName: 'Alpha', gil: 0, updatedAt: '2026-10-25T00:00:00Z' },
  { playerId: 2, playerName: 'Bravo', gil: 0, updatedAt: null },
  { playerId: 3, playerName: 'Charlie', gil: 0, updatedAt: null }
]

const responses: Record<string, InventorySearchResponse> = {
  relic: {
    totalMatches: 2,
    results: [
      {
        playerId: 1,
        playerName: 'Alpha',
        totalCount: 3,
        updatedAt: null,
        locations: [
          { bag: 'safe', itemId: 100, count: 1 },
          { bag: 'wardrobe', itemId: 102, count: 1 },
          { bag: 'inventory', itemId: 101, count: 1 },
          { bag: 'satchel', itemId: 200, count: 1 }
        ]
      },
      {
        playerId: 2,
        playerName: 'Bravo',
        totalCount: 1,
        updatedAt: null,
        locations: [{ bag: 'locker', itemId: 100, count: 1 }]
      }
    ]
  },
  prime: {
    totalMatches: 1,
    results: [
      {
        playerId: 1,
        playerName: 'Alpha',
        totalCount: 1,
        updatedAt: null,
        locations: [{ bag: 'inventory', count: 1 }] // single id searched: backend omits itemId
      }
    ]
  }
}

describe('stage labels', () => {
  const spharai = data.categories[0]!.weapons[0]!
  it('uses item level when present, otherwise level', () => {
    expect(stageBadge(spharai.stages[0]!)).toBe('75')
    expect(stageBadge(spharai.stages[3]!)).toBe('119')
  })

  it('numbers repeats at the same level in stage order', () => {
    expect(stageLabel(spharai, 0)).toBe('Lv 75')
    expect(stageLabel(spharai, 1)).toBe('Lv 99')
    expect(stageLabel(spharai, 2)).toBe('Lv 99 II')
    expect(stageLabel(spharai, 3)).toBe('iL 119')
    expect(stageLabel(spharai, 9)).toBe('')
  })

  it('collects every stage id of a category', () => {
    expect(categoryItemIds(data.categories[0]!)).toEqual([100, 101, 102, 103, 200])
  })
})

describe('buildRemaOverview', () => {
  const overview = buildRemaOverview(data, characters, responses)
  const alpha = overview.find((p) => p.playerId === 1)!

  it('includes every character, including those with none, ranked by weapons owned', () => {
    expect(overview.map((p) => p.playerName)).toEqual(['Alpha', 'Bravo', 'Charlie'])
    expect(overview.find((p) => p.playerId === 3)!.ownedTotal).toBe(0)
  })

  it('reports the highest stage held and how many stages are held', () => {
    const spharai = alpha.categories[0]!.owned.find((o) => o.weapon.name === 'Spharai')!
    expect(spharai.stageIndex).toBe(2)
    expect(spharai.stage.id).toBe(102)
    expect(spharai.stagesHeld).toBe(3)
    expect(spharai.locations.map((l) => l.label)).toEqual(['Mog Wardrobe'])
  })

  it('splits owned from missing per category and totals them', () => {
    const relic = alpha.categories[0]!
    expect(relic.owned.map((o) => o.weapon.name)).toEqual(['Spharai', 'Mandau'])
    expect(relic.missing).toEqual([])
    expect(alpha.ownedTotal).toBe(3)
    expect(alpha.weaponTotal).toBe(3)
    const bravo = overview.find((p) => p.playerId === 2)!
    expect(bravo.categories[0]!.missing.map((w) => w.name)).toEqual(['Mandau'])
  })

  it('resolves a single-id category when the backend omits itemId', () => {
    expect(alpha.categories[1]!.owned[0]!.weapon.name).toBe('Prime Sword')
  })

  it('ignores ids that do not belong to the category being read', () => {
    const stray = buildRemaOverview(data, [characters[0]!], {
      relic: { totalMatches: 1, results: [{ playerId: 1, playerName: 'Alpha', totalCount: 1, updatedAt: null, locations: [{ bag: 'safe', itemId: 300, count: 1 }] }] }
    })
    expect(stray[0]!.ownedTotal).toBe(0)
  })
})

describe('shipped rema-data.json', () => {
  const real = realData as unknown as RemaData

  it('has the five categories with plausible sizes', () => {
    expect(real.categories.map((c) => c.key)).toEqual(['relic', 'empyrean', 'mythic', 'aeonic', 'prime'])
    expect(real.categories.map((c) => c.weapons.length)).toEqual([16, 16, 22, 16, 13])
  })

  it('never reuses an item id across weapons', () => {
    const ids = real.categories.flatMap(categoryItemIds)
    expect(new Set(ids).size).toBe(ids.length)
  })

  it('keeps every category query short enough for one request', () => {
    for (const category of real.categories) {
      expect(categoryItemIds(category).join(',').length).toBeLessThan(2000)
    }
  })

  it('orders stages ascending', () => {
    for (const category of real.categories) {
      for (const weapon of category.weapons) {
        const sorted = [...weapon.stages].sort((a, b) => a.level - b.level || a.itemLevel - b.itemLevel || a.id - b.id)
        expect(weapon.stages).toEqual(sorted)
      }
    }
  })
})
