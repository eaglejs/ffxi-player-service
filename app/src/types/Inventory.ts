export interface InventoryItem {
  id: number
  count: number
}

export interface PlayerInventory {
  playerId: number
  playerName: string
  gil: number
  keyItems: number[]
  bags: Record<string, InventoryItem[]>
  updatedAt: string
}

export interface InventoryLocation {
  bag: string
  count: number
  itemId?: number
}

export interface InventorySearchResult {
  playerId: number
  playerName: string
  totalCount: number
  locations: InventoryLocation[]
  updatedAt: string | null
}

export interface InventorySearchResponse {
  totalMatches: number
  results: InventorySearchResult[]
}

export interface SearchParams {
  itemIds: number[]
  /** Optional character filter; sends the playerId so offline characters work too. */
  playerId?: number
  includeKeyItems?: boolean
  bags?: string[]
}

export interface ResultLocation {
  bag: string
  label: string
  count: number
}

export interface ResultRow {
  kind: 'item' | 'key'
  id: number
  name: string
  category: string
  total: number
  locations: ResultLocation[]
}

export interface CharacterResult {
  playerId: number
  playerName: string
  updatedAt: string | null
  total: number
  rows: ResultRow[]
}
