local http = require('socket.http')
local ltn12 = require('ltn12')
local host = "http://192.168.5.30:8080"

http.TIMEOUT = 0.5

local PlayerServiceInterface = {
  online = true,
  freezeOnlineCheck = false,
}
local players = {}

local function printHTTPError(status_code, status_text, endpoint)
  print("HTTP Response Status Code: ", status_code or "TIMEOUT")
  print("HTTP Response Status Text: ",
    (status_text or "TIMEOUT") .. " " .. endpoint)
  PlayerServiceInterface.online = false
  PlayerServiceInterface.freezeOnlineCheck = false
end

local function getHearBeat()
  if (PlayerServiceInterface.freezeOnlineCheck) then return end
  coroutine.schedule(function()
    local response_body = {}
    local _, status_code, headers, status_text = http.request({
      url = ("%s/health"):format(host),
      method = "GET",
      sink = ltn12.sink.table(response_body)
    })
    if status_code ~= 200 then
      printHTTPError(status_code, status_text, "/health")
    else
      if not PlayerServiceInterface.online then print("Heartbeat Online") end
      PlayerServiceInterface.online = true
    end
  end, math.random(0, 1))
end

function PlayerServiceInterface.post(endpoint, data)
  if not PlayerServiceInterface.online then return nil end

  coroutine.schedule(function()
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

    if status_code ~= 200 then
      printHTTPError(status_code, status_text, endpoint)
    end
  end, math.random(0, 1))
end

function PlayerServiceInterface.post_json(endpoint, data)
  if not PlayerServiceInterface.online then return nil end

  coroutine.schedule(function()
    local response_body = {}
    local _, status_code, headers, status_text = http.request({
      url = ("%s/%s"):format(host, endpoint),
      method = "POST",
      headers = {
        ["Content-Type"] = "application/json",
        ["Content-Length"] = tostring(#data)
      },
      source = ltn12.source.string(data),
      sink = ltn12.sink.table(response_body)
    })

    if status_code ~= 200 and status_text then
      printHTTPError(status_code, status_text, endpoint)
    end
  end, math.random(0, 1))
end

function PlayerServiceInterface.get(endpoint)
  if not PlayerServiceInterface.online then return nil end

  local response_body = {}
  local _, status_code, headers, status_text = http.request({
    url = ("%s/%s"):format(host, endpoint),
    method = "GET",
    sink = ltn12.sink.table(response_body)
  })

  if status_code ~= 200 and status_text then
    printHTTPError(status_code, status_text, endpoint)
  end

  return response_body
end

function PlayerServiceInterface.get_main_job(playerName)
  return players[playerName:lower()].mainJob
end

function PlayerServiceInterface.get_buffs(playerName)
  return players[playerName:lower()].buffs
end

function PlayerServiceInterface.toJSON(data)
  local parts = { "{" }
  local comma = false

  for key, value in pairs(data) do
    if comma then
      parts[#parts + 1] = ","
    else
      comma = true
    end

    parts[#parts + 1] = '"' .. key .. '":'

    if type(value) == "table" then
      parts[#parts + 1] = PlayerServiceInterface.toJSON(value)
    elseif type(value) == "string" then
      -- Escape backslashes first, then quotes and other control characters
      local escaped_value = value:gsub('[\\"\n\r\t\b\f]', function(c)
        local replacement = {
          ['\\'] = '\\\\',
          ['"'] = '\\"',
          ['\n'] = '\\n',
          ['\r'] = '\\r',
          ['\t'] = '\\t',
          ['\b'] = '\\b',
          ['\f'] = '\\f'
        }
        return replacement[c]
      end)
      parts[#parts + 1] = '"' .. escaped_value .. '"'
    elseif type(value) == "number" or type(value) == "boolean" then
      parts[#parts + 1] = tostring(value)
    else
      -- Handle unsupported data types
      parts[#parts + 1] = "null"
    end
  end

  parts[#parts + 1] = "}"
  return table.concat(parts)
end

getHearBeat:loop(10)

return PlayerServiceInterface
