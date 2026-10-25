#!/usr/bin/env node
// Builds src/assets/rema-data.json for the REMA weapons page: every Relic, Empyrean, Mythic,
// Aeonic and Prime weapon with the item id of each upgrade stage.
//
//   node scripts/build-rema-data.mjs [--resources <windower res dir>]
//
// The Relic/Empyrean/Mythic/Aeonic names come from scripts/data/rema-names.json (checked against
// BG Wiki); their item ids come from items.lua. Prime weapons are the "Prime ..." items in
// items.lua. Rerun after a game update.
import { readFile, writeFile } from 'node:fs/promises'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { loadResource, parseItemRows, resourcesDirFromArgs } from './lib/windower.mjs'

const CATEGORIES = [
  ['relic', 'Relic'],
  ['empyrean', 'Empyrean'],
  ['mythic', 'Mythic'],
  ['aeonic', 'Aeonic'],
  ['prime', 'Prime']
]

// Display order of weapon types within a category.
export const TYPE_ORDER = [
  'Hand-to-Hand', 'Dagger', 'Sword', 'Great Sword', 'Axe', 'Great Axe', 'Scythe', 'Polearm',
  'Katana', 'Great Katana', 'Club', 'Staff', 'Archery', 'Marksmanship', 'Instrument', 'Shield'
]

// items.lua `skill` -> type, used for Prime weapons (the name list supplies the others).
const SKILL_TYPES = {
  1: 'Hand-to-Hand', 2: 'Dagger', 3: 'Sword', 4: 'Great Sword', 5: 'Axe', 6: 'Great Axe',
  7: 'Scythe', 8: 'Polearm', 9: 'Katana', 10: 'Great Katana', 11: 'Club', 12: 'Staff',
  25: 'Archery', 26: 'Marksmanship', 41: 'Instrument', 42: 'Instrument'
}

const isGear = (row) => row.category === 'Weapon' || row.category === 'Armor'

// Upgrade stages run by level, then item level, then id (ids ascend with each stage).
const byStage = (a, b) => a.level - b.level || a.itemLevel - b.itemLevel || a.id - b.id

function stagesFor(rows, name) {
  return rows
    .filter((r) => r.name === name && isGear(r))
    .map((r) => ({ id: r.id, level: r.level ?? 0, itemLevel: r.itemLevel ?? 0 }))
    .sort(byStage)
}

export function buildRemaData(rows, names) {
  const categories = CATEGORIES.map(([key, label]) => {
    const weapons = []
    if (key === 'prime') {
      for (const row of rows.filter((r) => r.category === 'Weapon' && r.name?.startsWith('Prime '))) {
        const type = SKILL_TYPES[row.skill]
        if (!type) throw new Error(`Unknown weapon skill ${row.skill} for ${row.name}`)
        weapons.push({ name: row.name, type, stages: stagesFor(rows, row.name) })
      }
    } else {
      for (const [name, type] of Object.entries(names[key] ?? {})) {
        const stages = stagesFor(rows, name)
        if (!stages.length) throw new Error(`${label} weapon "${name}" not found in items.lua`)
        weapons.push({ name, type, stages })
      }
    }
    weapons.sort((a, b) => TYPE_ORDER.indexOf(a.type) - TYPE_ORDER.indexOf(b.type) || a.name.localeCompare(b.name))
    return { key, label, weapons }
  })
  return { categories }
}

async function main() {
  const NAMES = fileURLToPath(new URL('./data/rema-names.json', import.meta.url))
  const OUT = fileURLToPath(new URL('../src/assets/rema-data.json', import.meta.url))
  const names = JSON.parse(await readFile(NAMES, 'utf8'))
  const rows = parseItemRows(await loadResource('items.lua', resourcesDirFromArgs()))
  const data = buildRemaData(rows, names)

  await writeFile(OUT, JSON.stringify(data))
  for (const c of data.categories) {
    const ids = c.weapons.reduce((n, w) => n + w.stages.length, 0)
    console.log(`${c.label.padEnd(9)} ${String(c.weapons.length).padStart(2)} weapons, ${ids} item ids`)
  }
  console.log(`Wrote ${OUT}`)
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main().catch((e) => {
    console.error(e.message)
    process.exit(1)
  })
}
