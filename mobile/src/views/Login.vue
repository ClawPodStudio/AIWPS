<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast, showFailToast } from 'vant'
import { useUserStore } from '@/stores/user'
import request from '@/api/index'

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('teacher') // teacher | student
const loading = ref(false)
const form = ref({
  phone: '',
  password: '',
  username: '',
  captcha: ''
})

const isPhoneLogin = computed(() => loginType.value === 'phone')

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    showFailToast('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const loginData = {
      username: form.value.username,
      password: form.value.password
    }
    const res = await request.post('/user/login', loginData)
    const { token, user } = res
    userStore.setToken(token)
    userStore.setUserInfo(user)
    userStore.setUserType(loginType.value)
    showSuccessToast('登录成功')
    if (loginType.value === 'teacher') {
      router.replace('/teacher/home')
    } else {
      router.replace('/student/home')
    }
  } catch (e) {
    console.error('[Login] error:', e)
    showFailToast(e.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo">
        <svg width="80" height="80" viewBox="0 0 80 80" fill="none">
          <circle cx="40" cy="40" r="36" fill="url(#logoGrad)" />
          <path d="M25 40L35 50L55 30" stroke="white" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
          <defs>
            <linearGradient id="logoGrad" x1="0" y1="0" x2="80" y2="80">
              <stop offset="0%" stop-color="#667eea"/>
              <stop offset="100%" stop-color="#764ba2"/>
            </linearGradient>
          </defs>
        </svg>
      </div>
      <h1 class="title">AIWPS 智能辅导</h1>
      <p class="subtitle">个性化学习，让进步看得见</p>
    </div>

    <div class="login-form">
      <div class="role-switch">
        <div 
          class="role-tab" 
          :class="{ active: loginType === 'teacher' }"
          @click="loginType = 'teacher'"
        >
          教师端
        </div>
        <div 
          class="role-tab"
          :class="{ active: loginType === 'student' }"
          @click="loginType = 'student'"
        >
          学生端
        </div>
      </div>

      <div class="form-items">
        <div class="form-item">
          <van-field
            v-model="form.username"
            placeholder="请输入账号"
            :border="false"
            clearable
          >
            <template #left-icon>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
              </svg>
            </template>
          </van-field>
        </div>

        <div class="form-item">
          <van-field
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :border="false"
          >
            <template #left-icon>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#6b7280" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </template>
          </van-field>
        </div>
      </div>

      <button 
        class="login-btn" 
        :class="{ loading: loading }"
        @click="handleLogin"
        :disabled="loading"
      >
        <span v-if="!loading">登 录</span>
        <span v-else>登录中...</span>
      </button>

      <div class="extra-actions">
        <span class="link">忘记密码？</span>
        <span class="divider">|</span>
        <span class="link">快速注册</span>
      </div>
    </div>

    <div class="login-footer">
      <p>登录即表示同意</p>
      <p>
        <span class="link">《用户协议》</span> 和 <span class="link">《隐私政策》</span>
      </p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 24px 40px;
  display: flex;
  flex-direction: column;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
  
  .logo {
    margin-bottom: 20px;
  }
  
  .title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

.login-form {
  background: #fff;
  border-radius: 20px;
  padding: 32px 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.role-switch {
  display: flex;
  background: #f3f4f6;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 24px;
  
  .role-tab {
    flex: 1;
    text-align: center;
    padding: 10px;
    border-radius: 10px;
    font-size: 15px;
    font-weight: 500;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
    }
  }
}

.form-items {
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 16px;
  
  :deep(.van-field) {
    background: #f9fafb;
    border-radius: 12px;
    padding: 12px 16px;
    
    .van-field__left-icon {
      margin-right: 12px;
    }
  }
}

.login-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 25px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
  
  &.loading {
    opacity: 0.7;
  }
}

.extra-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  font-size: 14px;
  
  .divider {
    margin: 0 12px;
    color: #d1d5db;
  }
  
  .link {
    color: #667eea;
  }
}

.login-footer {
  margin-top: auto;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  padding-top: 30px;
  
  .link {
    color: rgba(255, 255, 255, 0.9);
  }
}
</style>
