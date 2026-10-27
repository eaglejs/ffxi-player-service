<template>
  <Teleport to="body">
    <div
      v-if="tooltip"
      ref="tooltipEl"
      class="card item-hover-tooltip"
      role="tooltip"
      :style="{ left: `${position.left}px`, top: `${position.top}px` }"
    >
      <div class="card-header tooltip-head">
        <div class="head-main">
          <ItemIcon :id="tooltip.id" :kind="kind" :tile="38" />
          <div class="title-wrap">
            <div class="item-title">{{ tooltip.name }}</div>
            <div v-if="tooltip.subtitle" class="item-subtitle">{{ tooltip.subtitle }}</div>
          </div>
        </div>
        <div v-if="tooltip.rare || tooltip.ex" class="flags">
          <span v-if="tooltip.rare" class="flag-badge flag-rare">Rare</span>
          <span v-if="tooltip.ex" class="flag-badge flag-ex">Ex</span>
        </div>
      </div>

      <div class="card-body tooltip-body">
        <div v-if="tooltip.slotLabel || tooltip.raceLabel" class="slot-row">
          <span v-if="tooltip.slotLabel" class="slot-text">{{ tooltip.slotLabel }}</span>
          <span v-if="tooltip.raceLabel" class="race-text">{{ tooltip.raceLabel }}</span>
        </div>

        <div v-if="tooltip.lines.length" class="stats-block">
          <div
            v-for="(line, idx) in tooltip.lines"
            :key="idx"
            class="stat-line"
            :class="statLineClass(line, idx)"
          >
            {{ line }}
          </div>
        </div>

        <div v-if="tooltip.chargeInfo" class="charge-line">
          {{ tooltip.chargeInfo }}
        </div>

        <div
          v-if="tooltip.levelJobLabel || tooltip.itemLevelLabel || tooltip.suLabel"
          class="req-row"
        >
          <span v-if="tooltip.levelJobLabel" class="level-jobs">{{ tooltip.levelJobLabel }}</span>
          <span class="ilvl-wrap">
            <span v-if="tooltip.suLabel" class="su-badge">{{ tooltip.suLabel }}</span>
            <span v-if="tooltip.itemLevelLabel" class="ilvl-badge">{{
              tooltip.itemLevelLabel
            }}</span>
          </span>
        </div>
      </div>

      <div v-if="tooltip.locations?.length" class="card-footer tooltip-foot">
        <span class="foot-label"><b>Held in:</b></span>
        <span v-for="loc in tooltip.locations" :key="loc" class="loc-pill">{{ loc }}</span>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ItemIcon from '@/components/inventory/ItemIcon.vue'
import type { ItemTooltipView } from '@/helpers/items'

const props = withDefaults(
  defineProps<{
    tooltip: ItemTooltipView | null
    x: number
    y: number
    kind?: 'item' | 'key'
  }>(),
  { kind: 'item' }
)

const tooltipEl = ref<HTMLElement | null>(null)

function statLineClass(line: string, idx: number) {
  return {
    'stat-primary': idx === 0 && /^(DMG:|DEF:)/.test(line),
    'stat-special':
      /^(Aftermath:|Afterglow|Ultimate Skillchain|Latent effect:|Unity Ranking:)/.test(line),
    'stat-ws': /^".*"$/.test(line.trim())
  }
}

const OFFSET = 16
const DEFAULT_WIDTH = 330
const DEFAULT_HEIGHT = 240
const MARGIN = 12

const position = computed(() => {
  const vw = typeof window !== 'undefined' ? window.innerWidth : 1280
  const vh = typeof window !== 'undefined' ? window.innerHeight : 800
  const rect = tooltipEl.value?.getBoundingClientRect()
  const w = rect?.width || DEFAULT_WIDTH
  const h = rect?.height || DEFAULT_HEIGHT

  let left = props.x + OFFSET
  let top = props.y + OFFSET

  if (left + w + MARGIN > vw) {
    left = Math.max(MARGIN, props.x - w - OFFSET)
  }
  if (top + h + MARGIN > vh) {
    top = Math.max(MARGIN, props.y - h - OFFSET)
  }

  return { left, top }
})
</script>

<style scoped lang="scss">
.item-hover-tooltip {
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.35);
  color: var(--bs-body-color);
  max-width: 350px;
  min-width: 250px;
  pointer-events: none;
  position: fixed;
  z-index: 2050;
}

.tooltip-head {
  align-items: flex-start;
  display: flex;
  gap: 0.6rem;
  justify-content: space-between;
  padding: 0.55rem 0.75rem;
}

.head-main {
  align-items: center;
  display: flex;
  gap: 0.55rem;
  min-width: 0;
}

.title-wrap {
  min-width: 0;
}

.item-title {
  font-size: 0.96rem;
  font-weight: 700;
  line-height: 1.2;
}

.item-subtitle {
  color: var(--bs-secondary-color);
  font-size: 0.74rem;
  margin-top: 0.1rem;
}

.flags {
  display: inline-flex;
  flex: none;
  gap: 0.25rem;
}

.flag-badge {
  border-radius: 4px;
  font-size: 0.66rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  line-height: 1.2;
  padding: 0.12rem 0.38rem;
  text-transform: uppercase;

  &.flag-rare {
    background-color: rgba(233, 164, 0, 0.18);
    border: 1px solid rgba(233, 164, 0, 0.55);
    color: #d49200;
  }

  &.flag-ex {
    background-color: rgba(57, 132, 250, 0.18);
    border: 1px solid rgba(57, 132, 250, 0.55);
    color: #3984fa;
  }
}

[data-bs-theme='dark'] {
  .flag-badge.flag-rare {
    color: #ffc107;
  }
  .flag-badge.flag-ex {
    color: #6ea8fe;
  }
}

.tooltip-body {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  padding: 0.6rem 0.75rem;
}

.slot-row {
  align-items: center;
  border-bottom: 1px solid var(--bs-border-color);
  color: var(--bs-secondary-color);
  display: flex;
  font-size: 0.76rem;
  font-weight: 600;
  justify-content: space-between;
  padding-bottom: 0.35rem;
}

.stats-block {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 6px;
  font-family:
    'Roboto',
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  font-size: 0.81rem;
  line-height: 1.42;
  padding: 0.45rem 0.6rem;
}

.stat-line {
  &.stat-primary {
    font-weight: 600;
  }

  &.stat-ws {
    color: var(--bs-primary);
    font-weight: 600;
  }

  &.stat-special {
    color: #4a9c58;
    font-weight: 500;
  }
}

[data-bs-theme='dark'] {
  .stat-line.stat-ws {
    color: #6ea8fe;
  }
  .stat-line.stat-special {
    color: #75b798;
  }
}

.charge-line {
  color: var(--bs-secondary-color);
  font-family: monospace;
  font-size: 0.76rem;
}

.req-row {
  align-items: baseline;
  border-top: 1px solid var(--bs-border-color);
  display: flex;
  flex-wrap: wrap;
  font-size: 0.76rem;
  gap: 0.35rem 0.6rem;
  justify-content: space-between;
  padding-top: 0.35rem;
}

.level-jobs {
  font-weight: 600;
}

.ilvl-wrap {
  align-items: center;
  display: inline-flex;
  gap: 0.35rem;
  margin-left: auto;
}

.su-badge {
  background-color: rgba(138, 25, 188, 0.15);
  border: 1px solid rgba(138, 25, 188, 0.45);
  border-radius: 4px;
  color: #8a19bc;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.02rem 0.35rem;
}

[data-bs-theme='dark'] {
  .su-badge {
    color: #c286e8;
  }
}

.ilvl-badge {
  color: var(--bs-secondary-color);
  font-family: monospace;
  font-size: 0.75rem;
  font-weight: 600;
}

.tooltip-foot {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  font-size: 0.75rem;
  gap: 0.3rem;
  padding: 0.4rem 0.75rem;
}

.foot-label {
  margin-right: 0.15rem;
}

.loc-pill {
  background-color: var(--bs-secondary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: 999px;
  font-size: 0.72rem;
  padding: 0.05rem 0.5rem;
}
</style>
