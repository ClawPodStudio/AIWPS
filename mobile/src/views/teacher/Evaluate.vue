<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const studentPaperId = route.params.studentPaperId
const loading = ref(false)

// Mock data
const wrongQuestions = ref([
  { 
    id: 1, 
    number: 5, 
    type: '选择题', 
    content: '下列函数中，是偶函数的是（ ）', 
    studentAnswer: 'A',
    correctAnswer: 'B',
    reason: '',
    knowledgePoints: ['偶函数定义', '函数性质']
  },
  { 
    id: 2, 
    number: 12, 
    type: '填空题', 
    content: '若函数f(x)为偶函数，且f(2)=3，则f(-2)=___', 
    studentAnswer: '2',
    correctAnswer: '3',
    reason: '',
    knowledgePoints: ['偶函数性质']
  }
])

const teacherComment = ref('')
const selectedReasons = ref({})

const reasonOptions = [
  '审题错误',
  '概念不清',
  '计算失误',
  '方法不当',
  '知识漏洞',
  '粗心大意',
  '理解偏差',
  '其他'
]

const toggleReason = (questionId, reason) => {
  if (!selectedReasons.value[questionId]) {
    selectedReasons.value[questionId] = []
  }
  const idx = selectedReasons.value[questionId].indexOf(reason)
  if (idx > -1) {
    selectedReasons.value[questionId].splice(idx, 1)
  } else {
    selectedReasons.value[questionId].push(reason)
  }
}

const submitEvaluation = () => {
  // Validate
  for (const q of wrongQuestions.value) {
    if (!selectedReasons.value[q.id] || selectedReasons.value[q.id].length === 0) {
      Toast('请为所有错题选择错题原因')
      return
    }
  }
  
  loading.value = true
  
  // Simulate API call
  setTimeout(() => {
    loading.value = false
    showSuccessToast('评价提交成功')
    router.back()
  }, 1000)
}
</script>

<template>
  <div class="evaluate-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">错题评价</h2>
      </div>
    </div>

    <div class="content">
      <div class="section">
        <h3 class="section-title">错题原因标注</h3>
        
        <div 
          v-for="question in wrongQuestions" 
          :key="question.id" 
          class="question-card"
        >
          <div class="question-header">
            <span class="number">第{{ question.number }}题</span>
            <span class="type">{{ question.type }}</span>
          </div>
          
          <p class="question-content">{{ question.content }}</p>
          
          <div class="answer-info">
            <div class="answer">
              <span class="label">学生答案:</span>
              <span class="value wrong">{{ question.studentAnswer }}</span>
            </div>
            <div class="answer">
              <span class="label">正确答案:</span>
              <span class="value correct">{{ question.correctAnswer }}</span>
            </div>
          </div>
          
          <div class="knowledge-tags">
            <span class="label">知识点:</span>
            <span 
              v-for="kp in question.knowledgePoints" 
              :key="kp" 
              class="tag"
            >
              {{ kp }}
            </span>
          </div>
          
          <div class="reason-section">
            <p class="reason-label">错题原因 (可多选):</p>
            <div class="reason-options">
              <span 
                v-for="reason in reasonOptions" 
                :key="reason"
                class="reason-chip"
                :class="{ active: selectedReasons[question.id]?.includes(reason) }"
                @click="toggleReason(question.id, reason)"
              >
                {{ reason }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">老师评语</h3>
        <div class="comment-box">
          <textarea 
            v-model="teacherComment" 
            placeholder="请输入对该生本次考试的整体评价..."
            rows="4"
          ></textarea>
        </div>
      </div>
    </div>

    <div class="footer">
      <button 
        class="submit-btn" 
        :class="{ loading }"
        @click="submitEvaluation"
        :disabled="loading"
      >
        {{ loading ? '提交中...' : '提交评价' }}
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.evaluate-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 100px;
}

.header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .back {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    background: rgba(255,255,255,0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    
    &:active {
      background: rgba(255,255,255,0.25);
    }
  }
  
  .header-content {
    flex: 1;
    margin-left: 12px;
    
    .title {
      font-size: 20px;
      font-weight: 600;
      color: #fff;
    }
  }
}

.content {
  padding: 16px;
}

.section {
  margin-bottom: 20px;
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12px;
  }
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  
  .number {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .type {
    font-size: 12px;
    color: #6b7280;
  }
}

.question-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  margin-bottom: 12px;
}

.answer-info {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  
  .answer {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .label {
      font-size: 13px;
      color: #6b7280;
    }
    
    .value {
      font-size: 14px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: 6px;
      
      &.wrong {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
      }
      
      &.correct {
        background: rgba(52, 211, 153, 0.1);
        color: #34d399;
      }
    }
  }
}

.knowledge-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
  
  .label {
    font-size: 13px;
    color: #6b7280;
  }
  
  .tag {
    padding: 3px 10px;
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
    border-radius: 10px;
    font-size: 12px;
  }
}

.reason-section {
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
  
  .reason-label {
    font-size: 13px;
    font-weight: 500;
    color: #1f2937;
    margin-bottom: 10px;
  }
  
  .reason-options {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .reason-chip {
      padding: 6px 14px;
      background: #f3f4f6;
      border-radius: 16px;
      font-size: 13px;
      color: #4b5563;
      cursor: pointer;
      transition: all 0.2s;
      
      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
  }
}

.comment-box {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  
  textarea {
    width: 100%;
    padding: 16px;
    border: none;
    font-size: 14px;
    color: #1f2937;
    resize: none;
    font-family: inherit;
    
    &::placeholder {
      color: #9ca3af;
    }
    
    &:focus {
      outline: none;
    }
  }
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.loading {
    opacity: 0.7;
  }
  
  &:disabled {
    cursor: not-allowed;
  }
}
</style>
