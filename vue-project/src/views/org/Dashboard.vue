<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalTeachers }}</div><div class="stat-label">教师数量</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><UserFilled /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalStudents }}</div><div class="stat-label">学生数量</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><Collection /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalClasses }}</div><div class="stat-label">班级数量</div></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-content"><div class="stat-value">{{ stats.totalQuestions }}</div><div class="stat-label">题库总量</div></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card><template #header><span>最近注册学生</span></template>
          <el-table :data="recentStudents" style="width:100%">
            <el-table-column prop="name" label="姓名" /><el-table-column prop="gradeName" label="年级" /><el-table-column prop="className" label="班级" /><el-table-column prop="createdAt" label="时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header><span>机构信息</span></template>
          <div class="org-info">
            <div class="info-item"><span class="label">机构名称：</span><span>{{ orgInfo.name }}</span></div>
            <div class="info-item"><span class="label">机构类型：</span><span>{{ orgInfo.type==='SCHOOL'?'学校':'培训机构' }}</span></div>
            <div class="info-item"><span class="label">所在省份：</span><span>{{ orgInfo.province }}</span></div>
            <div class="info-item"><span class="label">联系人：</span><span>{{ orgInfo.contactName }}</span></div>
            <div class="info-item"><span class="label">联系电话：</span><span>{{ orgInfo.contactMobile }}</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, UserFilled, Collection, Document } from '@element-plus/icons-vue'
import { getUserList } from '@/api/user'
import { getQuestionList } from '@/api/question'
import { getClassList } from '@/api/org'

const stats = reactive({ totalTeachers: 0, totalStudents: 0, totalClasses: 0, totalQuestions: 0 })
const recentStudents = ref([])
const orgInfo = reactive({ name: '示例机构', type: 'SCHOOL', province: '北京', contactName: '', contactMobile: '' })

onMounted(async () => {
  try {
    const [t, s, q, c] = await Promise.all([
      getUserList({ role: 'TEACHER', limit: 1 }),
      getUserList({ role: 'STUDENT', limit: 1 }),
      getQuestionList({ limit: 1 }),
      getClassList({ limit: 1 })
    ])
    stats.totalTeachers = t?.total || 0
    stats.totalStudents = s?.total || 0
    stats.totalQuestions = q?.total || 0
    stats.totalClasses = c?.total || 0
    const recent = await getUserList({ role: 'STUDENT', limit: 5 })
    recentStudents.value = recent?.list || []
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; margin-right: 16px; background: #e6f7ff; color: #1890ff; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 4px; }
.info-item { display: flex; padding: 12px 0; border-bottom: 1px solid #f5f7fa; }
.info-item:last-child { border-bottom: none; }
.info-item .label { width: 100px; color: #999; }
</style>
