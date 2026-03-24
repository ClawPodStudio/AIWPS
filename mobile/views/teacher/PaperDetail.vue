<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const paperId = route.params.paperId
const loading = ref(true)

const paper = ref({
  id: paperId,
  title: '高一数学期中考试',
  subject: '数学',
  totalScore: 100,
  time: '90分钟',
  date: '2024-03-20',
  questions: [
    { 
      id: 1, 
      number: 1, 
      type: '选择题', 
      content: '下列函数中，是偶函数的是（ ）', 
      options: [
        { label: 'A', text: 'y = x³' },
        { label: 'B', text: 'y = x²' },
        { label: 'C', text: 'y = 2ˣ' },
        { label: 'D', text: 'y = log₂x' }
      ],
      studentAnswer: 'A',
      correctAnswer: 'B',
      isCorrect: false,
      analysis: '偶函数满足f(-x)=f(x)，只有y=x²满足此条件'
    },
    { 
      id: 2, 
      number: 2, 
      type: '选择题', 
      content: '设集合A={1,2,3}，B={2,3,4}，则A∩B=（ ）', 
      options: [
        { label: 'A', text: '{1,2,3,4}' },
        { label: 'B', text: '{2,3}' },
        { label: 'C', text: '{1,4}' },
        { label: 'D', text: '{1}' }
      ],
      studentAnswer: 'B',
      correctAnswer: 'B',
      isCorrect: true,
      analysis: 'A∩B表示A和B的公共元素，为{2,3}'
    }
  ]
})

const score = ref(45)

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<template>
  <div class="paper-detail-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">试卷详情</h2>
      </div>
    </div>

    <div class="paper-info-card">
      <h3 class="paper-title">{{ paper.title }}</h3>
      <div class="paper-meta">
        <span class="tag subject">{{ paper.subject }}</span>
        <span class="tag">{{ paper.time }}</span>
        <span class="tag">{{ paper.date }}</span>
      </div>
      <div class="score-display">
        <div class="score-circle">
          <span class="score">{{ score }}</span>
          <span class="total">/{{ paper.totalScore }}</span>
        </div>
        <span class="score-label">得分</span>
      </div>
    </div>

    <div class="questions-list">
      <div 
        v-for="question in paper.questions" 
        :key="question.id" 
        class="question-card"
        :class="{ wrong: !question.isCorrect }"
      >
        <div class="question-header">
          <span class="question-number">第{{ question.number }}题</span>
          <span class="question-type">{{ question.type }}</span>
          <span class="result-tag" :class="question.isCorrect ? 'correct' : 'wrong'">
            {{ question.isCorrect ? '正确' : '错误' }}
          </span>
        </div>
        
        <p class="question-content">{{ question.content }}</p>
        
        <div v-if="question.options" class="options-list">
          <div 
            v-for="option in question.options" 
            :key="option.label" 
            class="option-item"
            :class="{ 
              correct: option.label === question.correctAnswer,
              wrong: option.label === question.studentAnswer && !question.isCorrect,
              selected: option.label === question.studentAnswer
            }"
          >
            <span class="option-label">{{ option.label }}</span>
            <span class="option-text">{{ option.text }}</span>
            <svg v-if="option.label === question.correctAnswer" class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="3">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
        </div>
        
        <div v-if="!question.isCorrect" class="analysis">
          <div class="analysis-label">解析</div>
          <p>{{ question.analysis }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.paper-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
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

.paper-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  padding-top: 0;
  
  .paper-title {
    font-size: 20px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 12px;
  }
  
  .paper-meta {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    
    .tag {
      padding: 4px 10px;
      background: rgba(255,255,255,0.2);
      border-radius: 12px;
      font-size: 12px;
      color: #fff;
    }
  }
  
  .score-display {
    display: flex;
    align-items: center;
    gap: 12px;
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    
    .score-circle {
      display: flex;
      align-items: baseline;
      
      .score {
        font-size: 32px;
        font-weight: 700;
        color: #667eea;
      }
      
      .total {
        font-size: 16px;
        color: #9ca3af;
      }
    }
    
    .score-label {
      font-size: 14px;
      color: #6b7280;
    }
  }
}

.questions-list {
  padding: 16px;
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  &.wrong {
    border-left: 4px solid #ef4444;
  }
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  
  .question-number {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }
  
  .question-type {
    font-size: 12px;
    color: #6b7280;
    flex: 1;
  }
  
  .result-tag {
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    
    &.correct {
      background: rgba(52, 211, 153, 0.1);
      color: #34d399;
    }
    
    &.wrong {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }
  }
}

.question-content {
  font-size: 15px;
  color: #1f2937;
  line-height: 1.6;
  margin-bottom: 12px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px solid transparent;
  
  &.correct {
    background: rgba(52, 211, 153, 0.1);
    border-color: #34d399;
  }
  
  &.wrong {
    background: rgba(239, 68, 68, 0.1);
    border-color: #ef4444;
  }
  
  .option-label {
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: #e5e7eb;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    color: #4b5563;
    margin-right: 10px;
  }
  
  .option-text {
    flex: 1;
    font-size: 14px;
    color: #4b5563;
  }
  
  .icon {
    margin-left: 8px;
  }
}

.analysis {
  margin-top: 12px;
  padding: 12px;
  background: #fef3c7;
  border-radius: 10px;
  
  .analysis-label {
    font-size: 13px;
    font-weight: 600;
    color: #f59e0b;
    margin-bottom: 6px;
  }
  
  p {
    font-size: 14px;
    color: #92400e;
    line-height: 1.5;
  }
}
</style>
