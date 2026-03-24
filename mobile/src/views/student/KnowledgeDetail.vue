<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const knowledgeId = route.params.knowledgeId
const loading = ref(true)

const knowledge = ref({
  id: knowledgeId,
  name: '偶函数定义',
  subject: '数学',
  content: `如果对于函数f(x)的定义域内任意一个x，都有f(-x)=f(x)，那么函数f(x)就叫做偶函数。

偶函数具有以下性质：

1. 图像关于y轴对称
2. f(0) = 0（如果0在定义域内）
3. 如果f(x)是偶函数，那么|f(x)|也是偶函数

常见偶函数：
- y = x²（平方函数）
- y = |x|（绝对值函数）
- y = cos x（余弦函数）

判断偶函数的方法：
1. 验证f(-x)是否等于f(x)
2. 观察函数图像是否关于y轴对称`,
  relatedQuestions: [
    { id: 1, title: '判断函数奇偶性', difficulty: '基础' },
    { id: 2, title: '偶函数的应用', difficulty: '中等' },
    { id: 3, title: '综合判断题', difficulty: '困难' }
  ],
  videoUrl: ''
})

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<template>
  <div class="knowledge-detail-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">{{ knowledge.name }}</h2>
        <span class="subject-tag">{{ knowledge.subject }}</span>
      </div>
    </div>

    <div class="content">
      <div class="section">
        <h3 class="section-title">知识点详解</h3>
        <div class="knowledge-content">
          <p v-for="(para, idx) in knowledge.content.split('\n\n')" :key="idx">{{ para }}</p>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">相关练习</h3>
        <div class="practice-list">
          <div 
            v-for="q in knowledge.relatedQuestions" 
            :key="q.id" 
            class="practice-card"
          >
            <div class="practice-info">
              <h4 class="practice-title">{{ q.title }}</h4>
              <span class="difficulty" :class="q.difficulty">{{ q.difficulty }}</span>
            </div>
            <button class="practice-btn">练习</button>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">掌握程度</h3>
        <div class="mastery-section">
          <div class="mastery-bar">
            <div class="mastery-progress" style="width: 60%"></div>
          </div>
          <p class="mastery-text">当前掌握度: 60%</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.knowledge-detail-page {
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
      margin-bottom: 6px;
    }
    
    .subject-tag {
      display: inline-block;
      padding: 3px 10px;
      background: rgba(255,255,255,0.2);
      border-radius: 10px;
      font-size: 12px;
      color: #fff;
    }
  }
}

.content {
  padding: 16px;
}

.section {
  margin-bottom: 24px;
  
  .section-title {
    font-size: 17px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 14px;
  }
}

.knowledge-content {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  
  p {
    font-size: 15px;
    color: #4b5563;
    line-height: 1.8;
    margin-bottom: 14px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.practice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.practice-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .practice-info {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .practice-title {
      font-size: 15px;
      font-weight: 500;
      color: #1f2937;
    }
    
    .difficulty {
      padding: 3px 8px;
      border-radius: 8px;
      font-size: 11px;
      font-weight: 500;
      
      &.基础 {
        background: rgba(52, 211, 153, 0.1);
        color: #34d399;
      }
      
      &.中等 {
        background: rgba(251, 191, 36, 0.1);
        color: #f59e0b;
      }
      
      &.困难 {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
      }
    }
  }
  
  .practice-btn {
    padding: 8px 18px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    
    &:active {
      opacity: 0.9;
    }
  }
}

.mastery-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  
  .mastery-bar {
    height: 10px;
    background: #e5e7eb;
    border-radius: 5px;
    overflow: hidden;
    margin-bottom: 10px;
    
    .mastery-progress {
      height: 100%;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 5px;
    }
  }
  
  .mastery-text {
    font-size: 14px;
    color: #6b7280;
    text-align: center;
  }
}
</style>
