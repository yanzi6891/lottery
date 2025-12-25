import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，则认为是错误
    if (res.code !== 200) {
      console.error('业务错误：', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res.data
  },
  error => {
    console.error('响应错误：', error)
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default request
