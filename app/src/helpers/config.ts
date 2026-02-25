
const host = window.location.hostname
const port = 80
const protocol = window.location.protocol
const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:'
const apiPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD || port === 80
  ? `/api`
  : `:${port}`;
// const wsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
//   ? `/ws/players`
//   : `:${port + 1}/ws/players`;
const wsPath = `/ws/players`;
const fullUrl = `${protocol}//${host}${apiPath}`
const fullWsUrl = `${wsProtocol}//${host}${wsPath}`

const iconsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/icons/`;

const imagesPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/images/`;

const uiPackageVersion = __APP_VERSION__;

export { fullUrl, fullWsUrl, iconsPath, imagesPath, uiPackageVersion }