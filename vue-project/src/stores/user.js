import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const userRole = ref(localStorage.getItem('userRole') || '')
  
  const isLoggedIn = computed(() => !!token.value)
  const isStudent = computed(() => userRole.value === 'STUDENT')
  const isTeacher = computed(() => userRole.value === 'TEACHER')
  const isOrg = computed(() => userRole.value === 'ORG' || userRole.value === 'ADMIN')
  
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  function setUserInfo(info) {
    userInfo.value = info
    userRole.value = info.role
    localStorage.setItem('userInfo', JSON.stringify(info))
    localStorage.setItem('userRole', info.role)
  }
  
  async function login(username, password) {
    const data = await loginApi({ username, password })
    setToken(data.token)
    setUserInfo(data.user)
    return data
  }
  
  async function logout() {
    try { await logoutApi() } catch (e) {}
    token.value = ''
    userInfo.value = {}
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
  }
  
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const data = await getUserInfoApi()
      setUserInfo(data)
    } catch (e) { logout() }
  }
  
  return { token, userInfo, userRole, isLoggedIn, isStudent, isTeacher, isOrg, setToken, setUserInfo, login, logout, fetchUserInfo }
})
