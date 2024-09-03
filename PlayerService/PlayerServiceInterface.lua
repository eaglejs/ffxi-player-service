local http = require('socket.http')
local ltn12 = require('ltn12')
local host = "http://192.168.5.30:8080"

http.TIMEOUT = 2

local PlayerServiceInterface = {}
local players = {}

function PlayerServiceInterface.post(endpoint, data)
  local response_body = {}
  local _, status_code, headers, status_text = http.request({
    url = ("%s/%s"):format(host, endpoint),
    method = "POST",
    headers = {
      ["Content-Type"] = "application/x-www-form-urlencoded",
      ["Content-Length"] = tostring(#data)
    },
    source = ltn12.source.string(data),
    sink = ltn12.sink.table(response_body)
  })

  if status_code ~= 200 and status_text then
    print("HTTP Response Status Code: ", status_code)
    print("HTTP Response Status Text: ", status_text .. " " .. endpoint)
  end
end

function PlayerServiceInterface.get(endpoint)
  local response_body = {}
  local _, status_code, headers, status_text = http.request({
    url = ("%s/%s"):format(host, endpoint),
    method = "GET",
    sink = ltn12.sink.table(response_body)
  })

  if status_code ~= 200 and status_text then
    print("HTTP Response Status Code: ", status_code)
    print("HTTP Response Status Text: ", status_text .. " " .. endpoint)
  end

  return response_body
end

function PlayerServiceInterface.get_main_job(playerName)
  return players[playerName:lower()].mainJob
end

function PlayerServiceInterface.get_buffs(playerName)
  return players[playerName:lower()].buffs
end

function PlayerServiceInterface.set_ability_recasts(playerName, abilities)
  local abilitiesStr = "["
  for i, ability in ipairs(abilities) do
    abilitiesStr = abilitiesStr .. string.format("{\"ability\":\"%s\",\"recast\":%d}", ability.ability, ability.recast)
    if i < #abilities then
      abilitiesStr = abilitiesStr .. ","
    end
  end
  abilitiesStr = abilitiesStr .. "]"
  
  local data = "playerName=" .. playerName .. "&abilities=" .. abilitiesStr
  local response_body = {}
  local _, status_code, headers, status_text = http.request({
    url = ("%s/set_ability_recasts"):format(host),
    method = "POST",
    headers = {
      ["Content-Type"] = "application/x-www-form-urlencoded",
      ["Content-Length"] = tostring(#data)
    },
    source = ltn12.source.string(data),
    sink = ltn12.sink.table(response_body)
  })

  if status_code ~= 200 and status_text then
    print("HTTP Response Status Code: ", status_code)
    print("HTTP Response Status Text: ", status_text .. " " .. 'set_ability_recasts')
  end
end

return PlayerServiceInterface
