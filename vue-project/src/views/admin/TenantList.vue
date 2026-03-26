<template>
  <div class="tenant-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>租户管理</span>
          <el-button type="primary" @click="goToForm(null)"><el-icon><Plus /></el-icon>新建租户</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索租户名称"
          style="width: 240px"
          clearable
          @clear="loadTenants"
          @keyup.enter="loadTenants"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="状态筛选" style="width: 140px" clearable @change="loadTenants">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button @click="loadTenants"><el-icon><Search /></el-icon>搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tenants" style="width:100%;margin-top:16px" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="租户名称" min-width="150" />
        <el-table-column prop="province" label="省份" width="120" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{row}">
            <el-tag :type="row.type === 'SPEED' ? 'success' : 'warning'" size="small">
              {{ row.type === 'SPEED' ? 'SPEED' : 'INSTITUTE' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phase" label="学段" width="120">
          <template #default="{row}">
            <span>{{ phaseText(row.phase) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link @click="goToForm(row.id)">编辑</el-button>
            <el-button type="danger" link @click="deleteTenant(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total,sizes,prev,pager,next,jumper"
        style="margin-top:20px;justify-content:flex-end"
        @size-change="loadTenants"
        @current-change="loadTenants"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getTenantList, deleteTenant as deleteTenantApi } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const tenants = ref([])

const searchForm = reactive({
  name: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const phaseText = (phase) => {
  const map = { 'MIDDLE': '初中', 'HIGH': '高中', 'BOTH': '初高中' }
  return map[phase] || phase || '-'
}

const loadTenants = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status

    const data = await getTenantList(params)
    tenants.value = data?.list || data || []
    pagination.total = data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goToForm = (id) => {
  if (id) {
    router.push(`/admin/tenant/${id}/edit`)
  } else {
    router.push('/admin/tenant/new')
  }
}

const deleteTenant = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除租户「${row.name}」吗？`, '提示', { type: 'warning' })
    await deleteTenantApi(row.id)
    ElMessage.success('删除成功')
    loadTenants()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadTenants()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
