<template>
  <div class="class-statistics">
    <!-- 顶部筛选栏 -->
    <el-card class="filter-card">
      <div class="filter-bar">
        <div class="filter-item">
          <span class="filter-label">选择班级：</span>
          <el-select v-model="selectedClassId" placeholder="请选择班级" style="width: 200px" @change="onClassChange">
            <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="filter-label">时间范围：</span>
          <el-radio-group v-model="timeRange" @change="onTimeRangeChange">
            <el-radio-button label="7">近7天</el-radio-button>
            <el-radio-button label="30">近30天</el-radio-button>
            <el-radio-button label="90">本学期</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="stats-row" v-if="selectedClassId">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon students"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalStudents }}</div>
            <div class="stat-label">学生数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon wrong"><el-icon><Warning /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalWrong }}</div>
            <div class="stat-label">总错题数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon rate"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.avgCorrectRate }}%</div>
            <div class="stat-label">平均正确率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon weak"><el-icon><CircleClose /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.weakPointCount }}</div>
            <div class="stat-label">薄弱知识点</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab页 -->
    <el-card class="tab-card" v-if="selectedClassId">
      <el-tabs v-model="activeTab">
        <!-- 高频错题 -->
        <el-tab-pane label="高频错题" name="wrong">
          <div class="chart-section">
            <el-table :data="wrongTopics" style="width:100%" stripe>
              <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip min-width="200" />
              <el-table-column prop="subjectName" label="科目" width="100" />
              <el-table-column prop="wrongCount" label="错误次数" width="100" />
              <el-table-column prop="wrongRate" label="错误率" width="100">
                <template #default="{row}">
                  <span :class="getRateClass(row.wrongRate)">{{ row.wrongRate }}%</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div ref="wrongChartRef" style="height:300px;margin-top:20px"></div>
        </el-tab-pane>

        <!-- 共性薄弱点 -->
        <el-tab-pane label="共性薄弱点" name="weak">
          <el-row :gutter="20">
            <el-col :span="12">
              <div ref="weakPieChartRef" style="height:350px"></div>
            </el-col>
            <el-col :span="12">
              <el-table :data="weakKnowledge" style="width:100%" stripe>
                <el-table-column prop="knowledgeName" label="知识点" min-width="150" />
                <el-table-column prop="wrongCount" label="错误人数" width="100" />
                <el-table-column prop="wrongRate" label="错误率" width="100">
                  <template #default="{row}">
                    <span :class="getRateClass(row.wrongRate)">{{ row.wrongRate }}%</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 题型分析 -->
        <el-tab-pane label="题型分析" name="type">
          <div ref="radarChartRef" style="height:400px"></div>
        </el-tab-pane>

        <!-- 知识点掌握度 -->
        <el-tab-pane label="知识点掌握度" name="mastery">
          <div ref="masteryChartRef" style="height:400px"></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 未选择班级时的提示 -->
    <el-empty v-if="!selectedClassId" description="请先选择班级" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { User, Warning, TrendCharts, CircleClose } from '@element-plus/icons-vue'
import { getClassList } from '@/api/org'
import {
  getClassWrongTopicStats,
  getClassWeakKnowledgeStats,
  getClassQuestionTypeStats,
  getClassKnowledgeMastery,
  getClassOverview
} from '@/api/statistics'

const selectedClassId = ref('')
const timeRange = ref('30')
const activeTab = ref('wrong')
const classList = ref([])

const overview = reactive({
  totalStudents: 0,
  totalWrong: 0,
  avgCorrectRate: 0,
  weakPointCount: 0
})

const wrongTopics = ref([])
const weakKnowledge = ref([])
const questionTypes = ref([])
const knowledgeMastery = ref([])

const wrongChartRef = ref(null)
const weakPieChartRef = ref(null)
const radarChartRef = ref(null)
const masteryChartRef = ref(null)

let wrongChartInstance = null
let weakPieChartInstance = null
let radarChartInstance = null
let masteryChartInstance = null

const getRateClass = (rate) => {
  if (rate >= 50) return 'rate-danger'
  if (rate >= 30) return 'rate-warning'
  return 'rate-normal'
}

// 加载班级列表
const loadClasses = async () => {
  try {
    const data = await getClassList({ page: 1, size: 100 })
    classList.value = data?.list || data || []
    if (classList.value.length > 0 && !selectedClassId.value) {
      selectedClassId.value = classList.value[0].id
    }
  } catch (e) {
    console.error(e)
  }
}

// 加载概览数据
const loadOverview = async () => {
  if (!selectedClassId.value) return
  try {
    const data = await getClassOverview(selectedClassId.value)
    Object.assign(overview, data || {})
  } catch (e) {
    console.error(e)
  }
}

// 加载高频错题
const loadWrongTopics = async () => {
  if (!selectedClassId.value) return
  try {
    const data = await getClassWrongTopicStats(selectedClassId.value, timeRange.value)
    wrongTopics.value = data?.list || data || []
    await nextTick()
    renderWrongChart()
  } catch (e) {
    console.error(e)
  }
}

// 加载薄弱知识点
const loadWeakKnowledge = async () => {
  if (!selectedClassId.value) return
  try {
    const data = await getClassWeakKnowledgeStats(selectedClassId.value, timeRange.value)
    weakKnowledge.value = data?.list || data || []
    await nextTick()
    renderWeakPieChart()
  } catch (e) {
    console.error(e)
  }
}

// 加载题型分析
const loadQuestionTypes = async () => {
  if (!selectedClassId.value) return
  try {
    const data = await getClassQuestionTypeStats(selectedClassId.value, timeRange.value)
    questionTypes.value = data || []
    await nextTick()
    renderRadarChart()
  } catch (e) {
    console.error(e)
  }
}

// 加载知识点掌握度
const loadKnowledgeMastery = async () => {
  if (!selectedClassId.value) return
  try {
    const data = await getClassKnowledgeMastery(selectedClassId.value)
    knowledgeMastery.value = data || []
    await nextTick()
    renderMasteryChart()
  } catch (e) {
    console.error(e)
  }
}

// 渲染高频错题柱状图
const renderWrongChart = () => {
  if (!wrongChartRef.value) return
  if (wrongChartInstance) wrongChartInstance.dispose()
  wrongChartInstance = echarts.init(wrongChartRef.value)

  const top10 = wrongTopics.value.slice(0, 10)
  const xData = top10.map(t => t.questionContent?.substring(0, 10) + '...')
  const yData = top10.map(t => t.wrongCount)

  wrongChartInstance.setOption({
    title: { text: 'Top10 高频错题', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '错误次数' },
    series: [{
      type: 'bar',
      data: yData,
      itemStyle: { color: '#f56c6c' },
      label: { show: true, position: 'top' }
    }]
  })
}

// 渲染薄弱知识点饼图
const renderWeakPieChart = () => {
  if (!weakPieChartRef.value) return
  if (weakPieChartInstance) weakPieChartInstance.dispose()
  weakPieChartInstance = echarts.init(weakPieChartRef.value)

  const pieData = weakKnowledge.value.map(k => ({
    name: k.knowledgeName,
    value: k.wrongRate
  }))

  weakPieChartInstance.setOption({
    title: { text: '知识点错误率分布', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { formatter: '{b}: {c}%' },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      data: pieData,
      label: { formatter: '{b}\n{c}%' }
    }]
  })
}

// 渲染题型雷达图
const renderRadarChart = () => {
  if (!radarChartRef.value) return
  if (radarChartInstance) radarChartInstance.dispose()
  radarChartInstance = echarts.init(radarChartRef.value)

  const indicator = questionTypes.value.map(q => ({
    name: q.typeName,
    max: 100
  }))

  radarChartInstance.setOption({
    title: { text: '各题型错误率对比', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: {},
    radar: { indicator, radius: '65%' },
    series: [{
      type: 'radar',
      data: [{
        value: questionTypes.value.map(q => q.wrongRate),
        name: '错误率',
        areaStyle: { color: 'rgba(64, 158, 255, 0.3)' },
        lineStyle: { color: '#409eff' },
        itemStyle: { color: '#409eff' }
      }]
    }]
  })
}

// 渲染知识点掌握度柱状图
const renderMasteryChart = () => {
  if (!masteryChartRef.value) return
  if (masteryChartInstance) masteryChartInstance.dispose()
  masteryChartInstance = echarts.init(masteryChartRef.value)

  const sorted = [...knowledgeMastery.value].sort((a, b) => a.masteryRate - b.masteryRate)
  const yData = sorted.map(k => k.knowledgeName)
  const xData = sorted.map(k => k.masteryRate)

  const getBarColor = (rate) => {
    if (rate < 40) return '#f56c6c'
    if (rate < 70) return '#e6a23c'
    return '#67c23a'
  }

  masteryChartInstance.setOption({
    title: { text: '知识点掌握度', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { formatter: '{b}: {c}%' },
    grid: { left: '20%', right: '15%', bottom: '10%', top: '15%' },
    xAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{c}%' } },
    yAxis: { type: 'category', data: yData },
    series: [{
      type: 'bar',
      data: xData,
      itemStyle: {
        color: (params) => getBarColor(params.value)
      },
      label: { show: true, position: 'right', formatter: '{c}%' }
    }]
  })
}

const onClassChange = () => {
  loadAllData()
}

const onTimeRangeChange = () => {
  loadAllData()
}

const loadAllData = async () => {
  await loadOverview()
  await Promise.all([
    loadWrongTopics(),
    loadWeakKnowledge(),
    loadQuestionTypes(),
    loadKnowledgeMastery()
  ])
}

const handleResize = () => {
  wrongChartInstance?.resize()
  weakPieChartInstance?.resize()
  radarChartInstance?.resize()
  masteryChartInstance?.resize()
}

onMounted(async () => {
  await loadClasses()
  if (selectedClassId.value) {
    await loadAllData()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  wrongChartInstance?.dispose()
  weakPieChartInstance?.dispose()
  radarChartInstance?.dispose()
  masteryChartInstance?.dispose()
})
</script>

<style scoped>
.class-statistics {
  padding: 0 32px 32px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
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

.stat-icon.students { background: #e6f7ff; color: #1890ff; }
.stat-icon.wrong { background: #fff1f0; color: #ff4d4f; }
.stat-icon.rate { background: #f6ffed; color: #52c41a; }
.stat-icon.weak { background: #fff7e6; color: #fa8c16; }

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

.tab-card {
  min-height: 500px;
}

.chart-section {
  margin-bottom: 20px;
}

.rate-danger { color: #f56c6c; font-weight: 600; }
.rate-warning { color: #e6a23c; font-weight: 600; }
.rate-normal { color: #67c23a; }

@media (max-width: 768px) {
  .class-statistics { padding: 0 16px 16px; }
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .stats-row .el-col { margin-bottom: 12px; }
}
</style>
