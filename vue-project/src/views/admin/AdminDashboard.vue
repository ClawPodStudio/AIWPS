<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#e6f7ff;color:#1890ff"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalTenants }}</div><div class="stat-label">租户数量</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fff7e6;color:#fa8c16"><el-icon><User /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalUsers }}</div><div class="stat-label">总用户数</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#f6ffed;color:#52c41a"><el-icon><UserFilled /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalStudents }}</div><div class="stat-label">学生数量</div></div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#f9f0ff;color:#722ed1"><el-icon><Document /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalQuestions }}</div><div class="stat-label">题库总量</div></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>平台概览</span>
          </template>
          <div class="overview-grid">
            <div class="overview-item">
              <span class="overview-label">租户总数</span>
              <span class="overview-value">{{ stats.totalTenants }}</span>
            </div>
            <div class="overview-item">
              <span class="overview-label">启用租户</span>
              <span class="overview-value success">{{ stats.activeTenants }}</span>
            </div>
            <div class="overview-item">
              <span class="overview-label">禁用租户</span>
              <span class="overview-value danger">{{ stats.inactiveTenants }}</span>
            </div>
            <div class="overview-item">
              <span class="overview-label">用户总数</span>
              <span class="overview-value">{{ stats.totalUsers }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { OfficeBuilding, User, UserFilled, Document } from '@element-plus/icons-vue'
import { getGlobalStats } from '@/api/admin'
import { getTenantList } from '@/api/admin'
import { getUserList } from '@/api/user'
import { getQuestionList } from '@/api/question'

const stats = reactive({
  totalTenants: 0,
  activeTenants: 0,
  inactiveTenants: 0,
  totalUsers: 0,
  totalStudents: 0,
  totalQuestions: 0
})

onMounted(async () => {
  try {
    const [tenantData, userData, questionData] = await Promise.all([
      getTenantList({ page: 1, size: 1 }),
      getUserList({ page: 1, size: 1 }),
      getQuestionList({ page: 1, size: 1 })
    ])

    stats.totalTenants = tenantData?.total || 0
    stats.totalUsers = userData?.total || 0
    stats.totalQuestions = questionData?.total || 0

    // 统计启用/禁用的租户
    if (tenantData?.list) {
      stats.activeTenants = tenantData.list.filter(t => t.status === 1).length
      stats.inactiveTenants = tenantData.list.filter(t => t.status === 0).length
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0 32px 32px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  margin-right: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  text-align: center;
}

.overview-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.overview-value.success { color: #52c41a; }
.overview-value.danger { color: #ff4d4f; }

@media (max-width: 768px) {
  .admin-dashboard { padding: 0 16px 16px; }
}
</style>
