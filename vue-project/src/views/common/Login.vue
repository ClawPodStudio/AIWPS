<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>AIWPS</h1>
        <p>初高中个性化辅导系统</p>
      </div>
      
      <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        
        <el-form-item prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择登录角色" size="large" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="机构/管理员" value="ORG" />
          </el-select>
        </el-form-item>
        
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const loginForm = reactive({ username: '', password: '', role: 'STUDENT' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择登录角色', trigger: 'change' }]
}

const roleRouteMap = { STUDENT: '/student', TEACHER: '/teacher', ORG: '/org' }

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password)
    localStorage.setItem('userRole', loginForm.role)
    ElMessage.success('登录成功')
    router.push(roleRouteMap[loginForm.role] || '/student')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 420px;
  max-width: 100%;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 36px;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 16px;
  color: #666;
}

.login-form {
  margin-top: 20px;
}

@media (max-width: 480px) {
  .login-box {
    padding: 24px 20px;
    border-radius: 8px;
  }
  .login-header h1 {
    font-size: 28px;
  }
  .login-header p {
    font-size: 14px;
  }
}
</style>
