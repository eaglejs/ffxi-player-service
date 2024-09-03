_addon.name = 'PlayerService'
_addon.author = 'eaglejs'
_addon.version = "1.2024.5.19"
_addon.commands = {'playerservice', 'ps', 'pserv'}

require('strings')
local packets = require('packets')
local res = require('resources')
local helpers = require('eaglejs/Helpers')
local Utils = require('./Utils')
local PSUI = require('./PlayerServiceInterface')
local pingClock = os.time()
local lastMessage = ''
local lastMessageTime = os.time()

local partySlots = L {'p1', 'p2', 'p3', 'p4', 'p5'}
math.randomseed(os.time())
local PlayerService = {
  active = true,
  debugger = true
}

function PlayerService.set_online()
  local player = windower.ffxi.get_player()
  if not player or not PlayerService.active then
    return
  end
  local data = ("playerName=%s&lastOnline=%s"):format(player.name, os.time())
  PSUI.post('set_online', data)
end

function PlayerService.set_jobs()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerName=%s&mainJob=%s&subJob=%s"):format(player.name, player.main_job, player.sub_job)

  PSUI.post('set_jobs', data)
end

function PlayerService.set_player_status(new, old)
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end
  local data = ("playerName=%s&status=%s"):format(player.name, new)
  PSUI.post('set_player_status', data)
end

function PlayerService.set_hpp()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerName=%s&hpp=%s"):format(player.name, player.vitals.hpp)

  PSUI.post('set_hpp', data)
end

function PlayerService.set_mpp()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerName=%s&mpp=%s"):format(player.name, player.vitals.mpp)

  PSUI.post('set_mpp', data)
end

function PlayerService.set_tp(newTP, oldTP)
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerName=%s&tp=%s"):format(player.name, newTP)

  PSUI.post('set_tp', data)
end

function PlayerService.set_stats(original)
  local player = windower.ffxi.get_player()
  local packet = packets.parse('incoming', original)
  local packet_stats = {
    ['masterLevel'] = packet['Master Level'],
    ['attack'] = packet['Attack'],
    ['defense'] = packet['Defense'],
    ['baseSTR'] = packet['Base STR'],
    ['baseDEX'] = packet['Base DEX'],
    ['baseVIT'] = packet['Base VIT'],
    ['baseAGI'] = packet['Base AGI'],
    ['baseINT'] = packet['Base INT'],
    ['baseMND'] = packet['Base MND'],
    ['baseCHR'] = packet['Base CHR'],
    ['addedSTR'] = packet['Added STR'],
    ['addedDEX'] = packet['Added DEX'],
    ['addedVIT'] = packet['Added VIT'],
    ['addedAGI'] = packet['Added AGI'],
    ['addedINT'] = packet['Added INT'],
    ['addedMND'] = packet['Added MND'],
    ['addedCHR'] = packet['Added CHR'],
    ['fireResistance'] = packet['Fire Resistance'],
    ['iceResistance'] = packet['Ice Resistance'],
    ['windResistance'] = packet['Wind Resistance'],
    ['earthResistance'] = packet['Earth Resistance'],
    ['lightningResistance'] = packet['Lightning Resistance'],
    ['waterResistance'] = packet['Water Resistance'],
    ['lightResistance'] = packet['Light Resistance'],
    ['darkResistance'] = packet['Dark Resistance'],
    ['title'] = packet['Title'],
    ['nationRank'] = packet['NationRank'],
    ['currentExemplar'] = packet['Current Exemplar Points'],
    ['requiredExemplar'] = packet['Required Exemplar Points'],
  }

  if not player or not PlayerService.active then
    return
  end

  local stats = {
    ['mainJobLevel'] = player.main_job_level,
    ['subJobLevel'] = player.sub_job_level,
    ['masterLevel'] = packet_stats.masterLevel,
    ['attack'] = packet_stats.attack,
    ['defense'] = packet_stats.defense,
    ['baseSTR'] = packet_stats.baseSTR,
    ['baseDEX'] = packet_stats.baseDEX,
    ['baseVIT'] = packet_stats.baseVIT,
    ['baseAGI'] = packet_stats.baseAGI,
    ['baseINT'] = packet_stats.baseINT,
    ['baseMND'] = packet_stats.baseMND,
    ['baseCHR'] = packet_stats.baseCHR,
    ['addedSTR'] = packet_stats.addedSTR,
    ['addedDEX'] = packet_stats.addedDEX,
    ['addedVIT'] = packet_stats.addedVIT,
    ['addedAGI'] = packet_stats.addedAGI,
    ['addedINT'] = packet_stats.addedINT,
    ['addedMND'] = packet_stats.addedMND,
    ['addedCHR'] = packet_stats.addedCHR,
    ['fireResistance'] = packet_stats.fireResistance,
    ['iceResistance'] = packet_stats.iceResistance,
    ['windResistance'] = packet_stats.windResistance,
    ['earthResistance'] = packet_stats.earthResistance,
    ['lightningResistance'] = packet_stats.lightningResistance,
    ['waterResistance'] = packet_stats.waterResistance,
    ['lightResistance'] = packet_stats.lightResistance,
    ['darkResistance'] = packet_stats.darkResistance,
    ['title'] = packet_stats.title,
    ['nationRank'] = packet_stats.nationRank,
    ['currentExemplar'] = packet_stats.currentExemplar,
    ['requiredExemplar'] = packet_stats.requiredExemplar
  }
  local data = ('playerName=%s&mainJobLevel=%s&subJobLevel=%s&masterLevel=%s&attack=%s&defense=%s&currentExemplar=%s&requiredExemplar=%s&baseSTR=%s&baseDEX=%s&baseVIT=%s&baseAGI=%s&baseINT=%s&baseMND=%s&baseCHR=%s&addedSTR=%s&addedDEX=%s&addedVIT=%s&addedAGI=%s&addedINT=%s&addedMND=%s&addedCHR=%s&fireResistance=%s&iceResistance=%s&windResistance=%s&earthResistance=%s&lightningResistance=%s&waterResistance=%s&lightResistance=%s&darkResistance=%s&title=%s&nationRank=%s')
    :format(player.name,
    stats.mainJobLevel,
    stats.subJobLevel,
    stats.masterLevel or 0,
    stats.attack or 0,
    stats.defense or 0,
    stats.currentExemplar or 0,
    stats.requiredExemplar or 0,
    stats.baseSTR or 0,
    stats.baseDEX or 0,
    stats.baseVIT or 0,
    stats.baseAGI or 0,
    stats.baseINT or 0,
    stats.baseMND or 0,
    stats.baseCHR or 0,
    stats.addedSTR or 0,
    stats.addedDEX or 0,
    stats.addedVIT or 0,
    stats.addedAGI or 0,
    stats.addedINT or 0,
    stats.addedMND or 0,
    stats.addedCHR or 0,
    stats.fireResistance or 0,
    stats.iceResistance or 0,
    stats.windResistance or 0,
    stats.earthResistance or 0,
    stats.lightningResistance or 0,
    stats.waterResistance or 0,
    stats.lightResistance or 0,
    stats.darkResistance or 0,
    res.titles[stats.title].en or '',
    stats.nationRank or 0
  )

  PSUI.post('set_stats', data)

end

function PlayerService.set_currency1(original)
  local player = windower.ffxi.get_player()
  local packet = packets.parse('incoming', original)

  if not player or not PlayerService.active then
    return
  end

  local packet_currency = {
    ['conquestPointsSandoria'] = packet['Conquest Points (San d\'Oria)'] or 0,
    ['conquestPointsBastok'] = packet['Conquest Points (Bastok)'] or 0,
    ['conquestPointsWindurst'] = packet['Conquest Points (Windurst)'] or 0,
    ['imperialStanding'] = packet['Imperial Standing'] or 0,
    ['dominionNotes'] = packet['Dominion Notes'] or 0,
    ['sparksOfEminence'] = packet['Sparks of Eminence'] or 0,
    ['unityAccolades'] = packet['Unity Accolades'] or 0,
    ['loginPoints'] = packet['Login Points'] or 0,
    ['deeds'] = packet['Deeds'] or 0,
  }

  local data = ('playerName=%s&conquestPointsSandoria=%s&conquestPointsBastok=%s&conquestPointsWindurst=%s&imperialStanding=%s&dominionNotes=%s&sparksOfEminence=%s&unityAccolades=%s&loginPoints=%s&deeds=%s')
    :format(player.name,
    packet_currency.conquestPointsSandoria,
    packet_currency.conquestPointsBastok,
    packet_currency.conquestPointsWindurst,
    packet_currency.imperialStanding,
    packet_currency.dominionNotes,
    packet_currency.sparksOfEminence,
    packet_currency.unityAccolades,
    packet_currency.loginPoints,
    packet_currency.deeds
  )

  PSUI.post('set_currency1', data)
end

function PlayerService.set_currency2(original)
  local player = windower.ffxi.get_player()
  local packet = packets.parse('incoming', original)

  if not player or not PlayerService.active then
    return
  end
  
  local packet_currency = {
    ['domainPoints'] = packet['Domain Points'],
    ['eschaBeads'] = packet['Escha Beads'],
    ['eschaSilt'] = packet['Escha Silt'],
    ['gallantry'] = packet['Badges of Gallantry'],
    ['gallimaufry'] = packet['Gallimaufry'],
    ['hallmarks'] = packet['Hallmarks'],
    ['mogSegments'] = packet['Mog Segments'],
    ['mweyaPlasmCorpuscles'] = packet['Mweya Plasm Corpuscles'],
    ['potpourri'] = packet['Potpourri']
  }

  local data = ('playerName=%s&domainPoints=%s&eschaBeads=%s&eschaSilt=%s&gallantry=%s&gallimaufry=%s&hallmarks=%s&mogSegments=%s&mweyaPlasmCorpuscles=%s&potpourri=%s')
    :format(player.name,
    packet_currency.domainPoints or 0,
    packet_currency.eschaBeads or 0,
    packet_currency.eschaSilt or 0,
    packet_currency.gallantry or 0,
    packet_currency.gallimaufry or 0,
    packet_currency.hallmarks or 0,
    packet_currency.mogSegments or 0,
    packet_currency.mweyaPlasmCorpuscles or 0,
    packet_currency.potpourri or 0
  )

  PSUI.post('set_currency2', data)
end

function PlayerService.set_buffs()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end
  
  local buffs = helpers.calculateBuffs(player['buffs'])
  local buffString = ''
  for buff, amount in pairs(buffs) do
    if amount > 1 then
      for i = 1, amount do
        buffString = buffString .. buff .. ','
      end
    else
      buffString = buffString .. buff .. ','
    end
  end

  local data = ("playerName=%s&buffs=%s"):format(player.name, buffString)

  PSUI.post('set_buffs', data)
end

function PlayerService.set_zone()
  local gameInfo = windower.ffxi.get_info()
  local player = windower.ffxi.get_player()

  PlayerService.active = true

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerName=%s&zone=%s"):format(player.name, res.zones[gameInfo.zone].en)

  PSUI.post('set_zone', data)
end

function PlayerService.incoming_chunk_handler(id, original, modified, injected, blocked)
  if id == 0x061 then
    PlayerService.set_stats(original)
  elseif id == 0x118 then
    PlayerService.set_currency2(original)
  elseif id == 0x113 then
    PlayerService.set_currency1(original)
  end

  if ((os.time() - pingClock) > 15) then
    PlayerService.set_online()
    pingClock = os.time()
    local player = windower.ffxi.get_player()
    if not player or not PlayerService.active then
      return
    end
    local data = ("playerName=%s&gil=%s"):format(player.name, windower.ffxi.get_items('gil'))
    PSUI.post('set_gil', data)
  end
end

function PlayerService.outgoing_chunk_handler(id, original, modified, injected, blocked)
  if id == 0x05E then
    PlayerService.activee = false
  end
end

function PlayerService.handle_action(action)
  if action.actor_id == windower.ffxi.get_player().id then
    if action.category == 6 then
      local ability_recasts = windower.ffxi.get_ability_recasts()
      local filtered_ability_recasts = {}
      for i, v in pairs(ability_recasts) do
        if v > 0 then
          table.insert(filtered_ability_recasts, { recast=os.time() + math.round(v), ability=res.ability_recasts[i].en})
        end
      end
      PSUI.set_ability_recasts(windower.ffxi.get_player().name, filtered_ability_recasts)
    end
  end
end

function PlayerService.set_ability_recasts()
  local ability_recasts = windower.ffxi.get_ability_recasts()
  local filtered_ability_recasts = {}
  
  if not PlayerService.active then
    return
  end

  for i, v in pairs(ability_recasts) do
    if v > 0 then
      table.insert(filtered_ability_recasts, { recast=os.time() + math.round(v), ability=res.ability_recasts[i].en})
    end
  end
  PSUI.set_ability_recasts(windower.ffxi.get_player().name, filtered_ability_recasts)
end

function PlayerService.fetchPlayerStats(playerName)
  local player = windower.ffxi.get_player()
  if not player or not PlayerService.active then
    return
  end
  coroutine.schedule(PlayerService.send_stats_request, math.random(0, 2))
  coroutine.sleep(1)
  coroutine.schedule(PlayerService.send_currency2_request, math.random(0, 2))
end

function PlayerService.send_stats_request()
  packets.inject(packets.new('outgoing', 0x061, {
    ['_unknown1'] = 0
  }))
end

function PlayerService.send_currency2_request()
  packets.inject(packets.new('outgoing', 0x115))
end

function PlayerService.handle_incoming_text(original, modified, original_mode, modified_mode, blocked)
  local newMessage
  local matchResult
  local messageType
  local player = windower.ffxi.get_player()

  if blocked or not player or not PlayerService.active then
    return
  end

  newMessage = Utils.convert_to_utf8(original)

  matchResult = string.match(newMessage, '^%(%a+%)') -- Party
  if matchResult then
    messageType = "PARTY"
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^%a+>>') -- Tell (From Person (Them))
    if matchResult then
      messageType = "TELL"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^>>%a+') -- Tell (To Person (You))
    if matchResult then
      messageType = "TELL"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^%[%d%]<[%u][%a]+>') -- Linkshell
    if matchResult then
      messageType = "LINKSHELL"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^[%a+] :') -- Shout/NPC/Say
    if matchResult then
      messageType = "SHOUT"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^%a+%[%a+%]:') -- Yell
    if matchResult then
      messageType = "YELL"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^{%a+') -- Unity
    if matchResult then
      messageType = "UNITY"
    end
  end
  if not matchResult then
    -- regex that will match this word "Eald'narche :" or "Aldo :" or "Agado Pugado :" or "Gorpa Masorpa :"
    matchResult = string.match(newMessage, '^%a+\'?%a*%a+ :') -- NPC
    if matchResult then
      messageType = "CUTSCENE"
    end
  end

  if not matchResult then
    return
  end

  if newMessage == lastMessage and os.clock() - 1 < lastMessageTime then
    return
  end

  lastMessage = newMessage
  lastMessageTime = os.clock()

  -- -- Convert newMessage to a byte array
  -- local byteArray = {}
  -- for i = 1, #newMessage do
  --   byteArray[i] = string.byte(newMessage, i)
  -- end

  -- -- Convert byte array to a string representation
  -- local byteArrayStr = table.concat(byteArray, ",")

  local data = ("playerName=%s&messageType=%s&message=%s"):format(player.name, messageType, newMessage)
  PSUI.post('set_message', data)
end

windower.register_event('incoming text', PlayerService.handle_incoming_text)
windower.register_event('incoming chunk', PlayerService.incoming_chunk_handler)
windower.register_event('outgoing chunk', PlayerService.outgoing_chunk_handler)
windower.register_event('action', PlayerService.handle_action)
windower.register_event('load', PlayerService.set_jobs)
windower.register_event('gain experience', PlayerService.fetchPlayerStats)
windower.register_event('lose experience', PlayerService.fetchPlayerStats)
windower.register_event('hpp change', PlayerService.set_hpp)
windower.register_event('mpp change', PlayerService.set_mpp)
windower.register_event('tp change', PlayerService.set_tp)
windower.register_event('gain buff', PlayerService.set_buffs)
windower.register_event('lose buff', PlayerService.set_buffs)
windower.register_event('job change', PlayerService.set_jobs)
windower.register_event('zone change', PlayerService.set_zone)
windower.register_event('status change', PlayerService.set_player_status)
windower.register_event('time change', PlayerService.set_ability_recasts)

PlayerService.set_online()