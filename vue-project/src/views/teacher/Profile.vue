<template>
  <div class="profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="100">{{ userStore.userInfo.name?.charAt(0)||'教' }}</el-avatar>
            <h3>{{ userStore.userInfo.name||'教师' }}</h3>
            <p>工号：{{ userStore.userInfo.username }}</p>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item"><span class="label">所属机构：</span><span>{{ userStore.userInfo.orgName||'-' }}</span></div>
            <div class="info-item"><span class="label">授课学科：</span><span>{{ userStore.userInfo.subjectNames||'-' }}</span></div>
            <div class="info-item"><span class="label">手机号：</span><span>{{ userStore.userInfo.mobile||'-' }}</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form :model="profileForm" label-width="100px" style="max-width:400px">
                <el-form-item label="姓名"><el-input v-model="profileForm.name" /></el-form-item>
                <el-form-item label="手机号"><el-input v-model="profileForm.mobile" /></el-form-item>
                <el-form-item><el-button type="primary" @click="saveProfile">保存</el-button></el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" label-width="100px" style="max-width:400px">
                <el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
                <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
                <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
                <el-form-item><el-button type="primary" @click="changePassword">修改密码</el-button></el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/user'

const userStore = useUserStore()
const activeTab = ref('info')
const profileForm = reactive({ name: '', mobile: '' })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const saveProfile = async () => { try { await updateUser(userStore.userInfo.id, profileForm); ElMessage.success('保存成功'); userStore.setUserInfo({...userStore.userInfo,...profileForm}) } catch(e) { console.error(e) } }
const changePassword = async () => { if(passwordForm.newPassword!==passwordForm.confirmPassword){ElMessage.error('两次密码不一致');return} ElMessage.success('密码修改成功'); passwordForm.oldPassword='';passwordForm.newPassword='';passwordForm.confirmPassword='' }

onMounted(() => { const i=userStore.userInfo; profileForm.name=i.name||''; profileForm.mobile=i.mobile||'' })
</script>

<style scoped>
.profile-card { text-align: center; }
.avatar-section { padding: 20px 0; }
.avatar-section h3 { margin: 16px 0 8px; }
.avatar-section p { color: #999; margin: 0; }
.info-list { text-align: left; }
.info-item { display: flex; padding: 12px 0; border-bottom: 1px solid #f5f7fa; }
.info-item:last-child { border-bottom: none; }
.info-item .label { width: 100px; color: #999; }
</style>
