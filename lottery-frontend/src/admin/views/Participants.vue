<template>
  <div class="participants">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>人员管理</span>
          <div class="header-actions">
            <el-upload
              action="/api/participants/import"
              :show-file-list="false"
              :on-success="handleImportSuccess"
              :on-error="handleImportError"
              accept=".xlsx,.xls"
            >
              <el-button type="success" :icon="Upload">Excel导入</el-button>
            </el-upload>
            <el-button type="primary" :icon="Plus" @click="showAddDialog">添加人员</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部" @change="loadData" clearable>
            <el-option label="全部" value="" />
            <el-option label="可抽取" value="AVAILABLE" />
            <el-option label="已中奖" value="WON" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Refresh" @click="loadData">刷新</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="participants" stripe v-loading="loading">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="employeeId" label="工号" width="150" />
        <el-table-column prop="department" label="部门" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'AVAILABLE'" type="success">可抽取</el-tag>
            <el-tag v-else type="info">已中奖</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="wonPrizeName" label="中奖奖项" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="form.employeeId" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.department" placeholder="请输入部门" />
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
import { Plus, Upload, Refresh } from '@element-plus/icons-vue'
import participantApi from '@/api/participant'

const loading = ref(false)
const participants = ref([])
const searchStatus = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('添加人员')
const form = ref({
  id: '',
  name: '',
  employeeId: '',
  department: ''
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    if (searchStatus.value) {
      participants.value = await participantApi.findByStatus(searchStatus.value)
    } else {
      participants.value = await participantApi.findAll()
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  dialogTitle.value = '添加人员'
  form.value = { id: '', name: '', employeeId: '', department: '' }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogTitle.value = '编辑人员'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入姓名')
    return
  }

  try {
    if (form.value.id) {
      await participantApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await participantApi.add(form.value)
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
    await ElMessageBox.confirm('确定要删除该人员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await participantApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message)
    }
  }
}

const handleImportSuccess = (response) => {
  if (response.code === 200) {
    const result = response.data
    ElMessage.success(`导入完成：成功 ${result.success} 条，失败 ${result.failed} 条`)
    loadData()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleImportError = (error) => {
  ElMessage.error('导入失败：' + error.message)
}
</script>

<style scoped lang="scss">
.participants {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>
