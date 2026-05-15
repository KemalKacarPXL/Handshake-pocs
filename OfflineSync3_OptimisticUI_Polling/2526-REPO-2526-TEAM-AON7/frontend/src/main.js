import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { syncQueue } from './utils/syncQueue.js'

import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

syncQueue.startPolling()

syncQueue.cleanup()
