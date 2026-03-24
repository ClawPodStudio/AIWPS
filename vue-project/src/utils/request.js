import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/aiwps/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果返回的是裸数组（如 /subject/list），直接返回
    if (Array.isArray(res)) {
      return res
    }
    // 标准包装格式 {code, msg, data}
    if (res && (res.code === 200 || res.code === 0)) {
      return res.data !== undefined ? res.data : res
    }
    // 其他情况（可能是分页结构或裸对象）也直接返回
    return res
  },
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        window.location.href = '/aiwps/login'
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
