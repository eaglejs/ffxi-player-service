import { describe, it, expect } from 'vitest'
import { bagLabel, compareBags } from '@/helpers/bags'
import {
  buildItemTooltipView,
  findIds,
  findItemIds,
  formatJobs,
  formatSlots,
  itemName,
  loadItemData,
  loadItemDetails,
  MAX_ITEM_IDS
} from '@/helpers/items'
import { timeAgo } from '@/helpers/time'

describe('findIds', () => {
  const names = { '1': 'Cure', '2': 'Cure Potion', '3': 'Super Cure Staff', '4': 'Fire Crystal', '5': 'cure' }

  it('treats a bare number as an id', () => {
    expect(findIds(names, '4096')).toEqual({ ids: [4096], total: 1, truncated: false })
  })

  it('ranks exact, then prefix, then substring, case-insensitively', () => {
    const { ids } = findIds(names, 'CURE')
    expect(ids.slice(0, 2).sort()).toEqual([1, 5])
    expect(ids.slice(2)).toEqual([2, 3])
  })

  it('returns nothing for empty, single-letter or unmatched queries', () => {
    expect(findIds(names, '').ids).toEqual([])
    expect(findIds(names, 'c').ids).toEqual([])
    expect(findIds(names, 'zzz').ids).toEqual([])
  })

  it('caps the ids sent to the backend and reports truncation', () => {
    const many: Record<string, string> = {}
    for (let i = 1; i <= 80; i++) many[String(i)] = `Potion ${i}`
    const result = findIds(many, 'potion')
    expect(result.ids).toHaveLength(MAX_ITEM_IDS)
    expect(result.total).toBe(80)
    expect(result.truncated).toBe(true)
  })
})

describe('real item data', () => {
  it('knows the crystals and falls back for unknown ids', async () => {
    const data = await loadItemData()
    expect(itemName(data, 4096)).toBe('Fire Crystal')
    expect(itemName(data, 4097)).toBe('Ice Crystal')
    expect(itemName(data, 999999)).toBe('Item #999999')
    expect(findItemIds(data, 'fire crystal').ids).toContain(4096)
  })

  it('builds BG-Wiki style tooltip views for weapons and armor', async () => {
    const details = await loadItemDetails()
    const nyame = buildItemTooltipView(23768, 'Nyame Mail', details['23768'])
    expect(nyame.rare).toBe(true)
    expect(nyame.ex).toBe(true)
    expect(nyame.slotLabel).toBe('[Body]')
    expect(nyame.raceLabel).toBe('All Races')
    expect(nyame.levelJobLabel).toBe('Lv. 99 All Jobs')
    expect(nyame.itemLevelLabel).toBe('<ItemLevel:119>')
    expect(nyame.lines[0]).toContain('DEF:189')
    expect(formatJobs(160)).toBe('RDM / PLD')
    expect(formatSlots(3)).toBe('[Main, Sub]')
  })
})

describe('bag labels', () => {
  it('names the usual bags, wardrobes and slips', () => {
    expect(bagLabel('inventory')).toBe('Inventory')
    expect(bagLabel('safe2')).toBe('Mog Safe 2')
    expect(bagLabel('wardrobe')).toBe('Mog Wardrobe')
    expect(bagLabel('wardrobe5')).toBe('Mog Wardrobe 5')
    expect(bagLabel('slip 07')).toBe('Porter Slip 07')
    expect(bagLabel('keyItems')).toBe('Key Items')
    expect(bagLabel('newbag')).toBe('newbag')
  })

  it('orders carried bags first and slips and key items last', () => {
    const sorted = ['slip 01', 'keyItems', 'wardrobe2', 'safe', 'wardrobe', 'inventory'].sort(compareBags)
    expect(sorted).toEqual(['inventory', 'wardrobe', 'wardrobe2', 'safe', 'slip 01', 'keyItems'])
  })
})

describe('timeAgo', () => {
  const now = Date.parse('2026-10-25T12:00:00Z')
  it('formats relative times', () => {
    expect(timeAgo('2026-10-25T11:59:50Z', now)).toBe('just now')
    expect(timeAgo('2026-10-25T11:55:00Z', now)).toBe('5 min ago')
    expect(timeAgo('2026-10-25T09:00:00Z', now)).toBe('3 h ago')
    expect(timeAgo('2026-10-23T12:00:00Z', now)).toBe('2 d ago')
  })
  it('is blank for missing or invalid input', () => {
    expect(timeAgo(null, now)).toBe('')
    expect(timeAgo('nope', now)).toBe('')
  })
})
