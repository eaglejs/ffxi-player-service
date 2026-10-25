export const KEY_ITEMS_BAG = 'keyItems'

const MAIN_BAGS: Record<string, string> = {
  inventory: 'Inventory',
  safe: 'Mog Safe',
  safe2: 'Mog Safe 2',
  storage: 'Storage',
  locker: 'Mog Locker',
  satchel: 'Mog Satchel',
  sack: 'Mog Sack',
  case: 'Mog Case',
  recycle: 'Recycle Bin',
  temporary: 'Temporary Items',
  [KEY_ITEMS_BAG]: 'Key Items'
}

/** Friendly name for a findAll bag key; unknown bags fall back to the raw key. */
export function bagLabel(bag: string): string {
  if (MAIN_BAGS[bag]) return MAIN_BAGS[bag]
  const wardrobe = /^wardrobe(\d*)$/.exec(bag)
  if (wardrobe) return wardrobe[1] ? `Mog Wardrobe ${wardrobe[1]}` : 'Mog Wardrobe'
  const slip = /^slip (\d+)$/.exec(bag)
  if (slip) return `Porter Slip ${slip[1]}`
  return bag
}

/** Sort position: carried bags first, then storage, wardrobes, slips, key items. */
export function bagOrder(bag: string): number {
  const order = ['inventory', 'wardrobe', 'safe', 'safe2', 'storage', 'locker', 'satchel', 'sack', 'case']
  const known = order.indexOf(bag)
  if (known > -1) return known
  const wardrobe = /^wardrobe(\d+)$/.exec(bag)
  if (wardrobe) return 1 + Number(wardrobe[1]) / 100
  if (bag.startsWith('slip ')) return 100
  if (bag === KEY_ITEMS_BAG) return 101
  return 50
}

export function compareBags(a: string, b: string): number {
  return bagOrder(a) - bagOrder(b) || a.localeCompare(b)
}
