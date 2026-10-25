# app

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vitejs.dev/config/).

## Project Setup

```sh
npm install
```

### Compile and Hot-Reload for Development

```sh
npm run dev
```

### Type-Check, Compile and Minify for Production

```sh
npm run build
```

### Run Unit Tests with [Vitest](https://vitest.dev/)

```sh
npm run test:unit
```

### Lint with [ESLint](https://eslint.org/)

```sh
npm run lint
```

## Inventory search (hidden)

A search over every character's stored inventory, hidden until unlocked. Type the passphrase anywhere on the site (outside an input), or tap the logo 7 times quickly on a phone to get a prompt. A magnifier button then appears in the nav (shortcut `3`) and the unlock is remembered in the browser until you press **Lock**. Only a hash of the passphrase is in the code, and the API endpoints themselves are not protected.

- Search by item name or id. "Include key items" searches key item names separately. Click a character to browse all of their bags.
- Item icons are served by the backend (`/api/items/icon/<id>.png`), which stores each one permanently after the PlayerService extracts it from the game. Until an icon exists a placeholder is shown.
- Names come from `src/assets/item-data.json`, built from Windower's resources and loaded only when the page is opened. Regenerate it after a game update:

```sh
npm run build:item-data -- --resources "<path to Windower/res>"   # or omit --resources to download from GitHub
```

## REMA weapons

Open to everyone (nav button, shortcut `4`). Shows every character with a synced inventory and which Relic, Empyrean, Mythic, Aeonic and Prime weapons they hold, with the highest upgrade stage as a badge and the missing ones listed per category.

- Data: one search per category against the inventory API, plus `GET /player/inventory/characters` so characters with none of these weapons still appear.
- The weapon list lives in `scripts/data/rema-names.json` (checked against BG Wiki; Prime weapons are found by their "Prime " name). Stage item ids come from Windower's `items.lua`. Rebuild after a game update, or to add a new weapon:

```sh
npm run build:rema-data -- --resources "<path to Windower/res>"   # or omit --resources to download from GitHub
```

- Stage labels ("Lv 99 II", "iL 119") come from the item data's level, item level and id order, not from in-game stage names; tooltips also show "stage k of n".
