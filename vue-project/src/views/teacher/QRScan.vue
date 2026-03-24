<template>
  <div class="qr-scan">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>试卷二维码验证</span>
        </div>
      </template>
      
      <div v-if="loading" style="text-align:center;padding:40px">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在验证...</p>
      </div>
      
      <div v-else-if="error" style="text-align:center;padding:40px">
        <el-result icon="error" title="验证失败" :sub-title="error">
          <template #footer>
            <el-button type="primary" @click="$router.push('/teacher/papers')">返回试卷管理</el-button>
          </template>
        </el-result>
      </div>
      
      <div v-else-if="paperInfo">
        <el-result icon="success" title="验证成功" sub-title="以下是该试卷信息">
          <template #footer>
            <el-button type="primary" @click="$router.push('/teacher/papers')">返回试卷管理</el-button>
          </template>
        </el-result>
        
        <el-descriptions :column="2" border style="margin-top:20px">
          <el-descriptions-item label="试卷标题">{{ paperInfo.title }}</el-descriptions-item>
          <el-descriptions-item label="学科">{{ paperInfo.subjectName }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ paperInfo.gradeName }}</el-descriptions-item>
          <el-descriptions-item label="题数">{{ paperInfo.questionCount }}</el-descriptions-item>
          <el-descriptions-item label="总分">{{ paperInfo.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="paperInfo.status === 1 ? 'success' : 'info'">
              {{ paperInfo.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div v-if="paperInfo.questions && paperInfo.questions.length > 0" style="margin-top:20px">
          <h4>题目列表</h4>
          <div v-for="(q, index) in paperInfo.questions" :key="q.id" class="question-item">
            <p><strong>{{ index + 1 }}.</strong> {{ q.content }}</p>
            <p class="answer">答案: {{ q.answer }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { scanQRCode } from '@/api/paper'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const paperInfo = ref(null)

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    error.value = '无效的二维码链接'
    loading.value = false
    return
  }
  
  try {
    const data = await scanQRCode(token)
    paperInfo.value = data
  } catch (e) {
    error.value = e.message || '验证失败，请检查链接是否有效'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.question-item { padding: 10px; margin-bottom: 10px; background: #f5f7fa; border-radius: 4px; }
.answer { color: #666; font-size: 14px; margin-top: 5px; }
</style>