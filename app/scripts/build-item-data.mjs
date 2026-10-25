#!/usr/bin/env node
// Builds src/assets/item-data.json (item id -> name) for the inventory search.
//
//   node scripts/build-item-data.mjs [--resources <windower res dir>]
//
// Reads items.lua and key_items.lua from a Windower `res` folder, or downloads them from
// the Windower/Resources GitHub repo when --resources is omitted. Rerun after a game update.
import { readFile, writeFile } from 'node:fs/promises'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

const RAW = 'https://raw.githubusercontent.com/Windower/Resources/master/resources_data'
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

async function load(file, resourcesDir) {
  if (resourcesDir) return readFile(join(resourcesDir, file), 'utf8')
  const res = await fetch(`${RAW}/${file}`)
  if (!res.ok) throw new Error(`Failed to download ${file}: HTTP ${res.status}`)
  return res.text()
}

async function main() {
  const i = process.argv.indexOf('--resources')
  const resourcesDir = i > -1 ? process.argv[i + 1] : undefined

  const items = parseResources(await load('items.lua', resourcesDir), true)
  const keyItems = parseResources(await load('key_items.lua', resourcesDir), false)
  if (Object.keys(items).length < 1000 || Object.keys(keyItems).length < 100) {
    throw new Error('Parsed suspiciously few rows; the resource format may have changed')
  }

  await writeFile(OUT, JSON.stringify({ items, keyItems }))
  console.log(`Wrote ${OUT}: ${Object.keys(items).length} items, ${Object.keys(keyItems).length} key items`)
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  main().catch((e) => {
    console.error(e.message)
    process.exit(1)
  })
}
