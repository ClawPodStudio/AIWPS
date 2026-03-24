import request from '@/utils/request'

export function getQuestionList(params) {
  return request({ url: '/question/list', method: 'get', params })
}

export function getQuestion(id) {
  return request({ url: `/question/${id}`, method: 'get' })
}

export function addQuestion(data) {
  return request({ url: '/question', method: 'post', data })
}

export function updateQuestion(id, data) {
  return request({ url: `/question/${id}`, method: 'put', data })
}

export function deleteQuestion(id) {
  return request({ url: `/question/${id}`, method: 'delete' })
}

export function getPracticeQuestions(params) {
  return request({ url: '/question/practice', method: 'get', params })
}

// 错题统计
export function getWrongQuestionStats(params) {
  return request({ url: '/wrong-question/stats', method: 'get', params })
}

export function getWrongQuestionList(params) {
  return request({ url: '/wrong-question/list', method: 'get', params })
}

// 错题智能排序
export function getSortedWrongQuestionList(params) {
  return request({ url: '/wrong-question/sorted-list', method: 'get', params })
}

export function configWrongQuestionSort(data) {
  return request({ url: '/wrong-question/config-sort', method: 'post', data })
}

// 知识点题型
export function getKnowledgePointByType(type) {
  return request({ url: `/knowledge-point/type/${type}`, method: 'get' })
}

export function bindKnowledgePointType(data) {
  return request({ url: '/knowledge-point/bind-type', method: 'post', data })
}

export function getKnowledgePointTypeBinding(id) {
  return request({ url: `/knowledge-point/type-binding/${id}`, method: 'get' })
}

// AI出题
export function generateQuestionsByAI(data) {
  return request({ url: '/question/ai-generate', method: 'post', data })
}
