import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import pinia from '@/stores'
import '@/styles/common.scss'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/Dashboard.vue')
  },
  {
    path: '/participants',
    name: 'Participants',
    component: () => import('./views/Participants.vue')
  },
  {
    path: '/prizes',
    name: 'Prizes',
    component: () => import('./views/Prizes.vue')
  },
  {
    path: '/records',
    name: 'Records',
    component: () => import('./views/Records.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('./views/Settings.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(pinia)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
