import request from '@/utils/request'

export function getPaperList(params) {
  return request({ url: '/paper/list', method: 'get', params })
}

export function getPaper(id) {
  return request({ url: `/paper/${id}`, method: 'get' })
}

export function addPaper(data) {
  return request({ url: '/paper', method: 'post', data })
}

export function updatePaper(id, data) {
  return request({ url: `/paper/${id}`, method: 'put', data })
}

export function deletePaper(id) {
  return request({ url: `/paper/${id}`, method: 'delete' })
}

export function publishPaper(id) {
  return request({ url: `/paper/${id}/publish`, method: 'post' })
}

export function getPaperQuestions(paperId) {
  return request({ url: `/paper/${paperId}/questions`, method: 'get' })
}

export function addPaperQuestion(data) {
  return request({ url: '/paper/question', method: 'post', data })
}

export function removePaperQuestion(paperId, questionId) {
  return request({ url: `/paper/${paperId}/question/${questionId}`, method: 'delete' })
}

// 生成试卷二维码
export function generatePaperQRCode(paperId) {
  return request({ url: `/paper/${paperId}/qrcode`, method: 'post' })
}

// 扫描二维码验证
export function scanQRCode(token) {
  return request({ url: '/qr/scan', method: 'get', params: { token } })
}

// AI生成试卷
export function aiGeneratePaper(data) {
  return request({ url: '/paper/ai-generate', method: 'post', data })
}

// 获取AI试卷模板列表
export function getPaperTemplates(subjectId) {
  return request({ url: '/paper/templates', method: 'get', params: { subjectId } })
}

// 获取知识点可选范围
export function getKnowledgeRange(subjectId, gradeId) {
  return request({ url: '/paper/knowledge-range', method: 'get', params: { subjectId, gradeId } })
}

// 获取试卷详情(含题目)
export function getPaperDetail(id) {
  return request({ url: `/paper/${id}`, method: 'get' })
}
