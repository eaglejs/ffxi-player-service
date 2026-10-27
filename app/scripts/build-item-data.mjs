#!/usr/bin/env node
// Builds src/assets/item-data.json (item id -> name) for the inventory search.
//
//   node scripts/build-item-data.mjs [--resources <windower res dir>]
//
// Reads items.lua and key_items.lua from a Windower `res` folder, or downloads them from
// the Windower/Resources GitHub repo when --resources is omitted. Rerun after a game update.
import { writeFile } from 'node:fs/promises'
import { fileURLToPath, pathToFileURL } from 'node:url'
import {
  loadResource,
  parseItemDescriptions,
  parseItemRows,
  resourcesDirFromArgs
} from './lib/windower.mjs'

const OUT = fileURLToPath(new URL('../src/assets/item-data.json', import.meta.url))
const OUT_DETAILS = fileURLToPath(new URL('../src/assets/item-details.json', import.meta.url))

// One table row per line: [123] = {id=123,en="Name",...,category="General",...}
const ROW = /^\s*\[(\d+)\]\s*=\s*\{.*?\ben="((?:[^"\\]|\\.)*)"/
const CATEGORY = /\bcategory="((?:[^"\\]|\\.)*)"/

const unescapeLua = (s) => s.replace(/\\(["\\])/g, '$1')

export function parseResources(lua, withCategory) {
  const out = {}
  for (const line of lua.split('\n')) {
    const m = ROW.exec(line)
    if (!m) continue
    const name = unescapeLua(m[2])
    if (!name) continue
    out[m[1]] = withCategory ? [name, unescapeLua(CATEGORY.exec(line)?.[1] ?? '')] : name
  }
  return out
}

export function buildItemDetails(rows, descriptions) {
  const details = {}
  for (const row of rows) {
    const d = descriptions[row.id]
    const f = ((row.flags ?? 0) >> 14) & 3 // bit 1 = Rare (0x8000), bit 0 = Ex (0x4000)
    const entry = {}
    if (d) entry.d = d
    if (f) entry.f = f
    if (row.level) entry.l = row.level
    if (row.itemLevel) entry.il = row.itemLevel
    if (row.superiorLevel) entry.su = row.superiorLevel
    if (row.jobs) entry.j = row.jobs
    if (row.slots) entry.s = row.slots
    if (row.races) entry.r = row.races
    if (row.skill) entry.sk = row.skill
    if (row.maxCharges) {
      entry.ch = [row.maxCharges, row.castTime ?? 0, row.recastDelay ?? 0, row.castDelay ?? 0]
    }
    if (Object.keys(entry).length > 0) {
      details[row.id] = entry
    }
  }
  return details
}

async function main() {
  const resourcesDir = resourcesDirFromArgs()
  const [itemsLua, keyItemsLua, descLua] = await Promise.all([
    loadResource('items.lua', resourcesDir),
    loadResource('key_items.lua', resourcesDir),
    loadResource('item_descriptions.lua', resourcesDir)
  ])

  const items = parseResources(itemsLua, true)
  const keyItems = parseResources(keyItemsLua, false)
  if (Object.keys(items).length < 1000 || Object.keys(keyItems).length < 100) {
    throw new Error('Parsed suspiciously few rows; the resource format may have changed')
  }

  const details = buildItemDetails(parseItemRows(itemsLua), parseItemDescriptions(descLua))

  await Promise.all([
    writeFile(OUT, JSON.stringify({ items, keyItems })),
    writeFile(OUT_DETAILS, JSON.stringify(details))
  ])
  console.log(
    `Wrote ${OUT}: ${Object.keys(items).length} items, ${Object.keys(keyItems).length} key items`
  )
  console.log(`Wrote ${OUT_DETAILS}: ${Object.keys(details).length} item detail entries`)
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main().catch((e) => {
    console.error(e.message)
    process.exit(1)
  })
}
