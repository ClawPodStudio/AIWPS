<template>
  <div class="wrong-questions">
    <el-card>
      <template #header>
        <span>错题本</span>
        <div style="float:right;display:flex;gap:10px;align-items:center">
          <el-switch v-model="smartSort" active-text="智能排序" @change="handleSortChange" />
          <el-button v-if="smartSort" type="warning" link @click="showSortConfig">排序配置</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="wrongQuestions" style="width:100%">
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="questionContent" label="题目" show-overflow-tooltip />
        <el-table-column prop="wrongAnswer" label="我的答案" width="100" />
        <el-table-column prop="correctAnswer" label="正确答案" width="100" />
        <el-table-column prop="wrongCount" label="错误次数" width="80" />
        <el-table-column v-if="smartSort" prop="sortScore" label="排序分" width="80">
          <template #default="{row}"><span style="color:#67C23A;font-weight:bold">{{ row.sortScore?.toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===2?'success':'warning'">{{ row.status===2?'已掌握':'未掌握' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="120"><template #default="{row}"><el-button type="primary" link @click="viewQuestion(row)">查看</el-button><el-button type="success" link @click="markMastered(row)">掌握</el-button></template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total,prev,pager,next" style="margin-top:20px;justify-content:flex-end" @current-change="loadWrongQuestions" />
    </el-card>

    <!-- 排序配置弹窗 -->
    <el-dialog v-model="configVisible" title="排序配置" width="400px">
      <el-form :model="sortConfig" label-width="100px">
        <el-form-item label="考点频次权重">
          <el-slider v-model="sortConfig.examFreqWeight" :min="0" :max="1" :step="0.1" show-input />
        </el-form-item>
        <el-form-item label="得分权重">
          <el-slider v-model="sortConfig.scoreWeight" :min="0" :max="1" :step="0.1" show-input />
        </el-form-item>
        <el-form-item label="难度权重">
          <el-slider v-model="sortConfig.difficultyWeight" :min="0" :max="1" :step="0.1" show-input />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="configVisible=false">取消</el-button><el-button type="primary" @click="saveSortConfig">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWrongQuestionList, getSortedWrongQuestionList, configWrongQuestionSort } from '@/api/question'
import { updateWrongQuestion } from '@/api/student'

const loading = ref(false)
const wrongQuestions = ref([])
const smartSort = ref(false)
const configVisible = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const sortConfig = reactive({ examFreqWeight: 0.3, scoreWeight: 0.4, difficultyWeight: 0.3 })

const loadWrongQuestions = async () => {
  loading.value = true
  try {
    let data
    if (smartSort.value) {
      data = await getSortedWrongQuestionList({ page: pagination.page, size: pagination.size })
      wrongQuestions.value = data?.list || []
    } else {
      data = await getWrongQuestionList({ page: pagination.page, size: pagination.size })
      wrongQuestions.value = data?.list || []
    }
    pagination.total = data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSortChange = () => {
  pagination.page = 1
  loadWrongQuestions()
}

const showSortConfig = () => {
  configVisible.value = true
}

const saveSortConfig = async () => {
  try {
    await configWrongQuestionSort(sortConfig)
    ElMessage.success('配置已保存')
    configVisible.value = false
    loadWrongQuestions()
  } catch (e) {
    console.error(e)
  }
}

const viewQuestion = (row) => { console.log('view', row) }
const markMastered = async (row) => {
  try {
    await updateWrongQuestion(row.id, { status: 2 })
    ElMessage.success('已标记')
    loadWrongQuestions()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => { loadWrongQuestions() })
</script>
