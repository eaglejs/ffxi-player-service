require('logger')
require('chat')

local Utils = {}

-- Strips a string of all colors.
function Utils.strip_colors(str)
  return (str:gsub('[' .. string.char(0x1E, 0x1F, 0x7F) .. '].', ''))
end

-- Strips a string of auto-translate tags.
function Utils.strip_auto_translate(str)
  return (str:gsub(string.char(0xEF) .. '[' .. string.char(0x27, 0x28) .. ']', ''))
end

-- Strips a string of all colors and auto-translate tags.
function Utils.strip_format(str)
  return str:strip_colors():strip_auto_translate()
end

function Utils.convert_to_utf8(str)
  -- strip out control characters
  str = str:gsub('[\1-\31]', '')
  -- strip out \n and \r
  str = str:gsub('[\n\r]', '')
  return windower.from_shift_jis(str)
end

function Utils.arrstring(...)
  local str = ''
  local args = { ... }

  for i = 1, select('#', ...) do
    if i > 1 then
      str = str .. ' '
    end
    str = str .. tostring(args[i])
  end

  return str
end

function Utils.calculate_server_start_time()
  -- Server start time in milliseconds (May 16, 2002)
  local server_start_time_ms = 1021555200000
  -- Increment period in years (2.27 years)
  local increment_period_years = 2.27
  -- Convert milliseconds to seconds for os.date() compatibility
  local start_time_sec = server_start_time_ms / 1000
  -- Get the current time in seconds since the Unix epoch
  local current_time_sec = os.time()

  -- Calculate the difference in seconds between current time and start time
  local time_diff_sec = current_time_sec - start_time_sec

  -- Convert seconds to years (365.25 accounts for leap years)
  local time_diff_years = time_diff_sec / (365.25 * 24 * 60 * 60)

  -- Calculate the number of increments by dividing by the increment period
  local increments = time_diff_years / increment_period_years

  -- Round to the nearest whole number and return the result
  return math.floor(increments + 0.5)
end

function Utils.convert_server_time_to_local_time()
  local now = os.time()
  local server_time = os.time() + 0x100000000 / 60 * Utils.calculate_server_start_time()
  local h, m = (os.difftime(now, os.time(os.date('!*t', now))) / 3600):modf()
  local utc_time = os.date('!*t', 1009810800 + (server_time / 60) + 0x100000000 / 60 * Utils.calculate_server_start_time())
  return utc_time;
end

function Utils.get_duration_local_time(duration)
  local now = os.time()
  local h, m = (os.difftime(now, os.time(os.date('!*t', now))) / 3600):modf()
  local utc_time = os.date('!*t', 1009810800 + (duration / 60) + 0x100000000 / 60 *
  Utils.calculate_server_start_time())
  return utc_time;
end

function Utils.get_current_time()
  local now = os.time()
  local utc_time = os.date('!*t', now)
  return utc_time
end

function Utils.get_formatted_local_time(utc_time)
 return string.format("%04d-%02d-%02dT%02d:%02d:%02dZ", utc_time.year, utc_time.month, utc_time.day, utc_time.hour, utc_time.min, utc_time.sec)
end

return Utils
