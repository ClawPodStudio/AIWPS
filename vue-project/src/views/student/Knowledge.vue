<template>
  <div class="knowledge">
    <el-row :gutter="20">
      <el-col :span="6"><el-card><template #header><span>知识点</span></template>
        <el-select v-model="selectedSubject" placeholder="选择学科" style="width:100%;margin-bottom:16px" @change="loadTree">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-scrollbar height="400px">
          <el-tree :data="knowledgeTree" :props="{children:'children',label:'name'}" node-key="id" @node-click="handleNodeClick" :expand-on-click-node="false" :default-expand-all="true" />
        </el-scrollbar>
      </el-card></el-col>
      <el-col :span="18"><el-card v-if="currentPoint"><template #header><span>{{ currentPoint.name }}</span></template>
        <div class="mastery-info"><span>掌握程度：</span><el-progress :percentage="currentPoint.masteryLevel||0" :color="getColor(currentPoint.masteryLevel)" style="width:200px" /></div>
      </el-card><el-empty v-else description="请选择知识点" /></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSubjectList, getKnowledgePointTree } from '@/api/base'

const subjects = ref([])
const selectedSubject = ref('')
const knowledgeTree = ref([])
const currentPoint = ref(null)
const getColor = (l) => { if(l>=80)return'#67c23a';if(l>=60)return'#e6a23c';return'#f56c6c' }
const loadTree = async () => { try{ knowledgeTree.value=await getKnowledgePointTree(selectedSubject.value,null)||[] }catch(e){console.error(e)} }
const handleNodeClick = (data) => { currentPoint.value=data }
onMounted(async ()=>{ subjects.value=await getSubjectList()||[]; if(subjects.value.length>0){selectedSubject.value=subjects.value[0].id;loadTree()} })
</script>
