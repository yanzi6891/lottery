<template>
  <div class="prizes">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>奖项管理</span>
          <el-button type="primary" :icon="Plus" @click="showAddDialog">添加奖项</el-button>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table :data="prizes" stripe v-loading="loading">
        <el-table-column prop="name" label="奖项名称" />
        <el-table-column prop="level" label="等级" width="80" sortable />
        <el-table-column label="中奖人数" width="150">
          <template #default="{ row }">
            {{ row.drawnCount }} / {{ row.count }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="奖品描述" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="info">待抽取</el-tag>
            <el-tag v-else-if="row.status === 'DRAWING'" type="warning">抽取中</el-tag>
            <el-tag v-else type="success">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)" :disabled="row.drawnCount > 0">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" :disabled="row.drawnCount > 0">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="奖项名称" required>
          <el-input v-model="form.name" placeholder="例如：一等奖" />
        </el-form-item>
        <el-form-item label="等级" required>
          <el-input-number v-model="form.level" :min="1" :max="10" />
          <div style="font-size: 12px; color: #999; margin-top: 5px">
            数字越小等级越高，1为最高等级
          </div>
        </el-form-item>
        <el-form-item label="中奖人数" required>
          <el-input-number v-model="form.count" :min="1" />
        </el-form-item>
        <el-form-item label="奖品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="例如：iPhone 15 Pro Max" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import prizeApi from '@/api/prize'

const loading = ref(false)
const prizes = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('添加奖项')
const form = ref({
  id: '',
  name: '',
  level: 1,
  count: 1,
  description: ''
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    prizes.value = await prizeApi.findAll()
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  dialogTitle.value = '添加奖项'
  form.value = { id: '', name: '', level: 1, count: 1, description: '' }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogTitle.value = '编辑奖项'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入奖项名称')
    return
  }

  try {
    if (form.value.id) {
      await prizeApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await prizeApi.create(form.value)
      ElMessage.success('添加成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该奖项吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await prizeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message)
    }
  }
}
</script>

<style scoped lang="scss">
.prizes {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
