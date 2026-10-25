import { describe, it, expect } from 'vitest'
// @ts-expect-error plain .mjs build script without type declarations
import { buildRemaData } from '../../scripts/build-rema-data.mjs'
// @ts-expect-error plain .mjs build script without type declarations
import { parseItemRows } from '../../scripts/lib/windower.mjs'

const lua = `
return {
    [10] = {id=10,en="Spharai",ja="x",category="Weapon",level=75,skill=1,type=4},
    [11] = {id=11,en="Spharai",ja="x",category="Weapon",item_level=119,level=99,skill=1,type=4},
    [20] = {id=20,en="Aegis",ja="x",category="Armor",level=75,type=5},
    [30] = {id=30,en="Prime Dagger",ja="x",category="Weapon",item_level=119,level=99,skill=2,type=4},
    [31] = {id=31,en="Primeval Brew",ja="x",category="Usable",type=7},
    [40] = {id=40,en="Quote \\"Q\\" Sword",ja="x",category="Weapon",level=1,skill=3,type=4},
}
`

describe('build-rema-data', () => {
  const rows = parseItemRows(lua)

  it('parses the fields the build needs', () => {
    expect(rows.find((r: { id: number }) => r.id === 11)).toMatchObject({ name: 'Spharai', category: 'Weapon', level: 99, itemLevel: 119, skill: 1 })
    expect(rows.find((r: { id: number }) => r.id === 40).name).toBe('Quote "Q" Sword')
  })

  it('collects stages in order, includes shields, and finds Prime weapons by name', () => {
    const data = buildRemaData(rows, { relic: { Spharai: 'Hand-to-Hand', Aegis: 'Shield' } })
    const relic = data.categories.find((c: { key: string }) => c.key === 'relic')
    expect(relic.weapons.map((w: { name: string }) => w.name)).toEqual(['Spharai', 'Aegis'])
    expect(relic.weapons[0].stages.map((s: { id: number }) => s.id)).toEqual([10, 11])
    const prime = data.categories.find((c: { key: string }) => c.key === 'prime')
    expect(prime.weapons).toEqual([{ name: 'Prime Dagger', type: 'Dagger', stages: [{ id: 30, level: 99, itemLevel: 119 }] }])
  })

  it('fails loudly when a listed weapon is missing from the resources', () => {
    expect(() => buildRemaData(rows, { relic: { Nonexistent: 'Sword' } })).toThrow(/Nonexistent/)
  })
})
