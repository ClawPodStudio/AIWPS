<template>
  <el-container class="teacher-layout">
    <!-- 移动端侧边栏抽屉 -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="220px" :with-header="false" class="mobile-drawer">
      <div class="logo"><h2>AIWPS</h2><p>教师端</p></div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu" @select="drawerVisible = false">
        <el-menu-item index="/teacher/dashboard"><el-icon><HomeFilled /></el-icon><span>教学首页</span></el-menu-item>
        <el-menu-item index="/teacher/questions"><el-icon><Document /></el-icon><span>题目管理</span></el-menu-item>
        <el-menu-item index="/teacher/papers"><el-icon><Tickets /></el-icon><span>试卷管理</span></el-menu-item>
        <el-menu-item index="/teacher/qr-scan"><el-icon><Link /></el-icon><span>二维码验证</span></el-menu-item>
        <el-menu-item index="/teacher/students"><el-icon><User /></el-icon><span>学生管理</span></el-menu-item>
        <el-menu-item index="/teacher/wrong-stats"><el-icon><Warning /></el-icon><span>错题统计</span></el-menu-item>
        <el-menu-item index="/teacher/ai-generate"><el-icon><MagicStick /></el-icon><span>AI智能出题</span></el-menu-item>
        <el-menu-item index="/teacher/statistics"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/teacher/profile"><el-icon><Setting /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-drawer>

    <!-- 桌面端侧边栏 -->
    <el-aside width="220px" class="desktop-sidebar">
      <div class="logo"><h2>AIWPS</h2><p>教师端</p></div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item index="/teacher/dashboard"><el-icon><HomeFilled /></el-icon><span>教学首页</span></el-menu-item>
        <el-menu-item index="/teacher/questions"><el-icon><Document /></el-icon><span>题目管理</span></el-menu-item>
        <el-menu-item index="/teacher/papers"><el-icon><Tickets /></el-icon><span>试卷管理</span></el-menu-item>
        <el-menu-item index="/teacher/qr-scan"><el-icon><Link /></el-icon><span>二维码验证</span></el-menu-item>
        <el-menu-item index="/teacher/students"><el-icon><User /></el-icon><span>学生管理</span></el-menu-item>
        <el-menu-item index="/teacher/wrong-stats"><el-icon><Warning /></el-icon><span>错题统计</span></el-menu-item>
        <el-menu-item index="/teacher/ai-generate"><el-icon><MagicStick /></el-icon><span>AI智能出题</span></el-menu-item>
        <el-menu-item index="/teacher/statistics"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/teacher/profile"><el-icon><Setting /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="hamburger" @click="drawerVisible = true"><Fold /></el-icon>
          <h3>{{ pageTitle }}</h3>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span class="user-name">{{ userStore.userInfo.name || '教师' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Document, Tickets, User, Warning, MagicStick, DataAnalysis, Setting, Fold, Link } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const drawerVisible = ref(false)

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '教学首页')

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/teacher/profile')
  }
}
</script>

<style scoped>
.teacher-layout { min-height: 100vh; }
.hamburger { display: none; font-size: 22px; cursor: pointer; margin-right: 12px; color: #333; }
.desktop-sidebar { background: #304156; color: white; }
@media (max-width: 768px) {
  .desktop-sidebar { display: none; }
  .hamburger { display: block; }
  .user-name { display: none; }
  .el-main { padding: 12px; }
  .el-header { padding: 0 12px !important; }
}
.el-aside { background: #304156; color: white; }
.logo { height: 60px; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #263445; }
.logo h2 { margin: 0; font-size: 24px; color: #409eff; }
.logo p { margin: 0; font-size: 12px; color: #999; }
.sidebar-menu { border-right: none; background: #304156; }
.sidebar-menu .el-menu-item { color: #bfcbd9; }
.sidebar-menu .el-menu-item:hover, .sidebar-menu .el-menu-item.is-active { background: #263445 !important; color: #409eff !important; }
.el-header { background: white; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.header-left { display: flex; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; color: #333; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 8px 12px; border-radius: 4px; }
.user-info:hover { background: #f5f7fa; }
.el-main { padding: 20px; background: #f5f7fa; }
</style>
