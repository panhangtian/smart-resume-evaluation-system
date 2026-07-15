import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(response => {
  const res = response.data
  if (res.code !== 200) {
    ElMessage.error(res.msg || '请求失败')
    if (res.code === 401) {
      localStorage.clear()
      window.location.href = '/#/login'
    }
    return Promise.reject(new Error(res.msg))
  }
  return res
}, error => {
  ElNotification.error({ title: '网络错误', message: error.message })
  return Promise.reject(error)
})

export default request
