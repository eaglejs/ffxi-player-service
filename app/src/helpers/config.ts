
const host = window.location.hostname
const port = 8080
const protocol = window.location.protocol
const wsProtocol = protocol === 'https:' ? 'wss:' : 'ws:'
const apiPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/api`
  : `:${port}`;
const wsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/ws`
  : `:${port + 1}`;
const fullUrl = `${protocol}//${host}${apiPath}`
const fullWsUrl = `${wsProtocol}//${host}${wsPath}`

const iconsPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/icons/`;

const imagesPath = import.meta.env.MODE === 'staging' || import.meta.env.PROD
  ? `/assets/`
  : `/src/assets/images/`;

export { fullUrl, fullWsUrl, iconsPath, imagesPath }