<template>
  <div class="admin-statistics">
    <el-card>
      <template #header>
        <span>全局统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="stats-summary">
            <div class="summary-item">
              <span class="summary-label">总用户数</span>
              <span class="summary-value">{{ stats.totalUsers || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">今日活跃</span>
              <span class="summary-value">{{ stats.todayActive || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">总做题数</span>
              <span class="summary-value">{{ stats.totalPractices || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">租户数</span>
              <span class="summary-value">{{ stats.totalTenants || 0 }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getGlobalStats } from '@/api/admin'

const stats = reactive({
  totalUsers: 0,
  todayActive: 0,
  totalPractices: 0,
  totalTenants: 0
})

onMounted(async () => {
  try {
    const data = await getGlobalStats({})
    if (data) {
      Object.assign(stats, data)
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.admin-statistics {
  padding: 0 32px 32px;
}

.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  padding: 24px;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  border-radius: 16px;
  color: #fff;
  text-align: center;
}

.summary-label {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 36px;
  font-weight: bold;
}
</style>
