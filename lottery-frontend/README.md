# 抽奖系统 - 前端

基于 Vue 3 + Vite 的抽奖系统前端应用，包含主屏幕、手机控制端和管理后台三个子应用。

## 技术栈

- Vue 3 (Composition API)
- Vite 5
- Vue Router 4
- Pinia (状态管理)
- Element Plus (UI组件库)
- GSAP (动画库)
- Axios (HTTP客户端)
- STOMP.js + SockJS (WebSocket)
- Web Speech API (语音识别)

## 项目结构

```
lottery-frontend/
├── index.html                   # 主屏幕入口
├── control.html                 # 手机控制端入口
├── admin.html                   # 管理后台入口
├── package.json
├── vite.config.js
└── src/
    ├── main-screen/             # 主屏幕应用
    │   ├── main.js
    │   ├── App.vue
    │   └── components/
    │       └── LotteryScreen.vue
    ├── control/                 # 手机控制端
    │   ├── main.js
    │   ├── App.vue
    │   └── components/
    │       └── VoiceControl.vue
    ├── admin/                   # 管理后台
    │   ├── main.js
    │   ├── App.vue
    │   └── views/
    │       ├── Dashboard.vue
    │       ├── Participants.vue
    │       ├── Prizes.vue
    │       ├── Records.vue
    │       └── Settings.vue
    ├── api/                     # API接口
    │   ├── request.js           # Axios实例
    │   ├── participant.js       # 人员API
    │   ├── prize.js             # 奖项API
    │   ├── lottery.js           # 抽奖API
    │   └── websocket.js         # WebSocket服务
    ├── stores/                  # Pinia状态管理
    │   ├── index.js
    │   └── lottery.js
    └── styles/                  # 全局样式
        └── common.scss
```

## 快速开始

### 1. 安装依赖

```bash
cd lottery-frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

开发服务器将在 http://localhost:3000 启动

### 3. 访问各个子应用

- 主屏幕：http://localhost:3000/
- 手机控制端：http://localhost:3000/control.html
- 管理后台：http://localhost:3000/admin.html

## 三个子应用说明

### 1. 主屏幕 (Main Screen)

用于投影仪显示，展示抽奖动画和结果。

功能特点：
- 炫酷的抽奖滚动动画
- 中奖结果卡片展示
- 实时统计信息
- 通过WebSocket接收控制指令

访问路径：`/` 或 `/index.html`

### 2. 手机控制端 (Control)

用于手机访问，通过语音控制抽奖。

功能特点：
- Web Speech API语音识别
- 按住说话的交互方式
- AI指令解析和反馈
- 快捷指令按钮
- WebSocket实时通信

访问路径：`/control.html`

支持的语音指令：
- 唤醒词：小宝
- 开始抽奖：一等奖开始、二等奖开始、开始
- 停止抽奖：停、暂停、停止
- 重置系统：重置、重来
- 作弊设置：给张三设置一等奖
- 撤销中奖：撤销张三的奖项

### 3. 管理后台 (Admin)

用于数据管理和系统配置。

功能特点：
- 数据看板：统计信息、奖项列表、最近记录
- 人员管理：增删改查、Excel导入
- 奖项管理：奖项配置、状态管理
- 抽奖记录：历史记录查询
- 系统设置：系统信息、重置操作

访问路径：`/admin.html`

## 开发说明

### API代理配置

在 `vite.config.js` 中配置了API代理：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    },
    '/ws': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      ws: true
    }
  }
}
```

### 状态管理

使用 Pinia 进行状态管理，主要的 store：

- **lotteryStore**: 管理抽奖相关状态
  - prizes: 奖项列表
  - participants: 参与人员列表
  - currentPrize: 当前抽奖奖项
  - isDrawing: 是否正在抽奖
  - winners: 中奖名单

### WebSocket通信

使用 STOMP协议进行WebSocket通信：

```javascript
// 连接
await websocket.connect()

// 发送语音指令
websocket.sendVoiceCommand(transcript)

// 订阅指令结果
websocket.subscribeCommandResult((data) => {
  console.log(data)
})

// 订阅抽奖结果
websocket.subscribeLotteryResult((result) => {
  console.log(result)
})
```

### 语音识别

使用浏览器原生的 Web Speech API：

```javascript
const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)()
recognition.lang = 'zh-CN'
recognition.continuous = false
recognition.interimResults = false

recognition.onresult = (event) => {
  const transcript = event.results[0][0].transcript
  // 处理识别结果
}

recognition.start()
```

注意：
- 仅Chrome/Edge浏览器支持语音识别
- 需要HTTPS或localhost环境
- 需要授予麦克风权限

## 构建部署

### 1. 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录。

### 2. 预览生产版本

```bash
npm run preview
```

### 3. 部署

将 `dist` 目录部署到静态服务器（如Nginx）即可。

#### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    # 主屏幕
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 手机控制端
    location /control.html {
        try_files $uri =404;
    }

    # 管理后台
    location /admin.html {
        try_files $uri =404;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 浏览器兼容性

- Chrome 88+
- Edge 88+
- Firefox 78+
- Safari 14+

语音识别功能仅支持：
- Chrome/Edge (基于Chromium)

## 使用流程

1. 打开管理后台，导入人员名单和配置奖项
2. 在投影仪上打开主屏幕
3. 用手机打开控制端（使用Chrome浏览器）
4. 通过语音"小宝"唤醒，然后说"一等奖开始"开始抽奖
5. 主屏幕会显示滚动动画，说"停"停止抽奖
6. 中奖结果会显示在主屏幕上

## 常见问题

### 1. 语音识别不工作

- 确保使用Chrome浏览器
- 检查是否授予了麦克风权限
- 确保网站使用HTTPS或localhost

### 2. WebSocket连接失败

- 检查后端服务是否启动
- 检查端口是否正确（默认8080）
- 查看浏览器控制台的错误信息

### 3. 页面显示不正常

- 清除浏览器缓存
- 检查网络连接
- 确保后端API正常运行

## 开发建议

1. 使用Chrome DevTools的移动设备模拟器测试手机端
2. 语音识别在安静环境下效果更好
3. 建议在年会前进行完整的彩排测试
4. 准备备用的键盘控制方案
