import axios from 'axios'

const API_URL = import.meta.env.VITE_APP_API_URL
const IS_DEV = import.meta.env.DEV

const getBaseURL = () => {
  // 개발 환경에서는 /api 프리픽스 사용
  if (IS_DEV) {
    return '/api'  
  }
  
  if (API_URL && API_URL !== 'undefined') {
    return API_URL
  }
  
  console.error('❌ VITE_APP_API_URL이 설정되지 않았습니다!')
  return ''
}

const axiosInstance = axios.create({
  baseURL: getBaseURL(),
  withCredentials: true,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// 요청 인터셉터
axiosInstance.interceptors.request.use(
  (config) => {
    console.log('🔗 API 요청 URL:', config.url)
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      if (error.config.url.includes('/guest/')) {
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        await axiosInstance.post('/v1/auth/reissue')
        return axiosInstance(error.config)
      } catch {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance 