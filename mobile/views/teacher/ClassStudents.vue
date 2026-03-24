<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Empty } from 'vant'

const route = useRoute()
const router = useRouter()
const classId = route.params.classId
const loading = ref(true)

const classInfo = ref({
  id: classId,
  name: '高一(1)班',
  studentCount: 45
})

const students = ref([
  { id: 1, name: '王小明', wrongCount: 8, avatar: '' },
  { id: 2, name: '李小红', wrongCount: 6, avatar: '' },
  { id: 3, name: '张小刚', wrongCount: 5, avatar: '' },
  { id: 4, name: '陈小丽', wrongCount: 4, avatar: '' },
  { id: 5, name: '赵小强', wrongCount: 3, avatar: '' }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})

const goToStudentWrong = (studentId) => {
  router.push(`/teacher/student-wrong/${studentId}`)
}
</script>

<template>
  <div class="class-students-page">
    <div class="header">
      <div class="back" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
      <div class="header-content">
        <h2 class="title">{{ classInfo.name }}</h2>
        <p class="desc">共{{ classInfo.studentCount }}名学生</p>
      </div>
    </div>

    <div class="student-list">
      <div 
        v-for="student in students" 
        :key="student.id" 
        class="student-card"
        @click="goToStudentWrong(student.id)"
      >
        <div class="student-avatar">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
            <circle cx="12" cy="8" r="4"/>
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          </svg>
        </div>
        <div class="student-info">
          <h3 class="name">{{ student.name }}</h3>
          <p class="wrong-info" v-if="student.wrongCount > 0">
            <span class="count">{{ student.wrongCount }}</span>道错题待批改
          </p>
          <p class="wrong-info done" v-else>暂无错题</p>
        </div>
        <svg class="arrow" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.class-students-page {
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

.student-list {
  padding: 12px 16px;
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
    width: 48px;
    height: 48px;
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
    }
    
    .wrong-info {
      font-size: 13px;
      color: #ef4444;
      margin-top: 4px;
      
      .count {
        font-weight: 600;
      }
      
      &.done {
        color: #34d399;
      }
    }
  }
  
  .arrow {
    opacity: 0.5;
  }
}
</style>
