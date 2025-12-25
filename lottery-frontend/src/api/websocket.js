import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = {}
  }

  /**
   * 连接WebSocket
   */
  connect() {
    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS('/ws'),
        debug: (str) => {
          console.log('[WebSocket]', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket连接成功')
          this.connected = true
          resolve()
        },
        onStompError: (frame) => {
          console.error('WebSocket错误：', frame)
          this.connected = false
          reject(new Error(frame.headers['message']))
        },
        onWebSocketClose: () => {
          console.log('WebSocket连接关闭')
          this.connected = false
        }
      })

      this.client.activate()
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.connected = false
    }
  }

  /**
   * 发送语音指令
   */
  sendVoiceCommand(transcript, sessionId = 'default') {
    if (!this.connected) {
      throw new Error('WebSocket未连接')
    }

    this.client.publish({
      destination: '/app/voice-command',
      body: JSON.stringify({ transcript, sessionId })
    })
  }

  /**
   * 订阅指令结果
   */
  subscribeCommandResult(callback) {
    if (!this.connected) {
      throw new Error('WebSocket未连接')
    }

    const subscription = this.client.subscribe('/topic/command-result', (message) => {
      const data = JSON.parse(message.body)
      callback(data)
    })

    this.subscriptions['commandResult'] = subscription
    return subscription
  }

  /**
   * 订阅抽奖结果
   */
  subscribeLotteryResult(callback) {
    if (!this.connected) {
      throw new Error('WebSocket未连接')
    }

    const subscription = this.client.subscribe('/topic/lottery-result', (message) => {
      const data = JSON.parse(message.body)
      callback(data)
    })

    this.subscriptions['lotteryResult'] = subscription
    return subscription
  }

  /**
   * 取消订阅
   */
  unsubscribe(key) {
    if (this.subscriptions[key]) {
      this.subscriptions[key].unsubscribe()
      delete this.subscriptions[key]
    }
  }

  /**
   * 取消所有订阅
   */
  unsubscribeAll() {
    Object.keys(this.subscriptions).forEach(key => {
      this.unsubscribe(key)
    })
  }
}

// 导出单例
export default new WebSocketService()
