<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)

const todayTasks = ref([
  { id: 1, title: '数学错题复习', subject: '数学', count: 5, deadline: '今天 18:00' },
  { id: 2, title: '物理知识点巩固', subject: '物理', count: 3, deadline: '今天 20:00' }
])

const wrongReminders = ref([
  { id: 1, subject: '数学', topic: '函数奇偶性', times: 3, level: 'high' },
  { id: 2, subject: '英语', topic: '定语从句', times: 2, level: 'medium' }
])

const recentWrongs = ref([
  { id: 1, subject: '数学', title: '高一数学期中考试', wrongCount: 3, date: '03-24' },
  { id: 2, subject: '物理', title: '高一物理月考', wrongCount: 2, date: '03-22' }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 600)
})

const goToWrongBook = () => {
  router.push('/student/wrong')
}

const goToTask = (taskId) => {
  router.push('/student/wrong')
}
</script>

<template>
  <div class="student-home">
    <div class="header">
      <div class="greeting">
        <h2 class="hello">你好，{{ userStore.userInfo.name || '同学' }} 👋</h2>
        <p class="date">今天也要加油呀！</p>
      </div>
    </div>

    <div class="stats-bar">
      <div class="stat-item">
        <span class="value">12</span>
        <span class="label">今日任务</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="value highlight">8</span>
        <span class="label">待完成</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="value success">23</span>
        <span class="label">本周错题</span>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3 class="section-title">今日任务</h3>
        <span class="more" @click="goToWrongBook">全部 ></span>
      </div>
      
      <div class="tasks-list">
        <div 
          v-for="task in todayTasks" 
          :key="task.id" 
          class="task-card"
          @click="goToTask(task.id)"
        >
          <div class="task-icon" :class="task.subject">
            <span>{{ task.subject.charAt(0) }}</span>
          </div>
          <div class="task-info">
            <h4 class="task-title">{{ task.title }}</h4>
            <p class="task-meta">{{ task.count }}道题 · {{ task.deadline }}</p>
          </div>
          <button class="start-btn">开始</button>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3 class="section-title">错题提醒</h3>
      </div>
      
      <div class="reminders-list">
        <div 
          v-for="reminder in wrongReminders" 
          :key="reminder.id" 
          class="reminder-card"
        >
          <div class="reminder-icon" :class="reminder.level">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <div class="reminder-info">
            <h4 class="reminder-title">{{ reminder.subject }} - {{ reminder.topic }}</h4>
            <p class="reminder-meta">已错{{ reminder.times }}次</p>
          </div>
          <span class="level-tag" :class="reminder.level">
            {{ reminder.level === 'high' ? '重点' : '复习' }}
          </span>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3 class="section-title">最近错题</h3>
        <span class="more" @click="goToWrongBook">查看全部 ></span>
      </div>
      
      <div class="recent-list">
        <div 
          v-for="wrong in recentWrongs" 
          :key="wrong.id" 
          class="recent-card"
          @click="router.push(`/student/wrong/${wrong.id}`)"
        >
          <div class="subject-tag" :class="wrong.subject">{{ wrong.subject }}</div>
          <div class="recent-info">
            <h4 class="recent-title">{{ wrong.title }}</h4>
            <p class="recent-meta">{{ wrong.wrongCount }}道错题 · {{ wrong.date }}</p>
          </div>
          <svg class="arrow" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.student-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  padding-bottom: 80px;
}

.header {
  padding: 20px 0 24px;
  
  .greeting {
    .hello {
      font-size: 26px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 6px;
    }
    
    .date {
      font-size: 15px;
      color: rgba(255,255,255,0.8);
    }
  }
}

.stats-bar {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.95);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  
  .stat-item {
    flex: 1;
    text-align: center;
    
    .value {
      display: block;
      font-size: 28px;
      font-weight: 700;
      color: #1f2937;
      
      &.highlight { color: #f59e0b; }
      &.success { color: #34d399; }
    }
    
    .label {
      font-size: 12px;
      color: #6b7280;
    }
  }
  
  .stat-divider {
    width: 1px;
    height: 40px;
    background: #e5e7eb;
  }
}

.section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }
  
  .more {
    font-size: 13px;
    color: rgba(255,255,255,0.7);
  }
}

.tasks-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
  
  .task-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    font-weight: 700;
    color: #fff;
    margin-right: 14px;
    
    &.数学 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.物理 { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
    &.英语 { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
  }
  
  .task-info {
    flex: 1;
    
    .task-title {
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .task-meta {
      font-size: 13px;
      color: #6b7280;
    }
  }
  
  .start-btn {
    padding: 8px 20px;
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

.reminders-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reminder-card {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.95);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  
  .reminder-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    
    &.high {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }
    
    &.medium {
      background: rgba(251, 191, 36, 0.1);
      color: #f59e0b;
    }
  }
  
  .reminder-info {
    flex: 1;
    
    .reminder-title {
      font-size: 14px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 2px;
    }
    
    .reminder-meta {
      font-size: 12px;
      color: #9ca3af;
    }
  }
  
  .level-tag {
    padding: 4px 10px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
    
    &.high {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }
    
    &.medium {
      background: rgba(251, 191, 36, 0.1);
      color: #f59e0b;
    }
  }
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
  
  .subject-tag {
    padding: 6px 12px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    margin-right: 12px;
    
    &.数学 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    &.物理 { background: linear-gradient(135deg, #34d399 0%, #10b981 100%); }
    &.英语 { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
  }
  
  .recent-info {
    flex: 1;
    
    .recent-title {
      font-size: 14px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 2px;
    }
    
    .recent-meta {
      font-size: 12px;
      color: #9ca3af;
    }
  }
  
  .arrow {
    opacity: 0.4;
  }
}
</style>
