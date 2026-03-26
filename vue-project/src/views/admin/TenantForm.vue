<template>
  <div class="tenant-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑租户' : '新建租户' }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:600px">
        <el-form-item label="租户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入租户名称" />
        </el-form-item>

        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>

        <el-form-item label="机构类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择机构类型" style="width:100%">
            <el-option label="学校" value="SCHOOL" />
            <el-option label="培训机构" value="TRAINING" />
          </el-select>
        </el-form-item>

        <el-form-item label="学段范围" prop="phase">
          <el-select v-model="form.phase" placeholder="请选择学段范围" style="width:100%">
            <el-option label="初中" value="MIDDLE" />
            <el-option label="高中" value="HIGH" />
            <el-option label="初高中" value="BOTH" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTenant, addTenant, updateTenant } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const saving = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  name: '',
  province: '',
  type: 'SCHOOL',
  phase: 'BOTH',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  type: [{ required: true, message: '请选择机构类型', trigger: 'change' }],
  phase: [{ required: true, message: '请选择学段范围', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadTenant = async () => {
  if (!route.params.id) return
  try {
    const data = await getTenant(route.params.id)
    if (data) {
      Object.assign(form, data)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载租户信息失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (isEdit.value) {
        await updateTenant(route.params.id, form)
        ElMessage.success('更新成功')
      } else {
        await addTenant(form)
        ElMessage.success('创建成功')
      }
      goBack()
    } catch (e) {
      console.error(e)
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      saving.value = false
    }
  })
}

const goBack = () => {
  router.push('/admin/tenants')
}

onMounted(() => {
  loadTenant()
})
</script>

<style scoped>
.tenant-form {
  padding: 0 32px 32px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
