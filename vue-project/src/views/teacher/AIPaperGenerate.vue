<template>
  <div class="ai-paper-generate">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI智能生成试卷</span>
        </div>
      </template>
      
      <el-steps :active="step" finish-status="success" style="margin-bottom:30px">
        <el-step title="配置参数" />
        <el-step title="生成预览" />
        <el-step title="保存试卷" />
      </el-steps>
      
      <!-- Step 1: 配置参数 -->
      <div v-show="step === 0">
        <el-form :model="config" label-width="100px" style="max-width:600px">
          <el-form-item label="学科" required>
            <el-select v-model="config.subjectId" placeholder="请选择学科" style="width:100%" @change="onSubjectChange">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="年级" required>
            <el-select v-model="config.gradeId" placeholder="请选择年级" style="width:100%">
              <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="试卷标题">
            <el-input v-model="config.title" placeholder="如不填写则自动生成" />
          </el-form-item>
          
          <el-form-item label="总分">
            <el-input-number v-model="config.totalScore" :min="0" :max="200" :step="10" />
          </el-form-item>
          
          <el-divider content-position="left">题型设置</el-divider>
          
          <el-form-item label="选择题">
            <div class="type-config">
              <span>数量:</span>
              <el-input-number v-model="config.questionTypes.CHOICE" :min="0" :max="50" size="small" />
              <span style="margin-left:20px">每题分数:</span>
              <el-input-number v-model="config.scores.CHOICE" :min="1" :max="20" size="small" />
            </div>
          </el-form-item>
          
          <el-form-item label="填空题">
            <div class="type-config">
              <span>数量:</span>
              <el-input-number v-model="config.questionTypes.BLANK" :min="0" :max="50" size="small" />
              <span style="margin-left:20px">每题分数:</span>
              <el-input-number v-model="config.scores.BLANK" :min="1" :max="20" size="small" />
            </div>
          </el-form-item>
          
          <el-form-item label="解答题">
            <div class="type-config">
              <span>数量:</span>
              <el-input-number v-model="config.questionTypes.ANSWER" :min="0" :max="50" size="small" />
              <span style="margin-left:20px">每题分数:</span>
              <el-input-number v-model="config.scores.ANSWER" :min="1" :max="30" size="small" />
            </div>
          </el-form-item>
          
          <el-divider content-position="left">知识点范围（可选）</el-divider>
          
          <el-form-item label="选择知识点">
            <el-tree
              v-if="knowledgeTree.length > 0"
              :data="knowledgeTree"
              :props="treeProps"
              show-checkbox
              node-key="id"
              :expand-on-click-node="false"
              @check="handleKnowledgeCheck"
              style="max-height:250px;overflow-y:auto"
            />
            <el-empty v-else description="请先选择学科" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :loading="generating" @click="generatePaper">
              <el-icon v-if="!generating"><MagicStick /></el-icon>
              生成试卷
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- Step 2: 生成预览 -->
      <div v-show="step === 1">
        <el-alert v-if="generatedPaper" type="success" :closable="false" style="margin-bottom:20px">
          <template #title>
            试卷生成成功！共 {{ generatedPaper.questionCount }} 道题，总分 {{ generatedPaper.totalScore }} 分
          </template>
        </el-alert>
        
        <div v-if="generatedPaper && generatedPaper.questions" class="paper-preview">
          <h3 style="text-align:center;margin-bottom:20px">{{ generatedPaper.paperName || 'AI生成试卷' }}</h3>
          
          <div v-for="(q, index) in generatedPaper.questions" :key="q.id || index" class="question-item">
            <div class="question-header">
              <span class="question-num">{{ index + 1 }}.</span>
              <el-tag :type="getDifficultyType(q.difficulty)" size="small">{{ getDifficultyText(q.difficulty) }}</el-tag>
              <el-tag type="info" size="small">{{ getTypeText(q.type) }}</el-tag>
              <span class="score">{{ q.score }}分</span>
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
        
        <div style="text-align:center;margin-top:20px">
          <el-button @click="step = 0">重新配置</el-button>
          <el-button type="primary" @click="savePaper">保存试卷</el-button>
        </div>
      </div>
      
      <!-- Step 3: 保存完成 -->
      <div v-show="step === 2">
        <el-result
          icon="success"
          title="试卷保存成功"
          :sub-title="`试卷ID: ${savedPaperId}`"
        >
          <template #extra>
            <el-button type="primary" @click="goToPaperList">查看试卷列表</el-button>
            <el-button @click="resetForm">继续生成</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import { getSubjectList, getGradeList, getKnowledgePointTree } from '@/api/base'
import { aiGeneratePaper, addPaper, getPaperQuestions } from '@/api/paper'

const router = useRouter()
const step = ref(0)
const generating = ref(false)
const subjects = ref([])
const grades = ref([])
const knowledgeTree = ref([])

const config = reactive({
  subjectId: null,
  gradeId: null,
  title: '',
  totalScore: 100,
  questionTypes: { CHOICE: 5, BLANK: 3, ANSWER: 2 },
  scores: { CHOICE: 5, BLANK: 5, ANSWER: 10 },
  selectedKnowledgePoints: []
})

const treeProps = { children: 'children', label: 'name' }
const generatedPaper = ref(null)
const savedPaperId = ref(null)

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

const onSubjectChange = async (subjectId) => {
  if (subjectId) {
    knowledgeTree.value = await getKnowledgePointTree(subjectId, null) || []
  }
}

const handleKnowledgeCheck = (data, checked) => {
  config.selectedKnowledgePoints = checked.checkedKeys.filter(k => k)
}

const generatePaper = async () => {
  if (!config.subjectId) {
    ElMessage.warning('请选择学科')
    return
  }
  
  const totalQuestions = Object.values(config.questionTypes).reduce((a, b) => a + b, 0)
  if (totalQuestions === 0) {
    ElMessage.warning('请至少设置一道题目')
    return
  }
  
  generating.value = true
  
  try {
    const data = await aiGeneratePaper({
      subjectId: config.subjectId,
      gradeId: config.gradeId,
      knowledgePointIds: config.selectedKnowledgePoints.length > 0 ? config.selectedKnowledgePoints : null,
      questionTypes: config.questionTypes,
      totalScore: config.totalScore
    })
    
    if (data?.code === 200) {
      generatedPaper.value = data.data
      step.value = 1
      ElMessage.success('试卷生成成功')
    } else {
      ElMessage.error(data?.message || '生成失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成失败，请重试')
  } finally {
    generating.value = false
  }
}

const savePaper = async () => {
  try {
    // 先创建试卷
    const paperData = {
      title: config.title || `AI试卷-${Date.now()}`,
      subjectId: config.subjectId,
      gradeId: config.gradeId,
      paperType: 'AI',
      totalScore: generatedPaper.value.totalScore,
      questionCount: generatedPaper.value.questionCount
    }
    
    const result = await addPaper(paperData)
    
    if (result?.code === 200 || result?.id) {
      savedPaperId.value = result.id || result.data?.id
      step.value = 2
      ElMessage.success('试卷保存成功')
    } else {
      ElMessage.error(result?.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  }
}

const goToPaperList = () => {
  router.push('/teacher/papers')
}

const resetForm = () => {
  step.value = 0
  config.title = ''
  config.questionTypes = { CHOICE: 5, BLANK: 3, ANSWER: 2 }
  generatedPaper.value = null
}

onMounted(async () => {
  subjects.value = await getSubjectList() || []
  grades.value = await getGradeList() || []
})
</script>

<style scoped>
.ai-paper-generate { }
.card-header { font-size: 16px; font-weight: bold; }
.type-config { display: flex; align-items: center; gap: 10px; }
.paper-preview { max-height: 600px; overflow-y: auto; padding: 10px; }
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
