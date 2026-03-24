<template>
  <div class="practice">
    <el-card v-if="!practiceStarted" class="start-card">
      <el-icon class="start-icon" style="font-size:80px;color:#409eff;margin-bottom:20px"><Edit /></el-icon>
      <h2>每日练习</h2><p>选择学科开始今日练习</p>
      <el-form :inline="true"><el-form-item label="学科"><el-select v-model="practiceParams.subjectId" placeholder="请选择学科" style="width:160px"><el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" /></el-select></el-form-item></el-form>
      <el-button type="primary" size="large" @click="startPractice" :loading="loading">开始练习</el-button>
    </el-card>
    <div v-else class="practice-questions">
      <div class="practice-header"><span>题目 {{ currentIndex + 1 }} / {{ questions.length }}</span><el-button @click="practiceStarted=false" type="info">结束练习</el-button></div>
      <el-card v-if="currentQuestion">
        <div class="question-content" v-html="currentQuestion.content"></div>
        <div v-if="currentQuestion.type==='CHOICE'" class="options-list">
          <div v-for="opt in getOptions(currentQuestion)" :key="opt.key" class="option-item" :class="{selected:selectedAnswer===opt.key}" @click="selectedAnswer=opt.key"><span class="option-key">{{opt.key}}.</span><span class="option-text">{{opt.text}}</span></div>
        </div>
        <el-input v-else v-model="selectedAnswer" type="textarea" :rows="4" placeholder="请输入答案" style="margin:20px 0" />
        <div style="text-align:center">
          <el-button v-if="currentIndex>0" @click="currentIndex--">上一题</el-button>
          <el-button v-if="currentIndex<questions.length-1" type="primary" @click="currentIndex++">下一题</el-button>
          <el-button v-if="currentIndex===questions.length-1" type="success" @click="submitPractice">提交</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getSubjectList } from '@/api/base'
import { getPracticeQuestions } from '@/api/question'

const loading = ref(false)
const practiceStarted = ref(false)
const subjects = ref([])
const questions = ref([])
const currentIndex = ref(0)
const selectedAnswer = ref('')
const practiceParams = reactive({ subjectId: '' })

const currentQuestion = computed(() => questions.value[currentIndex.value])
const getOptions = (q) => { const opts=[]; if(q.optionA)opts.push({key:'A',text:q.optionA});if(q.optionB)opts.push({key:'B',text:q.optionB});if(q.optionC)opts.push({key:'C',text:q.optionC});if(q.optionD)opts.push({key:'D',text:q.optionD});return opts }

const startPractice = async () => { if(!practiceParams.subjectId){ElMessage.warning('请选择学科');return} loading.value=true; try{ const data=await getPracticeQuestions({subjectId:practiceParams.subjectId,count:10}); questions.value=data||[]; if(questions.value.length===0){ElMessage.warning('暂无题目');return} practiceStarted.value=true;currentIndex.value=0;selectedAnswer.value='' }catch(e){console.error(e)}finally{loading.value=false} }
const submitPractice = () => { ElMessage.success('练习完成'); practiceStarted.value=false }

onMounted(async ()=>{ subjects.value=await getSubjectList()||[]; if(subjects.value.length>0) practiceParams.subjectId=subjects.value[0].id })
</script>

<style scoped>
.start-card{text-align:center;padding:40px}.practice-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;padding:16px;background:#fff;border-radius:8px}.question-content{font-size:16px;line-height:1.8;margin-bottom:24px;padding:16px;background:#f5f7fa;border-radius:8px}.options-list{display:flex;flex-direction:column;gap:12px;margin-bottom:24px}.option-item{display:flex;align-items:flex-start;padding:14px 16px;background:#f5f7fa;border:2px solid transparent;border-radius:8px;cursor:pointer;transition:all .2s}.option-item:hover{background:#ecf5ff;border-color:#409eff}.option-item.selected{background:#ecf5ff;border-color:#409eff}.option-key{font-weight:bold;margin-right:12px;color:#409eff}.option-text{line-height:1.6}
</style>
