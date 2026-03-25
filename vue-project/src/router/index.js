import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  // 根路径重定向到登录
  { path: '/', redirect: '/login' },
  
  // Login
  { path: '/login', name: 'Login', component: () => import('@/views/common/Login.vue') },
  
  // Student
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('@/views/student/StudentLayout.vue'),
    meta: { role: 'STUDENT' },
    children: [
      { path: '', redirect: '/student/dashboard' },
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('@/views/student/Dashboard.vue'), meta: { title: '学习首页' } },
      { path: 'practice', name: 'Practice', component: () => import('@/views/student/Practice.vue'), meta: { title: '每日练习' } },
      { path: 'wrong-questions', name: 'WrongQuestions', component: () => import('@/views/student/WrongQuestions.vue'), meta: { title: '错题本' } },
      { path: 'knowledge', name: 'Knowledge', component: () => import('@/views/student/Knowledge.vue'), meta: { title: '知识点学习' } },
      { path: 'review', name: 'Review', component: () => import('@/views/student/Review.vue'), meta: { title: '艾宾浩斯复习' } },
      { path: 'profile', name: 'StudentProfile', component: () => import('@/views/student/Profile.vue'), meta: { title: '个人中心' } }
    ]
  },
  
  // Teacher
  {
    path: '/teacher',
    name: 'TeacherLayout',
    component: () => import('@/views/teacher/TeacherLayout.vue'),
    meta: { role: 'TEACHER' },
    children: [
      { path: '', redirect: '/teacher/dashboard' },
      { path: 'dashboard', name: 'TeacherDashboard', component: () => import('@/views/teacher/Dashboard.vue'), meta: { title: '教学首页' } },
      { path: 'questions', name: 'QuestionManage', component: () => import('@/views/teacher/QuestionManage.vue'), meta: { title: '题目管理' } },
      { path: 'papers', name: 'PaperManage', component: () => import('@/views/teacher/PaperManage.vue'), meta: { title: '试卷管理' } },
      { path: 'students', name: 'StudentManage', component: () => import('@/views/teacher/StudentManage.vue'), meta: { title: '学生管理' } },
      { path: 'wrong-stats', name: 'WrongStats', component: () => import('@/views/teacher/WrongStats.vue'), meta: { title: '错题统计' } },
      { path: 'ai-generate', name: 'AIGenerate', component: () => import('@/views/teacher/AIGenerate.vue'), meta: { title: 'AI智能出题' } },
      { path: 'ai-paper', name: 'AIPaperGenerate', component: () => import('@/views/teacher/AIPaperGenerate.vue'), meta: { title: 'AI生成试卷' } },
      { path: 'paper/:id', name: 'PaperDetail', component: () => import('@/views/teacher/PaperDetail.vue'), meta: { title: '试卷详情' } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/teacher/Statistics.vue'), meta: { title: '数据统计' } },
      { path: 'profile', name: 'TeacherProfile', component: () => import('@/views/teacher/Profile.vue'), meta: { title: '个人中心' } },
      { path: 'qr-scan', name: 'QRScan', component: () => import('@/views/teacher/QRScan.vue'), meta: { title: '二维码验证' } }
    ]
  },
  
  // Organization
  {
    path: '/org',
    name: 'OrgLayout',
    component: () => import('@/views/org/OrgLayout.vue'),
    meta: { role: 'ORG' },
    children: [
      { path: '', redirect: '/org/dashboard' },
      { path: 'dashboard', name: 'OrgDashboard', component: () => import('@/views/org/Dashboard.vue'), meta: { title: '机构首页' } },
      { path: 'teachers', name: 'TeacherList', component: () => import('@/views/org/TeacherList.vue'), meta: { title: '教师管理' } },
      { path: 'classes', name: 'ClassManage', component: () => import('@/views/org/ClassManage.vue'), meta: { title: '班级管理' } },
      { path: 'statistics', name: 'OrgStatistics', component: () => import('@/views/org/Statistics.vue'), meta: { title: '数据看板' } },
      { path: 'settings', name: 'OrgSettings', component: () => import('@/views/org/Settings.vue'), meta: { title: '机构设置' } }
    ]
  },
  
  // 404
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/common/NotFound.vue') }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')
  
  if (to.path === '/login') {
    next()
    return
  }
  
  if (!token) {
    next('/login')
    return
  }
  
  // 检查角色权限（ORG_ADMIN 等同于 ORG）
  if (to.meta.role) {
    const roleMap = { 'ORG': ['ORG', 'ORG_ADMIN'], 'TEACHER': ['TEACHER'], 'STUDENT': ['STUDENT'] }
    const allowedRoles = roleMap[to.meta.role] || []
    if (!allowedRoles.includes(userRole)) {
      next('/login')
      return
    }
  }
  
  next()
})

export default router
