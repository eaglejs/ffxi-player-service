import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import axios from 'axios'
import { useInventoryStore } from '@/stores/inventory'

vi.mock('axios', async () => {
  const actual = await vi.importActual<typeof import('axios')>('axios')
  return { ...actual, default: { ...actual.default, get: vi.fn(), isAxiosError: actual.isAxiosError } }
})
vi.mock('@/helpers/config', () => ({ fullUrl: 'http://host/api', fullWsUrl: 'ws://host' }))

const get = axios.get as unknown as ReturnType<typeof vi.fn>

describe('useInventoryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    get.mockReset()
    get.mockResolvedValue({ data: { totalMatches: 0, results: [] } })
  })

  it('searches all characters with comma-separated ids', async () => {
    await useInventoryStore().searchInventory({ itemIds: [4096, 4097] })
    expect(get).toHaveBeenCalledWith('http://host/api/player/inventory/search?item=4096%2C4097&limit=500')
  })

  it('uses the per-player route when a character is chosen', async () => {
    await useInventoryStore().searchInventory({ itemIds: [4096], playerId: 7 })
    expect(get).toHaveBeenCalledWith('http://host/api/player/7/inventory/search?item=4096&limit=500')
  })

  it('searches key items through the keyItems bag only', async () => {
    await useInventoryStore().searchKeyItems({ itemIds: [10] })
    const url = get.mock.calls[0]![0] as string
    expect(url).toContain('includeKeyItems=true')
    expect(url).toContain('bag=keyItems')
  })

  it('returns the stored inventory, or null on 404', async () => {
    get.mockResolvedValueOnce({ data: { playerId: 7, gil: 5 } })
    expect(await useInventoryStore().getInventory(7)).toEqual({ playerId: 7, gil: 5 })

    const { AxiosError } = await vi.importActual<typeof import('axios')>('axios')
    get.mockRejectedValueOnce(new AxiosError('nf', '404', undefined, undefined, { status: 404 } as never))
    expect(await useInventoryStore().getInventory(8)).toBeNull()
  })

  it('rethrows other errors', async () => {
    get.mockRejectedValueOnce(new Error('network'))
    await expect(useInventoryStore().getInventory(9)).rejects.toThrow('network')
  })
})
