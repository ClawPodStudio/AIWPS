<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'


const route = useRoute()
const router = useRouter()
const studentId = route.params.studentId
const loading = ref(true)

const studentInfo = ref({
  id: studentId,
  name: '王小明',
  class: '高一(1)班'
})

const wrongPapers = ref([
  {
    id: 1,
    paperTitle: '高一数学期中考试',
    subject: '数学',
    wrongCount: 8,
    status: 'pending',
    wrongQuestions: [
      { id: 101, number: 5, type: '选择题', content: '下列函数中，是偶函数的是...', studentAnswer: 'A', correctAnswer: 'C' },
      { id: 102, number: 12, type: '填空题', content: '若函数f(x)...', studentAnswer: '2', correctAnswer: '-2' }
    ]
  },
  {
    id: 2,
    paperTitle: '高一物理月考',
    subject: '物理',
    wrongCount: 5,
    status: 'evaluated',
    wrongQuestions: []
  }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})

const goToEvaluate = (paperId) => {
  router.push(`/teacher/evaluate/${paperId}`)
}

const goToPaper = (paperId) => {
  router.push(`/teacher/paper/${paperId}`)
}
</script>

<template>
  <div class="student-wrong-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">{{ studentInfo.name }}的错题</h2>
        <p class="desc">{{ studentInfo.class }}</p>
      </div>
    </div>

    <div class="paper-list">
      <div 
        v-for="paper in wrongPapers" 
        :key="paper.id" 
        class="paper-card"
      >
        <div class="paper-header">
          <div class="paper-icon" :class="paper.subject">
            <span>{{ paper.subject.charAt(0) }}</span>
          </div>
          <div class="paper-info">
            <h3 class="paper-title">{{ paper.paperTitle }}</h3>
            <p class="paper-meta">
              <span class="subject">{{ paper.subject }}</span>
              <span class="dot">·</span>
              <span class="wrong-count">{{ paper.wrongCount }}道错题</span>
            </p>
          </div>
          <div class="status-tag" :class="paper.status">
            {{ paper.status === 'pending' ? '待批改' : '已批改' }}
          </div>
        </div>
        
        <div class="paper-actions">
          <button class="action-btn" @click="goToPaper(paper.id)">
            查看试卷
          </button>
          <button 
            v-if="paper.status === 'pending'" 
            class="action-btn primary" 
            @click="goToEvaluate(paper.id)"
          >
            评价错题
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.student-wrong-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 12px;
  
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
    
    .title {
      font-size: 20px;
      font-weight: 600;
      color: #fff;
    }
    
    .desc {
      font-size: 13px;
      color: rgba(255,255,255,0.7);
      margin-top: 2px;
    }
  }
}

.paper-list {
  padding: 12px 16px;
}

.paper-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.paper-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 14px;
  
  .paper-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    
    &.数学 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.物理 { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
    &.英语 { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
  }
  
  .paper-info {
    flex: 1;
    
    .paper-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .paper-meta {
      font-size: 13px;
      color: #6b7280;
      
      .dot {
        margin: 0 6px;
      }
    }
  }
  
  .status-tag {
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    
    &.pending {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }
    
    &.evaluated {
      background: rgba(52, 211, 153, 0.1);
      color: #34d399;
    }
  }
}

.paper-actions {
  display: flex;
  gap: 10px;
  
  .action-btn {
    flex: 1;
    padding: 10px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    border: 1px solid #e5e7eb;
    background: #fff;
    color: #4b5563;
    
    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border: none;
    }
    
    &:active {
      opacity: 0.9;
    }
  }
}
</style>
