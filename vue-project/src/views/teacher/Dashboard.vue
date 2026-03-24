<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon question"><el-icon><Document /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalQuestions }}</div>
            <div class="stat-label">题库总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon paper"><el-icon><Tickets /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalPapers }}</div>
            <div class="stat-label">试卷总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon student"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon wrong"><el-icon><Warning /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.wrongToday }}</div>
            <div class="stat-label">今日错题</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近录入题目</span>
              <el-button type="primary" link @click="$router.push('/teacher/questions')">管理 →</el-button>
            </div>
          </template>
          <el-table :data="recentQuestions" style="width: 100%">
            <el-table-column prop="content" label="题目" show-overflow-tooltip />
            <el-table-column prop="subjectName" label="学科" width="80" />
            <el-table-column prop="difficulty" label="难度" width="60">
              <template #default="{ row }">
                <el-tag :type="{1:'success',2:'warning',3:'danger'}[row.difficulty]" size="small">
                  {{ {1:'简单',2:'中',3:'难'}[row.difficulty] }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/teacher/questions')">
              <el-icon><Plus /></el-icon><span>添加题目</span>
            </div>
            <div class="action-item" @click="$router.push('/teacher/papers')">
              <el-icon><Tickets /></el-icon><span>创建试卷</span>
            </div>
            <div class="action-item" @click="$router.push('/teacher/ai-generate')">
              <el-icon><MagicStick /></el-icon><span>AI出题</span>
            </div>
            <div class="action-item" @click="$router.push('/teacher/wrong-stats')">
              <el-icon><Warning /></el-icon><span>错题统计</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Document, Tickets, User, Warning, Plus, MagicStick } from '@element-plus/icons-vue'
import { getQuestionList } from '@/api/question'
import { getPaperList } from '@/api/paper'
import { getUserList } from '@/api/user'

const stats = reactive({ totalQuestions: 0, totalPapers: 0, totalStudents: 0, wrongToday: 0 })
const recentQuestions = ref([])

onMounted(async () => {
  try {
    const [q, p, s] = await Promise.all([
      getQuestionList({ limit: 5 }),
      getPaperList({ limit: 1 }),
      getUserList({ role: 'STUDENT', limit: 1 })
    ])
    stats.totalQuestions = q?.total || 0
    stats.totalPapers = p?.total || 0
    stats.totalStudents = s?.total || 0
    recentQuestions.value = q?.list || []
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-right: 16px; }
.stat-icon.question { background: #e6f7ff; color: #1890ff; }
.stat-icon.paper { background: #f6ffed; color: #52c41a; }
.stat-icon.student { background: #fff7e6; color: #fa8c16; }
.stat-icon.wrong { background: #fff1f0; color: #ff4d4f; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.quick-actions { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.action-item { display: flex; align-items: center; gap: 12px; padding: 20px; background: #f5f7fa; border-radius: 8px; cursor: pointer; transition: all 0.3s; }
.action-item:hover { background: #ecf5ff; transform: translateY(-2px); }
.action-item .el-icon { font-size: 24px; color: #409eff; }
</style>
