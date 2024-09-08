import type { Ability } from './Ability';

export interface Player {
  abilities: Ability[],
  attack: number,
  buffs: string,
  currency1: {
    conquestPointsSandoria: number,
    conquestPointsBastok: number,
    conquestPointsWindurst: number,
    imperialStanding: number,
    dominionNotes: number,
    sparksOfEminence: number,
    unityAccolades: number,
    loginPoints: number,
    deeds: number,
  },
  currency2: {
    domainPoints: number,
    eschaBeads: number,
    eschaSilt: number,
    gallantry: number,
    gallimaufry: number,
    hallmarks: number,
    mogSegments: number,
    mweyaPlasmCorpuscles: number,
    potpourri: number,
  },
  currentExemplar: number,
  defense: number,
  gil: number,
  hpp: number,
  lastOnline: number,
  mainJob: string,
  mainJobLevel: number,
  masterLevel: number,
  mpp: number,
  nationRank: number,
  playerId: number,
  playerName: string,
  requiredExemplar: number,
  stats: {
    baseSTR: number,
    baseDEX: number,
    baseVIT: number,
    baseAGI: number,
    baseINT: number,
    baseMND: number,
    baseCHR: number,
    addedSTR: number,
    addedDEX: number,
    addedVIT: number,
    addedAGI: number,
    addedINT: number,
    addedMND: number,
    addedCHR: number,
    fireResistance: number,
    iceResistance: number,
    windResistance: number,
    earthResistance: number,
    lightningResistance: number,
    waterResistance: number,
    lightResistance: number,
    darkResistance: number,
  },
  status: number,
  subJob: string,
  subJobLevel: number,
  title: string,
  tp: number,
  zone: string,
}
