_addon.name = 'PlayerService'
_addon.author = 'eaglejs'
_addon.version = "1.2024.5.19"
_addon.commands = { 'playerservice', 'ps', 'pserv' }

require('strings')
local packets = require('packets')
local res = require('resources')
local helpers = require('eaglejs/Helpers')
local Utils = require('./Utils')
local PSUI = require('./PlayerServiceInterface')
local chars = require('chat.chars')

local pingClock = os.time()
local chat_purple = string.char(0x1F, 200)
local chat_grey = string.char(0x1F, 160)
local chat_red = string.char(0x1F, 167)
local chat_white = string.char(0x1F, 001)
local chat_green = string.char(0x1F, 214)
local chat_yellow = string.char(0x1F, 036)
local chat_drk_blue = string.char(0x1F, 207)
local chat_pink = string.char(0x1E, 5)
local chat_lt_blue = string.char(0x1E, 6)
local partySlots = L { 'p1', 'p2', 'p3', 'p4', 'p5' }
local chatDropAccumulator = S {}
local chatCoroutine = nil
local keeperOfBuffs = S {}

math.randomseed(os.time())
local PlayerService = {
  name = 'PlayerService',
  active = true,
  debugger = false,
  currentDetailedBuffs = {},
  lastStatusFetch = os.time(),
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

  if (new == 0 and old == 1) then
    if (os.time() - PlayerService.lastStatusFetch) < math.random(300, 600) then
      return
    end
    PlayerService.fetchPlayerStats();
    PlayerService.lastStatusFetch = os.time()
  end
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

  if PlayerService.debugger then
    PlayerService.log('Setting MPP: ' .. player.vitals.mpp, 'DEBUG')
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
  local data = ('playerId=%s&playerName=%s' ..
    '&mainJobLevel=%s' ..
    '&subJobLevel=%s' ..
    '&masterLevel=%s' ..
    '&attack=%s' ..
    '&defense=%s' ..
    '&currentExemplar=%s' ..
    '&requiredExemplar=%s' ..
    '&baseSTR=%s' ..
    '&baseDEX=%s' ..
    '&baseVIT=%s' ..
    '&baseAGI=%s' ..
    '&baseINT=%s' ..
    '&baseMND=%s' ..
    '&baseCHR=%s' ..
    '&addedSTR=%s' ..
    '&addedDEX=%s' ..
    '&addedVIT=%s' ..
    '&addedAGI=%s' ..
    '&addedINT=%s' ..
    '&addedMND=%s' ..
    '&addedCHR=%s' ..
    '&fireResistance=%s' ..
    '&iceResistance=%s' ..
    '&windResistance=%s' ..
    '&earthResistance=%s' ..
    '&lightningResistance=%s' ..
    '&waterResistance=%s' ..
    '&lightResistance=%s' ..
    '&darkResistance=%s' ..
    '&title=%s' ..
    '&nationRank=%s')
    :format(
      player.id,
      player.name,
      stats.mainJobLevel or 0,
      stats.subJobLevel or 0,
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

  local data = ('playerId=%s&playerName=%s' ..
    '&conquestPointsBastok=%s' ..
    '&conquestPointsSandoria=%s' ..
    '&conquestPointsWindurst=%s' ..
    '&deeds=%s' ..
    '&dominionNotes=%s' ..
    '&imperialStanding=%s' ..
    '&loginPoints=%s' ..
    '&nyzulTokens=%s' ..
    '&sparksOfEminence=%s' ..
    '&therionIchor=%s' ..
    '&unityAccolades=%s' ..
    '&voidstones=%s')
    :format(
      player.id,
      player.name,
      packet_currency.conquestPointsBastok or 0,
      packet_currency.conquestPointsSandoria or 0,
      packet_currency.conquestPointsWindurst or 0,
      packet_currency.deeds or 0,
      packet_currency.dominionNotes or 0,
      packet_currency.imperialStanding or 0,
      packet_currency.loginPoints or packet_currency.nyzulTokens,
      packet_currency.nyzulTokens or 0,
      packet_currency.sparksOfEminence or 0,
      packet_currency.therionIchor or 0,
      packet_currency.unityAccolades or 0,
      packet_currency.voidstones or 0
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
    ['coalitionImprimaturs'] = packet['Coalition Imprimaturs'],
    ['eschaBeads'] = packet['Escha Beads'],
    ['eschaSilt'] = packet['Escha Silt'],
    ['gallantry'] = packet['Badges of Gallantry'],
    ['gallimaufry'] = packet['Gallimaufry'],
    ['hallmarks'] = packet['Hallmarks'],
    ['mogSegments'] = packet['Mog Segments'],
    ['mweyaPlasmCorpuscles'] = packet['Mweya Plasm Corpuscles'],
    ['potpourri'] = packet['Potpourri'],
    ['temenosUnits'] = packet['Temenos Units'],
    ['apollyonUnits'] = packet['Apollyon Units'],
  }

  local data = ('playerId=%s&playerName=%s' ..
    '&coalitionImprimaturs=%s' ..
    '&domainPoints=%s' ..
    '&eschaBeads=%s' ..
    '&eschaSilt=%s' ..
    '&gallantry=%s' ..
    '&gallimaufry=%s' ..
    '&hallmarks=%s' ..
    '&mogSegments=%s' ..
    '&mweyaPlasmCorpuscles=%s' ..
    '&potpourri=%s' ..
    '&temenosUnits=%s' ..
    '&apollyonUnits=%s')
    :format(
      player.id,
      player.name,
      packet_currency.coalitionImprimaturs or 0,
      packet_currency.domainPoints or 0,
      packet_currency.eschaBeads or 0,
      packet_currency.eschaSilt or 0,
      packet_currency.gallantry or 0,
      packet_currency.gallimaufry or 0,
      packet_currency.hallmarks or 0,
      packet_currency.mogSegments or 0,
      packet_currency.mweyaPlasmCorpuscles or 0,
      packet_currency.potpourri or 0,
      packet_currency.temenosUnits or 0,
      packet_currency.apollyonUnits or 0
    )

  PSUI.post('set_currency2', data)
end

function PlayerService.set_buffs_with_timers()
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

  PSUI.post('set_zone', data)
  PSUI.post('reset_exp_history', ("playerId=%s&playerName=%s"):format(player.id, player.name))
  PlayerService.updateGil()
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
  elseif id == 0x063 then           -- the packet character data is in
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
          local fullSpellName = nil
          for buff, detailedBuff in pairs(PlayerService.currentDetailedBuffs) do
            if (detailedBuff.spell:contains(buff_name)) then
              fullSpellName = detailedBuff.spell
            end
          end

          if (buff_id) then
            local buff = {
              ["buff_id"] = buff_id,
              ["buff_name"] = fullSpellName or buff_name,
              ["buff_type"] = buff_name,
              ["buff_duration"] = buff_duration,
              ["utc_time"] = formatted_utc_time,
              ["timestamp"] = os.time()
            }
            table.insert(buffs, buff)
            if buff_name:lower() == 'march' then
              -- print(buff_duration)
              -- helpers.printTable(buff)
            end
          end
        end
      end
      if (deadCounter <= 1) then
        keeperOfBuffs = buffs
      end
      PlayerService.set_buffs_with_timers()
    elseif packet['Order'] == 2 then
      local meritPackage = {
        playerId = windower.ffxi.get_player().id,
        playerName = windower.ffxi.get_player().name,
        total = packet['Merit Points'],
        max = packet['Max Merit Points']
      }
      coroutine.schedule(function() PSUI.post_json('update_merits', PSUI.toJSON(meritPackage)) end, .5)
    elseif packet['Order'] == 5 then
      local player = windower.ffxi.get_player()
      if player then
        local job = player.main_job_full
        local cpPackage = {
          playerId = player.id,
          playerName = player.name,
          numberOfJobPoints = packet[job .. ' Job Points']
        }

        coroutine.schedule(function() PSUI.post_json('update_capacity_points', PSUI.toJSON(cpPackage)) end, 1)
      end
    end
  elseif id == 0x02D then
    -- (8|253) = exp, (371|372) = limit, (718|735) = capacity, (809|810) = exemplar
    -- Param 1 = EXP, Param 2 = Chain #
    local expMessageTypes = S { 8, 253, 371, 372, 718, 735, 809, 810 }
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
      coroutine.schedule(function() PSUI.post_json('update_exp_history', PSUI.toJSON(expPackage)) end, 2)
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
  coroutine.schedule(PlayerService.send_stats_request, math.random(0, 1) / 10)
  coroutine.sleep(1.1)
  coroutine.schedule(PlayerService.send_currency1_request, math.random(0, 1) / 10)
  coroutine.sleep(1.1)
  coroutine.schedule(PlayerService.send_currency2_request, math.random(0, 1) / 10)
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

function PlayerService.handle_outgoing_text(original, modified, original_mode, modified_mode, blocked)
  local newMessage = Utils.strip_colors(original)
  local match = string.match(newMessage, '^/ma "(.*)"')
  if match then
    newMessage = match
    local timestamp = os.time()
    PlayerService.currentDetailedBuffs[newMessage] = {
      ['spell'] = newMessage,
      ['timestamp'] = timestamp
    }
    local ipc_message = ('PlayerService:spell="%s"&timestamp="%s"'):format(newMessage, timestamp)
    windower.send_ipc_message(ipc_message)
  end
end

function PlayerService.handle_ipc_message(message)
  if message:startswith('PlayerService:Spell') then
    local spell = string.match(message, 'spell="(.-)"')
    local timestamp = string.match(message, 'timestamp="(.-)"')
    if spell and spell ~= '' then
      PlayerService.currentSpells[spell] = {
        ['spell'] = spell,
        ['timestamp'] = timestamp
      }
    end
  end
end

function PlayerService.handle_incoming_text(original, original_mode, blocked)
  local player = windower.ffxi.get_player()
  local messageType = nil

  if not player or not PlayerService.active then
    return
  end

  local newMessage = Utils.strip_colors(original)

  local patterns = {
    { '^%(%a+%)',                            "PARTY" },
    { '^%a+>>',                              "TELL" },
    { '^>>%a+',                              "TELL" },
    { '^%[1]<[%u][%a]+>',                    "LINKSHELL1" },
    { '^%[2]<[%u][%a]+>',                    "LINKSHELL2" },
    { '^[%a+] :',                            "SHOUT" },
    { '^%a+%[%a+%]:',                        "YELL" },
    { '^{%a+',                               "UNITY" },
    { '^%a+\'?%a*%a+ :',                     "SAY" },
    { ('^You find a'),                       "DROPS" },
    { ('^%s obtains a'):format(player.name), "OBTAINED" },
    { '^Obtained key item:',                 "OBTAINED" },
    { '^%a+ %d+:',                           "TRIAL" }
  }

  for _, pattern in ipairs(patterns) do
    local match = string.match(newMessage, pattern[1])
    if match then
      messageType = pattern[2]
      break
    end
  end

  if not messageType then
    return
  end

  newMessage = Utils.convert_to_utf8(newMessage)

  if messageType:contains('DROPS') or messageType:contains('OBTAINED') then
    table.insert(chatDropAccumulator, newMessage)
    if (chatCoroutine) then return end
    chatCoroutine = coroutine.schedule(function()
      if #chatDropAccumulator > 0 then
        PlayerService.write_chat_to_file(
          player.id,
          player.name,
          messageType,
          chatDropAccumulator
        )
        chatDropAccumulator = S {}
        chatCoroutine = nil
      end
    end, 2)
  else
    local messageAsTable = S {}
    table.insert(messageAsTable, newMessage)
    PlayerService.write_chat_to_file(
      player.id,
      player.name,
      messageType,
      messageAsTable
    )
  end
end

function PlayerService.get_chat_log_path(playerName)
  local addon_path = windower.addon_path
  local chat_logs_dir = addon_path .. 'chat_logs/'
  
  -- Create directory if it doesn't exist
  windower.create_dir(chat_logs_dir)
  
  return chat_logs_dir .. playerName .. '_chat.jsonl'
end

function PlayerService.clear_chat_logs()
  local addon_path = windower.addon_path
  local chat_logs_dir = addon_path .. 'chat_logs/'
  
  -- Create directory if it doesn't exist
  windower.create_dir(chat_logs_dir)
  
  -- Get all files in the directory
  local files = windower.get_dir(chat_logs_dir)
  
  if files then
    for _, filename in ipairs(files) do
      -- Only clear .jsonl files
      if filename:match('%.jsonl$') then
        local file_path = chat_logs_dir .. filename
        local file = io.open(file_path, 'w')
        if file then
          file:close()
          PlayerService.log('Cleared chat log: ' .. filename, 'DEBUG')
        end
      end
    end
  end
end

function PlayerService.write_chat_to_file(playerId, playerName, messageType, messages)
  local file_path = PlayerService.get_chat_log_path(playerName)
  local file = io.open(file_path, 'a')
  
  if not file then
    PlayerService.log('Failed to open chat log file: ' .. file_path, 'ERROR')
    return
  end
  
  local chat_entry = {
    playerId = playerId,
    playerName = playerName,
    messageType = messageType,
    messages = messages,
    timestamp = os.time(),
    utc_timestamp = Utils.get_formatted_local_time(Utils.get_current_time())
  }
  
  local json_line = PSUI.toJSON(chat_entry)
  file:write(json_line .. '\n')
  file:close()
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

windower.register_event('load', function()
  PlayerService.clear_chat_logs()
  PlayerService.set_jobs()
end)
windower.register_event('incoming text', PlayerService.handle_incoming_text)
windower.register_event('outgoing text', PlayerService.handle_outgoing_text)
windower.register_event('incoming chunk', PlayerService.incoming_chunk_handler)
windower.register_event('ipc message', PlayerService.handle_ipc_message)
windower.register_event('outgoing chunk', PlayerService.outgoing_chunk_handler)
windower.register_event('hpp change', PlayerService.set_hpp)
windower.register_event('mpp change', PlayerService.set_mpp)
windower.register_event('tp change', PlayerService.set_tp)
windower.register_event('lose buff', PlayerService.set_buffs_with_timers)
windower.register_event('job change', PlayerService.set_jobs)
windower.register_event('zone change', PlayerService.set_zone)
windower.register_event('status change', PlayerService.set_player_status)

windower.register_event('addon command', function(...)
  local args = T { ... }
  if args[1] == 'debug' then
    PlayerService.debugger = not PlayerService.debugger
    PlayerService.log('Debugger - ' .. tostring(PlayerService.debugger))
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
