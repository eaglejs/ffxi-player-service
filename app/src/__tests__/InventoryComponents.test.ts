import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import ResultCard from '@/components/inventory/ResultCard.vue'
import InventoryBrowser from '@/components/inventory/InventoryBrowser.vue'
import type { CharacterResult } from '@/types/Inventory'

vi.mock('@/helpers/config', () => ({ fullUrl: 'http://host/api', fullWsUrl: 'ws://host' }))

const getInventory = vi.fn()
vi.mock('@/stores/inventory', () => ({ useInventoryStore: () => ({ getInventory }) }))

describe('ItemIcon', () => {
  it('loads the icon from the backend store', () => {
    const img = mount(ItemIcon, { props: { id: 4096 } }).find('img')
    expect(img.attributes('src')).toBe('http://host/api/items/icon/4096.png')
    expect(img.attributes('loading')).toBe('lazy')
  })

  it('falls back to a placeholder when the icon is not stored yet', async () => {
    const wrapper = mount(ItemIcon, { props: { id: 4096 } })
    await wrapper.find('img').trigger('error')
    expect(wrapper.find('img').exists()).toBe(false)
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('resets the fallback when the id changes', async () => {
    const wrapper = mount(ItemIcon, { props: { id: 1 } })
    await wrapper.find('img').trigger('error')
    await wrapper.setProps({ id: 2 })
    expect(wrapper.find('img').attributes('src')).toContain('/2.png')
  })

  it('never requests an image for key items', () => {
    expect(mount(ItemIcon, { props: { id: 10, kind: 'key' } }).find('img').exists()).toBe(false)
  })
})

const character: CharacterResult = {
  playerId: 1,
  playerName: 'Alpha',
  updatedAt: null,
  total: 15,
  rows: [
    {
      kind: 'item',
      id: 4096,
      name: 'Fire Crystal',
      category: 'Usable',
      total: 15,
      locations: [
        { bag: 'inventory', label: 'Inventory', count: 12 },
        { bag: 'satchel', label: 'Mog Satchel', count: 3 }
      ]
    },
    { kind: 'key', id: 10, name: 'Map', category: 'Key Item', total: 1, locations: [{ bag: 'keyItems', label: 'Key Items', count: 1 }] }
  ]
}

describe('ResultCard', () => {
  it('shows the character, rows and per-bag counts', () => {
    const wrapper = mount(ResultCard, { props: { character } })
    expect(wrapper.text()).toContain('Alpha')
    expect(wrapper.text()).toContain('Fire Crystal')
    expect(wrapper.text()).toContain('× 15')
    const pills = wrapper.findAll('.pill').map((p) => p.text().replace(/\s+/g, ' '))
    expect(pills).toContain('Inventory 12')
    expect(pills).toContain('Mog Satchel 3')
  })

  it('links items to FFXIAH in a new tab but not key items', () => {
    const links = mount(ResultCard, { props: { character } }).findAll('a')
    expect(links).toHaveLength(1)
    expect(links[0]!.attributes('href')).toBe('https://www.ffxiah.com/item/4096')
    expect(links[0]!.attributes('rel')).toContain('noopener')
  })

  it('asks to browse the character when the name is clicked', async () => {
    const wrapper = mount(ResultCard, { props: { character } })
    await wrapper.find('.char-name').trigger('click')
    expect(wrapper.emitted('browse')![0]).toEqual([character])
  })
})

describe('InventoryBrowser', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    getInventory.mockReset()
  })

  it('renders gil, bags and key items', async () => {
    getInventory.mockResolvedValue({
      playerId: 1,
      playerName: 'Alpha',
      gil: 1234567,
      keyItems: [1],
      bags: { inventory: [{ id: 4096, count: 12 }], safe: [], 'slip 01': [{ id: 4097, count: 1 }] },
      updatedAt: new Date().toISOString()
    })
    const wrapper = mount(InventoryBrowser, { props: { playerId: 1, playerName: 'Alpha' } })
    await vi.waitFor(() => expect(wrapper.text()).toContain((1234567).toLocaleString()))
    const bags = wrapper.findAll('details').map((d) => d.find('summary').text())
    expect(bags[0]).toContain('Inventory')
    expect(bags.some((b) => b.includes('Porter Slip 01'))).toBe(true)
    expect(bags.some((b) => b.includes('Mog Safe'))).toBe(false) // empty bags are hidden
    expect(bags.some((b) => b.includes('Key Items'))).toBe(true)
  })

  it('explains when nothing has been synced yet', async () => {
    getInventory.mockResolvedValue(null)
    const wrapper = mount(InventoryBrowser, { props: { playerId: 1, playerName: 'Alpha' } })
    await vi.waitFor(() => expect(wrapper.text()).toContain('No inventory has been synced'))
  })

  it('closes from the button and the backdrop', async () => {
    getInventory.mockResolvedValue(null)
    const wrapper = mount(InventoryBrowser, { props: { playerId: 1, playerName: 'Alpha' } })
    await flushPromises()
    await wrapper.find('.close-btn').trigger('click')
    await wrapper.find('.backdrop').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(2)
  })
})
