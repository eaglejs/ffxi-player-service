import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap"
import './assets/main.scss'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import RadialProgress from "vue3-radial-progress";

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(RadialProgress)

app.mount('#app')
