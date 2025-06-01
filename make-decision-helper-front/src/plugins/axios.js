import axios from 'axios'

// 환경변수 가져오기
const API_URL = import.meta.env.VITE_APP_API_URL
const IS_DEV = import.meta.env.DEV

// baseURL 결정 로직
const getBaseURL = () => {
  console.log('🔍 환경변수 확인:', { API_URL, IS_DEV })
  
  // 개발환경: proxy 사용
  if (IS_DEV) {
    return ''  // 개발환경에서는 빈 문자열 (proxy가 처리)
  }
  
  // 프로덕션환경: 전체 URL 필요
  if (API_URL && API_URL !== 'undefined') {
    return API_URL
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

// 요청 인터셉터 - 일관된 API 경로 처리
axiosInstance.interceptors.request.use(
  (config) => {
    // /v1으로 시작하는 모든 URL을 /api/v1으로 변환
    if (config.url && config.url.startsWith('/v1/')) {
      config.url = '/api' + config.url
    }
    
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

// 응답 인터셉터
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    console.error('❌ API 응답 오류:', error.response?.status, error.response?.data)
    
    if (error.response?.status === 401) {
      // 게스트 요청인 경우 바로 로그인 페이지로
      if (error.config.url.includes('/api/v1/guest/')) {
        window.location.href = '/login'
        return Promise.reject(error)
      }

      // 토큰 재발급 시도
      try {
        await axiosInstance.post('/v1/auth/reissue')  // 자동으로 /api/v1/auth/reissue로 변환됨
        return axiosInstance(error.config)
      } catch (reissueError) {
        console.error('토큰 재발급 실패:', reissueError)
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance