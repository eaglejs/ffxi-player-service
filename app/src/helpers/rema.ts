import { bagLabel, compareBags } from '@/helpers/bags'
import type {
  CharacterSummary,
  InventorySearchResponse,
  ResultLocation
} from '@/types/Inventory'

export interface RemaStage {
  id: number
  level: number
  itemLevel: number
}

export interface RemaWeapon {
  name: string
  type: string
  /** Upgrade stages in order: level, then item level, then id. */
  stages: RemaStage[]
}

export interface RemaCategory {
  key: string
  label: string
  weapons: RemaWeapon[]
}

export interface RemaData {
  categories: RemaCategory[]
}

export interface OwnedWeapon {
  weapon: RemaWeapon
  /** Highest stage the character holds. */
  stageIndex: number
  stage: RemaStage
  /** How many different stages of this weapon the character holds. */
  stagesHeld: number
  locations: ResultLocation[]
}

export interface PlayerCategory {
  key: string
  label: string
  owned: OwnedWeapon[]
  missing: RemaWeapon[]
  total: number
}

export interface PlayerRema {
  playerId: number
  playerName: string
  updatedAt: string | null
  categories: PlayerCategory[]
  ownedTotal: number
  weaponTotal: number
}

export function categoryItemIds(category: RemaCategory): number[] {
  return category.weapons.flatMap((w) => w.stages.map((s) => s.id))
}

/** Short tile badge: the item level when there is one (99 -> 119), otherwise the level. */
export function stageBadge(stage: RemaStage): string {
  return String(stage.itemLevel > 0 ? stage.itemLevel : stage.level)
}

const ROMAN = ['', ' II', ' III', ' IV', ' V', ' VI']

/** "Lv 99", "Lv 99 II", "iL 119 III": repeats at the same level are numbered in stage order. */
export function stageLabel(weapon: RemaWeapon, stageIndex: number): string {
  const stage = weapon.stages[stageIndex]
  if (!stage) return ''
  const repeats = weapon.stages
    .slice(0, stageIndex)
    .filter((s) => s.level === stage.level && s.itemLevel === stage.itemLevel).length
  const base = stage.itemLevel > 0 ? `iL ${stage.itemLevel}` : `Lv ${stage.level}`
  return base + (ROMAN[repeats] ?? ` #${repeats + 1}`)
}

interface StageRef {
  category: string
  weaponIndex: number
  stageIndex: number
}

/**
 * Combines the character list with one search response per category into per-player ownership.
 * Every listed character appears, including those who own none of these weapons.
 */
export function buildRemaOverview(
  data: RemaData,
  characters: CharacterSummary[],
  responses: Record<string, InventorySearchResponse>
): PlayerRema[] {
  const refs = new Map<number, StageRef>()
  for (const category of data.categories) {
    category.weapons.forEach((weapon, weaponIndex) =>
      weapon.stages.forEach((stage, stageIndex) =>
        refs.set(stage.id, { category: category.key, weaponIndex, stageIndex })
      )
    )
  }

  const weaponTotal = data.categories.reduce((n, c) => n + c.weapons.length, 0)

  const overview = characters.map((character): PlayerRema => {
    const categories = data.categories.map((category): PlayerCategory => {
      const result = responses[category.key]?.results.find((r) => r.playerId === character.playerId)
      // held[weaponIndex][stageIndex] = locations of that stage
      const held = new Map<number, Map<number, ResultLocation[]>>()
      const onlyId = category.weapons.length === 1 && category.weapons[0]!.stages.length === 1
        ? category.weapons[0]!.stages[0]!.id
        : undefined

      for (const location of result?.locations ?? []) {
        const id = location.itemId ?? onlyId
        const ref = id === undefined ? undefined : refs.get(id)
        if (!ref || ref.category !== category.key) continue
        const stages = held.get(ref.weaponIndex) ?? new Map<number, ResultLocation[]>()
        const locations = stages.get(ref.stageIndex) ?? []
        locations.push({ bag: location.bag, label: bagLabel(location.bag), count: location.count })
        stages.set(ref.stageIndex, locations)
        held.set(ref.weaponIndex, stages)
      }

      const owned: OwnedWeapon[] = []
      const missing: RemaWeapon[] = []
      category.weapons.forEach((weapon, weaponIndex) => {
        const stages = held.get(weaponIndex)
        if (!stages) {
          missing.push(weapon)
          return
        }
        const stageIndex = Math.max(...stages.keys())
        owned.push({
          weapon,
          stageIndex,
          stage: weapon.stages[stageIndex]!,
          stagesHeld: stages.size,
          locations: [...stages.get(stageIndex)!].sort((a, b) => compareBags(a.bag, b.bag))
        })
      })
      return { key: category.key, label: category.label, owned, missing, total: category.weapons.length }
    })

    return {
      playerId: character.playerId,
      playerName: character.playerName,
      updatedAt: character.updatedAt,
      categories,
      ownedTotal: categories.reduce((n, c) => n + c.owned.length, 0),
      weaponTotal
    }
  })

  return overview.sort((a, b) => b.ownedTotal - a.ownedTotal || a.playerName.localeCompare(b.playerName))
}
