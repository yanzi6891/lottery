<template>
  <div class="chat-dialog" :class="{ collapsed: isCollapsed }">
    <!-- 标题栏 -->
    <div class="chat-header" @click="toggleCollapse">
      <div class="header-left">
        <div class="avatar">小宝</div>
        <div class="title">与小宝对话</div>
      </div>
      <div class="header-right">
        <button class="collapse-btn" :title="isCollapsed ? '展开' : '收起'">
          {{ isCollapsed ? '▲' : '▼' }}
        </button>
      </div>
    </div>

    <!-- 对话内容 -->
    <div class="chat-content" v-show="!isCollapsed" ref="chatContentRef">
      <div v-if="chatMessages.length === 0" class="empty-tip">
        暂无对话记录
      </div>
      <div v-else class="message-list">
        <div
          v-for="message in chatMessages"
          :key="message.id"
          class="message-item"
          :class="message.type"
        >
          <div class="message-sender">{{ message.sender }}</div>
          <div class="message-bubble">
            <div class="message-text">{{ message.text }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useLotteryStore } from '@/stores/lottery'

const lotteryStore = useLotteryStore()
const isCollapsed = ref(false)
const chatContentRef = ref(null)

// 获取对话记录
const chatMessages = computed(() => lotteryStore.chatMessages)

// 折叠/展开
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动到底部
watch(chatMessages, () => {
  if (!isCollapsed.value) {
    scrollToBottom()
  }
}, { deep: true })
</script>

<style scoped lang="scss">
.chat-dialog {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 380px;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
  transition: all 0.3s ease;

  &.collapsed {
    .chat-content {
      max-height: 0;
      opacity: 0;
    }
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: bold;
      color: white;
    }

    .title {
      font-size: 16px;
      font-weight: 600;
      color: white;
    }
  }

  .header-right {
    .collapse-btn {
      background: transparent;
      border: none;
      color: rgba(255, 255, 255, 0.6);
      font-size: 14px;
      cursor: pointer;
      padding: 4px 8px;
      transition: color 0.2s;

      &:hover {
        color: white;
      }
    }
  }
}

.chat-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px;
  transition: all 0.3s ease;

  /* 自定义滚动条 */
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .empty-tip {
    text-align: center;
    padding: 40px 20px;
    color: rgba(255, 255, 255, 0.4);
    font-size: 14px;
  }

  .message-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .message-item {
    display: flex;
    flex-direction: column;
    animation: messageSlideIn 0.3s ease-out;

    &.user {
      align-items: flex-end;

      .message-sender {
        color: #4ade80;
      }

      .message-bubble {
        background: linear-gradient(135deg, #4ade80 0%, #3b82f6 100%);
        border-radius: 12px 12px 4px 12px;
      }
    }

    &.ai {
      align-items: flex-start;

      .message-sender {
        color: #a78bfa;
      }

      .message-bubble {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px 12px 12px 4px;
      }
    }

    .message-sender {
      font-size: 12px;
      font-weight: 600;
      margin-bottom: 6px;
      padding: 0 8px;
    }

    .message-bubble {
      max-width: 80%;
      padding: 12px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

      .message-text {
        color: white;
        font-size: 14px;
        line-height: 1.6;
        word-wrap: break-word;
        margin-bottom: 6px;
      }

      .message-time {
        color: rgba(255, 255, 255, 0.6);
        font-size: 11px;
        text-align: right;
      }
    }
  }
}

@keyframes messageSlideIn {
  from {
    transform: translateY(10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
