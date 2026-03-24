import request from '@/utils/request'

export function getWrongQuestionList(params) {
  return request({ url: '/wrong-question/list', method: 'get', params })
}

export function addWrongQuestion(data) {
  return request({ url: '/wrong-question', method: 'post', data })
}

export function updateWrongQuestion(id, data) {
  return request({ url: `/wrong-question/${id}`, method: 'put', data })
}

export function deleteWrongQuestion(id) {
  return request({ url: `/wrong-question/${id}`, method: 'delete' })
}

export function getStudyRecordList(params) {
  return request({ url: '/study-record/list', method: 'get', params })
}

export function addStudyRecord(data) {
  return request({ url: '/study-record', method: 'post', data })
}

export function getKnowledgeMasteryList(params) {
  return request({ url: '/knowledge-mastery/list', method: 'get', params })
}

export function getReviewPlanList(params) {
  return request({ url: '/review-plan/list', method: 'get', params })
}

export function getTodayReview() {
  return request({ url: '/review-plan/today', method: 'get' })
}

export function completeReview(id) {
  return request({ url: `/review-plan/${id}/complete`, method: 'post' })
}
