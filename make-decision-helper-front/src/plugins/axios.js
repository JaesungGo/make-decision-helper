import axios from 'axios'

// 환경변수 가져오기
const API_URL = import.meta.env.VITE_APP_API_URL
const IS_DEV = import.meta.env.DEV

// baseURL 결정 로직
const getBaseURL = () => {
  console.log('🔍 환경변수 확인:', { API_URL, IS_DEV })
  
  // 개발환경: proxy 사용하므로 baseURL을 빈 문자열로 (proxy가 /api를 처리)
  if (IS_DEV) {
    return ''  // 개발환경에서는 빈 문자열
  }
  
  // 프로덕션환경: 전체 URL 필요
  if (API_URL && API_URL !== 'undefined') {
    return API_URL  // 백엔드 URL만 (auth.js에서 /api를 붙임)
  }
  
  // fallback
  console.error('❌ VITE_APP_API_URL이 설정되지 않았습니다!')
  return ''
}

const baseURL = getBaseURL()

const axiosInstance = axios.create({
  baseURL,
  withCredentials: true,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

axiosInstance.interceptors.request.use(
  (config) => {
    console.log('🔗 API 요청 URL:', (config.baseURL || '') + config.url)
    console.log('🌍 환경 정보:', {
      개발환경: IS_DEV,
      API_URL,
      baseURL,
      최종_URL: (config.baseURL || '') + config.url
    })
    return config
  },
  (error) => Promise.reject(error)
)

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    console.error('❌ API 응답 오류:', error.response?.status, error.response?.data)
    
    if (error.response?.status === 401) {
      if (error.config.url.includes('/api/v1/guest/')) {
        window.location.href = '/login'
        return Promise.reject(error)
      }

      try {
        await axiosInstance.post('/api/v1/auth/reissue')
        return axiosInstance(error.config)
      } catch {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance