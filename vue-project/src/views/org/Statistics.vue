<template>
  <div class="statistics">
    <!-- 核心指标 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon students">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学生总数</div>
            <div class="stat-change" :class="stats.studentChange >= 0 ? 'up' : 'down'">
              {{ stats.studentChange >= 0 ? '↑' : '↓' }} {{ Math.abs(stats.studentChange) }}% 较上周
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon teachers">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalTeachers }}</div>
            <div class="stat-label">教师数量</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon practice">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayPractice }}</div>
            <div class="stat-label">今日练习人次</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon rate">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.avgCorrectRate }}%</div>
            <div class="stat-label">整体正确率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 练习趋势 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>练习趋势</span>
              <el-radio-group v-model="trendType" size="small" @change="loadTrendData">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="semester">本学期</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      
      <!-- 学科分布 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>学科练习分布</span></template>
          <div ref="subjectChart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 年级对比 -->
      <el-col :span="12">
        <el-card>
          <template #header><span>各年级平均正确率</span></template>
          <div ref="gradeChart" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <!-- 错题类型分布 -->
      <el-col :span="12">
        <el-card>
          <template #header><span>错题类型分布</span></template>
          <div ref="wrongTypeChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>班级数据明细</span>
              <el-input v-model="classSearch" placeholder="搜索班级" style="width: 200px" clearable @change="loadClassData" prefix-icon="Search" />
            </div>
          </template>
          <el-table :data="classData" v-loading="loading">
            <el-table-column prop="name" label="班级名称" width="150" />
            <el-table-column prop="gradeName" label="年级" width="100" />
            <el-table-column prop="studentCount" label="学生数" width="100" />
            <el-table-column prop="practiceCount" label="练习次数" width="100" />
            <el-table-column prop="avgCorrectRate" label="平均正确率" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.avgCorrectRate" :color="getRateColor(row.avgCorrectRate)" style="width: 80px" />
              </template>
            </el-table-column>
            <el-table-column prop="wrongCount" label="错题数" width="100" />
            <el-table-column prop="activeRate" label="活跃率" width="120">
              <template #default="{ row }">
                <el-tag :type="row.activeRate >= 80 ? 'success' : row.activeRate >= 60 ? 'warning' : 'danger'">
                  {{ row.activeRate }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewClassDetail(row)">查看详情</el-button>
                <el-button type="info" link @click="exportClassData(row)">导出</el-button>
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
            @size-change="loadClassData"
            @current-change="loadClassData"
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
import { User, UserFilled, Edit, TrendCharts } from '@element-plus/icons-vue'
import { getGlobalStats, getPracticeTrend, getSubjectStats, getClassList } from '@/api/org'

const router = useRouter()
const loading = ref(false)
const trendType = ref('week')
const classSearch = ref('')
const trendChart = ref(null)
const subjectChart = ref(null)
const gradeChart = ref(null)
const wrongTypeChart = ref(null)
let trendChartInstance = null
let subjectChartInstance = null
let gradeChartInstance = null
let wrongTypeChartInstance = null

const stats = reactive({
  totalStudents: 0, totalTeachers: 0, todayPractice: 0, avgCorrectRate: 0, studentChange: 0
})

const classData = ref([])
const subjects = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const getRateColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const loadStats = async () => {
  try {
    const data = await getGlobalStats({})
    Object.assign(stats, data || { totalStudents: 0, totalTeachers: 0, todayPractice: 0, avgCorrectRate: 0, studentChange: 0 })
  } catch (e) { console.error(e) }
}

const loadTrendData = async () => {
  try {
    const data = await getPracticeTrend({ type: trendType.value })
    if (trendChartInstance) {
      const dates = data?.dates || []
      const practice = data?.practice || []
      const correct = data?.correct || []
      
      trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['练习人次', '正确人次'] },
        xAxis: { type: 'category', data: dates },
        yAxis: { type: 'value' },
        series: [
          { name: '练习人次', type: 'line', data: practice, smooth: true, itemStyle: { color: '#409eff' } },
          { name: '正确人次', type: 'line', data: correct, smooth: true, itemStyle: { color: '#67c23a' } }
        ]
      })
    }
  } catch (e) { console.error(e) }
}

const loadSubjectStats = async () => {
  try {
    const data = await getSubjectStats({})
    subjects.value = data?.list || []
    if (subjectChartInstance) {
      const names = subjects.value.map(s => s.name)
      const values = subjects.value.map(s => s.practiceCount)
      subjectChartInstance.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie', radius: ['40%', '70%'], data: names.map((name, i) => ({ name, value: values[i] })),
          label: { formatter: '{b}: {c} ({d}%)' }
        }]
      })
    }
  } catch (e) { console.error(e) }
}

const loadGradeStats = async () => {
  if (gradeChartInstance) {
    gradeChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['初一', '初二', '初三', '高一', '高二', '高三'] },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'bar', data: [75, 72, 68, 70, 65, 62],
        itemStyle: { color: '#409eff' },
        label: { show: true, position: 'top', formatter: '{c}%' }
      }]
    })
  }
}

const loadWrongTypeStats = async () => {
  if (wrongTypeChartInstance) {
    wrongTypeChartInstance.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: '70%',
        data: [
          { name: '选择题', value: 35 },
          { name: '填空题', value: 28 },
          { name: '解答题', value: 22 },
          { name: '其他', value: 15 }
        ]
      }]
    })
  }
}

const loadClassData = async () => {
  loading.value = true
  try {
    const data = await getClassList({ search: classSearch.value, page: pagination.page, size: pagination.size })
    classData.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const viewClassDetail = (row) => { router.push(`/org/classes?id=${row.id}`) }
const exportClassData = (row) => { console.log('export', row) }

onMounted(async () => {
  await loadStats()
  await nextTick()
  
  trendChartInstance = echarts.init(trendChart.value)
  subjectChartInstance = echarts.init(subjectChart.value)
  gradeChartInstance = echarts.init(gradeChart.value)
  wrongTypeChartInstance = echarts.init(wrongTypeChart.value)
  
  loadTrendData()
  loadSubjectStats()
  loadGradeStats()
  loadWrongTypeStats()
  loadClassData()
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-right: 16px; }
.stat-icon.students { background: #e6f7ff; color: #1890ff; }
.stat-icon.teachers { background: #f6ffed; color: #52c41a; }
.stat-icon.practice { background: #fff7e6; color: #fa8c16; }
.stat-icon.rate { background: #fff1f0; color: #ff4d4f; }
.stat-content { flex: 1; }
.stat-value { font-size: 32px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
.stat-change { font-size: 12px; margin-top: 4px; }
.stat-change.up { color: #67c23a; }
.stat-change.down { color: #f56c6c; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
