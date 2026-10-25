<template>
  <GameWindow :label="`${player.playerName} REMA weapons`">
    <template #header>
      <span class="char-name">
        <McIcon :path="mdiSwordCross" :size="18" />
        {{ player.playerName }}
      </span>
      <span class="meta">
        <strong>{{ player.ownedTotal }}</strong> of {{ player.weaponTotal }} weapons
        <template v-if="updated"> · synced {{ updated }}</template>
      </span>
    </template>

    <p v-if="!ownedInView" class="none empty">{{ emptyText }}</p>

    <section v-for="category in sections" :key="category.key" class="category">
      <h3 class="category-head">
        <span>{{ category.label }}</span>
        <span class="count" :class="{ done: category.owned.length === category.total }">
          {{ category.owned.length }}/{{ category.total }}
        </span>
      </h3>
      <div class="progress" aria-hidden="true">
        <div class="fill" :style="{ width: `${(category.owned.length / category.total) * 100}%` }" />
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
            <span class="badge">{{ stageBadge(owned.stage) }}</span>
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
  </GameWindow>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { mdiSwordCross } from '@mdi/js'
import { stageBadge, stageLabel, type OwnedWeapon, type PlayerRema } from '@/helpers/rema'
import { timeAgo } from '@/helpers/time'
import GameWindow from '@/components/inventory/GameWindow.vue'
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
  color: #f3e3b3;
  font-size: 1.15rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.meta {
  color: rgb(255 255 255 / 65%);
  font-size: 0.85rem;
}

.category {
  padding: 0.65rem 0.25rem 0.1rem;

  & + & {
    border-top: 1px solid rgb(255 255 255 / 8%);
    margin-top: 0.4rem;
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
  color: rgb(255 255 255 / 65%);
  font-variant-numeric: tabular-nums;

  &.done {
    color: #f3e3b3;
  }
}

.progress {
  background: rgb(255 255 255 / 10%);
  border-radius: 999px;
  height: 3px;
  margin-bottom: 0.6rem;
  overflow: hidden;
}

.fill {
  background: linear-gradient(90deg, #c9b27c, #f3e3b3);
  height: 100%;
  transition: width 0.4s ease;
}

.weapons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem 0.4rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.weapon {
  align-items: center;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  width: 96px;
}

.icon-wrap {
  position: relative;
}

.badge {
  background: #031633;
  border: 1px solid #c9b27c;
  border-radius: 999px;
  color: #f3e3b3;
  font-size: 0.65rem;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
  padding: 0 5px;
  position: absolute;
  right: -6px;
  top: -6px;
}

.name {
  color: rgb(255 255 255 / 85%);
  font-size: 0.72rem;
  line-height: 1.2;
  text-align: center;
  word-break: break-word;
}

.none {
  color: rgb(255 255 255 / 45%);
  font-size: 0.85rem;
  margin: 0.1rem 0 0.4rem;

  &.empty {
    margin: 0.9rem 0.25rem 0.5rem;
  }
}

.missing {
  color: rgb(255 255 255 / 55%);
  font-size: 0.78rem;
  margin: 0.5rem 0 0.2rem;

  summary {
    cursor: pointer;
  }

  p {
    margin: 0.3rem 0 0;
  }
}
</style>
