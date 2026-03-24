<template>
  <div class="class-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            创建班级
          </el-button>
        </div>
      </template>
      
      <el-table v-loading="loading" :data="classes" style="width: 100%">
        <el-table-column prop="name" label="班级名称" width="150" />
        <el-table-column prop="gradeName" label="年级" width="100" />
        <el-table-column prop="teacherName" label="班主任" width="100" />
        <el-table-column prop="studentCount" label="学生数" width="80" />
        <el-table-column prop="subjectNames" label="授课学科" show-overflow-tooltip min-width="150" />
        <el-table-column prop="practiceToday" label="今日练习" width="100" />
        <el-table-column prop="avgCorrectRate" label="平均正确率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.avgCorrectRate || 0" :color="getRateColor(row.avgCorrectRate)" style="width: 70px" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewClass(row)">查看</el-button>
            <el-button type="primary" link @click="editClass(row)">编辑</el-button>
            <el-button type="warning" link @click="manageStudents(row)">学生</el-button>
            <el-button type="danger" link @click="deleteClass(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadClasses"
        @current-change="loadClasses"
      />
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingClass ? '编辑班级' : '创建班级'" width="600px">
      <el-form ref="formRef" :model="classForm" :rules="formRules" label-width="100px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="classForm.name" placeholder="如：初一(1)班" />
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="classForm.gradeId" placeholder="请选择年级" style="width: 100%">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任">
          <el-select v-model="classForm.teacherId" placeholder="请选择班主任" style="width: 100%" clearable>
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="classForm.subjectTeacherIds" multiple placeholder="请选择授课教师" style="width: 100%">
            <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级简介">
          <el-input v-model="classForm.description" type="textarea" :rows="3" placeholder="请输入班级简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitClass" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 学生管理对话框 -->
    <el-dialog v-model="showStudentDialog" :title="`班级学生 - ${currentClass?.name}`" width="900px">
      <div class="student-toolbar">
        <el-button type="primary" @click="addStudent">
          <el-icon><Plus /></el-icon>
          添加学生
        </el-button>
        <el-button @click="importStudents">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
      
      <el-table :data="classStudents" v-loading="studentsLoading">
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="practiceCount" label="练习次数" width="100" />
        <el-table-column prop="correctRate" label="正确率" width="100">
          <template #default="{ row }">
            {{ row.correctRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="wrongCount" label="错题数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewStudent(row)">详情</el-button>
            <el-button type="danger" link @click="removeStudent(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'
import { getGradeList } from '@/api/base'
import { getUserList } from '@/api/user'
import { getClassList, addClass, updateClass, deleteClass as deleteClassApi, getClassStudents } from '@/api/org'

const loading = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const showStudentDialog = ref(false)
const studentsLoading = ref(false)
const editingClass = ref(null)
const currentClass = ref(null)
const classes = ref([])
const grades = ref([])
const teachers = ref([])
const classStudents = ref([])

const pagination = reactive({ page: 1, size: 10, total: 0 })

const classForm = reactive({ name: '', gradeId: '', teacherId: '', subjectTeacherIds: [], description: '' })
const formRules = { name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }], gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }] }

const getRateColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#e6a23c'
  return '#f56c6c'
}

const loadClasses = async () => {
  loading.value = true
  try {
    const data = await getClassList({ page: pagination.page, size: pagination.size })
    classes.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const loadBaseData = async () => {
  grades.value = await getGradeList() || []
  const teachersData = await getUserList({ role: 'TEACHER', limit: 100 })
  teachers.value = teachersData?.list || []
}

const editClass = (row) => {
  editingClass.value = row
  Object.assign(classForm, row)
  classForm.subjectTeacherIds = row.subjectTeacherIds || []
  showAddDialog.value = true
}

const viewClass = (row) => { manageStudents(row) }

const manageStudents = async (row) => {
  currentClass.value = row
  showStudentDialog.value = true
  studentsLoading.value = true
  try {
    const data = await getClassStudents(row.id)
    classStudents.value = data?.list || []
  } catch (e) { console.error(e) } finally { studentsLoading.value = false }
}

const deleteClass = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该班级吗？', '提示', { type: 'warning' })
    await deleteClassApi(row.id)
    ElMessage.success('删除成功')
    loadClasses()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const submitClass = async () => {
  try {
    if (editingClass.value) {
      await updateClass(editingClass.value.id, classForm)
      ElMessage.success('更新成功')
    } else {
      await addClass(classForm)
      ElMessage.success('创建成功')
    }
    showAddDialog.value = false
    loadClasses()
    editingClass.value = null
    Object.assign(classForm, { name: '', gradeId: '', teacherId: '', subjectTeacherIds: [], description: '' })
  } catch (e) { console.error(e) }
}

const addStudent = () => { ElMessage.info('添加学生功能') }
const importStudents = () => { ElMessage.info('批量导入功能') }
const viewStudent = (row) => { console.log('view', row) }
const removeStudent = async (row) => {
  try {
    await ElMessageBox.confirm('确定移除该学生吗？', '提示', { type: 'warning' })
    ElMessage.success('移除成功')
    manageStudents(currentClass.value)
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const formRef = ref()
onMounted(() => { loadBaseData(); loadClasses() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.student-toolbar { margin-bottom: 16px; display: flex; gap: 10px; }
</style>
