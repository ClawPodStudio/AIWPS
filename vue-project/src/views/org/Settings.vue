<template>
  <div class="settings">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="机构信息" name="org">
          <el-form :model="orgForm" label-width="120px" style="max-width:600px">
            <el-form-item label="机构名称"><el-input v-model="orgForm.name" /></el-form-item>
            <el-form-item label="机构类型">
              <el-radio-group v-model="orgForm.type">
                <el-radio label="SCHOOL">学校</el-radio>
                <el-radio label="INSTITUTE">培训机构</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="所在省份"><el-input v-model="orgForm.province" /></el-form-item>
            <el-form-item label="联系人"><el-input v-model="orgForm.contactName" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="orgForm.contactMobile" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveOrg">保存</el-button></el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="系统设置" name="system">
          <el-form label-width="150px" style="max-width:600px">
            <el-form-item label="开启学生注册"><el-switch v-model="settings.allowRegister" /></el-form-item>
            <el-form-item label="开启教师创建"><el-switch v-model="settings.allowTeacherCreate" /></el-form-item>
            <el-form-item label="默认学生练习数"><el-input-number v-model="settings.defaultPracticeCount" :min="5" :max="50" /></el-form-item>
            <el-form-item label="启用AI出题"><el-switch v-model="settings.enableAI" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveSettings">保存设置</el-button></el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrg, updateOrg } from '@/api/org'

const activeTab = ref('org')
const orgForm = reactive({ name: '', type: 'SCHOOL', province: '', contactName: '', contactMobile: '' })
const settings = reactive({ allowRegister: true, allowTeacherCreate: true, defaultPracticeCount: 10, enableAI: false })

const saveOrg = async () => { try { await updateOrg(1, orgForm); ElMessage.success('保存成功') } catch(e) { console.error(e) } }
const saveSettings = () => { localStorage.setItem('orgSettings', JSON.stringify(settings)); ElMessage.success('保存成功') }

onMounted(() => { const saved = localStorage.getItem('orgSettings'); if(saved) Object.assign(settings, JSON.parse(saved)) })
</script>
