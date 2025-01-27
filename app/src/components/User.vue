<template>
  <RouterLink :to="`/users/${user?.playerId}`">
    <div class="card user-card" :class="{'user-btn-animations': isNotPlayerDetailsPage}" >
      <svg ref="deadElement" class="dead" viewBox="0 0 25 25" width="30" height="30">
        <path class="skull-fill" :d="mdiSkullCrossbones"></path>
      </svg>
      <div class="card-header">
        <div class="d-flex justify-content-between">
          <h2 class="mb-0">
            <OnlineDot :user="user" />
            {{ playerName }} - M. lvl:
            {{ user?.masterLevel }} ({{ user?.mainJob }}{{ user?.mainJobLevel }}/{{ user?.subJob
            }}{{ user?.subJobLevel }})
          </h2>
        </div>
      </div>
      <div class="card-body" :class="{ 'dead-mask': dead }">
        <div class="row mb-2">
          <section class="col-8">
            <section class="row">
              <section class="col-12">
                <p class="mb-0"><b>Exemplar</b></p>
                <div class="progress mt-1">
                  <div
                    class="progress-bar"
                    :class="{
                      'progress-bar-animated': exemplarProgressRounded < 100,
                      'progress-bar-striped': exemplarProgressRounded < 100
                    }"
                    role="progressbar"
                    :style="{ width: exemplarProgress + '%' }"
                    aria-valuenow="25"
                    aria-valuemin="0"
                    aria-valuemax="100"
                  >
                    {{ exemplarProgressRounded }}%
                  </div>
                </div>
                <p class="mb-2 text-center">{{ currentExemplar }} / {{ requiredExemplar }}</p>
              </section>
            </section>
            <div v-if="playerBuffs.size">
              <Buffs :player="player" :buff-data="playerBuffs" />
            </div>
          </section>
          <section class="col-4">
            <p class="mb-0"><b>HP</b></p>
            <div class="progress mb-1 mt-1">
              <div
                class="progress-bar bg-danger"
                role="progressbar"
                :style="{ width: user?.hpp + '%' }"
                :aria-valuenow="user?.tp"
                aria-valuemin="0"
                aria-valuemax="100"
              >
                {{ user?.hpp }}%
              </div>
            </div>
            <p class="mb-0"><b>MP</b></p>
            <div class="progress mb-1 mt-1">
              <div
                class="progress-bar bg-success"
                role="progressbar"
                :style="{ width: user?.mpp + '%' }"
                :aria-valuenow="user?.tp"
                aria-valuemin="0"
                aria-valuemax="100"
              >
                {{ user?.mpp }}%
              </div>
            </div>
            <p class="mb-0"><b>TP</b></p>
            <div class="progress mb-1 mt-1">
              <div
                class="progress-bar bg-purple"
                role="progressbar"
                :style="{ width: getTP }"
                :aria-valuenow="user?.tp"
                aria-valuemin="0"
                aria-valuemax="3000"
              >
                {{ user?.tp }}
              </div>
            </div>
          </section>
        </div>
      </div>
      <div class="card-footer">
        <section>
          <div class="d-flex">
            <span class="me-3"><b class="me-1">Attack:</b>{{ user?.attack }}</span>
            <span class="me-auto"><b class="me-1">Defense:</b>{{ user?.defense }}</span>
            <span>{{ user?.zone }}</span>
          </div>
        </section>
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import { computed, ref, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { mdiSkullCrossbones } from '@mdi/js'
import Buffs from '@/components/Buffs.vue'
import type { ComputedRef } from 'vue'
import type { Ability } from '@/types/Ability'
import type { Buff } from '@/types/buff'
import { useUserStore } from '@/stores/user'
import type { Player } from '@/types/Player'
import OnlineDot from '@/components/OnlineDot.vue'

const props = defineProps({
  playerId: Number
})

const userStore = useUserStore()
const user = computed<Player>(() => userStore.players.get(props.playerId ?? 0))
const currentExemplar = computed(() => user.value?.currentExemplar.toLocaleString())
const exemplarProgressRounded = computed(() => Math.floor(exemplarProgress.value))
const playerBuffs: ComputedRef<Map<string, Buff>> = computed(() => {
  const buffs = user?.value?.buffs
  return buffs ? new Map(Object.entries(buffs)) : new Map()
})
const player = computed(() => {
  return {
    playerId: user?.value?.playerId,
    playerName: user?.value?.playerName
  }
})
const playerName = computed(
  () => user?.value?.playerName.charAt(0).toUpperCase() + user?.value?.playerName.slice(1)
)
const requiredExemplar = computed(() => user?.value?.requiredExemplar.toLocaleString())
const themeColor = computed(() => (themeStore.theme === 'dark' ? '#fff' : '#000'))
const getTP = computed(() => (user?.value?.tp / 3000) * 100 + '%')
const dead = computed(() => user?.value?.status == 2)
const isNotPlayerDetailsPage = computed(() => !window.location.pathname.includes('users'))
const exemplarProgress = computed(() => {
  if (user?.value?.requiredExemplar - user?.value?.currentExemplar <= 1) {
    return 100
  } else {
    return (user?.value?.currentExemplar / user?.value?.requiredExemplar) * 100
  }
})
const themeStore = useThemeStore()
const playerAbilities = ref([] as Ability[])
const deadElement = ref()

const filterAbilties = () => {
  let abilities =
    typeof user?.value?.abilities === 'string'
      ? JSON.parse(user?.value?.abilities)
      : user?.value?.abilities
  let filteredAbilities: Ability[] = []

  for (const ability of abilities) {
    const recastTime: Date = new Date(ability?.recast * 1000)
    if (new Date().getTime() - recastTime.getTime() <= 0) {
      filteredAbilities.push(ability)
    }
  }

  return filteredAbilities
}

function renderDeathAnimation(dead: number) {
  if (dead == 2) {
    deadElement.value?.classList.add('transition')
    setTimeout(() => {
      deadElement.value?.classList.add('fade-in')
    }, 100)
  } else {
    deadElement.value?.classList.remove('fade-in')
    setTimeout(() => {
      deadElement.value?.classList.remove('transition')
    }, 100)
  }
}

watch(userStore.players, (players: any) => {
  const player = players.get(props.playerId)
  renderDeathAnimation(player.status)
})


</script>

<style scoped lang="scss">
.card {
  &.user-card {
    height: 100%;
    cursor: default;
  }

  &.user-btn-animations {
    box-shadow: 0 0 0 rgba(0, 0, 0, 0.2);
    transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
    cursor: pointer;

    &:hover {
      transform: scale(1.02);
      box-shadow: 0px 5px 10px rgba(0, 0, 0, 0.2);
    }
  }

  .dead-mask:before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1;
  }

  .dead {
    position: absolute;
    top: 50%;
    right: 50%;
    transform: translate(50%, -50%) scale(1.2);
    z-index: 2;
    opacity: 0;
    transition:
      transform 0.3s ease-in-out,
      opacity 0.3s ease-in-out;
  }

  .dead.transition {
    width: 250px;
    height: 250px;
  }

  .dead.fade-in {
    transform: translate(50%, -50%) scale(1);
    width: 250px;
    height: 250px;
    opacity: 1;
  }

  .fill {
    fill: v-bind(themeColor);
  }

  .skull-fill {
    fill: darkred;
    stroke: white;
    stroke-width: 0.2px;
  }

  .progress-bar {
    font-size: 0.95rem;
    font-weight: bold;
    &.bg-purple {
      background-color: #8a19bc;
    }
  }
}
</style>
