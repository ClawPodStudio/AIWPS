import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  // Teacher routes
  {
    path: '/teacher',
    redirect: '/teacher/home'
  },
  {
    path: '/teacher/home',
    name: 'TeacherHome',
    component: () => import('@/views/teacher/Home.vue')
  },
  {
    path: '/teacher/scan',
    name: 'TeacherScan',
    component: () => import('@/views/teacher/Scan.vue')
  },
  {
    path: '/teacher/wrong',
    name: 'TeacherWrong',
    component: () => import('@/views/teacher/WrongBook.vue')
  },
  {
    path: '/teacher/students/:classId',
    name: 'ClassStudents',
    component: () => import('@/views/teacher/ClassStudents.vue')
  },
  {
    path: '/teacher/student-wrong/:studentId',
    name: 'StudentWrong',
    component: () => import('@/views/teacher/StudentWrong.vue')
  },
  {
    path: '/teacher/paper/:paperId',
    name: 'PaperDetail',
    component: () => import('@/views/teacher/PaperDetail.vue')
  },
  {
    path: '/teacher/evaluate/:studentPaperId',
    name: 'Evaluate',
    component: () => import('@/views/teacher/Evaluate.vue')
  },
  {
    path: '/teacher/profile',
    name: 'TeacherProfile',
    component: () => import('@/views/teacher/Profile.vue')
  },
  // Student routes
  {
    path: '/student',
    redirect: '/student/home'
  },
  {
    path: '/student/home',
    name: 'StudentHome',
    component: () => import('@/views/student/Home.vue')
  },
  {
    path: '/student/wrong',
    name: 'StudentWrongBook',
    component: () => import('@/views/student/WrongBook.vue')
  },
  {
    path: '/student/knowledge/:knowledgeId',
    name: 'KnowledgeDetail',
    component: () => import('@/views/student/KnowledgeDetail.vue')
  },
  {
    path: '/student/wrong/:wrongId',
    name: 'StudentWrongDetail',
    component: () => import('@/views/student/WrongDetail.vue')
  },
  {
    path: '/student/profile',
    name: 'StudentProfile',
    component: () => import('@/views/student/Profile.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
