<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6"><el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background:#e6f7ff;color:#1890ff"><el-icon><Edit /></el-icon></div>
        <div class="stat-content"><div class="stat-value">{{ stats.todayPractice }}</div><div class="stat-label">今日练习</div></div>
      </el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background:#f6ffed;color:#52c41a"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-content"><div class="stat-value">{{ stats.todayCorrect }}%</div><div class="stat-label">正确率</div></div>
      </el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background:#fff1f0;color:#ff4d4f"><el-icon><Warning /></el-icon></div>
        <div class="stat-content"><div class="stat-value">{{ stats.wrongCount }}</div><div class="stat-label">错题数量</div></div>
      </el-card></el-col>
      <el-col :span="6"><el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background:#fff7e6;color:#fa8c16"><el-icon><Timer /></el-icon></div>
        <div class="stat-content"><div class="stat-value">{{ stats.todayReview }}</div><div class="stat-label">待复习</div></div>
      </el-card></el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8"><el-card><template #header><span>快速开始</span></template>
        <div class="action-list">
          <div class="action-item" @click="$router.push('/student/practice')"><div class="action-icon" style="background:#e6f7ff;color:#1890ff"><el-icon><Edit /></el-icon></div><div class="action-text"><div class="action-title">开始练习</div><div class="action-desc">每日精选题目</div></div></div>
          <div class="action-item" @click="$router.push('/student/review')"><div class="action-icon" style="background:#fff7e6;color:#fa8c16"><el-icon><Timer /></el-icon></div><div class="action-text"><div class="action-title">复习计划</div><div class="action-desc">艾宾浩斯记忆复习</div></div></div>
          <div class="action-item" @click="$router.push('/student/wrong-questions')"><div class="action-icon" style="background:#fff1f0;color:#ff4d4f"><el-icon><Warning /></el-icon></div><div class="action-text"><div class="action-title">错题复习</div><div class="action-desc">巩固薄弱知识点</div></div></div>
        </div>
      </el-card></el-col>
      <el-col :span="16"><el-card><template #header><span>最近学习记录</span></template>
        <el-table :data="recentRecords" style="width:100%">
          <el-table-column prop="subjectName" label="学科" width="80" />
          <el-table-column prop="questionContent" label="题目" show-overflow-tooltip />
          <el-table-column prop="result" label="结果" width="60">
            <template #default="{row}"><el-tag :type="row.result===1?'success':'danger'" size="small">{{ row.result===1?'正确':'错误' }}</el-tag></template>
          </el-table-column>
        </el-table>
      </el-card></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Edit, CircleCheck, Warning, Timer } from '@element-plus/icons-vue'
import { getStudyRecordList, getWrongQuestionList, getTodayReview } from '@/api/student'

const stats = reactive({ todayPractice: 0, todayCorrect: 0, wrongCount: 0, todayReview: 0 })
const recentRecords = ref([])

onMounted(async () => {
  try {
    const [records, wrongList, review] = await Promise.all([getStudyRecordList({limit:10}), getWrongQuestionList({status:1}), getTodayReview()])
    recentRecords.value = records?.list || []
    stats.wrongCount = wrongList?.total || 0
    stats.todayReview = review?.length || 0
    const today = new Date().toDateString()
    const todayRecords = (records?.list || []).filter(r => new Date(r.createdAt).toDateString() === today)
    stats.todayPractice = todayRecords.length
    const correct = todayRecords.filter(r => r.result === 1).length
    stats.todayCorrect = todayRecords.length > 0 ? Math.round((correct / todayRecords.length) * 100) : 0
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stats-row{margin-bottom:20px}.stat-card{display:flex;align-items:center;padding:20px}.stat-icon{width:60px;height:60px;border-radius:12px;display:flex;align-items:center;justify-content:center;font-size:28px;margin-right:16px}.stat-value{font-size:28px;font-weight:bold;color:#333}.stat-label{font-size:14px;color:#999;margin-top:4px}.action-list{display:flex;flex-direction:column;gap:16px}.action-item{display:flex;align-items:center;padding:16px;background:#f5f7fa;border-radius:8px;cursor:pointer;transition:all .3s}.action-item:hover{background:#ecf5ff;transform:translateX(4px)}.action-icon{width:44px;height:44px;border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:20px;margin-right:14px}.action-title{font-size:15px;font-weight:500;color:#333}.action-desc{font-size:13px;color:#999;margin-top:2px}
</style>
