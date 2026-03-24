<template>
  <div class="wrong-stats">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon wrong-total">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalWrong }}</div>
            <div class="stat-label">总错题数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon unmastered">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.unmastered }}</div>
            <div class="stat-label">未掌握</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon mastered">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.mastered }}</div>
            <div class="stat-label">已掌握</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon hot">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.hotKnowledge }}</div>
            <div class="stat-label">高频错点</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 错题趋势图 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>错题趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendData">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="semester">本学期</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <!-- 高频错题知识点 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>高频错题知识点TOP10</span></template>
          <div class="knowledge-list">
            <div v-for="(item, index) in hotKnowledgePoints" :key="index" class="knowledge-item">
              <div class="knowledge-rank">{{ index + 1 }}</div>
              <div class="knowledge-info">
                <div class="knowledge-name">{{ item.name }}</div>
                <div class="knowledge-count">{{ item.wrongCount }} 次错误</div>
              </div>
              <el-progress :percentage="item.percentage" :color="getPercentageColor(item.percentage)" style="width: 80px" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 错题详细列表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>错题详情</span>
              <div class="header-actions">
                <el-select v-model="filterSubject" placeholder="筛选学科" clearable style="width: 140px" @change="loadWrongList">
                  <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
                </el-select>
                <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 120px" @change="loadWrongList">
                  <el-option label="未掌握" :value="1" />
                  <el-option label="已掌握" :value="2" />
                </el-select>
              </div>
            </div>
          </template>
          
          <el-table :data="wrongList" v-loading="loading">
            <el-table-column prop="subjectName" label="学科" width="80" />
            <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip min-width="200" />
            <el-table-column prop="wrongCount" label="错误次数" width="100" />
            <el-table-column prop="studentCount" label="涉及学生" width="100" />
            <el-table-column prop="masteryRate" label="掌握率" width="100">
              <template #default="{ row }">
                <el-progress :percentage="row.masteryRate || 0" :color="getPercentageColor(row.masteryRate)" style="width: 60px" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 2 ? 'success' : 'warning'">
                  {{ row.status === 2 ? '已掌握' : '未掌握' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
                <el-button type="success" link @click="createPractice(row)">组卷练习</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; justify-content: flex-end"
            @size-change="loadWrongList"
            @current-change="loadWrongList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Warning, CircleClose, CircleCheck, Star } from '@element-plus/icons-vue'
import { getWrongQuestionStats, getWrongQuestionList } from '@/api/question'
import { getSubjectList } from '@/api/base'

const router = useRouter()
const loading = ref(false)
const trendType = ref('week')
const filterSubject = ref('')
const filterStatus = ref('')
const trendChart = ref(null)
let trendChartInstance = null

const stats = reactive({ totalWrong: 0, unmastered: 0, mastered: 0, hotKnowledge: 0 })
const hotKnowledgePoints = ref([])
const wrongList = ref([])
const subjects = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })

const getPercentageColor = (pct) => {
  if (pct >= 80) return '#67c23a'
  if (pct >= 60) return '#e6a23c'
  return '#f56c6c'
}

const loadStats = async () => {
  try {
    const data = await getWrongQuestionStats({})
    Object.assign(stats, data || { totalWrong: 0, unmastered: 0, mastered: 0, hotKnowledge: 0 })
    hotKnowledgePoints.value = data?.hotKnowledgePoints || []
  } catch (e) { console.error(e) }
}

const loadTrendData = async () => {
  try {
    const data = await getWrongQuestionStats({ type: trendType.value })
    if (trendChartInstance) {
      const dates = data?.trend?.map(i => i.date) || []
      const counts = data?.trend?.map(i => i.count) || []
      trendChartInstance.setOption({
        xAxis: { type: 'category', data: dates },
        yAxis: { type: 'value' },
        series: [{ data: counts, type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' } }]
      })
    }
  } catch (e) { console.error(e) }
}

const loadWrongList = async () => {
  loading.value = true
  try {
    const data = await getWrongQuestionList({
      subjectId: filterSubject.value,
      status: filterStatus.value,
      page: pagination.page,
      size: pagination.size
    })
    wrongList.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const loadSubjects = async () => {
  subjects.value = await getSubjectList() || []
}

const viewDetail = (row) => { router.push(`/teacher/questions?id=${row.questionId}`) }
const createPractice = (row) => { router.push(`/teacher/papers?wrong=${row.questionId}`) }

onMounted(async () => {
  await loadSubjects()
  await loadStats()
  await nextTick()
  trendChartInstance = echarts.init(trendChart.value)
  loadTrendData()
  loadWrongList()
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-right: 16px; }
.stat-icon.wrong-total { background: #fff1f0; color: #ff4d4f; }
.stat-icon.unmastered { background: #fff7e6; color: #fa8c16; }
.stat-icon.mastered { background: #f6ffed; color: #52c41a; }
.stat-icon.hot { background: #e6f7ff; color: #1890ff; }
.stat-content { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.knowledge-list { max-height: 350px; overflow-y: auto; }
.knowledge-item { display: flex; align-items: center; padding: 12px; border-bottom: 1px solid #f5f7fa; }
.knowledge-rank { width: 24px; height: 24px; background: #409eff; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; margin-right: 12px; }
.knowledge-info { flex: 1; }
.knowledge-name { font-size: 14px; color: #333; }
.knowledge-count { font-size: 12px; color: #999; }
</style>
