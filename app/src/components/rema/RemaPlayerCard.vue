<template>
  <section class="card rema-card" :aria-label="`${player.playerName} REMA weapons`">
    <div class="card-header d-flex flex-wrap justify-content-between align-items-baseline gap-1">
      <h2 class="mb-0 char-name">
        <McIcon :path="mdiSwordCross" :size="20" />
        {{ player.playerName }}
      </h2>
      <span class="meta">
        <strong>{{ player.ownedTotal }}</strong> of {{ player.weaponTotal }} weapons
        <template v-if="updated"> · synced {{ updated }}</template>
      </span>
    </div>

    <div class="card-body">
      <p v-if="!ownedInView" class="none empty">{{ emptyText }}</p>

      <section v-for="category in sections" :key="category.key" class="category">
        <h3 class="category-head">
          <span>{{ category.label }}</span>
          <span class="count" :class="{ done: category.owned.length === category.total }">
            {{ category.owned.length }}/{{ category.total }}
          </span>
        </h3>
        <div class="progress" aria-hidden="true">
          <div
            class="progress-bar"
            :style="{ width: `${(category.owned.length / category.total) * 100}%` }"
          />
        </div>

        <ul v-if="category.owned.length" class="weapons">
          <li
            v-for="owned in category.owned"
            :key="owned.weapon.name"
            class="weapon"
            :title="tooltip(owned)"
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
          <p>{{ category.missing.map((w) => w.name).join(', ') }}</p>
        </details>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mdiSwordCross } from '@mdi/js'
import { stageBadge, stageLabel, type OwnedWeapon, type PlayerRema } from '@/helpers/rema'
import { timeAgo } from '@/helpers/time'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import McIcon from '@/components/inventory/McIcon.vue'

const props = defineProps<{ player: PlayerRema; categoryKeys?: string[] }>()

const updated = computed(() => timeAgo(props.player.updatedAt))
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

function tooltip(owned: OwnedWeapon): string {
  const where = owned.locations.map((l) => l.label).join(', ')
  const stage = `${stageLabel(owned.weapon, owned.stageIndex)} (stage ${owned.stageIndex + 1} of ${owned.weapon.stages.length})`
  return `${owned.weapon.name}, ${owned.weapon.type} · ${stage} · ${where}`
}
</script>

<style scoped lang="scss">
.char-name {
  align-items: center;
  display: inline-flex;
  font-size: 1.25rem;
  gap: 0.4rem;
}

.meta {
  font-size: 0.85rem;
  opacity: 0.85;
}

.category {
  & + & {
    border-top: 1px solid var(--bs-border-color);
    margin-top: 0.75rem;
    padding-top: 0.75rem;
  }
}

.category-head {
  align-items: baseline;
  display: flex;
  font-size: 0.9rem;
  font-weight: 600;
  justify-content: space-between;
  letter-spacing: 0.04em;
  margin: 0 0 0.3rem;
  text-transform: uppercase;
}

.count {
  color: var(--bs-secondary-color);
  font-variant-numeric: tabular-nums;

  &.done {
    color: var(--bs-success);
    font-weight: 600;
  }
}

.progress {
  height: 4px;
  margin-bottom: 0.75rem;
}

.weapons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 0.5rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.weapon {
  align-items: center;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  width: 96px;
}

.icon-wrap {
  position: relative;
}

.badge {
  font-size: 0.65rem;
  font-variant-numeric: tabular-nums;
  position: absolute;
  right: -8px;
  top: -6px;
}

.name {
  font-size: 0.75rem;
  line-height: 1.2;
  text-align: center;
  word-break: break-word;
}

.none {
  color: var(--bs-secondary-color);
  font-size: 0.85rem;
  margin: 0.1rem 0 0.4rem;

  &.empty {
    margin: 0;
  }
}

.missing {
  color: var(--bs-secondary-color);
  font-size: 0.8rem;
  margin-top: 0.5rem;

  summary {
    cursor: pointer;
  }

  p {
    margin: 0.3rem 0 0;
  }
}
</style>
