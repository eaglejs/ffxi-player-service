// Shared helpers for reading Windower resource tables (items.lua, key_items.lua, ...).
import { readFile } from 'node:fs/promises'
import { join } from 'node:path'

const RAW = 'https://raw.githubusercontent.com/Windower/Resources/master/resources_data'

/** Reads a resource file from a local Windower `res` dir, or downloads it from GitHub. */
export async function loadResource(file, resourcesDir) {
  if (resourcesDir) return readFile(join(resourcesDir, file), 'utf8')
  const res = await fetch(`${RAW}/${file}`)
  if (!res.ok) throw new Error(`Failed to download ${file}: HTTP ${res.status}`)
  return res.text()
}

export function resourcesDirFromArgs(argv = process.argv) {
  const i = argv.indexOf('--resources')
  return i > -1 ? argv[i + 1] : undefined
}

const unescapeLua = (s) => s.replace(/\\(["\\])/g, '$1')
const ROW = /^\s*\[(\d+)\]\s*=\s*\{(.*)\},?\s*$/

function field(body, key) {
  const m = new RegExp(`\\b${key}=("(?:[^"\\\\]|\\\\.)*"|-?\\d+)`).exec(body)
  if (!m) return undefined
  return m[1].startsWith('"') ? unescapeLua(m[1].slice(1, -1)) : Number(m[1])
}

/** Parses every `[id] = {id=..,en="..",category="..",level=..,item_level=..,skill=..}` row. */
export function parseItemRows(lua) {
  const rows = []
  for (const line of lua.split('\n')) {
    const m = ROW.exec(line)
    if (!m) continue
    rows.push({
      id: Number(m[1]),
      name: field(m[2], 'en'),
      category: field(m[2], 'category'),
      level: field(m[2], 'level'),
      itemLevel: field(m[2], 'item_level'),
      skill: field(m[2], 'skill')
    })
  }
  return rows
}
