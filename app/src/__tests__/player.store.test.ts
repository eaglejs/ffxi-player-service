import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePlayerStore } from '@/stores/player'
import axios from 'axios'

// Mock axios
vi.mock('axios')

// Mock config
vi.mock('@/helpers/config', () => ({
  fullUrl: 'http://localhost:8080',
  fullWsUrl: 'ws://localhost:8080'
}))

// Mock window.location
Object.defineProperty(window, 'location', {
  value: {
    pathname: '/players/123'
  },
  writable: true
})

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(() => 'dark'),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

describe('usePlayerStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initialization', () => {
    it('creates store with empty players Map', () => {
      const store = usePlayerStore()
      
      expect(store.players).toBeInstanceOf(Map)
      expect(store.players.size).toBe(0)
    })

    it('creates store with empty chatLog array', () => {
      const store = usePlayerStore()
      
      expect(Array.isArray(store.chatLog)).toBe(true)
      expect(store.chatLog.length).toBe(0)
    })
  })

  describe('getPlayerById', () => {
    it('returns undefined for non-existent player', () => {
      const store = usePlayerStore()
      
      const player = store.getPlayerById(999)
      
      expect(player).toBeUndefined()
    })

    it('returns player when exists in Map', () => {
      const store = usePlayerStore()
      const mockPlayer = { playerId: 1, playerName: 'Test' }
      
      store.players.set(1, mockPlayer)
      const player = store.getPlayerById(1)
      
      expect(player).toEqual(mockPlayer)
    })

    it('returns correct player by ID', () => {
      const store = usePlayerStore()
      const player1 = { playerId: 1, playerName: 'Player1' }
      const player2 = { playerId: 2, playerName: 'Player2' }
      
      store.players.set(1, player1)
      store.players.set(2, player2)
      
      expect(store.getPlayerById(1)).toEqual(player1)
      expect(store.getPlayerById(2)).toEqual(player2)
    })
  })

  describe('fetchPlayers', () => {
    it('fetches and stores multiple players', async () => {
      const store = usePlayerStore()
      const mockPlayers = [
        { playerId: 1, playerName: 'Player1' },
        { playerId: 2, playerName: 'Player2' }
      ]
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: mockPlayers })
      
      await store.fetchPlayers()
      
      expect(store.players.size).toBe(2)
      expect(store.players.get(1)).toEqual(mockPlayers[0])
      expect(store.players.get(2)).toEqual(mockPlayers[1])
    })

    it('calls correct API endpoint', async () => {
      const store = usePlayerStore()
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: [] })
      
      await store.fetchPlayers()
      
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/players/get_players')
    })

    it('returns players ref', async () => {
      const store = usePlayerStore()
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: [] })
      
      const result = await store.fetchPlayers()
      
      expect(result).toBeDefined()
    })
  })

  describe('fetchPlayer', () => {
    it('fetches single player by ID', async () => {
      const store = usePlayerStore()
      const mockPlayer = { playerId: 42, playerName: 'TestPlayer' }
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: mockPlayer })
      
      await store.fetchPlayer(42)
      
      expect(store.players.get(42)).toEqual(mockPlayer)
    })

    it('calls correct API endpoint with playerId', async () => {
      const store = usePlayerStore()
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: { playerId: 42 } })
      
      await store.fetchPlayer(42)
      
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/players/get_player?playerId=42')
    })

    it('returns fetched player', async () => {
      const store = usePlayerStore()
      const mockPlayer = { playerId: 42, playerName: 'Test' }
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: mockPlayer })
      
      const result = await store.fetchPlayer(42)
      
      expect(result).toEqual(mockPlayer)
    })

    it('overwrites existing player data', async () => {
      const store = usePlayerStore()
      const oldPlayer = { playerId: 1, playerName: 'Old' }
      const newPlayer = { playerId: 1, playerName: 'New' }
      
      store.players.set(1, oldPlayer)
      vi.mocked(axios.get).mockResolvedValueOnce({ data: newPlayer })
      
      await store.fetchPlayer(1)
      
      expect(store.players.get(1)).toEqual(newPlayer)
    })
  })

  describe('fetchChatLog', () => {
    it('fetches and stores chat log', async () => {
      const store = usePlayerStore()
      const mockChatLog = [
        { message: 'Hello', messageType: 'say' },
        { message: 'World', messageType: 'yell' }
      ]
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: mockChatLog })
      
      await store.fetchChatLog(1)
      
      expect(store.chatLog).toEqual(mockChatLog)
    })

    it('calls correct API endpoint', async () => {
      const store = usePlayerStore()
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: [] })
      
      await store.fetchChatLog(123)
      
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/player/get_chat_log?playerId=123')
    })

    it('replaces existing chat log', async () => {
      const store = usePlayerStore()
      
      store.chatLog = [{ message: 'Old' }]
      vi.mocked(axios.get).mockResolvedValueOnce({ data: [{ message: 'New' }] })
      
      await store.fetchChatLog(1)
      
      expect(store.chatLog).toEqual([{ message: 'New' }])
    })
  })

  describe('fetchChatLogByMessageType', () => {
    it('fetches filtered chat log', async () => {
      const store = usePlayerStore()
      const mockChatLog = [{ message: 'Party message', messageType: 'party' }]
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: mockChatLog })
      
      await store.fetchChatLogByMessageType(1, 'PARTY')
      
      expect(store.chatLog).toEqual(mockChatLog)
    })

    it('calls correct API endpoint with messageType', async () => {
      const store = usePlayerStore()
      
      vi.mocked(axios.get).mockResolvedValueOnce({ data: [] })
      
      await store.fetchChatLogByMessageType(123, 'SAY')
      
      expect(axios.get).toHaveBeenCalledWith(
        'http://localhost:8080/player/get_chat_log_by_type?playerId=123&messageType=SAY'
      )
    })
  })

  describe('clearChatLog', () => {
    it('clears chat log array', () => {
      const store = usePlayerStore()
      
      store.chatLog = [{ message: 'Test' }]
      store.clearChatLog()
      
      expect(store.chatLog).toEqual([])
      expect(store.chatLog.length).toBe(0)
    })

    it('can be called on empty chat log', () => {
      const store = usePlayerStore()
      
      store.clearChatLog()
      
      expect(store.chatLog).toEqual([])
    })
  })

  describe('store exports', () => {
    it('exports players state', () => {
      const store = usePlayerStore()
      
      expect(store.players).toBeDefined()
    })

    it('exports chatLog state', () => {
      const store = usePlayerStore()
      
      expect(store.chatLog).toBeDefined()
    })

    it('exports all expected actions', () => {
      const store = usePlayerStore()
      
      expect(store.fetchPlayers).toBeDefined()
      expect(store.fetchPlayer).toBeDefined()
      expect(store.fetchChatLog).toBeDefined()
      expect(store.fetchChatLogByMessageType).toBeDefined()
      expect(store.clearChatLog).toBeDefined()
      expect(store.refreshBuffs).toBeDefined()
      expect(store.getPlayerById).toBeDefined()
    })
  })

  describe('reactivity', () => {
    it('players Map is reactive', () => {
      const store = usePlayerStore()
      
      const initialSize = store.players.size
      store.players.set(1, { playerId: 1 })
      
      expect(store.players.size).toBe(initialSize + 1)
    })

    it('chatLog array is reactive', () => {
      const store = usePlayerStore()
      
      store.chatLog.push({ message: 'Test' })
      
      expect(store.chatLog.length).toBe(1)
    })
  })
})
