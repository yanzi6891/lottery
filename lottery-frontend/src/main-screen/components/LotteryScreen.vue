<template>
  <div class="lottery-screen">
    <!-- 背景粒子效果 -->
    <div class="particles"></div>

    <!-- 标题区域 -->
    <div class="header">
      <h1 class="title">{{ prizeTitle }}</h1>
      <p class="subtitle">{{ prizeSubtitle }}</p>
    </div>

    <!-- 抽奖滚动区域 -->
    <div class="lottery-container" v-if="lotteryStore.isDrawing">
      <div class="scroll-area" ref="scrollArea">
        <div
          v-for="(participant, index) in scrollList"
          :key="index"
          class="scroll-item"
        >
          <div class="name">{{ participant.name }}</div>
          <div class="info">{{ participant.department }}</div>
        </div>
      </div>
    </div>

    <!-- 中奖结果展示 -->
    <div class="winners-container" v-if="!lotteryStore.isDrawing && lotteryStore.winners.length > 0">
      <div class="winner-title">恭喜中奖</div>
      <div class="winners-grid">
        <div
          v-for="(winner, index) in lotteryStore.winners"
          :key="winner.id"
          class="winner-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="winner-avatar">{{ winner.name[0] }}</div>
          <div class="winner-name">{{ winner.name }}</div>
          <div class="winner-dept">{{ winner.department }}</div>
          <div class="winner-id">工号: {{ winner.employeeId }}</div>
        </div>
      </div>
    </div>

    <!-- 待机界面 -->
    <div class="idle-screen" v-if="!lotteryStore.isDrawing && lotteryStore.winners.length === 0">
      <div class="idle-logo">
        <svg viewBox="0 0 100 100" class="logo-svg">
          <circle cx="50" cy="50" r="45" fill="none" stroke="white" stroke-width="2" />
          <text x="50" y="60" text-anchor="middle" fill="white" font-size="40" font-weight="bold">抽</text>
        </svg>
      </div>
      <p class="idle-text">请通过手机语音控制开始抽奖</p>
      <div class="stats">
        <div class="stat-item">
          <div class="stat-value">{{ lotteryStore.availableParticipants.length }}</div>
          <div class="stat-label">待抽人数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ lotteryStore.pendingPrizes.length }}</div>
          <div class="stat-label">待抽奖项</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ lotteryStore.wonParticipants.length }}</div>
          <div class="stat-label">已中奖人数</div>
        </div>
      </div>
    </div>

    <!-- 音效提示 -->
    <audio ref="scrollAudio" src="/sounds/scroll.mp3" loop></audio>
    <audio ref="winAudio" src="/sounds/win.mp3"></audio>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useLotteryStore } from '@/stores/lottery'
import gsap from 'gsap'

const lotteryStore = useLotteryStore()

const scrollArea = ref(null)
const scrollAudio = ref(null)
const winAudio = ref(null)

// 滚动列表
const scrollList = ref([])
let scrollAnimation = null

// 奖项标题
const prizeTitle = computed(() => {
  if (lotteryStore.currentPrize) {
    return lotteryStore.currentPrize.name
  }
  return 'AI智能抽奖系统'
})

const prizeSubtitle = computed(() => {
  if (lotteryStore.currentPrize) {
    const remaining = lotteryStore.currentPrize.count - lotteryStore.currentPrize.drawnCount
    return `本轮抽取 ${remaining} 位幸运儿`
  }
  return '基于AI语音控制的智能抽奖'
})

// 监听抽奖状态
watch(() => lotteryStore.isDrawing, (isDrawing) => {
  if (isDrawing) {
    startScrollAnimation()
  } else {
    stopScrollAnimation()
  }
})

// 开始滚动动画
const startScrollAnimation = () => {
  // 准备滚动数据
  prepareScrollData()

  // 播放滚动音效
  if (scrollAudio.value) {
    scrollAudio.value.currentTime = 0
    scrollAudio.value.play()
  }

  // 创建滚动动画
  if (scrollArea.value) {
    gsap.to(scrollArea.value, {
      y: '-50%',
      duration: 1,
      ease: 'none',
      repeat: -1,
      onRepeat: () => {
        // 每次循环更新数据
        prepareScrollData()
      }
    })
  }
}

// 停止滚动动画
const stopScrollAnimation = () => {
  // 停止音效
  if (scrollAudio.value) {
    scrollAudio.value.pause()
  }

  // 停止动画
  if (scrollArea.value) {
    gsap.killTweensOf(scrollArea.value)
    gsap.to(scrollArea.value, {
      y: 0,
      duration: 0.5,
      ease: 'power2.out'
    })
  }

  // 播放中奖音效
  if (winAudio.value && lotteryStore.winners.length > 0) {
    winAudio.value.currentTime = 0
    winAudio.value.play()
  }
}

// 准备滚动数据
const prepareScrollData = () => {
  const available = lotteryStore.availableParticipants
  if (available.length === 0) return

  // 随机打乱并取多个副本
  const shuffled = [...available].sort(() => Math.random() - 0.5)
  scrollList.value = [
    ...shuffled,
    ...shuffled,
    ...shuffled
  ]
}

onMounted(() => {
  // 初始化粒子效果
  initParticles()
})

// 初始化粒子效果
const initParticles = () => {
  // TODO: 可以使用 canvas 实现粒子效果
}
</script>

<style scoped lang="scss">
.lottery-screen {
  position: relative;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  color: white;
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.header {
  text-align: center;
  padding: 40px 0;
  position: relative;
  z-index: 10;

  .title {
    font-size: 4rem;
    font-weight: bold;
    margin-bottom: 10px;
    text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    animation: glow 2s ease-in-out infinite alternate;
  }

  .subtitle {
    font-size: 1.5rem;
    opacity: 0.9;
  }
}

.lottery-container {
  position: relative;
  height: calc(100vh - 200px);
  overflow: hidden;
  padding: 0 100px;

  .scroll-area {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .scroll-item {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    text-align: center;
    border: 2px solid rgba(255, 255, 255, 0.2);

    .name {
      font-size: 3rem;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .info {
      font-size: 1.5rem;
      opacity: 0.8;
    }
  }
}

.winners-container {
  padding: 40px 100px;
  position: relative;
  z-index: 10;

  .winner-title {
    font-size: 3rem;
    font-weight: bold;
    text-align: center;
    margin-bottom: 40px;
    animation: bounce 1s ease-in-out;
  }

  .winners-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 30px;
    max-width: 1200px;
    margin: 0 auto;
  }

  .winner-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    text-align: center;
    border: 2px solid rgba(255, 255, 255, 0.3);
    animation: slideIn 0.5s ease-out forwards;
    transform: translateY(50px);
    opacity: 0;

    .winner-avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 2rem;
      font-weight: bold;
      margin: 0 auto 20px;
    }

    .winner-name {
      font-size: 1.8rem;
      font-weight: bold;
      margin-bottom: 10px;
    }

    .winner-dept {
      font-size: 1rem;
      opacity: 0.8;
      margin-bottom: 5px;
    }

    .winner-id {
      font-size: 0.9rem;
      opacity: 0.6;
    }
  }
}

.idle-screen {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  width: 100%;
  padding: 0 100px;

  .idle-logo {
    margin-bottom: 30px;

    .logo-svg {
      width: 200px;
      height: 200px;
      animation: rotate 10s linear infinite;
    }
  }

  .idle-text {
    font-size: 1.5rem;
    opacity: 0.8;
    margin-bottom: 60px;
  }

  .stats {
    display: flex;
    justify-content: center;
    gap: 80px;

    .stat-item {
      .stat-value {
        font-size: 3rem;
        font-weight: bold;
        margin-bottom: 10px;
      }

      .stat-label {
        font-size: 1.2rem;
        opacity: 0.7;
      }
    }
  }
}

@keyframes glow {
  from {
    text-shadow: 0 0 10px rgba(255, 255, 255, 0.5),
                 0 0 20px rgba(255, 255, 255, 0.3);
  }
  to {
    text-shadow: 0 0 20px rgba(255, 255, 255, 0.8),
                 0 0 30px rgba(255, 255, 255, 0.5);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes slideIn {
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
