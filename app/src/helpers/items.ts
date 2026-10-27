import { stageLabel, type OwnedWeapon, type RemaStage, type RemaWeapon } from '@/helpers/rema'

export interface ItemData {
  /** item id -> [name, category] */
  items: Record<string, [string, string]>
  /** key item id -> name */
  keyItems: Record<string, string>
}

export interface ItemDetailRecord {
  /** Stat / description text from item_descriptions.lua (newline-separated) */
  d?: string
  /** Bitmask: bit 1 (2) = Rare, bit 0 (1) = Ex */
  f?: number
  /** Equippable level */
  l?: number
  /** Item level */
  il?: number
  /** Superior level (Su1..Su5) */
  su?: number
  /** Jobs bitmask */
  j?: number
  /** Equipment slots bitmask */
  s?: number
  /** Races bitmask */
  r?: number
  /** Combat/magic skill ID */
  sk?: number
  /** Enchantment charges: [maxCharges, castTime, recastDelay, castDelay] */
  ch?: [number, number, number, number]
}

export type ItemDetailsMap = Record<string, ItemDetailRecord>

export interface ItemTooltipView {
  id: number
  name: string
  subtitle?: string
  rare: boolean
  ex: boolean
  slotLabel: string
  raceLabel: string
  lines: string[]
  chargeInfo: string
  levelJobLabel: string
  itemLevelLabel: string
  suLabel: string
  locations?: string[]
}

/** The backend takes the ids in the query string, so one request carries at most this many. */
export const MAX_ITEM_IDS = 50
export const MIN_NAME_QUERY = 2

let loading: Promise<ItemData> | null = null
let loadingDetails: Promise<ItemDetailsMap> | null = null

/** Loads the (~900 KB) names table on first use so it stays out of the main bundle. */
export function loadItemData(): Promise<ItemData> {
  if (!loading) {
    loading = import('@/assets/item-data.json').then((m) => (m.default ?? m) as unknown as ItemData)
    loading.catch(() => {
      loading = null
    })
  }
  return loading
}

/** Loads the BG-Wiki style item details table on first use. */
export function loadItemDetails(): Promise<ItemDetailsMap> {
  if (!loadingDetails) {
    loadingDetails = import('@/assets/item-details.json').then(
      (m) => (m.default ?? m) as unknown as ItemDetailsMap
    )
    loadingDetails.catch(() => {
      loadingDetails = null
    })
  }
  return loadingDetails
}

export function itemName(data: ItemData, id: number): string {
  return data.items[id]?.[0] ?? `Item #${id}`
}

export function itemCategory(data: ItemData, id: number): string {
  return data.items[id]?.[1] ?? ''
}

export function keyItemName(data: ItemData, id: number): string {
  return data.keyItems[id] ?? `Key item #${id}`
}

const JOBS = [
  '',
  'WAR',
  'MNK',
  'WHM',
  'BLM',
  'RDM',
  'THF',
  'PLD',
  'DRK',
  'BST',
  'BRD',
  'RNG',
  'SAM',
  'NIN',
  'DRG',
  'SMN',
  'BLU',
  'COR',
  'PUP',
  'DNC',
  'SCH',
  'GEO',
  'RUN'
]
const ALL_JOBS_MASK = 8388606

export function formatJobs(mask?: number): string {
  if (!mask) return ''
  if ((mask & ALL_JOBS_MASK) === ALL_JOBS_MASK) return 'All Jobs'
  const list: string[] = []
  for (let i = 1; i <= 22; i++) {
    if (mask & (1 << i)) list.push(JOBS[i]!)
  }
  return list.join(' / ')
}

export function formatSlots(mask?: number): string {
  if (!mask) return ''
  const out: string[] = []
  if (mask & 1) out.push('Main')
  if (mask & 2) out.push('Sub')
  if (mask & 4) out.push('Range')
  if (mask & 8) out.push('Ammo')
  if (mask & 16) out.push('Head')
  if (mask & 32) out.push('Body')
  if (mask & 64) out.push('Hands')
  if (mask & 128) out.push('Legs')
  if (mask & 256) out.push('Feet')
  if (mask & 512) out.push('Neck')
  if (mask & 1024) out.push('Waist')
  if (mask & 6144) out.push('Ear')
  if (mask & 24576) out.push('Ring')
  if (mask & 32768) out.push('Back')
  return out.length ? `[${out.join(', ')}]` : ''
}

export function formatRaces(mask?: number): string {
  if (!mask) return ''
  if ((mask & 510) === 510) return 'All Races'
  if (mask === 298) return '♂'
  if (mask === 212) return '♀'
  const races: string[] = []
  if (mask & 6) races.push('Hume')
  if (mask & 24) races.push('Elvaan')
  if (mask & 96) races.push('Tarutaru')
  if (mask & 128) races.push('Mithra')
  if (mask & 256) races.push('Galka')
  return races.join(' / ')
}

const SKILLS: Record<number, string> = {
  1: 'Hand-to-Hand',
  2: 'Dagger',
  3: 'Sword',
  4: 'Great Sword',
  5: 'Axe',
  6: 'Great Axe',
  7: 'Scythe',
  8: 'Polearm',
  9: 'Katana',
  10: 'Great Katana',
  11: 'Club',
  12: 'Staff',
  25: 'Archery',
  26: 'Marksmanship',
  27: 'Throwing',
  30: 'Shield',
  41: 'Stringed Instrument',
  42: 'Wind Instrument',
  45: 'Handbell'
}

export function formatSkill(skillId?: number): string {
  return skillId ? (SKILLS[skillId] ?? '') : ''
}

function formatSeconds(sec: number): string {
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

export function formatChargeInfo(ch?: [number, number, number, number]): string {
  if (!ch || !ch[0]) return ''
  const [maxCharges, castTime, recastDelay, castDelay] = ch
  return `<${maxCharges}/${maxCharges} ${formatSeconds(castTime)}/[${formatSeconds(recastDelay)}, ${formatSeconds(castDelay)}]>`
}

export function buildItemTooltipView(
  id: number,
  name: string,
  detail?: ItemDetailRecord,
  options?: { category?: string; subtitle?: string; locations?: string[] }
): ItemTooltipView {
  const rare = detail?.f !== undefined ? (detail.f & 2) !== 0 : false
  const ex = detail?.f !== undefined ? (detail.f & 1) !== 0 : false
  const slotPart = formatSlots(detail?.s)
  const skillPart = formatSkill(detail?.sk)
  const slotLabel = [slotPart, skillPart].filter(Boolean).join(' ')
  const raceLabel = formatRaces(detail?.r)
  const lines = detail?.d ? detail.d.split('\n').filter((l) => l.trim().length > 0) : []
  const chargeInfo = formatChargeInfo(detail?.ch)
  const jobs = formatJobs(detail?.j)
  const levelJobLabel = detail?.l ? `Lv. ${detail.l}${jobs ? ` ${jobs}` : ''}` : jobs
  const itemLevelLabel = detail?.il ? `<ItemLevel:${detail.il}>` : ''
  const suLabel = detail?.su ? `Su${detail.su}` : ''

  return {
    id,
    name,
    subtitle: options?.subtitle ?? options?.category,
    rare,
    ex,
    slotLabel,
    raceLabel,
    lines,
    chargeInfo,
    levelJobLabel,
    itemLevelLabel,
    suLabel,
    locations: options?.locations
  }
}

export function buildRemaStageTooltipView(
  weapon: RemaWeapon,
  stage: RemaStage,
  stageIndex: number,
  options?: { categoryLabel?: string; locations?: string[]; detail?: ItemDetailRecord }
): ItemTooltipView {
  const detail = options?.detail
  const flags = stage.flags
  const rare =
    flags !== undefined ? (flags & 0x8000) !== 0 : detail?.f !== undefined ? (detail.f & 2) !== 0 : true
  const ex =
    flags !== undefined ? (flags & 0x4000) !== 0 : detail?.f !== undefined ? (detail.f & 1) !== 0 : true
  const slots = stage.slots ?? detail?.s
  const slotPart = formatSlots(slots)
  const slotLabel = [slotPart, weapon.type].filter(Boolean).join(' ')
  const raceLabel = formatRaces(stage.races ?? detail?.r ?? 510)
  const desc = stage.description ?? detail?.d ?? ''
  const lines = desc ? desc.split('\n').filter((l) => l.trim().length > 0) : []
  const jobs = formatJobs(stage.jobs ?? detail?.j)
  const level = stage.level || detail?.l || 0
  const itemLevel = stage.itemLevel || detail?.il || 0
  const levelJobLabel = level ? `Lv. ${level}${jobs ? ` ${jobs}` : ''}` : jobs
  const itemLevelLabel = itemLevel ? `<ItemLevel:${itemLevel}>` : ''
  const stageStr = `${stageLabel(weapon, stageIndex)} (Stage ${stageIndex + 1} of ${weapon.stages.length})`
  const subtitle = options?.categoryLabel ? `${options.categoryLabel} · ${stageStr}` : stageStr

  return {
    id: stage.id,
    name: weapon.name,
    subtitle,
    rare,
    ex,
    slotLabel,
    raceLabel,
    lines,
    chargeInfo: '',
    levelJobLabel,
    itemLevelLabel,
    suLabel: '',
    locations: options?.locations
  }
}

export function buildOwnedRemaTooltipView(
  owned: OwnedWeapon,
  categoryLabel?: string,
  detail?: ItemDetailRecord
): ItemTooltipView {
  return buildRemaStageTooltipView(owned.weapon, owned.stage, owned.stageIndex, {
    categoryLabel,
    locations: owned.locations.map((l) => (l.count > 1 ? `${l.label} (×${l.count})` : l.label)),
    detail
  })
}

export interface IdMatches {
  ids: number[]
  /** matches before the MAX_ITEM_IDS cap */
  total: number
  truncated: boolean
}

const EMPTY: IdMatches = { ids: [], total: 0, truncated: false }

/**
 * Resolves a search box value to ids. A bare number is an id; otherwise names are matched
 * case-insensitively and ranked exact, then prefix, then substring.
 */
export function findIds(names: Record<string, string>, query: string): IdMatches {
  const q = query.trim().toLowerCase()
  if (!q) return EMPTY
  if (/^\d{1,5}$/.test(q)) return { ids: [Number(q)], total: 1, truncated: false }
  if (q.length < MIN_NAME_QUERY) return EMPTY

  const ranked: Array<[number, number, string]> = []
  for (const [id, name] of Object.entries(names)) {
    const lower = name.toLowerCase()
    const at = lower.indexOf(q)
    if (at === -1) continue
    ranked.push([lower === q ? 0 : at === 0 ? 1 : 2, Number(id), lower])
  }
  ranked.sort((a, b) => a[0] - b[0] || a[2].localeCompare(b[2]) || a[1] - b[1])

  return {
    ids: ranked.slice(0, MAX_ITEM_IDS).map((r) => r[1]),
    total: ranked.length,
    truncated: ranked.length > MAX_ITEM_IDS
  }
}

export function findItemIds(data: ItemData, query: string): IdMatches {
  const names: Record<string, string> = {}
  for (const [id, [name]] of Object.entries(data.items)) names[id] = name
  return findIds(names, query)
}

export function findKeyItemIds(data: ItemData, query: string): IdMatches {
  return findIds(data.keyItems, query)
}

export function ffxiahUrl(itemId: number): string {
  return `https://www.ffxiah.com/item/${itemId}`
}

export function bgWikiUrl(name: string): string {
  return `https://www.bg-wiki.com/ffxi/${encodeURIComponent(name.replace(/ /g, '_'))}`
}
