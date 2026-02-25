import { describe, it, expect, beforeEach, vi } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ExperiencePoints from '@/components/ExperiencePoints.vue'
import type { Player } from '@/types/Player'

// Mock window.location
Object.defineProperty(window, 'location', {
  value: { pathname: '/charts' },
  writable: true
})

describe('ExperiencePoints.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mockPlayer: Player = {
    playerId: 1,
    playerName: 'testPlayer',
    merits: { total: 50, max: 100 },
    capacityPoints: { total: 200 },
    expHistory: {
      experience: [
        { points: 1000, timestamp: '2024-01-01T00:00:00Z' },
        { points: 2000, timestamp: '2024-01-01T01:00:00Z' }
      ],
      capacity: [
        { points: 500, timestamp: '2024-01-01T00:00:00Z' },
        { points: 1500, timestamp: '2024-01-01T01:00:00Z' }
      ],
      exemplar: [
        { points: 100, timestamp: '2024-01-01T00:00:00Z' },
        { points: 300, timestamp: '2024-01-01T01:00:00Z' }
      ]
    }
  } as Player

  describe('rendering', () => {
    it('renders component with player data', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('renders player name in dashboard mode', () => {
      Object.defineProperty(window, 'location', {
        value: { pathname: '/charts' },
        writable: true
      })

      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.text()).toContain('TestPlayer')
    })

    it('renders "Experience Points" title in non-dashboard mode', () => {
      Object.defineProperty(window, 'location', {
        value: { pathname: '/players/1' },
        writable: true
      })

      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.text()).toContain('Experience Points')
    })

    it('displays merits total', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.text()).toContain('50')
    })

    it('displays capacity points total', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.text()).toContain('200')
    })
  })

  describe('computed properties', () => {
    it('capitalizes player name', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.vm.playerName).toBe('TestPlayer')
    })

    it('returns empty string for undefined player name', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: undefined }
      })
      
      expect(wrapper.vm.playerName).toBe('')
    })

    it('calculates total merits', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.vm.totalMerits).toBe(50)
    })

    it('calculates total capacity points', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.vm.totalCapacityPoints).toBe(200)
    })
  })

  describe('graph data', () => {
    it('creates experience graph with datasets', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.vm.experienceGraph.datasets).toHaveLength(3)
      expect(wrapper.vm.experienceGraph.datasets[0].label).toBe('XP')
      expect(wrapper.vm.experienceGraph.datasets[1].label).toBe('CP')
      expect(wrapper.vm.experienceGraph.datasets[2].label).toBe('EX')
    })

    it('generates 50 labels for graph', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      expect(wrapper.vm.experienceGraph.labels).toHaveLength(50)
    })
  })

  describe('edge cases', () => {
    it('handles undefined player gracefully', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: undefined }
      })
      
      expect(wrapper.exists()).toBe(true)
    })

    it('handles missing expHistory', () => {
      const playerWithoutHistory = {
        playerId: 1,
        playerName: 'testPlayer',
        merits: { total: 0, max: 0 },
        capacityPoints: { total: 0 },
        expHistory: undefined
      } as Player

      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: playerWithoutHistory }
      })
      
      expect(wrapper.vm.totalMerits).toBe(0)
    })

    it('handles empty experience arrays', () => {
      const playerWithEmptyHistory = {
        ...mockPlayer,
        expHistory: {
          experience: [],
          capacity: [],
          exemplar: []
        }
      } as Player

      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: playerWithEmptyHistory }
      })
      
      expect(wrapper.vm.averageExperiencePts).toBe(0)
      expect(wrapper.vm.averageCapacityPts).toBe(0)
      expect(wrapper.vm.averageExemplarPts).toBe(0)
    })
  })

  describe('analyzePoints function', () => {
    it('calculates average points per hour', () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      // With mock data: 3000 points over 1 hour = 3k/hr
      expect(wrapper.vm.averageExperiencePts).toBeGreaterThan(0)
    })

    it('returns 0 for empty points array', () => {
      const emptyPlayer = {
        ...mockPlayer,
        expHistory: {
          experience: [],
          capacity: [],
          exemplar: []
        }
      } as Player

      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: emptyPlayer }
      })
      
      expect(wrapper.vm.averageExperiencePts).toBe(0)
    })
  })

  describe('reactivity', () => {
    it('updates when player data changes', async () => {
      const wrapper = shallowMount(ExperiencePoints, {
        props: { player: mockPlayer }
      })
      
      const updatedPlayer = {
        ...mockPlayer,
        merits: { total: 75, max: 100 }
      }
      
      await wrapper.setProps({ player: updatedPlayer })
      
      expect(wrapper.vm.totalMerits).toBe(75)
    })
  })
})
