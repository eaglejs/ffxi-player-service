import { defineStore } from 'pinia'
import axios from 'axios'
import { fullUrl } from '@/helpers/config'
import { KEY_ITEMS_BAG } from '@/helpers/bags'
import type { InventorySearchResponse, PlayerInventory, SearchParams } from '@/types/Inventory'

// All characters fit comfortably in one page, so paging is never needed from the UI.
const SEARCH_LIMIT = 500

export const useInventoryStore = defineStore('inventory', () => {
  async function searchInventory(params: SearchParams): Promise<InventorySearchResponse> {
    const query = new URLSearchParams()
    query.set('item', params.itemIds.join(','))
    query.set('limit', String(SEARCH_LIMIT))
    if (params.includeKeyItems) query.set('includeKeyItems', 'true')
    for (const bag of params.bags ?? []) query.append('bag', bag)

    // With a character chosen, use the per-player route (works for offline characters too).
    const path =
      params.playerId !== undefined
        ? `/player/${params.playerId}/inventory/search`
        : '/player/inventory/search'

    const response = await axios.get<InventorySearchResponse>(`${fullUrl}${path}?${query}`)
    return response.data
  }

  /** Key items live in their own id space, so they get their own request. */
  function searchKeyItems(params: Omit<SearchParams, 'includeKeyItems' | 'bags'>) {
    return searchInventory({ ...params, includeKeyItems: true, bags: [KEY_ITEMS_BAG] })
  }

  /** Returns null when no inventory has been stored for the player yet. */
  async function getInventory(playerId: number): Promise<PlayerInventory | null> {
    try {
      const response = await axios.get<PlayerInventory>(`${fullUrl}/player/${playerId}/inventory`)
      return response.data
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) return null
      throw error
    }
  }

  return { searchInventory, searchKeyItems, getInventory }
})
