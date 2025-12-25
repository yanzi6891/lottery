# AI智能抽奖系统 - 部署手册
## 1. 开发环境部署

### 1.1 安装前置软件

#### 1.1.1 安装JDK 17

**Windows**:
```bash
# 1. 下载
访问: https://adoptium.net/
下载: OpenJDK 17 (Windows x64 MSI)

# 2. 安装
运行下载的MSI文件
勾选 "Set JAVA_HOME variable"
勾选 "Add to PATH"

# 3. 验证
打开新终端:
java -version
# 应显示: openjdk version "17.x.x"
```

**Linux**:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证
java -version
```

#### 1.1.2 安装Maven

**Windows**:
```bash
# 1. 下载
访问: https://maven.apache.org/download.cgi
下载: apache-maven-3.9.6-bin.zip

# 2. 解压
解压到: D:\Program Files\apache-maven-3.9.6

# 3. 配置环境变量
MAVEN_HOME = D:\Program Files\apache-maven-3.9.6
PATH += %MAVEN_HOME%\bin

# 4. 验证
打开新终端:
mvn -version
# 应显示: Apache Maven 3.9.6
```

**Linux**:
```bash
# 下载并安装
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
tar -xzf apache-maven-3.9.6-bin.tar.gz
sudo mv apache-maven-3.9.6 /opt/

# 配置环境变量
echo 'export MAVEN_HOME=/opt/apache-maven-3.9.6' >> ~/.bashrc
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# 验证
mvn -version
```

**配置Maven镜像** (可选，提速):
编辑 `%MAVEN_HOME%/conf/settings.xml`:
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <name>Aliyun Maven</name>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

#### 2.1.3 安装Node.js

**Windows**:
```bash
# 1. 下载
访问: https://nodejs.org/
下载: LTS版本 (Windows 64-bit MSI)

# 2. 安装
运行MSI文件，全部使用默认设置

# 3. 验证
node -v  # v20.17.0
npm -v   # 10.x.x
```

**Linux**:
```bash
# 使用NodeSource仓库
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# 验证
node -v
npm -v
```

**配置npm镜像** (可选，提速):
```bash
npm config set registry https://registry.npmmirror.com
```

### 1.2 部署后端

#### 步骤1: 获取项目代码
```bash
cd C:\Users\Administrator\Desktop\lottery
cd lottery-backend
```

#### 步骤2: 配置数据库
编辑 `src/main/resources/application.yml`:
```yaml
spring:
  profiles:
    active: h2  # 开发环境使用H2数据库

---
spring:
  config:
    activate:
      on-profile: h2
  datasource:
    url: jdbc:h2:file:./data/lottery;MODE=MySQL;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
      path: /h2-console
```

#### 步骤3: 构建项目
```bash
# 首次构建（下载依赖，可能需要5-10分钟）
mvn clean package -DskipTests

# 查看构建结果
# 成功: target/lottery-backend-1.0.0.jar
```

#### 步骤4: 启动后端

**方式A: 使用Maven直接运行** (开发推荐):
```bash
mvn spring-boot:run

# 看到以下信息表示成功:
# Started LotteryApplication in X seconds
# Tomcat started on port(s): 8080
```

**方式B: 使用JAR文件运行**:
```bash
java -jar target/lottery-backend-1.0.0.jar

# 后台运行（Linux）:
nohup java -jar target/lottery-backend-1.0.0.jar > app.log 2>&1 &
```

**方式C: 使用启动脚本** (最简单):
```bash
# Windows
双击: start-backend.bat

# Linux
./start-backend.sh
```

#### 步骤5: 验证后端
```bash
# 健康检查
浏览器访问: http://localhost:8080/api/system/health
预期返回: {"code":200,"message":"success","data":"OK"}

# H2控制台（可选）
访问: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/lottery
Username: sa
Password: password
```

### 1.3 部署前端

#### 步骤1: 进入前端目录
```bash
cd C:\Users\Administrator\Desktop\lottery\lottery-frontend
```

#### 步骤2: 安装依赖
```bash
# 首次安装（需要1-3分钟）
npm install

# 如果失败，清理后重试:
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

#### 步骤3: 配置后端地址
编辑 `src/api/request.js`:
```javascript
const request = axios.create({
  baseURL: '/api',  // 开发环境通过Vite代理
  timeout: 10000
})
```

检查 `vite.config.js`:
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
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

#### 步骤4: 启动前端

**方式A: 开发模式** (推荐):
```bash
npm run dev

# 看到以下信息表示成功:
# VITE ready in XXX ms
# ➜ Local: http://localhost:3000/
```

**方式B: 使用启动脚本**:
```bash
# Windows
双击: start-frontend.bat

# Linux
./start-frontend.sh
```

#### 步骤5: 验证前端
```bash
浏览器访问:
✅ 主屏幕:   http://localhost:3000/
✅ 控制端:   http://localhost:3000/control.html (Chrome)
✅ 管理后台: http://localhost:3000/admin.html

检查点:
- 页面正常显示
- 无控制台错误
- WebSocket连接成功
```

### 1.4 开发环境测试

参考 `TEST_GUIDE.md` 和 `TESTING_SUMMARY.md` 进行完整测试。

快速测试流程:
```bash
1. 管理后台 → 添加测试人员（5人）
2. 管理后台 → 添加测试奖项（1个，2人）
3. 打开主屏幕 + 控制端
4. 控制端 → 说"小宝" → 确认回复
5. 说"开始" → 主屏幕滚动
6. 说"停" → 显示中奖者
7. 管理后台 → 查看记录
```