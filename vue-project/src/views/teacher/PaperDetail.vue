<template>
  <div class="paper-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>试卷详情</span>
          <div>
            <el-button @click="goBack">返回</el-button>
            <el-button type="primary" @click="showQRCode">生成二维码</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="paper" class="paper-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="试卷标题">{{ paper.title }}</el-descriptions-item>
          <el-descriptions-item label="学科">{{ paper.subjectName }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ paper.gradeName }}</el-descriptions-item>
          <el-descriptions-item label="试卷类型">
            <el-tag :type="paper.paperType === 'AI' ? 'success' : 'info'">
              {{ paper.paperType === 'AI' ? 'AI生成' : paper.paperType === 'GENERAL' ? '普通' : paper.paperType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总分">{{ paper.totalScore }}分</el-descriptions-item>
          <el-descriptions-item label="题目数量">{{ paper.questionCount }}道</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="paper.status === 1 ? 'success' : 'info'">
              {{ paper.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(paper.createdAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-divider />
      
      <h4>题目列表</h4>
      <div v-if="questions.length > 0" class="questions-list">
        <div v-for="(q, index) in questions" :key="q.id || index" class="question-item">
          <div class="question-header">
            <span class="question-num">{{ index + 1 }}.</span>
            <el-tag :type="getDifficultyType(q.difficulty)" size="small">{{ getDifficultyText(q.difficulty) }}</el-tag>
            <el-tag type="info" size="small">{{ getTypeText(q.type) }}</el-tag>
            <span class="score">{{ q.score || 5 }}分</span>
          </div>
          
          <div class="question-content" v-html="q.content"></div>
          
          <div v-if="q.type === 'CHOICE'" class="question-options">
            <div v-if="q.optionA">A. {{ q.optionA }}</div>
            <div v-if="q.optionB">B. {{ q.optionB }}</div>
            <div v-if="q.optionC">C. {{ q.optionC }}</div>
            <div v-if="q.optionD">D. {{ q.optionD }}</div>
          </div>
          
          <div class="question-answer">
            <span class="label">正确答案：</span>
            <span class="answer">{{ q.answer }}</span>
          </div>
          
          <div v-if="q.analysis" class="question-analysis">
            <span class="label">解析：</span>
            <span>{{ q.analysis }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无题目" />
    </el-card>
    
    <!-- 二维码对话框 -->
    <el-dialog v-model="showQRDialog" title="试卷二维码" width="400px" align-center>
      <div style="text-align:center">
        <canvas ref="qrCanvas" style="max-width:100%"></canvas>
        <p v-if="qrToken" style="margin-top:10px;color:#666;font-size:12px">
          有效期至：{{ qrExpireTime }}
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="downloadQRCode">下载二维码</el-button>
        <el-button @click="showQRDialog=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { getPaperDetail, generatePaperQRCode } from '@/api/paper'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paper = ref(null)
const questions = ref([])

const showQRDialog = ref(false)
const qrCanvas = ref(null)
const qrToken = ref('')
const qrExpireTime = ref('')

const getDifficultyType = (level) => {
  const types = { 1: 'success', 2: 'warning', 3: 'danger' }
  return types[level] || 'info'
}

const getDifficultyText = (level) => {
  const texts = { 1: '简单', 2: '中等', 3: '困难' }
  return texts[level] || '中等'
}

const getTypeText = (type) => {
  const texts = { CHOICE: '选择', BLANK: '填空', ANSWER: '解答' }
  return texts[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const goBack = () => {
  router.push('/teacher/papers')
}

const loadPaperDetail = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('试卷ID不存在')
    goBack()
    return
  }
  
  loading.value = true
  try {
    const data = await getPaperDetail(id)
    if (data?.code === 200) {
      paper.value = data.data.paper
      questions.value = data.data.questions || []
    } else {
      ElMessage.error(data?.message || '加载失败')
      goBack()
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const showQRCode = async () => {
  if (!paper.value?.id) return
  
  try {
    const data = await generatePaperQRCode(paper.value.id)
    qrToken.value = data.token
    qrExpireTime.value = data.expireTime ? new Date(data.expireTime).toLocaleString() : '永久有效'
    
    showQRDialog.value = true
    await nextTick()
    
    const qrUrl = `${window.location.origin}/aiwps/api/qr/scan?token=${data.token}`
    if (qrCanvas.value) {
      QRCode.toCanvas(qrCanvas.value, qrUrl, { width: 280, margin: 2 })
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成二维码失败')
  }
}

const downloadQRCode = () => {
  if (!qrCanvas.value) return
  const link = document.createElement('a')
  link.download = `paper-${paper.value.id}-qrcode.png`
  link.href = qrCanvas.value.toDataURL('image/png')
  link.click()
}

onMounted(() => { loadPaperDetail() })
</script>

<style scoped>
.paper-detail { }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.paper-info { margin-bottom: 20px; }
.questions-list { max-height: 500px; overflow-y: auto; }
.question-item { padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; }
.question-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.question-num { font-weight: bold; color: #409eff; }
.score { margin-left: auto; color: #909399; font-size: 14px; }
.question-content { padding: 12px; background: #f5f7fa; border-radius: 6px; margin-bottom: 12px; }
.question-options { margin-bottom: 12px; padding-left: 20px; }
.question-options div { padding: 4px 0; }
.question-answer { margin-bottom: 8px; }
.question-answer .label { font-weight: bold; color: #67c23a; }
.question-answer .answer { color: #67c23a; }
.question-analysis { padding: 12px; background: #f0f9eb; border-radius: 6px; font-size: 14px; }
.question-analysis .label { font-weight: bold; color: #909399; }
</style>
