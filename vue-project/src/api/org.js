import request from '@/utils/request'

export function getOrgList(params) {
  return request({ url: '/organization/list', method: 'get', params })
}

export function getOrg(id) {
  return request({ url: `/organization/${id}`, method: 'get' })
}

export function addOrg(data) {
  return request({ url: '/organization', method: 'post', data })
}

export function updateOrg(id, data) {
  return request({ url: `/organization/${id}`, method: 'put', data })
}

export function getClassList(params) {
  return request({ url: '/class/list', method: 'get', params })
}

export function getClass(id) {
  return request({ url: `/class/${id}`, method: 'get' })
}

export function addClass(data) {
  return request({ url: '/class', method: 'post', data })
}

export function updateClass(id, data) {
  return request({ url: `/class/${id}`, method: 'put', data })
}

export function deleteClass(id) {
  return request({ url: `/class/${id}`, method: 'delete' })
}

export function getClassStudents(classId) {
  return request({ url: `/class/${classId}/students`, method: 'get' })
}

// 全局数据统计
export function getGlobalStats(params) {
  return request({ url: '/statistics/global', method: 'get', params })
}

export function getPracticeTrend(params) {
  return request({ url: '/statistics/practice-trend', method: 'get', params })
}

export function getSubjectStats(params) {
  return request({ url: '/statistics/subject', method: 'get', params })
}
