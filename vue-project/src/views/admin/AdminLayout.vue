<template>
  <div class="admin-layout">
    <!-- Mobile Header -->
    <div class="mobile-header">
      <button class="menu-btn" @click="sidebarOpen = !sidebarOpen">
        <span v-if="!sidebarOpen">☰</span>
        <span v-else>✕</span>
      </button>
      <div class="mobile-brand">🏛️ AIWPS</div>
      <div class="mobile-user">
        <el-dropdown @command="handleCommand">
          <span class="user-avatar">👤</span>
          <template #dropdown>
            <el-dropdown-menu>
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
          <span class="brand-icon">🏛️</span>
          <span class="brand-text">AIWPS</span>
        </div>
        <div class="brand-subtitle">超级管理员</div>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-group">
          <div class="nav-group-title">系统管理</div>
          <router-link
            v-for="item in systemNavItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="sidebarOpen = false"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-text">{{ item.label }}</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">运营分析</div>
          <router-link
            v-for="item in opsNavItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="sidebarOpen = false"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-text">{{ item.label }}</span>
          </router-link>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar-lg">👤</span>
          <div class="user-details">
            <div class="user-name">{{ adminName }}</div>
            <div class="user-role">超级管理员</div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-content">
      <!-- Top Bar with Tenant Switcher -->
      <div class="top-bar">
        <div class="top-bar-left">
          <h2 class="page-title">{{ currentPageTitle }}</h2>
        </div>
        <div class="top-bar-right">
          <!-- Tenant Switcher -->
          <el-dropdown @command="handleTenantSwitch" trigger="click">
            <el-button class="tenant-switcher">
              <span class="tenant-icon">🏢</span>
              <span class="tenant-name">{{ currentTenantName }}</span>
              <span class="arrow">▼</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="t in tenantList" :key="t.id" :command="t.id" :disabled="t.id === currentTenantId">
                  <span :class="{ 'tenant-active': t.id === currentTenantId }">{{ t.name }}</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTenantList } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

const systemNavItems = [
  { path: '/admin/dashboard', label: '数据看板', icon: '🏠' },
  { path: '/admin/tenants', label: '租户管理', icon: '🏢' }
]

const opsNavItems = [
  { path: '/admin/statistics', label: '全局统计', icon: '📊' }
]

const navItems = [...systemNavItems, ...opsNavItems]

const currentPageTitle = computed(() => {
  const item = navItems.find(i => route.path.startsWith(i.path))
  return item ? item.label : ''
})

const adminName = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.name || userInfo.username || '管理员'
  } catch {
    return '管理员'
  }
})

const tenantList = ref([])
const currentTenantId = computed(() => localStorage.getItem('currentTenantId') || '')
const currentTenantName = computed(() => {
  const t = tenantList.value.find(t => String(t.id) === String(currentTenantId.value))
  return t ? t.name : '选择租户'
})

const isActive = (path) => route.path.startsWith(path)

const loadTenants = async () => {
  try {
    const data = await getTenantList({ page: 1, size: 100 })
    tenantList.value = data?.list || data || []
    // 如果没有 currentTenantId，设置第一个为默认
    if (!currentTenantId.value && tenantList.value.length > 0) {
      localStorage.setItem('currentTenantId', tenantList.value[0].id)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleTenantSwitch = (tenantId) => {
  localStorage.setItem('currentTenantId', tenantId)
  ElMessage.success('已切换租户')
  router.go(0)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}

onMounted(() => {
  loadTenants()
})
</script>

<style scoped>
.admin-layout {
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
  color: #4F46E5;
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
  background: linear-gradient(180deg, #1E1B4B 0%, #312E81 100%);
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
  gap: 16px;
  overflow-y: auto;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-group-title {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: rgba(255, 255, 255, 0.4);
  padding: 8px 16px 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
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

.nav-icon { font-size: 18px; }
.nav-text { font-size: 14px; }

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
  padding: 0;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  padding: 16px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1E1B4B;
  margin: 0;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tenant-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #F5F3FF;
  border: 1px solid #E0E7FF;
  border-radius: 8px;
  color: #4F46E5;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.tenant-switcher:hover {
  background: #EEF2FF;
  border-color: #4F46E5;
}

.tenant-icon { font-size: 16px; }
.tenant-name { font-size: 14px; }
.arrow { font-size: 10px; opacity: 0.7; }

.tenant-active {
  color: #4F46E5;
  font-weight: 600;
}

@media (max-width: 768px) {
  .mobile-header { display: flex; }
  .sidebar-overlay { display: block; }
  .sidebar { transform: translateX(-100%); }
  .sidebar.open { transform: translateX(0); }
  .main-content { margin-left: 0; padding: 80px 16px 32px; }
  .top-bar { padding: 12px 16px; }
}
</style>
