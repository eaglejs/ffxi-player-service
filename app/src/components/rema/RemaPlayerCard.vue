<template>
  <section class="card rema-card" :aria-label="`${player.playerName} REMA weapons`">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
      <h2 class="mb-0 char-name">
        <McIcon :path="mdiSwordCross" :size="20" class="char-icon" />
        {{ player.playerName }}
      </h2>
      <span class="meta">
        <strong>{{ player.ownedTotal }}</strong> of {{ player.weaponTotal }} weapons
        <template v-if="updated"> · synced {{ updated }}</template>
      </span>
    </div>

    <div class="card-body">
      <p v-if="!ownedInView" class="none empty">{{ emptyText }}</p>

      <section
        v-for="category in sections"
        :key="category.key"
        class="category"
        :class="`cat-${category.key}`"
      >
        <h3 class="category-head">
          <span class="category-label">
            <span class="category-dot" :class="`dot-${category.key}`" />
            {{ category.label }}
          </span>
          <span class="count" :class="{ done: category.owned.length === category.total }">
            {{ category.owned.length }}/{{ category.total }}
          </span>
        </h3>
        <div class="progress" aria-hidden="true">
          <div
            class="progress-bar"
            :class="`bar-${category.key}`"
            :style="{ width: `${(category.owned.length / category.total) * 100}%` }"
          />
        </div>

        <ul v-if="category.owned.length" class="weapons">
          <li
            v-for="owned in category.owned"
            :key="owned.weapon.name"
            class="weapon"
            :title="hoveredKey === `${category.key}-${owned.weapon.name}` ? undefined : tooltip(owned)"
            @mouseenter="onHoverOwned(owned, category.label, `${category.key}-${owned.weapon.name}`, $event)"
            @mousemove="onMouseMove"
            @mouseleave="onMouseLeave"
          >
            <span class="icon-wrap">
              <ItemIcon :id="owned.stage.id" :tile="44" />
              <span class="badge rounded-pill text-bg-primary">{{ stageBadge(owned.stage) }}</span>
            </span>
            <span class="name">{{ owned.weapon.name }}</span>
            <span class="visually-hidden">{{ tooltip(owned) }}</span>
          </li>
        </ul>
        <p v-else class="none">None yet</p>

        <details v-if="category.missing.length && category.owned.length" class="missing">
          <summary>Missing ({{ category.missing.length }})</summary>
          <p>
            <span
              v-for="(w, idx) in category.missing"
              :key="w.name"
              class="missing-item"
              @mouseenter="onHoverMissing(w, category.label, `${category.key}-missing-${w.name}`, $event)"
              @mousemove="onMouseMove"
              @mouseleave="onMouseLeave"
            >{{ w.name }}<template v-if="idx < category.missing.length - 1">, </template></span>
          </p>
        </details>
      </section>
    </div>

    <div class="card-footer">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 small">
        <span>
          <b class="me-1">Completion:</b>
          {{ completionPercent }}% ({{ player.ownedTotal }}/{{ player.weaponTotal }})
        </span>
        <span v-if="updated" class="text-secondary">Synced {{ updated }}</span>
      </div>
    </div>

    <ItemHoverTooltip :tooltip="activeTooltip" :x="mouseX" :y="mouseY" />
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { mdiSwordCross } from '@mdi/js'
import {
  stageBadge,
  stageLabel,
  type OwnedWeapon,
  type PlayerRema,
  type RemaWeapon
} from '@/helpers/rema'
import {
  buildOwnedRemaTooltipView,
  buildRemaStageTooltipView,
  type ItemTooltipView
} from '@/helpers/items'
import { timeAgo } from '@/helpers/time'
import ItemHoverTooltip from '@/components/inventory/ItemHoverTooltip.vue'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'

const props = defineProps<{ player: PlayerRema; categoryKeys?: string[] }>()

const updated = computed(() => timeAgo(props.player.updatedAt))
const completionPercent = computed(() =>
  props.player.weaponTotal > 0
    ? Math.round((props.player.ownedTotal / props.player.weaponTotal) * 100)
    : 0
)
const visibleCategories = computed(() =>
  props.categoryKeys?.length
    ? props.player.categories.filter((c) => props.categoryKeys!.includes(c.key))
    : props.player.categories
)

const ownedInView = computed(() => visibleCategories.value.some((c) => c.owned.length > 0))
const emptyText = computed(() =>
  props.categoryKeys?.length ? 'None in the selected categories' : 'No REMA weapons yet'
)
// A character with nothing in view gets one line instead of a column of empty sections.
const sections = computed(() => (ownedInView.value ? visibleCategories.value : []))

const activeTooltip = ref<ItemTooltipView | null>(null)
const hoveredKey = ref<string | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)

function onHoverOwned(owned: OwnedWeapon, categoryLabel: string, key: string, e: MouseEvent) {
  hoveredKey.value = key
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  activeTooltip.value = buildOwnedRemaTooltipView(owned, categoryLabel)
}

function onHoverMissing(weapon: RemaWeapon, categoryLabel: string, key: string, e: MouseEvent) {
  const lastIndex = weapon.stages.length - 1
  const stage = weapon.stages[lastIndex]
  if (!stage) return
  hoveredKey.value = key
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  activeTooltip.value = buildRemaStageTooltipView(weapon, stage, lastIndex, {
    categoryLabel,
    locations: ['Not yet acquired']
  })
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function onMouseLeave() {
  hoveredKey.value = null
  activeTooltip.value = null
}

function tooltip(owned: OwnedWeapon): string {
  const where = owned.locations.map((l) => l.label).join(', ')
  const stage = `${stageLabel(owned.weapon, owned.stageIndex)} (stage ${owned.stageIndex + 1} of ${owned.weapon.stages.length})`
  return `${owned.weapon.name}, ${owned.weapon.type} · ${stage} · ${where}`
}
</script>

<style scoped lang="scss">
.rema-card {
  height: 100%;
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.char-name {
  align-items: center;
  display: inline-flex;
  font-size: 1.25rem;
  gap: 0.45rem;
}

.char-icon {
  color: var(--bs-secondary-color);
}

.meta {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.8rem;
  padding: 0.15rem 0.65rem;

  strong {
    color: var(--bs-body-color);
  }
}

.category {
  & + & {
    border-top: 1px solid var(--bs-border-color);
    margin-top: 0.9rem;
    padding-top: 0.9rem;
  }
}

.category-head {
  align-items: center;
  display: flex;
  font-size: 0.85rem;
  font-weight: 600;
  justify-content: space-between;
  letter-spacing: 0.04em;
  margin: 0 0 0.35rem;
  text-transform: uppercase;
}

.category-label {
  align-items: center;
  display: inline-flex;
  gap: 0.4rem;
}

.category-dot {
  border-radius: 50%;
  display: inline-block;
  height: 9px;
  width: 9px;

  &.dot-relic {
    background-color: #e9a400;
  }
  &.dot-empyrean {
    background-color: #3984fa;
  }
  &.dot-mythic {
    background-color: #8a19bc;
  }
  &.dot-aeonic {
    background-color: #4a9c58;
  }
  &.dot-prime {
    background-color: #ff6384;
  }
}

.count {
  background-color: var(--bs-secondary-bg);
  border-radius: 999px;
  color: var(--bs-secondary-color);
  font-size: 0.75rem;
  font-variant-numeric: tabular-nums;
  padding: 0.05rem 0.5rem;

  &.done {
    background-color: var(--bs-success-bg-subtle);
    color: var(--bs-success-text-emphasis);
    font-weight: 600;
  }
}

.progress {
  border-radius: 999px;
  height: 6px;
  margin-bottom: 0.85rem;
  overflow: hidden;

  .progress-bar {
    border-radius: 999px;
    transition: width 0.3s ease;

    &.bar-relic {
      background-color: #e9a400;
    }
    &.bar-empyrean {
      background-color: #3984fa;
    }
    &.bar-mythic {
      background-color: #8a19bc;
    }
    &.bar-aeonic {
      background-color: #4a9c58;
    }
    &.bar-prime {
      background-color: #ff6384;
    }
  }
}

.weapons {
  display: grid;
  gap: 0.55rem;
  grid-template-columns: repeat(auto-fill, minmax(96px, 1fr));
  list-style: none;
  margin: 0;
  padding: 0;
}

.weapon {
  align-items: center;
  background-color: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 0.55rem 0.35rem 0.45rem;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    transform 0.15s ease;

  &:hover {
    border-color: var(--bs-primary);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
    transform: translateY(-2px);
  }
}

.icon-wrap {
  position: relative;
}

.badge {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  font-size: 0.65rem;
  font-variant-numeric: tabular-nums;
  position: absolute;
  right: -8px;
  top: -6px;
}

.name {
  font-size: 0.78rem;
  font-weight: 500;
  line-height: 1.2;
  text-align: center;
  word-break: break-word;
}

.none {
  color: var(--bs-secondary-color);
  font-size: 0.85rem;
  font-style: italic;
  margin: 0.1rem 0 0.4rem;

  &.empty {
    font-style: normal;
    margin: 0;
    padding: 0.5rem 0;
    text-align: center;
  }
}

.missing {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 6px;
  color: var(--bs-secondary-color);
  font-size: 0.8rem;
  margin-top: 0.65rem;
  padding: 0.35rem 0.65rem;

  summary {
    cursor: pointer;
    font-weight: 500;
    user-select: none;

    &:hover {
      color: var(--bs-body-color);
    }
  }

  p {
    border-top: 1px solid var(--bs-border-color);
    line-height: 1.45;
    margin: 0.4rem 0 0.1rem;
    padding-top: 0.4rem;
  }

  .missing-item {
    cursor: help;
    transition: color 0.15s ease;

    &:hover {
      color: var(--bs-primary);
      text-decoration: underline;
    }
  }
}
</style>
