<template>
  <el-container class="admin-app">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>抽奖管理</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        class="menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/participants">
          <el-icon><User /></el-icon>
          <span>人员管理</span>
        </el-menu-item>
        <el-menu-item index="/prizes">
          <el-icon><Trophy /></el-icon>
          <span>奖项管理</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Document /></el-icon>
          <span>抽奖记录</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="openMainScreen" :icon="Monitor">
            打开主屏幕
          </el-button>
          <el-button @click="openControlScreen" :icon="Iphone">
            打开控制端
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Monitor, Iphone } from '@element-plus/icons-vue'

const route = useRoute()

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': '数据看板',
    '/participants': '人员管理',
    '/prizes': '奖项管理',
    '/records': '抽奖记录',
    '/settings': '系统设置'
  }
  return titles[route.path] || '抽奖管理系统'
})

const openMainScreen = () => {
  window.open('/', '_blank')
}

const openControlScreen = () => {
  window.open('/control.html', '_blank')
}
</script>

<style scoped lang="scss">
.admin-app {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #bfcbd9;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3a4a;
    color: white;

    h2 {
      font-size: 1.2rem;
      font-weight: bold;
    }
  }

  .menu {
    border: none;
  }
}

.header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .header-left {
    .page-title {
      font-size: 1.2rem;
      font-weight: 500;
      color: #333;
    }
  }

  .header-right {
    display: flex;
    gap: 10px;
  }
}

.main {
  background-color: #f5f5f5;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
