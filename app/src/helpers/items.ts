export interface ItemData {
  /** item id -> [name, category] */
  items: Record<string, [string, string]>
  /** key item id -> name */
  keyItems: Record<string, string>
}

/** The backend takes the ids in the query string, so one request carries at most this many. */
export const MAX_ITEM_IDS = 50
export const MIN_NAME_QUERY = 2

let loading: Promise<ItemData> | null = null

/** Loads the (~900 KB) names table on first use so it stays out of the main bundle. */
export function loadItemData(): Promise<ItemData> {
  if (!loading) {
    loading = import('@/assets/item-data.json').then((m) => (m.default ?? m) as unknown as ItemData)
    loading.catch(() => {
      loading = null
    })
  }
  return loading
}

export function itemName(data: ItemData, id: number): string {
  return data.items[id]?.[0] ?? `Item #${id}`
}

export function itemCategory(data: ItemData, id: number): string {
  return data.items[id]?.[1] ?? ''
}

export function keyItemName(data: ItemData, id: number): string {
  return data.keyItems[id] ?? `Key item #${id}`
}

export interface IdMatches {
  ids: number[]
  /** matches before the MAX_ITEM_IDS cap */
  total: number
  truncated: boolean
}

const EMPTY: IdMatches = { ids: [], total: 0, truncated: false }

/**
 * Resolves a search box value to ids. A bare number is an id; otherwise names are matched
 * case-insensitively and ranked exact, then prefix, then substring.
 */
export function findIds(names: Record<string, string>, query: string): IdMatches {
  const q = query.trim().toLowerCase()
  if (!q) return EMPTY
  if (/^\d{1,5}$/.test(q)) return { ids: [Number(q)], total: 1, truncated: false }
  if (q.length < MIN_NAME_QUERY) return EMPTY

  const ranked: Array<[number, number, string]> = []
  for (const [id, name] of Object.entries(names)) {
    const lower = name.toLowerCase()
    const at = lower.indexOf(q)
    if (at === -1) continue
    ranked.push([lower === q ? 0 : at === 0 ? 1 : 2, Number(id), lower])
  }
  ranked.sort((a, b) => a[0] - b[0] || a[2].localeCompare(b[2]) || a[1] - b[1])

  return {
    ids: ranked.slice(0, MAX_ITEM_IDS).map((r) => r[1]),
    total: ranked.length,
    truncated: ranked.length > MAX_ITEM_IDS
  }
}

export function findItemIds(data: ItemData, query: string): IdMatches {
  const names: Record<string, string> = {}
  for (const [id, [name]] of Object.entries(data.items)) names[id] = name
  return findIds(names, query)
}

export function findKeyItemIds(data: ItemData, query: string): IdMatches {
  return findIds(data.keyItems, query)
}

export function ffxiahUrl(itemId: number): string {
  return `https://www.ffxiah.com/item/${itemId}`
}
