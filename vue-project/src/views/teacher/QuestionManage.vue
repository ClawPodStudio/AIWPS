<template>
  <div class="question-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <el-button type="primary" @click="showAddDialog = true"><el-icon><Plus /></el-icon>添加题目</el-button>
        </div>
      </template>
      
      <div class="filter-bar">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="学科">
            <el-select v-model="searchForm.subjectId" placeholder="全部" clearable style="width: 120px">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="searchForm.gradeId" placeholder="全部" clearable style="width: 120px">
              <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 100px">
              <el-option label="选择" value="CHOICE" /><el-option label="填空" value="BLANK" /><el-option label="解答" value="ANSWER" />
            </el-select>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="loadQuestions">搜索</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
        </el-form>
      </div>
      
      <el-table v-loading="loading" :data="questions" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="content" label="题目内容" show-overflow-tooltip min-width="200" />
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="gradeName" label="年级" width="80" />
        <el-table-column prop="type" label="题型" width="80">
          <template #default="{ row }">{{ {CHOICE:'选择',BLANK:'填空',ANSWER:'解答'}[row.type] }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="{1:'success',2:'warning',3:'danger'}[row.difficulty]" size="small">{{ {1:'简',2:'中',3:'难'}[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="editQuestion(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" style="margin-top:20px;justify-content:flex-end" @size-change="loadQuestions" @current-change="loadQuestions" />
    </el-card>
    
    <el-dialog v-model="showAddDialog" :title="editingQuestion ? '编辑题目' : '添加题目'" width="700px">
      <el-form ref="formRef" :model="questionForm" :rules="formRules" label-width="80px">
        <el-form-item label="学科" prop="subjectId">
          <el-select v-model="questionForm.subjectId" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="questionForm.gradeId" style="width:100%">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="questionForm.type">
            <el-radio label="CHOICE">选择题</el-radio><el-radio label="BLANK">填空题</el-radio><el-radio label="ANSWER">解答题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="3" />
        </el-form-item>
        <div v-if="questionForm.type === 'CHOICE'">
          <el-form-item label="A"><el-input v-model="questionForm.optionA" /></el-form-item>
          <el-form-item label="B"><el-input v-model="questionForm.optionB" /></el-form-item>
          <el-form-item label="C"><el-input v-model="questionForm.optionC" /></el-form-item>
          <el-form-item label="D"><el-input v-model="questionForm.optionD" /></el-form-item>
        </div>
        <el-form-item label="答案" prop="answer"><el-input v-model="questionForm.answer" /></el-form-item>
        <el-form-item label="解析"><el-input v-model="questionForm.analysis" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="questionForm.difficulty">
            <el-radio :label="1">简单</el-radio><el-radio :label="2">中等</el-radio><el-radio :label="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getSubjectList, getGradeList } from '@/api/base'
import { getQuestionList, addQuestion, updateQuestion, deleteQuestion as deleteQuestionApi } from '@/api/question'

const loading = ref(false)
const showAddDialog = ref(false)
const editingQuestion = ref(null)
const subjects = ref([])
const grades = ref([])
const questions = ref([])
const searchForm = reactive({ subjectId: '', gradeId: '', type: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const questionForm = reactive({ subjectId: '', gradeId: '', type: 'CHOICE', content: '', optionA: '', optionB: '', optionC: '', optionD: '', answer: '', analysis: '', difficulty: 2 })
const formRules = { subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }], gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }], content: [{ required: true, message: '请输入题目', trigger: 'blur' }], answer: [{ required: true, message: '请输入答案', trigger: 'blur' }] }

const loadQuestions = async () => {
  loading.value = true
  try {
    const data = await getQuestionList({ ...searchForm, page: pagination.page, size: pagination.size })
    questions.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const editQuestion = (row) => { editingQuestion.value = row; Object.assign(questionForm, row); showAddDialog.value = true }
const deleteQuestion = async (row) => {
  try { await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }); await deleteQuestionApi(row.id); ElMessage.success('删除成功'); loadQuestions() } catch (e) { if (e !== 'cancel') console.error(e) }
}
const submitQuestion = async () => {
  try {
    if (editingQuestion.value) await updateQuestion(editingQuestion.value.id, questionForm)
    else await addQuestion(questionForm)
    ElMessage.success('保存成功'); showAddDialog.value = false; loadQuestions()
  } catch (e) { console.error(e) }
}
const resetSearch = () => { Object.keys(searchForm).forEach(k => searchForm[k] = ''); loadQuestions() }
const formRef = ref()

onMounted(async () => { subjects.value = await getSubjectList() || []; grades.value = await getGradeList() || []; loadQuestions() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 20px; padding: 16px; background: #f5f7fa; border-radius: 8px; }
</style>
