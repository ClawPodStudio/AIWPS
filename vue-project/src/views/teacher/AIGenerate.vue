<template>
  <div class="ai-generate">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <span>AI智能出题</span>
              <el-tag type="success">基于大模型</el-tag>
            </div>
          </template>
          
          <el-form :model="config" label-width="100px">
            <el-form-item label="出题模式">
              <el-radio-group v-model="config.mode">
                <el-radio label="knowledge">根据知识点出题</el-radio>
                <el-radio label="ai">自由描述出题</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="学科" required>
              <el-select v-model="config.subjectId" placeholder="请选择学科" style="width: 100%">
                <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="年级" required>
              <el-select v-model="config.gradeId" placeholder="请选择年级" style="width: 100%">
                <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="题型">
              <el-checkbox-group v-model="config.types">
                <el-checkbox label="CHOICE">选择题</el-checkbox>
                <el-checkbox label="BLANK">填空题</el-checkbox>
                <el-checkbox label="ANSWER">解答题</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="题目数量">
              <el-slider v-model="config.count" :min="1" :max="20" show-input />
            </el-form-item>
            
            <el-form-item label="难度分布">
              <div class="difficulty-config">
                <div class="diff-item">
                  <span>简单</span>
                  <el-input-number v-model="config.easyRatio" :min="0" :max="100" size="small" />%
                </div>
                <div class="diff-item">
                  <span>中等</span>
                  <el-input-number v-model="config.mediumRatio" :min="0" :max="100" size="small" />%
                </div>
                <div class="diff-item">
                  <span>困难</span>
                  <el-input-number v-model="config.hardRatio" :min="0" :max="100" size="small" />%
                </div>
              </div>
            </el-form-item>
            
            <!-- 知识点模式 -->
            <el-form-item v-if="config.mode === 'knowledge'" label="选择知识点">
              <el-tree
                :data="knowledgeTree"
                :props="treeProps"
                show-checkbox
                node-key="id"
                :expand-on-click-node="false"
                @check="handleKnowledgeCheck"
              />
            </el-form-item>
            
            <!-- AI自由模式 -->
            <el-form-item v-if="config.mode === 'ai'" label="出题描述">
              <el-input
                v-model="config.prompt"
                type="textarea"
                :rows="6"
                placeholder="请描述您想要的题目类型，例如：'出5道关于二次函数图像变换的选择题，难度适中'"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" :loading="generating" @click="generateQuestions">
                <el-icon><MagicStick /></el-icon>
                开始生成
              </el-button>
              <el-button @click="resetConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <!-- 生成结果 -->
      <el-col :span="12">
        <el-card class="result-card">
          <template #header>
            <div class="card-header">
              <span>生成结果</span>
              <div v-if="generatedQuestions.length > 0">
                <el-button type="success" size="small" @click="saveAllQuestions">批量保存</el-button>
                <el-button size="small" @click="exportQuestions">导出</el-button>
              </div>
            </div>
          </template>
          
          <div v-if="generating" class="generating-state">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <p>AI正在生成题目中...</p>
            <el-progress :percentage="generateProgress" :status="generateProgress === 100 ? 'success' : ''" />
          </div>
          
          <div v-else-if="generatedQuestions.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p>暂无生成结果</p>
            <span>请在左侧配置题目参数并点击生成</span>
          </div>
          
          <div v-else class="questions-list">
            <div v-for="(q, index) in generatedQuestions" :key="index" class="question-item">
              <div class="question-header">
                <span class="question-num">第{{ index + 1 }}题</span>
                <el-tag :type="getDifficultyType(q.difficulty)" size="small">
                  {{ getDifficultyText(q.difficulty) }}
                </el-tag>
                <el-tag type="info" size="small">{{ getTypeText(q.type) }}</el-tag>
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
              
              <div class="question-actions">
                <el-button type="primary" link size="small" @click="editQuestion(q)">编辑</el-button>
                <el-button type="success" link size="small" @click="saveQuestion(q)">保存</el-button>
                <el-button type="danger" link size="small" @click="removeQuestion(index)">删除</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Loading, Document } from '@element-plus/icons-vue'
import { getSubjectList, getGradeList, getKnowledgePointTree } from '@/api/base'
import { generateQuestionsByAI, addQuestion } from '@/api/question'

const generating = ref(false)
const generateProgress = ref(0)
const generatedQuestions = ref([])
const subjects = ref([])
const grades = ref([])
const knowledgeTree = ref([])

const config = reactive({
  mode: 'knowledge',
  subjectId: '',
  gradeId: '',
  types: ['CHOICE'],
  count: 5,
  easyRatio: 30,
  mediumRatio: 50,
  hardRatio: 20,
  selectedKnowledgePoints: [],
  prompt: ''
})

const treeProps = { children: 'children', label: 'name' }

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

const handleKnowledgeCheck = (data, checked) => {
  config.selectedKnowledgePoints = checked.checkedKeys
}

const loadBaseData = async () => {
  subjects.value = await getSubjectList() || []
  grades.value = await getGradeList() || []
  if (subjects.value[0]) {
    config.subjectId = subjects.value[0].id
    knowledgeTree.value = await getKnowledgePointTree(config.subjectId, null) || []
  }
}

const generateQuestions = async () => {
  if (!config.subjectId || !config.gradeId) {
    ElMessage.warning('请选择学科和年级')
    return
  }
  
  if (config.mode === 'knowledge' && config.selectedKnowledgePoints.length === 0) {
    ElMessage.warning('请选择知识点')
    return
  }
  
  if (config.mode === 'ai' && !config.prompt.trim()) {
    ElMessage.warning('请输入出题描述')
    return
  }
  
  generating.value = true
  generateProgress.value = 0
  
  const timer = setInterval(() => {
    if (generateProgress.value < 90) {
      generateProgress.value += Math.random() * 20
    }
  }, 500)
  
  try {
    const data = await generateQuestionsByAI({
      mode: config.mode,
      subjectId: config.subjectId,
      gradeId: config.gradeId,
      types: config.types,
      count: config.count,
      difficulty: { easy: config.easyRatio, medium: config.mediumRatio, hard: config.hardRatio },
      knowledgePointIds: config.selectedKnowledgePoints,
      prompt: config.prompt
    })
    
    generatedQuestions.value = data?.questions || []
    generateProgress.value = 100
    ElMessage.success(`成功生成 ${generatedQuestions.value.length} 道题目`)
  } catch (e) {
    console.error(e)
    ElMessage.error('生成失败，请重试')
  } finally {
    clearInterval(timer)
    setTimeout(() => { generating.value = false }, 500)
  }
}

const resetConfig = () => {
  config.types = ['CHOICE']
  config.count = 5
  config.easyRatio = 30
  config.mediumRatio = 50
  config.hardRatio = 20
  config.selectedKnowledgePoints = []
  config.prompt = ''
  generatedQuestions.value = []
}

const editQuestion = (q) => {
  // 编辑题目
}

const saveQuestion = async (q) => {
  try {
    await addQuestion({ ...q, subjectId: config.subjectId, gradeId: config.gradeId })
    ElMessage.success('保存成功')
  } catch (e) { console.error(e) }
}

const saveAllQuestions = async () => {
  try {
    for (const q of generatedQuestions.value) {
      await addQuestion({ ...q, subjectId: config.subjectId, gradeId: config.gradeId })
    }
    ElMessage.success('批量保存成功')
    generatedQuestions.value = []
  } catch (e) { console.error(e) }
}

const removeQuestion = (index) => {
  generatedQuestions.value.splice(index, 1)
}

const exportQuestions = () => {
  ElMessage.info('导出功能开发中')
}

onMounted(() => { loadBaseData() })
</script>

<style scoped>
.ai-generate { }
.config-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.difficulty-config { display: flex; gap: 20px; }
.diff-item { display: flex; align-items: center; gap: 8px; }
.generating-state { text-align: center; padding: 60px 20px; }
.loading-icon { font-size: 48px; color: #409eff; margin-bottom: 20px; animation: rotate 1s linear infinite; }
@keyframes rotate { from { transform: rotate(0deg) } to { transform: rotate(360deg) } }
.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 64px; color: #dcdfe6; margin-bottom: 20px; }
.empty-state p { font-size: 16px; color: #606266; margin-bottom: 8px; }
.empty-state span { font-size: 14px; color: #909399; }
.questions-list { max-height: 600px; overflow-y: auto; }
.question-item { padding: 16px; border: 1px solid #ebeef5; border-radius: 8px; margin-bottom: 16px; }
.question-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.question-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.question-num { font-weight: bold; color: #409eff; }
.question-content { padding: 12px; background: #f5f7fa; border-radius: 6px; margin-bottom: 12px; }
.question-options { margin-bottom: 12px; padding-left: 20px; }
.question-options div { padding: 4px 0; }
.question-answer { margin-bottom: 8px; }
.question-answer .label { font-weight: bold; color: #67c23a; }
.question-answer .answer { color: #67c23a; }
.question-analysis { padding: 12px; background: #f0f9eb; border-radius: 6px; font-size: 14px; }
.question-analysis .label { font-weight: bold; color: #909399; }
.question-actions { margin-top: 12px; display: flex; gap: 10px; justify-content: flex-end; }
</style>
