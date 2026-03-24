<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.totalStudents }}</div>
          <div class="stat-label">学生总数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.activeStudents }}</div>
          <div class="stat-label">活跃学生</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.avgCorrectRate }}%</div>
          <div class="stat-label">平均正确率</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card><template #header><span>学科题目分布</span></template></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header><span>学生成绩分布</span></template></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
const stats = reactive({ totalStudents: 0, activeStudents: 0, avgCorrectRate: 0 })
</script>

<style scoped>
.stat-card { text-align: center; padding: 30px; }
.stat-value { font-size: 36px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #999; margin-top: 8px; }
</style>
