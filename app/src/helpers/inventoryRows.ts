import { bagLabel, compareBags, KEY_ITEMS_BAG } from '@/helpers/bags'
import { itemCategory, itemName, keyItemName, type ItemData } from '@/helpers/items'
import type {
  CharacterResult,
  InventorySearchResponse,
  InventorySearchResult,
  ResultRow
} from '@/types/Inventory'

function rowsFor(
  result: InventorySearchResult,
  kind: 'item' | 'key',
  fallbackId: number | undefined,
  data: ItemData
): ResultRow[] {
  const byId = new Map<number, ResultRow>()
  for (const location of result.locations) {
    // The backend omits itemId when a single id was searched.
    const id = location.itemId ?? fallbackId
    if (id === undefined) continue
    let row = byId.get(id)
    if (!row) {
      row = {
        kind,
        id,
        name: kind === 'key' ? keyItemName(data, id) : itemName(data, id),
        category: kind === 'key' ? 'Key Item' : itemCategory(data, id),
        total: 0,
        locations: []
      }
      byId.set(id, row)
    }
    row.total += location.count
    row.locations.push({ bag: location.bag, label: bagLabel(location.bag), count: location.count })
  }
  for (const row of byId.values()) {
    row.locations.sort((a, b) => compareBags(a.bag, b.bag))
  }
  return [...byId.values()]
}

/**
 * Merges the item search and the key item search into one card per character.
 * Key items are searched separately because their ids are a different id space.
 */
export function buildCharacterResults(
  items: { response: InventorySearchResponse; ids: number[] } | null,
  keyItems: { response: InventorySearchResponse; ids: number[] } | null,
  data: ItemData
): CharacterResult[] {
  const characters = new Map<number, CharacterResult>()

  const add = (
    part: { response: InventorySearchResponse; ids: number[] } | null,
    kind: 'item' | 'key'
  ) => {
    if (!part) return
    const fallbackId = part.ids.length === 1 ? part.ids[0] : undefined
    for (const result of part.response.results) {
      let character = characters.get(result.playerId)
      if (!character) {
        character = {
          playerId: result.playerId,
          playerName: result.playerName,
          updatedAt: result.updatedAt,
          total: 0,
          rows: []
        }
        characters.set(result.playerId, character)
      }
      character.rows.push(...rowsFor(result, kind, fallbackId, data))
    }
  }

  add(items, 'item')
  add(keyItems, 'key')

  const results = [...characters.values()]
  for (const character of results) {
    character.rows.sort((a, b) => b.total - a.total || a.name.localeCompare(b.name))
    character.total = character.rows.reduce((sum, row) => sum + row.total, 0)
  }
  return results.sort((a, b) => b.total - a.total || a.playerName.localeCompare(b.playerName))
}

/** Narrows results to the chosen bags (client-side, so toggling chips costs no request). */
export function filterByBags(results: CharacterResult[], bags: string[]): CharacterResult[] {
  if (bags.length === 0) return results
  const wanted = new Set(bags)
  const filtered: CharacterResult[] = []
  for (const character of results) {
    const rows: ResultRow[] = []
    for (const row of character.rows) {
      const locations = row.locations.filter((l) => wanted.has(l.bag))
      if (locations.length) {
        rows.push({ ...row, locations, total: locations.reduce((s, l) => s + l.count, 0) })
      }
    }
    if (rows.length) {
      filtered.push({ ...character, rows, total: rows.reduce((s, r) => s + r.total, 0) })
    }
  }
  return filtered.sort((a, b) => b.total - a.total || a.playerName.localeCompare(b.playerName))
}

/** Every bag that appears in the results, in display order, for the filter chips. */
export function bagsInResults(results: CharacterResult[]): string[] {
  const bags = new Set<string>()
  for (const character of results) {
    for (const row of character.rows) {
      for (const location of row.locations) bags.add(location.bag)
    }
  }
  return [...bags].sort(compareBags)
}

export { KEY_ITEMS_BAG }
