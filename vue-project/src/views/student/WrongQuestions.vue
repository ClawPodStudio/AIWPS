<template>
  <div class="wrong-questions">
    <el-card>
      <template #header><span>错题本</span></template>
      <el-table v-loading="loading" :data="wrongQuestions" style="width:100%">
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="questionContent" label="题目" show-overflow-tooltip />
        <el-table-column prop="wrongAnswer" label="我的答案" width="100" />
        <el-table-column prop="correctAnswer" label="正确答案" width="100" />
        <el-table-column prop="wrongCount" label="错误次数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===2?'success':'warning'">{{ row.status===2?'已掌握':'未掌握' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120"><template #default="{row}"><el-button type="primary" link @click="viewQuestion(row)">查看</el-button><el-button type="success" link @click="markMastered(row)">掌握</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total,prev,pager,next" style="margin-top:20px;justify-content:flex-end" @current-change="loadWrongQuestions" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWrongQuestionList, updateWrongQuestion } from '@/api/student'

const loading = ref(false)
const wrongQuestions = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const loadWrongQuestions = async () => { loading.value=true; try{ const data=await getWrongQuestionList({page:pagination.page,size:pagination.size}); wrongQuestions.value=data?.list||[]; pagination.total=data?.total||0 }catch(e){console.error(e)}finally{loading.value=false} }
const viewQuestion = (row) => { console.log('view', row) }
const markMastered = async (row) => { try{ await updateWrongQuestion(row.id,{status:2}); ElMessage.success('已标记'); loadWrongQuestions() }catch(e){console.error(e)} }
onMounted(() => { loadWrongQuestions() })
</script>
