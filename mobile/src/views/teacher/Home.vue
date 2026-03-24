<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)

// Mock data
const stats = ref({
  institutionCount: 3,
  classCount: 12,
  studentCount: 456,
  wrongCount: 89
})

const institutions = ref([
  { id: 1, name: '北京市第一中学', classes: [
    { id: 101, name: '高一(1)班', studentCount: 45 },
    { id: 102, name: '高一(2)班', studentCount: 42 }
  ]},
  { id: 2, name: '北京市第二中学', classes: [
    { id: 201, name: '高二(1)班', studentCount: 48 },
    { id: 202, name: '高二(2)班', studentCount: 46 }
  ]}
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 800)
})

const goToClass = (classId) => {
  router.push(`/teacher/students/${classId}`)
}
</script>

<template>
  <div class="teacher-home">
    <div class="header">
      <div class="user-info">
        <div class="avatar">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="8" r="4" fill="white"/>
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="info">
          <h2 class="name">{{ userStore.userInfo.name || '张老师' }}</h2>
          <p class="role">教师</p>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-value">{{ stats.institutionCount }}</p>
          <p class="stat-label">管理机构</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #34d399 0%, #10b981 100%);">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-value">{{ stats.classCount }}</p>
          <p class="stat-label">班级数量</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-value">{{ stats.studentCount }}</p>
          <p class="stat-label">学生总数</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-value">{{ stats.wrongCount }}</p>
          <p class="stat-label">待批改</p>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">我的机构</h3>
      
      <div v-if="loading" class="loading-skeleton">
        <div class="skeleton-card" v-for="i in 2" :key="i">
          <van-skeleton :row="2" />
        </div>
      </div>
      
      <div v-else class="institution-list">
        <div 
          v-for="institution in institutions" 
          :key="institution.id" 
          class="institution-card"
        >
          <div class="institution-header">
            <div class="institution-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </div>
            <span class="institution-name">{{ institution.name }}</span>
          </div>
          
          <div class="classes-list">
            <div 
              v-for="cls in institution.classes" 
              :key="cls.id" 
              class="class-item"
              @click="goToClass(cls.id)"
            >
              <div class="class-info">
                <span class="class-name">{{ cls.name }}</span>
                <span class="class-count">{{ cls.studentCount }}人</span>
              </div>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.teacher-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  padding-bottom: 80px;
}

.header {
  padding: 20px 0 30px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
  
  .avatar {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0.1) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba(255,255,255,0.3);
  }
  
  .info {
    .name {
      font-size: 22px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 4px;
    }
    
    .role {
      font-size: 14px;
      color: rgba(255,255,255,0.8);
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255,255,255,0.95);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  
  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .stat-content {
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #1f2937;
    }
    
    .stat-label {
      font-size: 12px;
      color: #6b7280;
    }
  }
}

.section {
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 16px;
    padding-left: 4px;
  }
}

.institution-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.institution-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.institution-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
  
  .institution-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, rgba(102,126,234,0.1) 0%, rgba(118,75,162,0.1) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .institution-name {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }
}

.classes-list {
  padding: 8px 0;
}

.class-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:active {
    background: #f9fafb;
  }
  
  .class-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .class-name {
      font-size: 15px;
      color: #1f2937;
    }
    
    .class-count {
      font-size: 13px;
      color: #9ca3af;
    }
  }
}

.loading-skeleton {
  .skeleton-card {
    background: #fff;
    border-radius: 16px;
    padding: 16px;
    margin-bottom: 12px;
  }
}
</style>
