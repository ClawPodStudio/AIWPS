<template>
  <div class="teacher-layout">
    <!-- Mobile Header -->
    <div class="mobile-header">
      <button class="menu-btn" @click="sidebarOpen = !sidebarOpen">
        <span v-if="!sidebarOpen">☰</span>
        <span v-else>✕</span>
      </button>
      <div class="mobile-brand">👨‍🏫 AIWPS</div>
      <div class="mobile-user">
        <el-dropdown @command="handleCommand">
          <span class="user-avatar">👤</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- Sidebar Overlay -->
    <div v-if="sidebarOpen" class="sidebar-overlay" @click="sidebarOpen = false"></div>
    
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-header">
        <div class="brand">
          <span class="brand-icon">👨‍🏫</span>
          <span class="brand-text">AIWPS</span>
        </div>
        <div class="brand-subtitle">教师端</div>
      </div>
      
      <nav class="sidebar-nav">
        <router-link 
          v-for="item in navItems" 
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="sidebarOpen = false"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.label }}</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar-lg">👤</span>
          <div class="user-details">
            <div class="user-name">{{ userName }}</div>
            <div class="user-role">教师</div>
          </div>
        </div>
      </div>
    </aside>
    
    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

const navItems = [
  { path: '/teacher/dashboard', label: '教学首页', icon: '🏠' },
  { path: '/teacher/questions', label: '题目管理', icon: '📝' },
  { path: '/teacher/papers', label: '试卷管理', icon: '📄' },
  { path: '/teacher/qr-scan', label: '二维码验证', icon: '📱' },
  { path: '/teacher/students', label: '学生管理', icon: '👥' },
  { path: '/teacher/wrong-stats', label: '错题统计', icon: '❌' },
  { path: '/teacher/ai-generate', label: 'AI智能出题', icon: '🤖' },
  { path: '/teacher/statistics', label: '数据统计', icon: '📊' },
  { path: '/teacher/profile', label: '个人中心', icon: '⚙️' }
]

const userName = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.name || '教师'
  } catch {
    return '教师'
  }
})

const isActive = (path) => route.path.startsWith(path)

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/teacher/profile')
  }
}
</script>

<style scoped>
.teacher-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.mobile-header {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1001;
  align-items: center;
  justify-content: space-between;
}

.menu-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
  border-radius: 8px;
}

.menu-btn:hover { background: #f5f5f5; }

.mobile-brand {
  font-family: var(--font-heading);
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
}

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 260px;
  height: 100vh;
  background: linear-gradient(180deg, #4F46E5 0%, #6366F1 100%);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  transition: transform 0.3s ease;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon { font-size: 28px; }
.brand-text {
  font-family: var(--font-heading);
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.brand-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  color: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-weight: 600;
}

.nav-icon { font-size: 20px; }
.nav-text { font-size: 15px; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.user-avatar-lg { font-size: 32px; }

.user-details { flex: 1; }

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.user-role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.main-content {
  margin-left: 260px;
  min-height: 100vh;
  padding: 32px;
}

@media (max-width: 768px) {
  .mobile-header { display: flex; }
  .sidebar-overlay { display: block; }
  .sidebar { transform: translateX(-100%); }
  .sidebar.open { transform: translateX(0); }
  .main-content { margin-left: 0; padding: 80px 16px 32px; }
}
</style>
