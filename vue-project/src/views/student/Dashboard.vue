<template>
  <div class="student-dashboard">
    <!-- Welcome Section -->
    <div class="welcome-section animate-fade-in">
      <div class="welcome-content">
        <h1 class="welcome-title">👋 你好，{{ userName }}！</h1>
        <p class="welcome-subtitle">今天也要加油学习哦～</p>
      </div>
      <div class="welcome-illustration">📚</div>
    </div>
    
    <!-- Quick Stats -->
    <div class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalQuestions }}</div>
          <div class="stat-label">总练习题</div>
        </div>
      </div>
      
      <div class="stat-card stat-success">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.correctRate }}%</div>
          <div class="stat-label">正确率</div>
        </div>
      </div>
      
      <div class="stat-card stat-warning">
        <div class="stat-icon">❌</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.wrongCount }}</div>
          <div class="stat-label">错题数</div>
        </div>
      </div>
      
      <div class="stat-card stat-cta">
        <div class="stat-icon">🔥</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.streak }}</div>
          <div class="stat-label">连续学习</div>
        </div>
      </div>
    </div>
    
    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Today's Review -->
      <el-card class="review-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">📅 今日复习</span>
            <el-tag type="warning" size="small">{{ todayReviewCount }} 题待复习</el-tag>
          </div>
        </template>
        <div v-if="todayReview.length > 0" class="review-list">
          <div v-for="item in todayReview" :key="item.id" class="review-item">
            <div class="review-info">
              <span class="review-subject">{{ item.subjectName }}</span>
              <span class="review-knowledge">{{ item.knowledgePointName }}</span>
            </div>
            <el-button type="primary" size="small" @click="startPractice(item)">
              开始练习
            </el-button>
          </div>
        </div>
        <el-empty v-else description="今日无待复习内容" />
      </el-card>
      
      <!-- Wrong Questions -->
      <el-card class="wrong-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">❌ 最近错题</span>
            <el-button type="primary" link @click="$router.push('/student/wrong-questions')">
              查看全部
            </el-button>
          </div>
        </template>
        <div v-if="recentWrong.length > 0" class="wrong-list">
          <div v-for="item in recentWrong" :key="item.id" class="wrong-item">
            <div class="wrong-content">
              <span class="wrong-subject">{{ item.subjectName }}</span>
              <p class="wrong-question">{{ item.questionContent }}</p>
            </div>
            <div class="wrong-meta">
              <span class="wrong-time">{{ formatTime(item.wrongTime) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无错题" />
      </el-card>
      
      <!-- Knowledge Mastery -->
      <el-card class="mastery-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">🧠 知识点掌握</span>
            <el-button type="primary" link @click="$router.push('/student/knowledge')">
              查看详情
            </el-button>
          </div>
        </template>
        <div class="mastery-list">
          <div v-for="item in knowledgeMastery" :key="item.id" class="mastery-item">
            <div class="mastery-info">
              <span class="mastery-name">{{ item.knowledgePointName }}</span>
              <span class="mastery-subject">{{ item.subjectName }}</span>
            </div>
            <div class="mastery-progress">
              <el-progress 
                :percentage="item.masteryLevel" 
                :color="getMasteryColor(item.masteryLevel)"
                :stroke-width="8"
              />
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- Quick Actions -->
      <el-card class="actions-card">
        <template #header>
          <span class="card-title">⚡ 快捷操作</span>
        </template>
        <div class="quick-actions">
          <div class="action-item" @click="$router.push('/student/practice')">
            <div class="action-icon">📝</div>
            <span class="action-text">开始练习</span>
          </div>
          <div class="action-item" @click="$router.push('/student/wrong-questions')">
            <div class="action-icon">❌</div>
            <span class="action-text">复习错题</span>
          </div>
          <div class="action-item" @click="$router.push('/student/review')">
            <div class="action-icon">📅</div>
            <span class="action-text">复习计划</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getTodayReview } from '@/api/student'
import { getWrongQuestionList } from '@/api/question'
import { getKnowledgeMasteryList } from '@/api/student'

const userName = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.name || userInfo.username || '同学'
  } catch {
    return '同学'
  }
})

const stats = ref({
  totalQuestions: 156,
  correctRate: 78,
  wrongCount: 12,
  streak: 5
})

const todayReview = ref([])
const recentWrong = ref([])
const knowledgeMastery = ref([])

const todayReviewCount = computed(() => todayReview.value.length)

const loadData = async () => {
  try {
    // 今日复习
    const reviewRes = await getTodayReview()
    if (reviewRes.code === 200) {
      todayReview.value = (reviewRes.data || []).slice(0, 3)
    }
  } catch (e) {
    console.error(e)
  }
  
  try {
    // 最近错题
    const wrongRes = await getWrongQuestionList({ page: 1, size: 5 })
    if (wrongRes.code === 200) {
      recentWrong.value = wrongRes.data?.list || []
    }
  } catch (e) {
    console.error(e)
  }
  
  try {
    // 知识点掌握
    const masteryRes = await getKnowledgeMasteryList()
    if (masteryRes.code === 200) {
      knowledgeMastery.value = (masteryRes.data || []).slice(0, 5)
    }
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getMasteryColor = (percentage) => {
  if (percentage >= 80) return '#10B981'
  if (percentage >= 50) return '#F59E0B'
  return '#EF4444'
}

const startPractice = (item) => {
  console.log('开始练习:', item)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.student-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  border-radius: 20px;
  padding: 32px 40px;
  margin-bottom: 32px;
  color: #fff;
}

.welcome-title {
  font-family: 'Fredoka', sans-serif;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #fff;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-illustration {
  font-size: 64px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.15);
}

.stat-icon {
  font-size: 36px;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.stat-primary .stat-icon {
  background: rgba(79, 70, 229, 0.1);
}

.stat-success .stat-icon {
  background: rgba(16, 185, 129, 0.1);
}

.stat-warning .stat-icon {
  background: rgba(245, 158, 11, 0.1);
}

.stat-cta .stat-icon {
  background: rgba(249, 115, 22, 0.1);
}

.stat-value {
  font-family: 'Fredoka', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: #1E1B4B;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748B;
  margin-top: 4px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-family: 'Fredoka', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E1B4B;
}

/* Review Card */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #F5F7FF;
  border-radius: 12px;
}

.review-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-subject {
  font-size: 13px;
  color: #4F46E5;
  font-weight: 600;
}

.review-knowledge {
  font-size: 14px;
  color: #1E1B4B;
}

/* Wrong Card */
.wrong-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.wrong-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: #FEF2F2;
  border-radius: 12px;
}

.wrong-subject {
  font-size: 12px;
  color: #EF4444;
  font-weight: 600;
}

.wrong-question {
  font-size: 14px;
  color: #1E1B4B;
  margin: 4px 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 280px;
}

.wrong-time {
  font-size: 12px;
  color: #94A3B8;
}

/* Mastery Card */
.mastery-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mastery-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mastery-info {
  display: flex;
  justify-content: space-between;
}

.mastery-name {
  font-size: 14px;
  color: #1E1B4B;
  font-weight: 500;
}

.mastery-subject {
  font-size: 12px;
  color: #94A3B8;
}

/* Quick Actions */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #F5F7FF;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-item:hover {
  background: #EEF2FF;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
}

.action-text {
  font-size: 13px;
  color: #4F46E5;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    padding: 24px;
  }
  
  .welcome-title {
    font-size: 22px;
  }
  
  .welcome-illustration {
    font-size: 48px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    font-size: 28px;
    width: 48px;
    height: 48px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}
</style>
