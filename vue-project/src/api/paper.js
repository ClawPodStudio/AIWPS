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
