<template>
  <div class="records">
    <el-card>
      <template #header>
        <span>抽奖记录</span>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="奖项">
          <el-select v-model="searchPrizeId" placeholder="全部" @change="loadData" clearable>
            <el-option label="全部" value="" />
            <el-option
              v-for="prize in prizes"
              :key="prize.id"
              :label="prize.name"
              :value="prize.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="participantName" label="姓名" width="120" />
        <el-table-column prop="prizeName" label="奖项" width="150" />
        <el-table-column prop="prizeLevel" label="等级" width="80" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.action === 'RIGGED'" type="warning">指定中奖</el-tag>
            <el-tag v-else type="success">随机中奖</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="drawTime" label="中奖时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.drawTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isCancelled" type="info">已撤销</el-tag>
            <el-tag v-else type="success">有效</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import prizeApi from '@/api/prize'
import request from '@/api/request'

const loading = ref(false)
const records = ref([])
const prizes = ref([])
const searchPrizeId = ref('')

onMounted(() => {
  loadPrizes()
  loadData()
})

const loadPrizes = async () => {
  try {
    prizes.value = await prizeApi.findAll()
  } catch (error) {
    console.error('加载奖项失败：', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    if (searchPrizeId.value) {
      records.value = await request.get(`/lottery-records/prize/${searchPrizeId.value}`)
    } else {
      records.value = await request.get('/lottery-records')
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.records {
  .search-form {
    margin-bottom: 20px;
  }
}
</style>
