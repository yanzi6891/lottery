<template>
  <div class="control-app">
    <VoiceControl />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import VoiceControl from './components/VoiceControl.vue'
import websocket from '@/api/websocket'

onMounted(async () => {
  // 连接WebSocket
  try {
    await websocket.connect()
    console.log('WebSocket连接成功')
  } catch (error) {
    console.error('WebSocket连接失败：', error)
  }
})

onUnmounted(() => {
  websocket.disconnect()
})
</script>

<style scoped lang="scss">
.control-app {
  width: 100%;
  height: 100%;
}
</style>
