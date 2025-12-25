import request from './request'

/**
 * 抽奖API
 */
export default {
  // 执行抽奖
  draw(prizeId, operator = 'System') {
    return request.post('/lottery/draw', { prizeId, operator })
  },

  // 撤销中奖
  cancelWin(participantId, operator = 'System') {
    return request.post('/lottery/cancel-win', { participantId, operator })
  },

  // 重置系统
  reset() {
    return request.post('/lottery/reset')
  }
}
