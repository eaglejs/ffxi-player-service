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
local chars = require('chat.chars')

local pingClock = os.time()
local lastMessage = ''
local lastMessageTime = os.time()
local chat_purple = string.char(0x1F, 200)
local chat_grey = string.char(0x1F, 160)
local chat_red = string.char(0x1F, 167)
local chat_white = string.char(0x1F, 001)
local chat_green = string.char(0x1F, 214)
local chat_yellow = string.char(0x1F, 036)
local chat_drk_blue = string.char(0x1F, 207)
local chat_pink = string.char(0x1E, 5)
local chat_lt_blue = string.char(0x1E, 6)
local partySlots = L {'p1', 'p2', 'p3', 'p4', 'p5'}
local chatDropAccumulator = S {}
local chatDropAccumulatorTimer = os.time()
local keeperOfBuffs = S {}

math.randomseed(os.time())
local PlayerService = {
  name = 'PlayerService',
  active = true,
  debugger = true
}

function PlayerService.initialize_player()
  local player = windower.ffxi.get_player()
  if not player then
    return
  end
  local data = ("playerId=%s&playerName=%s&lastOnline=%s"):format(player.id, player.name, os.time())

  PSUI.post('initialize_player', data)

end

function PlayerService.set_online()
  local player = windower.ffxi.get_player()
  if not player or not PlayerService.active then
    return
  end
  local data = ("playerId=%s&playerName=%s&lastOnline=%s"):format(player.id, player.name, os.time())

  PSUI.post('set_online', data)

end

function PlayerService.set_jobs()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerId=%s&playerName=%s&mainJob=%s&subJob=%s"):format(player.id, player.name, player.main_job or "",
   player.sub_job or "")

  PSUI.post('set_jobs', data)

end

function PlayerService.set_player_status(new, old)
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local status_data = ("playerId=%s&playerName=%s&status=%s"):format(player.id, player.name, new)

  PSUI.post('set_player_status', status_data)
end

function PlayerService.set_hpp()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerId=%s&playerName=%s&hpp=%s"):format(player.id, player.name, player.vitals.hpp)

  PSUI.post('set_hpp', data)

end

function PlayerService.set_mpp()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerId=%s&playerName=%s&mpp=%s"):format(player.id, player.name, player.vitals.mpp)

  PSUI.post('set_mpp', data)

end

function PlayerService.set_tp(newTP, oldTP)
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerId=%s&playerName=%s&tp=%s"):format(player.id, player.name, newTP)

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
    ['requiredExemplar'] = packet['Required Exemplar Points']
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
  local data =
   ('playerId=%s&playerName=%s&mainJobLevel=%s&subJobLevel=%s&masterLevel=%s&attack=%s&defense=%s&currentExemplar=%s&requiredExemplar=%s&baseSTR=%s&baseDEX=%s&baseVIT=%s&baseAGI=%s&baseINT=%s&baseMND=%s&baseCHR=%s&addedSTR=%s&addedDEX=%s&addedVIT=%s&addedAGI=%s&addedINT=%s&addedMND=%s&addedCHR=%s&fireResistance=%s&iceResistance=%s&windResistance=%s&earthResistance=%s&lightningResistance=%s&waterResistance=%s&lightResistance=%s&darkResistance=%s&title=%s&nationRank=%s'):format(
    player.id, player.name, stats.mainJobLevel or 0, stats.subJobLevel or 0, stats.masterLevel or 0, stats.attack or 0,
    stats.defense or 0, stats.currentExemplar or 0, stats.requiredExemplar or 0, stats.baseSTR or 0, stats.baseDEX or 0,
    stats.baseVIT or 0, stats.baseAGI or 0, stats.baseINT or 0, stats.baseMND or 0, stats.baseCHR or 0,
    stats.addedSTR or 0, stats.addedDEX or 0, stats.addedVIT or 0, stats.addedAGI or 0, stats.addedINT or 0,
    stats.addedMND or 0, stats.addedCHR or 0, stats.fireResistance or 0, stats.iceResistance or 0,
    stats.windResistance or 0, stats.earthResistance or 0, stats.lightningResistance or 0, stats.waterResistance or 0,
    stats.lightResistance or 0, stats.darkResistance or 0, res.titles[stats.title].en or '', stats.nationRank or 0)

  PSUI.post('set_stats', data)

end

function PlayerService.set_currency1(original)
  local player = windower.ffxi.get_player()
  local packet = packets.parse('incoming', original)

  if not player or not PlayerService.active then
    return
  end

  local packet_currency = {
    ['conquestPointsBastok'] = packet['Conquest Points (Bastok)'] or 0,
    ['conquestPointsSandoria'] = packet['Conquest Points (San d\'Oria)'] or 0,
    ['conquestPointsWindurst'] = packet['Conquest Points (Windurst)'] or 0,
    ['deeds'] = packet['Deeds'] or 0,
    ['dominionNotes'] = packet['Dominion Notes'] or 0,
    ['imperialStanding'] = packet['Imperial Standing'] or 0,
    ['loginPoints'] = packet['Login Points'] or 0,
    ['nyzulTokens'] = packet['Nyzul Tokens'] or 0,
    ['sparksOfEminence'] = packet['Sparks of Eminence'] or 0,
    ['therionIchor'] = packet['Therion Ichor'] or 0,
    ['unityAccolades'] = packet['Unity Accolades'] or 0,
    ['voidstones'] = packet['Voidstones'] or 0
  }

  local data =
   ('playerId=%s&playerName=%s&conquestPointsBastok=%s&conquestPointsSandoria=%s&conquestPointsWindurst=%s&deeds=%s&dominionNotes=%s&imperialStanding=%s&loginPoints=%s&nyzulTokens=%s&sparksOfEminence=%s&therionIchor=%s&unityAccolades=%s&voidstones=%s'):format(
    player.id, player.name, packet_currency.conquestPointsBastok or 0, packet_currency.conquestPointsSandoria or 0,
    packet_currency.conquestPointsWindurst or 0, packet_currency.deeds or 0, packet_currency.dominionNotes or 0,
    packet_currency.imperialStanding or 0, packet_currency.loginPoints or packet_currency.nyzulTokens,
    packet_currency.nyzulTokens or 0, packet_currency.sparksOfEminence or 0, packet_currency.therionIchor or 0,
    packet_currency.unityAccolades or 0, packet_currency.voidstones or 0)

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
    ['coalitionImprimaturs'] = packet['Coalition Imprimaturs'],
    ['eschaBeads'] = packet['Escha Beads'],
    ['eschaSilt'] = packet['Escha Silt'],
    ['gallantry'] = packet['Badges of Gallantry'],
    ['gallimaufry'] = packet['Gallimaufry'],
    ['hallmarks'] = packet['Hallmarks'],
    ['mogSegments'] = packet['Mog Segments'],
    ['mweyaPlasmCorpuscles'] = packet['Mweya Plasm Corpuscles'],
    ['potpourri'] = packet['Potpourri']
  }

  local data =
   ('playerId=%s&playerName=%s&coalitionImprimaturs=%s&domainPoints=%s&eschaBeads=%s&eschaSilt=%s&gallantry=%s&gallimaufry=%s&hallmarks=%s&mogSegments=%s&mweyaPlasmCorpuscles=%s&potpourri=%s'):format(
    player.id, player.name, packet_currency.coalitionImprimaturs or 0, packet_currency.domainPoints or 0,
    packet_currency.eschaBeads or 0, packet_currency.eschaSilt or 0, packet_currency.gallantry or 0,
    packet_currency.gallimaufry or 0, packet_currency.hallmarks or 0, packet_currency.mogSegments or 0,
    packet_currency.mweyaPlasmCorpuscles or 0, packet_currency.potpourri or 0)

  PSUI.post('set_currency2', data)

end

function PlayerService.set_buffs(id)
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

  local data = ("playerId=%s&playerName=%s&buffs=%s"):format(player.id, player.name, buffString)

  PSUI.post('set_buffs', data)

end

function PlayerService.set_buff_with_timers()
  local player = windower.ffxi.get_player()

  if not player or not PlayerService.active then
    return
  end

  local pkg = {
    playerId = player.id,
    playerName = player.name,
    buffs = keeperOfBuffs
  }

  pkg = PSUI.toJSON(pkg)

  PSUI.post_json('set_buffs_json', pkg)

end

function PlayerService.set_zone()
  local gameInfo = windower.ffxi.get_info()
  local player = windower.ffxi.get_player()

  PlayerService.active = true
  PSUI.online = true
  PSUI.freezeOnlineCheck = true

  if not player or not PlayerService.active then
    return
  end

  local data = ("playerId=%s&playerName=%s&zone=%s"):format(player.id, player.name, res.zones[gameInfo.zone].en)

  coroutine.schedule(function()
    PSUI.post('set_zone', data)
  end, math.random(0, 3))
  coroutine.schedule(function()
    PSUI.post('reset_exp_history', ("playerId=%s&playerName=%s"):format(player.id, player.name))
  end, math.random(0, 5))
  coroutine.schedule(function()
    PlayerService.updateGil()
  end, math.random(0, 5))

end

function PlayerService.updateGil()
  local player = windower.ffxi.get_player()
  if not player or not PlayerService.active then
    return
  end
  local data = ("playerId=%s&playerName=%s&gil=%s"):format(player.id, player.name, windower.ffxi.get_items('gil'))

  PSUI.post('set_gil', data)
end

function PlayerService.incoming_chunk_handler(id, original, modified, injected, blocked)
  if id == 0x061 then
    PlayerService.set_stats(original)
  elseif id == 0x118 then
    PlayerService.set_currency2(original)
  elseif id == 0x113 then
    PlayerService.set_currency1(original)
  elseif id == 0x063 then -- the packet character data is in
    local packet = packets.parse('incoming', modified)
    if packet['Order'] == 0x09 then -- the sub packet buff data is in
      local buffs = S {}
      local deadCounter = 0
      for i = 1, 32 do
        local buff = packet['Buffs ' .. i]
        if buff and buff % 0x8000 ~= 255 then
          local buff_id = packet['Buffs ' .. i]
          local buff_duration = packet['Time ' .. i]
          local buff_name = res.buffs[buff_id].en
          local utc_time = Utils.get_duration_local_time(buff_duration)
          local formatted_utc_time = Utils.get_formatted_local_time(utc_time)
          if buff_id == 0 then
            deadCounter = deadCounter + 1
          end
          -- print('Buff ID: ' .. buff_id .. ' | Buff Name: ' .. buff_name .. ' | Buff Duration: ' .. buff_duration ..
          --        ' | UTC Time: ' .. formatted_utc_time)
          table.insert(buffs, {
            ["buff_id"] = buff_id,
            ["buff_name"] = buff_name,
            ["buff_duration"] = buff_duration,
            ["utc_time"] = formatted_utc_time
          })
        end
      end
      if (deadCounter <= 1) then
        keeperOfBuffs = buffs
      end
    elseif packet['Order'] == 2 then
      local meritPackage = {
        playerId = windower.ffxi.get_player().id,
        playerName = windower.ffxi.get_player().name,
        total = packet['Merit Points'],
        max = packet['Max Merit Points']
      }
      coroutine.schedule(function()
        PSUI.post_json('update_merits', PSUI.toJSON(meritPackage))
      end, math.random(0, 5))

    elseif packet['Order'] == 5 then
      local player = windower.ffxi.get_player()
      if player then
        local job = player.main_job_full
        local cpPackage = {
          playerId = player.id,
          playerName = player.name,
          numberOfJobPoints = packet[job .. ' Job Points']
        }

        coroutine.schedule(function()
          PSUI.post_json('update_capacity_points', PSUI.toJSON(cpPackage))
        end, math.random(0, 5))
      end
    end
  elseif id == 0x02D then
    -- (8|253) = exp, (371|372) = limit, (718|735) = capacity, (809|810) = exemplar
    -- Param 1 = EXP, Param 2 = Chain #
    local expMessageTypes = S {8, 253, 371, 372, 718, 735, 809, 810}
    local packet = packets.parse('incoming', original)
    -- print('Message: ' .. packet['Message'] .. ' ' .. packet['Param 1'] .. ' ' .. packet['Param 2'])
    if expMessageTypes:contains(packet['Message']) then
      local formatted_utc_time = Utils.get_formatted_local_time(Utils.get_current_time())
      local expPackage = {
        ["playerId"] = windower.ffxi.get_player().id,
        ["playerName"] = windower.ffxi.get_player().name,
        ["expType"] = packet['Message'],
        ["points"] = packet['Param 1'],
        ["chain"] = packet['Param 2'],
        ["timestamp"] = formatted_utc_time
      }
      coroutine.schedule(function()
        PSUI.post_json('update_exp_history', PSUI.toJSON(expPackage))
      end, math.random(0, 5))

    end
  end

  if ((os.time() - pingClock) > 15) then
    PlayerService.set_online()
    pingClock = os.time()
  end
end

function PlayerService.outgoing_chunk_handler(id, original, modified, injected, blocked)
  if id == 0x05E then
    PlayerService.active = false
  elseif id == 0x00D then
    PSUI.online = false
    PSUI.freezeOnlineCheck = false
  end
end

function PlayerService.fetchPlayerStats()
  local player = windower.ffxi.get_player()
  if not player or not PlayerService.active then
    return
  end
  coroutine.schedule(PlayerService.send_stats_request, math.random(0, 2))
  coroutine.schedule(PlayerService.send_currency1_request, math.random(0, 5))
  coroutine.schedule(PlayerService.send_currency2_request, math.random(0, 5))
end

function PlayerService.send_stats_request()
  packets.inject(packets.new('outgoing', 0x061, {
    ['_unknown1'] = 0
  }))
end

function PlayerService.send_currency1_request()
  packets.inject(packets.new('outgoing', 0x10F))
end

function PlayerService.send_currency2_request()
  packets.inject(packets.new('outgoing', 0x115))
end

function PlayerService.handle_incoming_text(original, modified, original_mode, modified_mode, blocked)
  local newMessage
  local matchResult
  local messageType
  local safeMessageTypes = S {'OBTAINED', 'DROPS'}
  local player = windower.ffxi.get_player()

  if blocked or not player or not PlayerService.active then
    return
  end

  -- newMessage = original
  newMessage = Utils.strip_colors(original)

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
    matchResult = string.match(newMessage, '^%[1]<[%u][%a]+>') -- Linkshell[1]
    if matchResult then
      messageType = "LINKSHELL1"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^%[2]<[%u][%a]+>') -- Linkshell[2]
    if matchResult then
      messageType = "LINKSHELL2"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^[%a+] :') -- Shout
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
    matchResult = string.match(newMessage, '^%a+\'?%a*%a+ :') -- NPC/say
    if matchResult then
      if original_mode <= 10 then
        messageType = "SAY"
      else
        messageType = "CUTSCENE"
      end
    end
  end
  if not matchResult then
    -- matches on text: "<Person> Obtains a" and "Obtained key item:""
    matchResult = string.match(newMessage, ('^%s obtains a'):format(player.name)) or
                   string.match(newMessage, '^Obtained key item:') -- Obtained
    if matchResult then
      messageType = "OBTAINED"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, ('^You find a'):format(player.name)) -- Drops
    if matchResult then
      messageType = "DROPS"
    end
  end
  if not matchResult then
    matchResult = string.match(newMessage, '^%a+ %d+:') -- Moogle Trial Weapons
    if matchResult then
      messageType = "TRIAL"
    end
  end

  if not matchResult then
    return
  end

  if newMessage == lastMessage and os.clock() - 1 < lastMessageTime and not safeMessageTypes:contains(messageType) then
    return
  end
  newMessage = Utils.convert_to_utf8(newMessage)
  lastMessage = newMessage
  lastMessageTime = os.clock()

  if (messageType == 'DROPS') then
    table.insert(chatDropAccumulator, newMessage)
  end
  local data = {}
  if (messageType == 'DROPS') then
    coroutine.schedule(function()
      if (#chatDropAccumulator == 0) then
        return
      end
      data = PSUI.toJSON({
        playerId = player.id,
        playerName = player.name,
        messageType = messageType,
        messages = chatDropAccumulator
      })
      PSUI.post_json('set_messages', data)
      chatDropAccumulator = S {}
    end, 3)
  else
    local messageAsTable = S {}
    table.insert(messageAsTable, newMessage)
    data = {
      playerId = player.id,
      playerName = player.name,
      messageType = messageType,
      messages = messageAsTable
    }
    PSUI.post_json('set_messages', PSUI.toJSON(data))
  end
end

function PlayerService.log(message, messageType)
  local delieveredMessage = ''
  if messageType == 'WARNING' then
    delieveredMessage = chat_yellow .. message
  elseif messageType == 'ERROR' then
    delieveredMessage = chat_red .. message
  else
    delieveredMessage = chat_purple .. message
  end
  windower.add_to_chat(207, 'PlayerService: ' .. delieveredMessage)
end

windower.register_event('load', PlayerService.set_jobs)
windower.register_event('incoming text', PlayerService.handle_incoming_text)
windower.register_event('incoming chunk', PlayerService.incoming_chunk_handler)
windower.register_event('outgoing chunk', PlayerService.outgoing_chunk_handler)
-- windower.register_event('action', PlayerService.handle_action)
windower.register_event('gain experience', PlayerService.fetchPlayerStats)
windower.register_event('lose experience', PlayerService.fetchPlayerStats)
windower.register_event('hpp change', PlayerService.set_hpp)
windower.register_event('mpp change', PlayerService.set_mpp)
windower.register_event('tp change', PlayerService.set_tp)
windower.register_event('gain buff', PlayerService.set_buff_with_timers)
windower.register_event('lose buff', PlayerService.set_buff_with_timers)
windower.register_event('job change', PlayerService.set_jobs)
windower.register_event('zone change', PlayerService.set_zone)
windower.register_event('status change', PlayerService.set_player_status)
-- windower.register_event('time change', PlayerService.set_ability_recasts)

windower.register_event('addon command', function(...)
  local args = T {...}
  if args[1] == 'debug' then
    PlayerService.debugger = not PlayerService.debugger
    PlayerService.log(tostring(PlayerService.debugger))
  elseif args[1] == 'init' then
    PlayerService.initialize_player()
  elseif args[1] == 'refresh' then
    if args[2] == 'stats' then
      PlayerService.fetchPlayerStats()
      PlayerService.log('Refreshed player stats')
    else
      PlayerService.log('Invalid refresh command', "ERROR")
    end
  elseif args[1] == 'help' then
    windower.add_to_chat(207, 'PlayerService Commands:')
    windower.add_to_chat(207, 'ps debug - Toggles the debugger on and off.')
    windower.add_to_chat(207, 'ps init - Initializes the player.')
    windower.add_to_chat(207, 'ps refresh stats - Refreshes the player stats.')
  end
end)

PlayerService.set_online()
