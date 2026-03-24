<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Tab, Tabs, Empty } from 'vant'

const router = useRouter()
const activeTab = ref(0)
const loading = ref(true)

// Mock data
const byClass = ref([
  { 
    classId: 101, 
    className: '高一(1)班', 
    wrongCount: 23,
    students: [
      { id: 1, name: '王小明', wrongCount: 8, lastTime: '2024-03-24' },
      { id: 2, name: '李小红', wrongCount: 6, lastTime: '2024-03-23' },
      { id: 3, name: '张小刚', wrongCount: 5, lastTime: '2024-03-22' }
    ]
  },
  { 
    classId: 102, 
    className: '高一(2)班', 
    wrongCount: 18,
    students: [] 
  }
])

const byStudent = ref([
  { id: 1, name: '王小明', class: '高一(1)班', subject: '数学', wrongCount: 8, avatar: '' },
  { id: 2, name: '李小红', class: '高一(1)班', subject: '物理', wrongCount: 6, avatar: '' },
  { id: 3, name: '张小刚', class: '高一(1)班', subject: '英语', wrongCount: 5, avatar: '' }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 600)
})

const goToStudentWrong = (studentId) => {
  router.push(`/teacher/student-wrong/${studentId}`)
}

const goToClass = (classId) => {
  router.push(`/teacher/students/${classId}`)
}
</script>

<template>
  <div class="wrong-book-page">
    <div class="header">
      <h2 class="title">错题管理</h2>
      <p class="desc">查看和管理学生的错题情况</p>
    </div>

    <van-tabs v-model:active="activeTab" sticky color="#667eea">
      <van-tab title="按班级">
        <div class="tab-content">
          <div 
            v-for="cls in byClass" 
            :key="cls.classId" 
            class="class-card"
            @click="goToClass(cls.classId)"
          >
            <div class="class-header">
              <div class="class-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
              <div class="class-info">
                <h3 class="class-name">{{ cls.className }}</h3>
                <p class="class-stats">{{ cls.wrongCount }}道错题</p>
              </div>
              <svg class="arrow" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </div>
            
            <div v-if="cls.students.length" class="student-preview">
              <div 
                v-for="student in cls.students.slice(0, 3)" 
                :key="student.id" 
                class="student-chip"
                @click.stop="goToStudentWrong(student.id)"
              >
                <span class="student-name">{{ student.name }}</span>
                <span class="wrong-count">{{ student.wrongCount }}题</span>
              </div>
              <span v-if="cls.students.length > 3" class="more">
                +{{ cls.students.length - 3 }}人
              </span>
            </div>
          </div>
        </div>
      </van-tab>
      
      <van-tab title="按学生">
        <div class="tab-content">
          <div 
            v-for="student in byStudent" 
            :key="student.id" 
            class="student-card"
            @click="goToStudentWrong(student.id)"
          >
            <div class="student-avatar">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
                <circle cx="12" cy="8" r="4"/>
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              </svg>
            </div>
            <div class="student-info">
              <h3 class="name">{{ student.name }}</h3>
              <p class="detail">{{ student.class }} · {{ student.subject }}</p>
            </div>
            <div class="wrong-badge">
              <span class="count">{{ student.wrongCount }}</span>
              <span class="label">错题</span>
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

.class-card {
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

.class-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  
  .class-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(102,126,234,0.1) 0%, rgba(118,75,162,0.1) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .class-info {
    flex: 1;
    
    .class-name {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }
    
    .class-stats {
      font-size: 13px;
      color: #ef4444;
      margin-top: 2px;
    }
  }
  
  .arrow {
    opacity: 0.5;
  }
}

.student-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  
  .student-chip {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: #f9fafb;
    border-radius: 20px;
    font-size: 13px;
    
    .student-name {
      color: #1f2937;
    }
    
    .wrong-count {
      color: #ef4444;
      font-weight: 500;
    }
    
    &:active {
      background: #f3f4f6;
    }
  }
  
  .more {
    padding: 6px 12px;
    font-size: 13px;
    color: #9ca3af;
  }
}

.student-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
  
  .student-avatar {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    background: linear-gradient(135deg, rgba(102,126,234,0.15) 0%, rgba(118,75,162,0.15) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 14px;
  }
  
  .student-info {
    flex: 1;
    
    .name {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .detail {
      font-size: 13px;
      color: #6b7280;
    }
  }
  
  .wrong-badge {
    text-align: center;
    
    .count {
      display: block;
      font-size: 22px;
      font-weight: 700;
      color: #ef4444;
    }
    
    .label {
      font-size: 11px;
      color: #9ca3af;
    }
  }
}
</style>
