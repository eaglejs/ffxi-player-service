import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RemaPlayerCard from '@/components/rema/RemaPlayerCard.vue'
import type { PlayerRema } from '@/helpers/rema'

vi.mock('@/helpers/config', () => ({ fullUrl: 'http://host/api', fullWsUrl: 'ws://host' }))

const spharai = {
  name: 'Spharai',
  type: 'Hand-to-Hand',
  stages: [
    { id: 100, level: 75, itemLevel: 0 },
    { id: 103, level: 99, itemLevel: 119 }
  ]
}
const mandau = { name: 'Mandau', type: 'Dagger', stages: [{ id: 200, level: 75, itemLevel: 0 }] }
const prime = { name: 'Prime Sword', type: 'Sword', stages: [{ id: 300, level: 99, itemLevel: 119 }] }

const player: PlayerRema = {
  playerId: 1,
  playerName: 'Alpha',
  updatedAt: null,
  ownedTotal: 1,
  weaponTotal: 3,
  categories: [
    {
      key: 'relic',
      label: 'Relic',
      total: 2,
      owned: [
        {
          weapon: spharai,
          stageIndex: 1,
          stage: spharai.stages[1]!,
          stagesHeld: 2,
          locations: [{ bag: 'safe', label: 'Mog Safe', count: 1 }]
        }
      ],
      missing: [mandau]
    },
    { key: 'prime', label: 'Prime', total: 1, owned: [], missing: [prime] }
  ]
}

describe('RemaPlayerCard', () => {
  it('shows the character, per-category progress and owned weapons with their stage badge', () => {
    const wrapper = mount(RemaPlayerCard, { props: { player } })
    expect(wrapper.text()).toContain('Alpha')
    expect(wrapper.text()).toContain('1 of 3 weapons')
    expect(wrapper.text()).toContain('1/2')
    expect(wrapper.text()).toContain('0/1')
    const weapon = wrapper.find('.weapon')
    expect(weapon.text()).toContain('Spharai')
    expect(weapon.find('.badge').text()).toBe('119')
    expect(weapon.attributes('title')).toBe('Spharai, Hand-to-Hand · iL 119 (stage 2 of 2) · Mog Safe')
  })

  it('shows the icon of the highest stage held', () => {
    const img = mount(RemaPlayerCard, { props: { player } }).find('img')
    expect(img.attributes('src')).toBe('http://host/api/items/icon/103.png')
  })

  it('says "None yet" for an empty category and lists what is missing otherwise', () => {
    const wrapper = mount(RemaPlayerCard, { props: { player } })
    expect(wrapper.find('.none').text()).toBe('None yet')
    const missing = wrapper.findAll('.missing')
    expect(missing).toHaveLength(1) // not offered when nothing is owned in the category
    expect(missing[0]!.text()).toContain('Mandau')
  })

  it('limits the sections to the selected categories', () => {
    const wrapper = mount(RemaPlayerCard, { props: { player, categoryKeys: ['relic'] } })
    expect(wrapper.findAll('.category')).toHaveLength(1)
    expect(wrapper.text()).toContain('Relic')
    expect(wrapper.text()).not.toContain('Prime')
  })

  it('says so when the selected categories hold nothing for the character', () => {
    const wrapper = mount(RemaPlayerCard, { props: { player, categoryKeys: ['prime'] } })
    expect(wrapper.findAll('.category')).toHaveLength(0)
    expect(wrapper.find('.empty').text()).toBe('None in the selected categories')
  })

  it('collapses to one line for a character with none of the weapons in view', () => {
    const empty: PlayerRema = {
      ...player,
      ownedTotal: 0,
      categories: player.categories.map((c) => ({ ...c, owned: [], missing: [...c.missing] }))
    }
    const wrapper = mount(RemaPlayerCard, { props: { player: empty } })
    expect(wrapper.find('.empty').text()).toBe('No REMA weapons yet')
    expect(wrapper.findAll('.category')).toHaveLength(0)
  })
})
