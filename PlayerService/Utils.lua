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
  return windower.from_shift_jis(str)
end

function Utils.arrstring(...)
  local str = ''
  local args = {...}

  for i = 1, select('#', ...) do
    if i > 1 then
      str = str .. ' '
    end
    str = str .. tostring(args[i])
  end

  return str
end

return Utils