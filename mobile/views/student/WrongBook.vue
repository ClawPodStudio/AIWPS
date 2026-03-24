<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Tab, Tabs } from 'vant'

const router = useRouter()
const activeTab = ref(0)
const loading = ref(true)

const subjects = ['全部', '数学', '物理', '英语', '化学', '生物']

const wrongBySubject = ref({
  '数学': [
    { id: 1, title: '函数奇偶性', question: '下列函数中，是偶函数的是...', correctAnswer: 'B', knowledgePoints: ['偶函数定义', '函数性质'], times: 3 },
    { id: 2, title: '幂函数图像', question: '已知幂函数...', correctAnswer: 'C', knowledgePoints: ['幂函数'], times: 1 }
  ],
  '物理': [
    { id: 3, title: '动能定理', question: '关于动能定理，下列说法正确的是...', correctAnswer: 'A', knowledgePoints: ['动能', '功'], times: 2 }
  ],
  '英语': []
})

const allWrongs = ref([
  { id: 1, subject: '数学', title: '函数奇偶性', question: '下列函数中，是偶函数的是...', correctAnswer: 'B', knowledgePoints: ['偶函数定义', '函数性质'], times: 3, date: '03-24' },
  { id: 2, subject: '数学', title: '幂函数图像', question: '已知幂函数...', correctAnswer: 'C', knowledgePoints: ['幂函数'], times: 1, date: '03-22' },
  { id: 3, subject: '物理', title: '动能定理', question: '关于动能定理，下列说法正确的是...', correctAnswer: 'A', knowledgePoints: ['动能', '功'], times: 2, date: '03-20' }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 600)
})

const currentSubject = ref('全部')

const getCurrentList = () => {
  if (currentSubject.value === '全部') {
    return allWrongs.value
  }
  return wrongBySubject[currentSubject.value] || []
}

const goToDetail = (wrongId) => {
  router.push(`/student/wrong/${wrongId}`)
}

const goToKnowledge = (knowledgeId) => {
  router.push(`/student/knowledge/${knowledgeId}`)
}

const onTabChange = (index) => {
  currentSubject.value = subjects[index]
}
</script>

<template>
  <div class="wrong-book-page">
    <div class="header">
      <h2 class="title">错题本</h2>
      <p class="desc">共{{ allWrongs.length }}道错题</p>
    </div>

    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky color="#667eea">
      <van-tab v-for="subject in subjects" :key="subject" :title="subject">
        <div class="tab-content">
          <div v-if="getCurrentList().length === 0" class="empty-state">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <p>这个学科还没有错题</p>
          </div>
          
          <div 
            v-else
            v-for="wrong in getCurrentList()" 
            :key="wrong.id" 
            class="wrong-card"
            @click="goToDetail(wrong.id)"
          >
            <div class="card-header">
              <span class="subject-tag" :class="wrong.subject">{{ wrong.subject }}</span>
              <span class="times" v-if="wrong.times > 1">错{{ wrong.times }}次</span>
            </div>
            
            <h3 class="question-title">{{ wrong.title }}</h3>
            <p class="question-content">{{ wrong.question }}</p>
            
            <div class="answer-row">
              <span class="label">正确答案:</span>
              <span class="answer">{{ wrong.correctAnswer }}</span>
            </div>
            
            <div class="knowledge-row">
              <span class="label">知识点:</span>
              <div class="tags">
                <span 
                  v-for="kp in wrong.knowledgePoints" 
                  :key="kp" 
                  class="tag"
                  @click.stop="goToKnowledge(kp)"
                >
                  {{ kp }}
                </span>
              </div>
            </div>
            
            <div class="card-footer">
              <span class="date">{{ wrong.date }}</span>
              <span class="view-detail">查看详情 ></span>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<style lang="scss" scoped>
.wrong-book-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  padding: 20px 16px;
  
  .title {
    font-size: 24px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 4px;
  }
  
  .desc {
    font-size: 14px;
    color: rgba(255,255,255,0.8);
  }
}

.tab-content {
  padding: 12px 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
  
  p {
    margin-top: 16px;
    font-size: 15px;
  }
}

.wrong-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  
  .subject-tag {
    padding: 4px 12px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    
    &.数学 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.物理 { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
    &.英语 { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
    &.化学 { background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%); }
    &.生物 { background: linear-gradient(135deg, #22d3ee 0%, #06b6d4 100%); }
  }
  
  .times {
    font-size: 12px;
    color: #ef4444;
    font-weight: 500;
  }
}

.question-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 6px;
}

.question-content {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.answer-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  
  .label {
    font-size: 13px;
    color: #6b7280;
    margin-right: 8px;
  }
  
  .answer {
    font-size: 14px;
    font-weight: 600;
    color: #34d399;
    background: rgba(52, 211, 153, 0.1);
    padding: 4px 10px;
    border-radius: 6px;
  }
}

.knowledge-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  
  .label {
    font-size: 13px;
    color: #6b7280;
    margin-right: 8px;
    flex-shrink: 0;
  }
  
  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    
    .tag {
      padding: 4px 10px;
      background: rgba(102, 126, 234, 0.1);
      color: #667eea;
      border-radius: 10px;
      font-size: 12px;
      
      &:active {
        background: rgba(102, 126, 234, 0.2);
      }
    }
  }
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  
  .date {
    font-size: 12px;
    color: #9ca3af;
  }
  
  .view-detail {
    font-size: 13px;
    color: #667eea;
    font-weight: 500;
  }
}
</style>
