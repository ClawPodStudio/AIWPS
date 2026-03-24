<template>
  <div class="review">
    <el-row :gutter="20">
      <el-col :span="16"><el-card><template #header><span>今日复习 ({{ todayReviews.length }} 条)</span></template>
        <div v-if="todayReviews.length===0" class="empty-state" style="text-align:center;padding:60px">
          <el-icon style="font-size:60px;color:#67c23a;margin-bottom:20px"><CircleCheck /></el-icon><p style="font-size:18px;color:#333">今日复习任务已完成！</p><span style="color:#999">根据艾宾浩斯遗忘曲线，明天再来复习吧</span>
        </div>
        <el-table v-else :data="todayReviews" style="width:100%">
          <el-table-column prop="knowledgePointName" label="知识点" />
          <el-table-column prop="reviewRound" label="复习轮次" width="100" />
          <el-table-column prop="intervalDays" label="间隔天数" width="100" />
          <el-table-column label="操作" width="100"><template #default="{row}"><el-button type="primary" link @click="startReview(row)">复习</el-button></template></el-table-column>
        </el-table>
      </el-card></el-col>
      <el-col :span="8"><el-card><template #header><span>复习统计</span></template>
        <div style="text-align:center;padding:20px">
          <div style="font-size:36px;font-weight:bold;color:#409eff;margin-bottom:8px">{{ reviewStats.totalReviews }}</div><div style="color:#999">总复习次数</div>
        </div>
      </el-card></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import { getTodayReview, getReviewPlanList } from '@/api/student'

const todayReviews = ref([])
const reviewStats = reactive({ totalReviews: 0, rememberRate: 0 })

const loadTodayReviews = async () => { try{ todayReviews.value=await getTodayReview()||[] }catch(e){console.error(e)} }
const loadReviewStats = async () => { try{ const data=await getReviewPlanList({limit:100}); reviewStats.totalReviews=data?.total||0 }catch(e){console.error(e)} }
const startReview = (row) => { console.log('review', row) }

onMounted(()=>{ loadTodayReviews(); loadReviewStats() })
</script>
