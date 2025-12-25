<template>
  <div class="voice-control">
    <!-- 标题 -->
    <div class="header">
      <h1 class="title">AI语音控制</h1>
      <p class="subtitle">小宝</p>
    </div>

    <!-- 状态显示 -->
    <div class="status-section">
      <div class="status-card">
        <div class="status-icon" :class="{ active: isListening }">
          <svg viewBox="0 0 24 24" width="48" height="48">
            <path
              fill="currentColor"
              d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z"
            />
            <path
              fill="currentColor"
              d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z"
            />
          </svg>
        </div>
        <div class="status-text">{{ statusText }}</div>
      </div>
    </div>

    <!-- 语音按钮 -->
    <div class="control-section">
      <button
        class="voice-button"
        :class="{ listening: isListening, disabled: !supported || !wsConnected }"
        @touchstart="startListening"
        @touchend="stopListening"
        @mousedown="startListening"
        @mouseup="stopListening"
        :disabled="!supported || !wsConnected"
      >
        <div class="button-ripple" v-if="isListening"></div>
        <div class="button-text">
          {{ isListening ? '松开结束' : '按住说话' }}
        </div>
      </button>
    </div>

    <!-- 识别结果 -->
    <div class="result-section" v-if="transcript">
      <div class="result-card">
        <div class="result-label">识别内容</div>
        <div class="result-text">{{ transcript }}</div>
      </div>
    </div>

    <!-- AI回复 -->
    <div class="reply-section" v-if="reply">
      <div class="reply-card">
        <div class="reply-avatar">小宝</div>
        <div class="reply-content">
          <div class="reply-text">{{ reply }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷指令 -->
    <div class="shortcuts-section">
      <div class="shortcuts-title">快捷指令</div>
      <div class="shortcuts-grid">
        <button
          v-for="cmd in quickCommands"
          :key="cmd.text"
          class="shortcut-button"
          @click="sendCommand(cmd.text)"
        >
          {{ cmd.label }}
        </button>
      </div>
    </div>

    <!-- 不支持提示 -->
    <div class="error-tip" v-if="!supported">
      您的浏览器不支持语音识别，请使用Chrome浏览器
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import websocket from '@/api/websocket'

const supported = ref(false)
const isListening = ref(false)
const transcript = ref('')
const reply = ref('')
const wsConnected = ref(false)  // WebSocket 连接状态

let recognition = null

// 状态文本
const statusText = computed(() => {
  if (!supported.value) {
    return '浏览器不支持语音识别'
  }
  if (!wsConnected.value) {
    return '正在连接服务器...'
  }
  if (isListening.value) {
    return '正在监听...'
  }
  return '待命中'
})

// 快捷指令
const quickCommands = [
  { label: '开始抽奖', text: '开始' },
  { label: '停止', text: '停' },
  { label: '重置系统', text: '重置' }
]

onMounted(async () => {
  initSpeechRecognition()
  await connectWebSocket()
})

onUnmounted(() => {
  if (recognition) {
    recognition.stop()
  }
  // 断开 WebSocket 连接
  websocket.disconnect()
})

// 初始化语音识别
const initSpeechRecognition = () => {
  // 检查浏览器支持
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) {
    console.error('浏览器不支持语音识别')
    supported.value = false
    return
  }

  supported.value = true

  // 创建识别实例
  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.continuous = false
  recognition.interimResults = false
  recognition.maxAlternatives = 1

  // 识别结果
  recognition.onresult = (event) => {
    const result = event.results[0][0].transcript
    transcript.value = result
    console.log('识别结果：', result)

    // 发送到后端
    sendCommand(result)
  }

  // 识别错误
  recognition.onerror = (event) => {
    console.error('识别错误：', event.error)
    isListening.value = false

    if (event.error === 'no-speech') {
      reply.value = '没有听到您说话'
    } else if (event.error === 'audio-capture') {
      reply.value = '无法访问麦克风'
    } else {
      reply.value = '识别失败，请重试'
    }
  }

  // 识别结束
  recognition.onend = () => {
    isListening.value = false
  }
}

// 开始监听
const startListening = () => {
  if (!supported.value || isListening.value) {
    return
  }

  try {
    transcript.value = ''
    reply.value = ''
    recognition.start()
    isListening.value = true
  } catch (error) {
    console.error('启动识别失败：', error)
  }
}

// 停止监听
const stopListening = () => {
  if (!isListening.value) {
    return
  }

  try {
    recognition.stop()
  } catch (error) {
    console.error('停止识别失败：', error)
  }
}

// 发送指令
const sendCommand = (text) => {
  if (!text) {
    return
  }

  transcript.value = text

  try {
    websocket.sendVoiceCommand(text)
  } catch (error) {
    console.error('发送指令失败：', error)
    reply.value = '发送失败，请检查网络连接'
  }
}

// 连接 WebSocket
const connectWebSocket = async () => {
  try {
    console.log('正在连接 WebSocket...')
    await websocket.connect()
    wsConnected.value = true
    console.log('WebSocket 连接成功')

    // 连接成功后订阅消息
    subscribeWebSocket()
  } catch (error) {
    console.error('WebSocket 连接失败：', error)
    wsConnected.value = false
    reply.value = 'WebSocket 连接失败，请刷新页面重试'
  }
}

// 订阅WebSocket消息
const subscribeWebSocket = () => {
  try {
    websocket.subscribeCommandResult((data) => {
      console.log('收到指令结果：', data)
      reply.value = data.reply || '指令已执行'
    })
  } catch (error) {
    console.error('订阅消息失败：', error)
  }
}
</script>

<style scoped lang="scss">
.voice-control {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.header {
  text-align: center;
  padding: 30px 0;

  .title {
    font-size: 2rem;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .subtitle {
    font-size: 1.2rem;
    opacity: 0.8;
  }
}

.status-section {
  padding: 20px 0;

  .status-card {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 30px;
    text-align: center;
    border: 2px solid rgba(255, 255, 255, 0.2);

    .status-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;

      &.active {
        background: rgba(255, 255, 255, 0.4);
        animation: pulse 1.5s ease-in-out infinite;
      }

      svg {
        color: white;
      }
    }

    .status-text {
      font-size: 1.2rem;
      font-weight: 500;
    }
  }
}

.control-section {
  padding: 40px 0;
  text-align: center;

  .voice-button {
    position: relative;
    width: 200px;
    height: 200px;
    border-radius: 50%;
    border: none;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
    font-size: 1.2rem;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 10px 40px rgba(245, 87, 108, 0.5);
    transition: all 0.3s;
    overflow: hidden;

    &:active:not(.disabled) {
      transform: scale(0.95);
    }

    &.listening {
      animation: buttonPulse 1.5s ease-in-out infinite;
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .button-ripple {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100%;
      height: 100%;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.3);
      animation: ripple 1.5s ease-out infinite;
    }

    .button-text {
      position: relative;
      z-index: 1;
    }
  }
}

.result-section {
  margin-bottom: 20px;

  .result-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 15px;
    padding: 20px;
    border: 2px solid rgba(255, 255, 255, 0.2);

    .result-label {
      font-size: 0.9rem;
      opacity: 0.7;
      margin-bottom: 10px;
    }

    .result-text {
      font-size: 1.2rem;
      font-weight: 500;
    }
  }
}

.reply-section {
  margin-bottom: 20px;

  .reply-card {
    display: flex;
    gap: 15px;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: 15px;
    padding: 20px;
    border: 2px solid rgba(255, 255, 255, 0.2);
    animation: slideIn 0.3s ease-out;

    .reply-avatar {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.9rem;
      font-weight: bold;
      flex-shrink: 0;
    }

    .reply-content {
      flex: 1;

      .reply-text {
        font-size: 1.1rem;
        line-height: 1.6;
      }
    }
  }
}

.shortcuts-section {
  margin-top: 40px;

  .shortcuts-title {
    font-size: 1.1rem;
    font-weight: 500;
    margin-bottom: 15px;
    opacity: 0.9;
  }

  .shortcuts-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;

    .shortcut-button {
      padding: 15px;
      background: rgba(255, 255, 255, 0.15);
      backdrop-filter: blur(10px);
      border: 2px solid rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      color: white;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s;

      &:active {
        transform: scale(0.95);
        background: rgba(255, 255, 255, 0.25);
      }
    }
  }
}

.error-tip {
  margin-top: 20px;
  padding: 15px;
  background: rgba(255, 87, 87, 0.2);
  border: 2px solid rgba(255, 87, 87, 0.5);
  border-radius: 12px;
  text-align: center;
  font-size: 0.95rem;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

@keyframes buttonPulse {
  0%, 100% {
    box-shadow: 0 10px 40px rgba(245, 87, 108, 0.5);
  }
  50% {
    box-shadow: 0 10px 60px rgba(245, 87, 108, 0.8);
  }
}

@keyframes ripple {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

@keyframes slideIn {
  from {
    transform: translateX(-20px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
