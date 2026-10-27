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

const unescapeLua = (s) => s.replace(/\\n/g, '\n').replace(/\\(["\\])/g, '$1')
const ROW = /^\s*\[(\d+)\]\s*=\s*\{(.*)\},?\s*$/

function field(body, key) {
  const m = new RegExp(`\\b${key}=("(?:[^"\\\\]|\\\\.)*"|-?\\d+(?:\\.\\d+)?)`).exec(body)
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
      superiorLevel: field(m[2], 'superior_level'),
      skill: field(m[2], 'skill'),
      flags: field(m[2], 'flags'),
      jobs: field(m[2], 'jobs'),
      slots: field(m[2], 'slots'),
      races: field(m[2], 'races'),
      damage: field(m[2], 'damage'),
      delay: field(m[2], 'delay'),
      castTime: field(m[2], 'cast_time'),
      castDelay: field(m[2], 'cast_delay'),
      recastDelay: field(m[2], 'recast_delay'),
      maxCharges: field(m[2], 'max_charges')
    })
  }
  return rows
}

/** Parses `item_descriptions.lua` into a map of `id -> English stat/description text`. */
export function parseItemDescriptions(lua) {
  const out = {}
  for (const line of lua.split('\n')) {
    const m = ROW.exec(line)
    if (!m) continue
    const en = field(m[2], 'en')
    if (en) out[Number(m[1])] = en.trimEnd()
  }
  return out
}
