import request from '@/utils/request'

export function getSubjectList() {
  return request({ url: '/subject/list', method: 'get' })
}

export function getGradeList() {
  return request({ url: '/grade/list', method: 'get' })
}

export function getKnowledgePointList(params) {
  return request({ url: '/knowledge-point/list', method: 'get', params })
}

export function getKnowledgePointTree(subjectId, gradeId) {
  return request({ url: '/knowledge-point/tree', method: 'get', params: { subjectId, gradeId } })
}

// 题型管理
export function getQuestionTypeList() {
  return request({ url: '/question-type/list', method: 'get' })
}
