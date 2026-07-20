const host = import.meta.env.VITE_SERVER_URL || window.location.hostname
const port = import.meta.env.VITE_PORT || 80
const protocol = window.location.protocol
const apiPath =
  import.meta.env.MODE === 'staging' || import.meta.env.PROD || port === 80
    ? `/api`
    : `:${port}/api`
const wsPath = `/ws/players`

function buildWsUrl(host: string, protocol: string, configuredPort?: string | number) {
  const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:'
  const normalizedPort =
    configuredPort === undefined || configuredPort === null ? '' : String(configuredPort).trim()

  if (
    !normalizedPort ||
    (wsProtocol === 'ws:' && normalizedPort === '80') ||
    (wsProtocol === 'wss:' && normalizedPort === '443')
  ) {
    return `${wsProtocol}//${host}${wsPath}`
  }

  return `${wsProtocol}//${host}:${normalizedPort}${wsPath}`
}

function resolveWsPort(
  isProdLike: boolean,
  configuredAppPort?: string | number,
  configuredWsPort?: string | number
) {
  const normalizedWsPort =
    configuredWsPort === undefined || configuredWsPort === null
      ? ''
      : String(configuredWsPort).trim()

  if (normalizedWsPort) {
    return normalizedWsPort
  }

  if (isProdLike) {
    return undefined
  }

  return configuredAppPort
}

const fullUrl = `${protocol}//${host}${apiPath}`
const fullWsUrl = buildWsUrl(
  host,
  protocol,
  resolveWsPort(
    import.meta.env.MODE === 'staging' || import.meta.env.PROD,
    import.meta.env.VITE_PORT,
    import.meta.env.VITE_WEBSOCKET_PORT
  )
)

const iconsPath =
  import.meta.env.MODE === 'staging' || import.meta.env.PROD ? `/assets/` : `/src/assets/icons/`

const imagesPath =
  import.meta.env.MODE === 'staging' || import.meta.env.PROD ? `/assets/` : `/src/assets/images/`

const uiPackageVersion = __APP_VERSION__

export { buildWsUrl, resolveWsPort, fullUrl, fullWsUrl, iconsPath, imagesPath, uiPackageVersion }
