<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ participantStats.total || 0 }}</div>
              <div class="stat-label">总人数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon><TrophyBase /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ participantStats.won || 0 }}</div>
              <div class="stat-label">已中奖人数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon><Checked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ participantStats.available || 0 }}</div>
              <div class="stat-label">可抽人数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon><Present /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ participantStats.winRate?.toFixed(1) || 0 }}%</div>
              <div class="stat-label">中奖率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 奖项列表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>奖项列表</span>
              <el-button type="primary" size="small" @click="goToPrizes">管理奖项</el-button>
            </div>
          </template>

          <el-table :data="prizes" stripe>
            <el-table-column prop="name" label="奖项名称" />
            <el-table-column prop="level" label="等级" width="80" />
            <el-table-column label="中奖人数" width="120">
              <template #default="{ row }">
                {{ row.drawnCount }} / {{ row.count }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'PENDING'" type="info">待抽取</el-tag>
                <el-tag v-else-if="row.status === 'DRAWING'" type="warning">抽取中</el-tag>
                <el-tag v-else type="success">已完成</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>

          <div class="quick-actions">
            <el-button type="primary" size="large" style="width: 100%; margin-bottom: 15px" @click="startLottery">
              <el-icon><DArrowRight /></el-icon>
              <span>开始抽奖</span>
            </el-button>

            <el-button type="danger" size="large" style="width: 100%; margin-bottom: 15px" @click="resetSystem">
              <el-icon><RefreshLeft /></el-icon>
              <span>重置系统</span>
            </el-button>

            <el-divider />

            <div class="quick-links">
              <el-button text @click="goToParticipants">
                <el-icon><User /></el-icon>
                <span>人员管理</span>
              </el-button>
              <el-button text @click="goToRecords">
                <el-icon><Document /></el-icon>
                <span>抽奖记录</span>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近中奖记录 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近中奖记录</span>
              <el-button type="primary" size="small" @click="goToRecords">查看全部</el-button>
            </div>
          </template>

          <el-table :data="recentRecords" stripe>
            <el-table-column prop="participantName" label="姓名" width="120" />
            <el-table-column prop="prizeName" label="奖项" />
            <el-table-column prop="drawTime" label="中奖时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.drawTime) }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.action === 'RIGGED'" type="warning">指定中奖</el-tag>
                <el-tag v-else type="success">随机中奖</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import participantApi from '@/api/participant'
import prizeApi from '@/api/prize'
import lotteryApi from '@/api/lottery'
import request from '@/api/request'

const router = useRouter()

const participantStats = ref({})
const prizeStats = ref({})
const prizes = ref([])
const recentRecords = ref([])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    const [pStats, prizesData, records] = await Promise.all([
      participantApi.getStatistics(),
      prizeApi.findAll(),
      request.get('/lottery-records/valid')
    ])

    participantStats.value = pStats
    prizes.value = prizesData
    recentRecords.value = records.slice(0, 10)
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  }
}

const goToParticipants = () => {
  router.push('/participants')
}

const goToPrizes = () => {
  router.push('/prizes')
}

const goToRecords = () => {
  router.push('/records')
}

const startLottery = () => {
  window.open('/', '_blank')
}

const resetSystem = async () => {
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
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败：' + error.message)
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.dashboard {
  .stats-row {
    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 20px;

        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 28px;
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #333;
            margin-bottom: 5px;
          }

          .stat-label {
            font-size: 14px;
            color: #999;
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .quick-actions {
    .quick-links {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .el-button {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }
}
</style>
