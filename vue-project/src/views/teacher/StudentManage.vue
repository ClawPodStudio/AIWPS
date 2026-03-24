<template>
  <div class="student-manage">
    <el-card>
      <template #header><span>学生管理</span></template>
      <el-table v-loading="loading" :data="students" style="width:100%">
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gradeName" label="年级" width="80" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="practiceCount" label="练习次数" width="100" />
        <el-table-column prop="correctRate" label="正确率" width="100"><template #default="{row}">{{ row.correctRate||0 }}%</template></el-table-column>
        <el-table-column prop="wrongCount" label="错题数" width="80" />
        <el-table-column label="操作" width="150">
          <template #default="{row}">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button type="warning" link @click="assignTask(row)">布置任务</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total,prev,pager,next" style="margin-top:20px;justify-content:flex-end" @current-change="loadStudents" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList } from '@/api/user'

const loading = ref(false)
const students = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const loadStudents = async () => { loading.value = true; try { const data = await getUserList({role:'STUDENT', page:pagination.page, size:pagination.size}); students.value=data?.list||[]; pagination.total=data?.total||0 } catch(e) { console.error(e) } finally { loading.value = false } }
const viewDetail = (row) => { console.log('view', row) }
const assignTask = (row) => { console.log('assign', row) }

onMounted(() => { loadStudents() })
</script>
