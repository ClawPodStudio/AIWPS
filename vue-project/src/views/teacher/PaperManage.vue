<template>
  <div class="paper-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>试卷管理</span>
          <el-button type="primary" @click="showAddDialog = true"><el-icon><Plus /></el-icon>创建试卷</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="papers" style="width:100%">
        <el-table-column prop="title" label="试卷标题" min-width="200" />
        <el-table-column prop="subjectName" label="学科" width="80" />
        <el-table-column prop="gradeName" label="年级" width="80" />
        <el-table-column prop="questionCount" label="题数" width="60" />
        <el-table-column prop="totalScore" label="总分" width="60" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewPaper(row)">查看</el-button>
            <el-button type="primary" link @click="editPaper(row)">编辑</el-button>
            <el-button type="success" link @click="publishPaper(row)" v-if="row.status===0">发布</el-button>
            <el-button type="warning" link @click="showQRCode(row)">二维码</el-button>
            <el-button type="danger" link @click="deletePaper(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" layout="total,prev,pager,next" style="margin-top:20px;justify-content:flex-end" @current-change="loadPapers" />
    </el-card>
    
    <el-dialog v-model="showAddDialog" :title="editingPaper?'编辑试卷':'创建试卷'" width="600px">
      <el-form ref="formRef" :model="paperForm" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="paperForm.title" /></el-form-item>
        <el-form-item label="学科" prop="subjectId">
          <el-select v-model="paperForm.subjectId" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级" prop="gradeId">
          <el-select v-model="paperForm.gradeId" style="width:100%">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="paperForm.paperType">
            <el-radio label="GENERAL">普通</el-radio><el-radio label="WRONG">错题卷</el-radio><el-radio label="KNOWLEDGE">知识点卷</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="总分"><el-input-number v-model="paperForm.totalScore" :min="0" :max="200" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog=false">取消</el-button>
        <el-button type="primary" @click="submitPaper">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 二维码对话框 -->
    <el-dialog v-model="showQRDialog" title="试卷二维码" width="400px" align-center>
      <div style="text-align:center">
        <canvas ref="qrCanvas" style="max-width:100%"></canvas>
        <p v-if="qrToken" style="margin-top:10px;color:#666;font-size:12px">
          有效期至：{{ qrExpireTime }}
        </p>
      </div>
      <template #footer>
        <el-button type="primary" @click="downloadQRCode">下载二维码</el-button>
        <el-button @click="showQRDialog=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { getSubjectList, getGradeList } from '@/api/base'
import { getPaperList, addPaper, updatePaper, deletePaper as deletePaperApi, publishPaper as publishPaperApi, generatePaperQRCode } from '@/api/paper'

const loading = ref(false)
const showAddDialog = ref(false)
const showQRDialog = ref(false)
const editingPaper = ref(null)
const subjects = ref([])
const grades = ref([])
const papers = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const paperForm = reactive({ title: '', subjectId: '', gradeId: '', paperType: 'GENERAL', totalScore: 100 })
const formRules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }], subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }], gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }] }

// QR code related
const qrCanvas = ref(null)
const qrToken = ref('')
const qrExpireTime = ref('')
const currentPaperId = ref(null)

const loadPapers = async () => { loading.value = true; try { const data = await getPaperList({ page: pagination.page, size: pagination.size }); papers.value = data?.list||[]; pagination.total = data?.total||0 } catch(e) { console.error(e) } finally { loading.value = false } }
const editPaper = (row) => { editingPaper.value = row; Object.assign(paperForm, row); showAddDialog.value = true }
const viewPaper = (row) => { console.log('view', row) }
const publishPaper = async (row) => { try { await publishPaperApi(row.id); ElMessage.success('发布成功'); loadPapers() } catch(e) { console.error(e) } }
const deletePaper = async (row) => { try { await ElMessageBox.confirm('确定删除吗？','提示',{type:'warning'}); await deletePaperApi(row.id); ElMessage.success('删除成功'); loadPapers() } catch(e) { if(e!=='cancel') console.error(e) } }
const submitPaper = async () => { try { if(editingPaper.value) await updatePaper(editingPaper.value.id, paperForm); else await addPaper(paperForm); ElMessage.success('保存成功'); showAddDialog.value = false; loadPapers() } catch(e) { console.error(e) } }

// 生成二维码
const showQRCode = async (row) => {
  currentPaperId.value = row.id
  try {
    const data = await generatePaperQRCode(row.id)
    qrToken.value = data.token
    qrExpireTime.value = data.expireTime ? new Date(data.expireTime).toLocaleString() : '永久有效'
    
    showQRDialog.value = true
    await nextTick()
    
    // 生成二维码图片
    const qrUrl = `${window.location.origin}/aiwps/api/qr/scan?token=${data.token}`
    if (qrCanvas.value) {
      QRCode.toCanvas(qrCanvas.value, qrUrl, { width: 280, margin: 2 })
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('生成二维码失败')
  }
}

// 下载二维码
const downloadQRCode = () => {
  if (!qrCanvas.value) return
  const link = document.createElement('a')
  link.download = `paper-${currentPaperId.value}-qrcode.png`
  link.href = qrCanvas.value.toDataURL('image/png')
  link.click()
}

const formRef = ref()

onMounted(async () => { subjects.value = await getSubjectList()||[]; grades.value = await getGradeList()||[]; loadPapers() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
