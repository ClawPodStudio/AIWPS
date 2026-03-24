<script setup>
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { id: 1, icon: 'user', title: '个人信息', path: '' },
  { id: 2, icon: 'bell', title: '通知设置', path: '' },
  { id: 3, icon: 'shield', title: '账号安全', path: '' },
  { id: 4, icon: 'question', title: '帮助反馈', path: '' },
  { id: 5, icon: 'info', title: '关于我们', path: '' }
]

const handleLogout = () => {
  showDialog({
    title: '确认退出',
    message: '确定要退出登录吗？',
    confirmButtonColor: '#667eea'
  }).then(() => {
    userStore.logout()
    showSuccessToast('已退出登录')
    router.replace('/login')
  }).catch(() => {})
}
</script>

<template>
  <div class="profile-page">
    <div class="header">
      <div class="user-card">
        <div class="avatar">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="8" r="4" fill="white"/>
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="info">
          <h2 class="name">{{ userStore.userInfo.name || '张老师' }}</h2>
          <p class="phone">{{ userStore.userInfo.phone || '138****8000' }}</p>
        </div>
      </div>
    </div>

    <div class="menu-section">
      <div 
        v-for="item in menuItems" 
        :key="item.id" 
        class="menu-item"
      >
        <div class="menu-left">
          <div class="menu-icon">
            <svg v-if="item.icon === 'user'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <svg v-else-if="item.icon === 'bell'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <svg v-else-if="item.icon === 'shield'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <svg v-else-if="item.icon === 'question'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="16" x2="12" y2="12"/>
              <line x1="12" y1="8" x2="12.01" y2="8"/>
            </svg>
          </div>
          <span class="menu-title">{{ item.title }}</span>
        </div>
        <svg class="arrow" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </div>
    </div>

    <div class="logout-section">
      <button class="logout-btn" @click="handleLogout">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
          <polyline points="16 17 21 12 16 7"/>
          <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        退出登录
      </button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 80px;
}

.header {
  padding: 30px 16px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(10px);
  
  .avatar {
    width: 72px;
    height: 72px;
    border-radius: 20px;
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
    
    .phone {
      font-size: 14px;
      color: rgba(255,255,255,0.8);
    }
  }
}

.menu-section {
  background: #fff;
  margin: 0 16px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:active {
    background: #f9fafb;
  }
  
  .menu-left {
    display: flex;
    align-items: center;
    gap: 14px;
    
    .menu-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: linear-gradient(135deg, rgba(102,126,234,0.1) 0%, rgba(118,75,162,0.1) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    .menu-title {
      font-size: 15px;
      color: #1f2937;
    }
  }
  
  .arrow {
    opacity: 0.4;
  }
}

.logout-section {
  padding: 24px 16px;
  
  .logout-btn {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 16px;
    background: rgba(255,255,255,0.15);
    border: 1px solid rgba(255,255,255,0.3);
    border-radius: 16px;
    color: #fff;
    font-size: 16px;
    cursor: pointer;
    
    &:active {
      background: rgba(255,255,255,0.25);
    }
  }
}
</style>
