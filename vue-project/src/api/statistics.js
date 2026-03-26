import request from '@/utils/request'

// 班级高频错题统计
export function getClassWrongTopicStats(classId, days) {
  return request({ url: '/statistics/class/wrong-topics', method: 'get', params: { classId, days } })
}

// 班级共性薄弱点统计
export function getClassWeakKnowledgeStats(classId, days) {
  return request({ url: '/statistics/class/weak-knowledge', method: 'get', params: { classId, days } })
}

// 班级题型分析统计
export function getClassQuestionTypeStats(classId, days) {
  return request({ url: '/statistics/class/question-type', method: 'get', params: { classId, days } })
}

// 班级知识点掌握度
export function getClassKnowledgeMastery(classId) {
  return request({ url: '/statistics/class/knowledge-mastery', method: 'get', params: { classId } })
}

// 班级整体概览
export function getClassOverview(classId) {
  return request({ url: '/statistics/class/overview', method: 'get', params: { classId } })
}
