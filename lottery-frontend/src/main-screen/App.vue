<template>
  <div class="lottery-main">
    <LotteryScreen />
    <ChatDialog />
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import LotteryScreen from './components/LotteryScreen.vue'
import ChatDialog from './components/ChatDialog.vue'
import { useLotteryStore } from '@/stores/lottery'
import websocket from '@/api/websocket'

const lotteryStore = useLotteryStore()

onMounted(async () => {
  // 加载数据
  await lotteryStore.loadAll()

  // 连接WebSocket
  try {
    await websocket.connect()

    // 订阅指令结果
    websocket.subscribeCommandResult((data) => {
      console.log('收到指令结果：', data)
      handleCommandResult(data)
    })
  } catch (error) {
    console.error('WebSocket连接失败：', error)
  }
})

onUnmounted(() => {
  websocket.disconnect()
})

// 处理指令结果
const handleCommandResult = (data) => {
  if (!data.success) {
    return
  }

  // 添加用户消息和AI回复到对话记录
  if (data.rawText) {
    lotteryStore.addUserMessage(data.rawText)
  }
  if (data.reply) {
    lotteryStore.addAIMessage(data.reply, data.type)
  }

  const action = data.data?.action

  switch (action) {
    case 'START_DRAW':
      // 开始抽奖
      const prize = data.data.prize
      lotteryStore.startDraw(prize)
      break

    case 'STOP_DRAW':
      // 停止抽奖
      lotteryStore.stopDraw()
      break

    case 'RESET':
      // 重置系统
      lotteryStore.loadAll()
      break

    case 'CANCEL':
      // 撤销中奖
      lotteryStore.loadAll()
      break
  }
}
</script>

<style scoped lang="scss">
.lottery-main {
  width: 100%;
  height: 100%;
}
</style>
