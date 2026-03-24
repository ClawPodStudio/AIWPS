<template>
  <el-container class="student-layout">
    <el-aside width="220px">
      <div class="logo"><h2>AIWPS</h2><p>学生端</p></div>
      <el-menu :default-active="activeMenu" router class="sidebar-menu">
        <el-menu-item index="/student/dashboard"><el-icon><HomeFilled /></el-icon><span>学习首页</span></el-menu-item>
        <el-menu-item index="/student/practice"><el-icon><Edit /></el-icon><span>每日练习</span></el-menu-item>
        <el-menu-item index="/student/wrong-questions"><el-icon><Warning /></el-icon><span>错题本</span></el-menu-item>
        <el-menu-item index="/student/knowledge"><el-icon><Reading /></el-icon><span>知识点</span></el-menu-item>
        <el-menu-item index="/student/review"><el-icon><Timer /></el-icon><span>艾宾浩斯复习</span></el-menu-item>
        <el-menu-item index="/student/profile"><el-icon><User /></el-icon><span>个人中心</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left"><h3>{{ pageTitle }}</h3></div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info"><el-icon><User /></el-icon><span>{{ userStore.userInfo.name||'学生' }}</span></span>
            <template #dropdown>
              <el-dropdown-menu><el-dropdown-item command="profile">个人中心</el-dropdown-item><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Edit, Warning, Reading, Timer, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '学习首页')
const handleCommand = (command) => { if(command==='logout'){userStore.logout();router.push('/login')}else if(command==='profile'){router.push('/student/profile')} }
</script>

<style scoped>
.student-layout{min-height:100vh}.el-aside{background:#304156;color:#fff}.logo{height:60px;display:flex;flex-direction:column;align-items:center;justify-content:center;background:#263445}.logo h2{margin:0;font-size:24px;color:#409eff}.logo p{margin:0;font-size:12px;color:#999}.sidebar-menu{border-right:none;background:#304156}.sidebar-menu .el-menu-item{color:#bfcbd9}.sidebar-menu .el-menu-item:hover,.sidebar-menu .el-menu-item.is-active{background:#263445!important;color:#409eff!important}.el-header{background:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 20px;box-shadow:0 1px 4px rgba(0,0,0,.08)}.header-left h3{margin:0;font-size:18px;color:#333}.header-right{display:flex;align-items:center}.user-info{display:flex;align-items:center;gap:8px;cursor:pointer;padding:8px 12px;border-radius:4px}.user-info:hover{background:#f5f7fa}.el-main{padding:20px;background:#f5f7fa}
</style>
