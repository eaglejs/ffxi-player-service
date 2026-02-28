
const host = import.meta.env.VITE_SERVER_URL || window.location.hostname
const port = import.meta.env.VITE_PORT || 80
const protocol = window.location.protocol
const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:'
const apiPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD || port === 80
  ? `/api`
  : `:${port}/api`;
// const wsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
//   ? `/ws/players`
//   : `:${port + 1}/ws/players`;
const wsPath = `/ws/players`;
const fullUrl = `${protocol}//${host}${apiPath}`
const fullWsUrl = `${wsProtocol}//${host}${port ? `:${port}` : ''}${wsPath}`

const iconsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/icons/`;

const imagesPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/images/`;

const uiPackageVersion = __APP_VERSION__;

export { fullUrl, fullWsUrl, iconsPath, imagesPath, uiPackageVersion }