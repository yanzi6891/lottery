import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import prizeApi from '@/api/prize'
import participantApi from '@/api/participant'
import lotteryApi from '@/api/lottery'

export const useLotteryStore = defineStore('lottery', () => {
  // 状态
  const prizes = ref([])
  const participants = ref([])
  const currentPrize = ref(null)
  const isDrawing = ref(false)
  const winners = ref([])
  const chatMessages = ref([]) // 对话记录

  // 计算属性
  const pendingPrizes = computed(() =>
    prizes.value.filter(p => p.status === 'PENDING')
  )

  const availableParticipants = computed(() =>
    participants.value.filter(p => p.status === 'AVAILABLE')
  )

  const wonParticipants = computed(() =>
    participants.value.filter(p => p.status === 'WON')
  )

  // 加载奖项列表
  const loadPrizes = async () => {
    try {
      prizes.value = await prizeApi.findAll()
    } catch (error) {
      console.error('加载奖项失败：', error)
      throw error
    }
  }

  // 加载参与人员列表
  const loadParticipants = async () => {
    try {
      participants.value = await participantApi.findAll()
    } catch (error) {
      console.error('加载人员失败：', error)
      throw error
    }
  }

  // 加载所有数据
  const loadAll = async () => {
    await Promise.all([loadPrizes(), loadParticipants()])
  }

  // 开始抽奖
  const startDraw = (prize) => {
    currentPrize.value = prize
    isDrawing.value = true
    winners.value = []
  }

  // 停止抽奖
  const stopDraw = async () => {
    if (!currentPrize.value) {
      return
    }

    try {
      // 调用后端接口执行抽奖
      const result = await lotteryApi.draw(currentPrize.value.id, 'System')

      // 更新中奖名单
      winners.value = result.winners

      // 重新加载数据
      await loadAll()

      isDrawing.value = false

      return result
    } catch (error) {
      console.error('抽奖失败：', error)
      isDrawing.value = false
      throw error
    }
  }

  // 重置系统
  const reset = async () => {
    try {
      await lotteryApi.reset()
      await loadAll()
      currentPrize.value = null
      isDrawing.value = false
      winners.value = []
    } catch (error) {
      console.error('重置失败：', error)
      throw error
    }
  }

  // 撤销中奖
  const cancelWin = async (participantId) => {
    try {
      await lotteryApi.cancelWin(participantId, 'System')
      await loadAll()
    } catch (error) {
      console.error('撤销中奖失败：', error)
      throw error
    }
  }

  // 添加对话消息
  const addChatMessage = (message) => {
    chatMessages.value.push({
      id: Date.now(),
      timestamp: new Date(),
      ...message
    })

    // 只保留最近20条消息
    if (chatMessages.value.length > 20) {
      chatMessages.value = chatMessages.value.slice(-20)
    }
  }

  // 添加用户消息
  const addUserMessage = (text) => {
    addChatMessage({
      type: 'user',
      text: text,
      sender: '用户'
    })
  }

  // 添加AI回复
  const addAIMessage = (text, commandType = 'CHAT') => {
    addChatMessage({
      type: 'ai',
      text: text,
      sender: '小宝',
      commandType: commandType
    })
  }

  // 清空对话记录
  const clearChatMessages = () => {
    chatMessages.value = []
  }

  return {
    // 状态
    prizes,
    participants,
    currentPrize,
    isDrawing,
    winners,
    chatMessages,

    // 计算属性
    pendingPrizes,
    availableParticipants,
    wonParticipants,

    // 方法
    loadPrizes,
    loadParticipants,
    loadAll,
    startDraw,
    stopDraw,
    reset,
    cancelWin,
    addChatMessage,
    addUserMessage,
    addAIMessage,
    clearChatMessages
  }
})
