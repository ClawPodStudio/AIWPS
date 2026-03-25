<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="login-container animate-fade-in">
      <div class="login-left">
        <div class="brand">
          <div class="brand-icon">📚</div>
          <h1 class="brand-name">AIWPS</h1>
        </div>
        <p class="brand-tagline">初高中个性化辅导SaaS系统</p>
        
        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">🎯</span>
            <span>AI智能出题</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📝</span>
            <span>错题本管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🧠</span>
            <span>艾宾浩斯记忆</span>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-card">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录到您的账户</p>
          
          <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <div class="input-group">
                <span class="input-icon">👤</span>
                <el-input 
                  v-model="form.username" 
                  placeholder="请输入用户名"
                  size="large"
                  :prefix-icon="User"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="password">
              <div class="input-group">
                <span class="input-icon">🔒</span>
                <el-input 
                  v-model="form.password" 
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>
            
            <el-form-item prop="role">
              <el-radio-group v-model="form.role" class="role-group">
                <el-radio-button label="STUDENT">学生</el-radio-button>
                <el-radio-button label="TEACHER">老师</el-radio-button>
                <el-radio-button label="ORG_ADMIN">机构</el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                :loading="loading" 
                class="login-btn"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="login-footer">
            <span class="demo-hint">演示账号: admin / admin123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: 'STUDENT'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const res = await login({
        username: form.username,
        password: form.password,
        role: form.role
      })
      
      if (res && res.token) {
        localStorage.setItem('token', res.token)
        localStorage.setItem('userInfo', JSON.stringify(res.user || {}))
        localStorage.setItem('role', form.role)
        
        ElMessage.success('登录成功')
        
        // 根据角色跳转
        if (form.role === 'STUDENT') {
          router.push('/student/dashboard')
        } else if (form.role === 'TEACHER') {
          router.push('/teacher/dashboard')
        } else {
          router.push('/org/dashboard')
        }
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1) 0%, rgba(129, 140, 248, 0.05) 100%);
  top: -200px;
  right: -100px;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.1) 0%, rgba(251, 146, 60, 0.05) 100%);
  bottom: -100px;
  left: -100px;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(52, 211, 153, 0.05) 100%);
  top: 50%;
  left: 30%;
}

.login-container {
  display: flex;
  align-items: stretch;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(79, 70, 229, 0.15);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.brand-icon {
  font-size: 48px;
}

.brand-name {
  font-family: 'Fredoka', sans-serif;
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}

.brand-tagline {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 48px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 16px;
  font-weight: 500;
}

.feature-icon {
  font-size: 24px;
}

.login-right {
  flex: 1;
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 360px;
}

.login-title {
  font-family: 'Fredoka', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: #1E1B4B;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 15px;
  color: #64748B;
  margin: 0 0 32px;
}

.input-group {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  z-index: 1;
  pointer-events: none;
}

.role-group {
  width: 100%;
  display: flex;
}

.role-group :deep(.el-radio-button) {
  flex: 1;
}

.role-group :deep(.el-radio-button__inner) {
  width: 100%;
  border-radius: 8px;
  border: 2px solid #E0E7FF;
  margin: 0;
}

.role-group :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px 0 0 8px;
}

.role-group :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 8px 8px 0;
}

.role-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #4F46E5;
  border-color: #4F46E5;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #4F46E5 0%, #6366F1 100%);
  border: none;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.4);
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.demo-hint {
  font-size: 13px;
  color: #94A3B8;
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    max-width: 100%;
    min-height: 100vh;
    border-radius: 0;
  }
  
  .login-left {
    padding: 32px;
  }
  
  .brand-icon {
    font-size: 36px;
  }
  
  .brand-name {
    font-size: 28px;
  }
  
  .features {
    display: none;
  }
  
  .login-right {
    padding: 32px;
  }
}
</style>
