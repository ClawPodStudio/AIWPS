import request from '@/utils/request'

// 分页查询租户列表
export function getTenantList(params) {
  return request({ url: '/tenant/list', method: 'get', params })
}

// 获取单个租户
export function getTenant(id) {
  return request({ url: `/tenant/${id}`, method: 'get' })
}

// 新增租户
export function addTenant(data) {
  return request({ url: '/tenant', method: 'post', data })
}

// 更新租户
export function updateTenant(id, data) {
  return request({ url: `/tenant/${id}`, method: 'put', data })
}

// 删除租户
export function deleteTenant(id) {
  return request({ url: `/tenant/${id}`, method: 'delete' })
}

// 获取全局统计数据
export function getGlobalStats(params) {
  return request({ url: '/statistics/global', method: 'get', params })
}
