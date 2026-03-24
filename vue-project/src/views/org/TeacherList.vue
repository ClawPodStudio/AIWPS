<template>
  <div class="teacher-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教师管理</span>
          <el-button type="primary" @click="showAddDialog = true"><el-icon><Plus /></el-icon>添加教师</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="teachers" style="width:100%">
        <el-table-column prop="username" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="subjectNames" label="授课学科" width="150" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="classCount" label="带班数" width="80" />
        <el-table-column prop="studentCount" label="学生数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{row}"><el-button type="primary" link @click="editTeacher(row)">编辑</el-button><el-button type="danger" link @click="deleteTeacher(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total,prev,pager,next" style="margin-top:20px;justify-content:flex-end" @current-change="loadTeachers" />
    </el-card>
    <el-dialog v-model="showAddDialog" :title="editingTeacher?'编辑教师':'添加教师'" width="500px">
      <el-form ref="formRef" :model="teacherForm" :rules="formRules" label-width="80px">
        <el-form-item label="工号" prop="username"><el-input v-model="teacherForm.username" /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="teacherForm.name" /></el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingTeacher"><el-input v-model="teacherForm.password" type="password" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="teacherForm.mobile" /></el-form-item>
        <el-form-item label="授课学科">
          <el-select v-model="teacherForm.subjectIds" multiple style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog=false">取消</el-button>
        <el-button type="primary" @click="submitTeacher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getSubjectList } from '@/api/base'
import { getUserList, addUser, updateUser, deleteUser as deleteUserApi } from '@/api/user'

const loading = ref(false)
const showAddDialog = ref(false)
const editingTeacher = ref(null)
const subjects = ref([])
const teachers = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const teacherForm = reactive({ username: '', name: '', password: '', mobile: '', subjectIds: [] })
const formRules = { username: [{ required: true, message: '请输入工号', trigger: 'blur' }], name: [{ required: true, message: '请输入姓名', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }

const loadTeachers = async () => { loading.value = true; try { const data = await getUserList({role:'TEACHER',page:pagination.page,size:pagination.size}); teachers.value=data?.list||[]; pagination.total=data?.total||0 } catch(e) { console.error(e) } finally { loading.value = false } }
const editTeacher = (row) => { editingTeacher.value = row; Object.assign(teacherForm, row); showAddDialog.value = true }
const deleteTeacher = async (row) => { try { await ElMessageBox.confirm('确定删除该教师吗？','提示',{type:'warning'}); await deleteUserApi(row.id); ElMessage.success('删除成功'); loadTeachers() } catch(e) { if(e!=='cancel') console.error(e) } }
const submitTeacher = async () => { try { if(editingTeacher.value) await updateUser(editingTeacher.value.id, teacherForm); else await addUser({...teacherForm,role:'TEACHER'}); ElMessage.success('保存成功'); showAddDialog.value=false; loadTeachers() } catch(e) { console.error(e) } }
const formRef = ref()

onMounted(async () => { subjects.value = await getSubjectList()||[]; loadTeachers() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
