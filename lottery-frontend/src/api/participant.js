import request from './request'

/**
 * 参与人员API
 */
export default {
  // 查询所有人员
  findAll() {
    return request.get('/participants')
  },

  // 根据状态查询
  findByStatus(status) {
    return request.get(`/participants/status/${status}`)
  },

  // 根据ID查询
  findById(id) {
    return request.get(`/participants/${id}`)
  },

  // 添加人员
  add(data) {
    return request.post('/participants', data)
  },

  // 更新人员
  update(id, data) {
    return request.put(`/participants/${id}`, data)
  },

  // 删除人员
  delete(id) {
    return request.delete(`/participants/${id}`)
  },

  // 批量删除
  deleteBatch(ids) {
    return request.delete('/participants/batch', { data: ids })
  },

  // Excel导入
  importExcel(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/participants/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 获取统计信息
  getStatistics() {
    return request.get('/participants/statistics')
  }
}
