import request from './request'

/**
 * 奖项API
 */
export default {
  // 查询所有奖项
  findAll() {
    return request.get('/prizes')
  },

  // 根据状态查询
  findByStatus(status) {
    return request.get(`/prizes/status/${status}`)
  },

  // 根据ID查询
  findById(id) {
    return request.get(`/prizes/${id}`)
  },

  // 创建奖项
  create(data) {
    return request.post('/prizes', data)
  },

  // 更新奖项
  update(id, data) {
    return request.put(`/prizes/${id}`, data)
  },

  // 删除奖项
  delete(id) {
    return request.delete(`/prizes/${id}`)
  },

  // 获取下一个待抽取的奖项
  getNextPendingPrize() {
    return request.get('/prizes/next-pending')
  },

  // 获取统计信息
  getStatistics() {
    return request.get('/prizes/statistics')
  }
}
