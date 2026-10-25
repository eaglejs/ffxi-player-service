#!/usr/bin/env node
// Builds src/assets/item-data.json (item id -> name) for the inventory search.
//
//   node scripts/build-item-data.mjs [--resources <windower res dir>]
//
// Reads items.lua and key_items.lua from a Windower `res` folder, or downloads them from
// the Windower/Resources GitHub repo when --resources is omitted. Rerun after a game update.
import { writeFile } from 'node:fs/promises'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { loadResource, resourcesDirFromArgs } from './lib/windower.mjs'

const OUT = fileURLToPath(new URL('../src/assets/item-data.json', import.meta.url))

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

async function main() {
  const resourcesDir = resourcesDirFromArgs()

  const items = parseResources(await loadResource('items.lua', resourcesDir), true)
  const keyItems = parseResources(await loadResource('key_items.lua', resourcesDir), false)
  if (Object.keys(items).length < 1000 || Object.keys(keyItems).length < 100) {
    throw new Error('Parsed suspiciously few rows; the resource format may have changed')
  }

  await writeFile(OUT, JSON.stringify({ items, keyItems }))
  console.log(`Wrote ${OUT}: ${Object.keys(items).length} items, ${Object.keys(keyItems).length} key items`)
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main().catch((e) => {
    console.error(e.message)
    process.exit(1)
  })
}
