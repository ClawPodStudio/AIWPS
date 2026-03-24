<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const wrongId = route.params.wrongId
const loading = ref(true)

const wrongDetail = ref({
  id: wrongId,
  subject: '数学',
  title: '函数奇偶性',
  question: '下列函数中，是偶函数的是（ ）',
  options: [
    { label: 'A', text: 'y = x³' },
    { label: 'B', text: 'y = x²' },
    { label: 'C', text: 'y = 2ˣ' },
    { label: 'D', text: 'y = log₂x' }
  ],
  studentAnswer: 'A',
  correctAnswer: 'B',
  analysis: '偶函数是指满足f(-x)=f(x)的函数。\n\ny=x²满足(-x)²=x²，所以是偶函数。\ny=x³不满足，因为(-x)³=-x³。\ny=2ˣ不满足，因为2⁻ˣ≠2ˣ。\ny=log₂x定义域不对称。',
  knowledgePoints: [
    { id: 1, name: '偶函数定义', content: '如果对于函数f(x)的定义域内任意一个x，都有f(-x)=f(x)，那么函数f(x)就叫做偶函数。' },
    { id: 2, name: '函数性质', content: '偶函数的图像关于y轴对称。' }
  ],
  teacherComment: '这道题考察了偶函数的概念，需要理解偶函数的定义并能灵活运用。建议加强对函数基本性质的理解。',
  date: '2024-03-24',
  times: 3
})

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})

const goToKnowledge = (id) => {
  router.push(`/student/knowledge/${id}`)
}
</script>

<template>
  <div class="wrong-detail-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">错题详情</h2>
      </div>
    </div>

    <div class="content">
      <div class="question-card">
        <div class="card-header">
          <span class="subject-tag" :class="wrongDetail.subject">{{ wrongDetail.subject }}</span>
          <span class="times" v-if="wrongDetail.times > 1">错{{ wrongDetail.times }}次</span>
        </div>
        
        <h3 class="question-title">{{ wrongDetail.title }}</h3>
        <p class="question-content">{{ wrongDetail.question }}</p>
        
        <div class="options">
          <div 
            v-for="option in wrongDetail.options" 
            :key="option.label" 
            class="option"
            :class="{ 
              correct: option.label === wrongDetail.correctAnswer,
              wrong: option.label === wrongDetail.studentAnswer 
            }"
          >
            <span class="option-label">{{ option.label }}</span>
            <span class="option-text">{{ option.text }}</span>
            <svg v-if="option.label === wrongDetail.correctAnswer" class="icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#34d399" stroke-width="3">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="section analysis-section">
        <h3 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          答案解析
        </h3>
        <p class="analysis-text">{{ wrongDetail.analysis }}</p>
      </div>

      <div class="section">
        <h3 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
          知识点
        </h3>
        <div class="knowledge-list">
          <div 
            v-for="kp in wrongDetail.knowledgePoints" 
            :key="kp.id" 
            class="knowledge-card"
            @click="goToKnowledge(kp.id)"
          >
            <h4 class="kp-name">{{ kp.name }}</h4>
            <p class="kp-content">{{ kp.content }}</p>
            <span class="learn-more">学习 ></span>
          </div>
        </div>
      </div>

      <div class="section comment-section">
        <h3 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          老师评语
        </h3>
        <div class="comment-card">
          <p>{{ wrongDetail.teacherComment }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.wrong-detail-page {
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

.content {
  padding: 16px;
}

.question-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  
  .subject-tag {
    padding: 4px 12px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    
    &.数学 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.物理 { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
  }
  
  .times {
    font-size: 12px;
    color: #ef4444;
    font-weight: 500;
  }
}

.question-title {
  font-size: 17px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.question-content {
  font-size: 15px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 16px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option {
  display: flex;
  align-items: center;
  padding: 14px;
  background: #f9fafb;
  border-radius: 12px;
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
    width: 32px;
    height: 32px;
    border-radius: 10px;
    background: #e5e7eb;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    font-weight: 600;
    color: #4b5563;
    margin-right: 12px;
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

.section {
  margin-bottom: 16px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12px;
  }
}

.analysis-section {
  .analysis-text {
    background: #fff;
    border-radius: 16px;
    padding: 16px;
    font-size: 14px;
    color: #4b5563;
    line-height: 1.8;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  }
}

.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.knowledge-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
  
  .kp-name {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 6px;
  }
  
  .kp-content {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .learn-more {
    display: inline-block;
    margin-top: 10px;
    font-size: 13px;
    color: #667eea;
    font-weight: 500;
  }
}

.comment-section {
  .comment-card {
    background: linear-gradient(135deg, rgba(102,126,234,0.08) 0%, rgba(118,75,162,0.08) 100%);
    border-radius: 16px;
    padding: 16px;
    border-left: 4px solid #667eea;
    
    p {
      font-size: 14px;
      color: #4b5563;
      line-height: 1.6;
    }
  }
}
</style>
