<template>
  <div class="settings">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">
              AI智能抽奖系统
            </el-descriptions-item>
            <el-descriptions-item label="版本号">
              {{ systemInfo.version }}
            </el-descriptions-item>
            <el-descriptions-item label="服务器时间">
              {{ formatTime(systemInfo.serverTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="总人数">
              {{ systemInfo.participantStats?.total || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="已中奖人数">
              {{ systemInfo.participantStats?.won || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="中奖率">
              {{ systemInfo.participantStats?.winRate?.toFixed(2) || 0 }}%
            </el-descriptions-item>
          </el-descriptions>

          <el-button type="primary" style="margin-top: 20px" @click="loadSystemInfo">
            刷新信息
          </el-button>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统操作</span>
          </template>

          <el-space direction="vertical" :size="15" style="width: 100%">
            <el-alert
              title="危险操作"
              type="warning"
              description="以下操作会影响系统数据，请谨慎操作"
              :closable="false"
            />

            <el-button
              type="danger"
              size="large"
              style="width: 100%"
              @click="handleReset"
            >
              <el-icon><RefreshLeft /></el-icon>
              <span>重置系统</span>
            </el-button>

            <div style="font-size: 12px; color: #999">
              重置系统将清空所有抽奖数据和中奖记录，但不会删除人员和奖项配置
            </div>
          </el-space>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>
            <span>快捷入口</span>
          </template>

          <el-space direction="vertical" :size="10" style="width: 100%">
            <el-button style="width: 100%" @click="openMainScreen">
              <el-icon><Monitor /></el-icon>
              <span>打开主屏幕</span>
            </el-button>

            <el-button style="width: 100%" @click="openControlScreen">
              <el-icon><Iphone /></el-icon>
              <span>打开手机控制端</span>
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Monitor, Iphone } from '@element-plus/icons-vue'
import lotteryApi from '@/api/lottery'
import request from '@/api/request'

const systemInfo = ref({})

onMounted(() => {
  loadSystemInfo()
})

const loadSystemInfo = async () => {
  try {
    systemInfo.value = await request.get('/system/info')
  } catch (error) {
    ElMessage.error('加载系统信息失败：' + error.message)
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '重置系统将清空所有抽奖数据和中奖记录，此操作不可恢复。确定要继续吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await lotteryApi.reset()
    ElMessage.success('系统重置成功')
    loadSystemInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败：' + error.message)
    }
  }
}

const openMainScreen = () => {
  window.open('/', '_blank')
}

const openControlScreen = () => {
  window.open('/control.html', '_blank')
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.settings {
  // 样式
}
</style>
