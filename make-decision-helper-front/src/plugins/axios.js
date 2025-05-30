import axios from 'axios'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL + (import.meta.env.VITE_APP_API_BASE_PATH || ''),
  withCredentials: true,
  timeout: 10000, // 5초 → 10초로 증가
  headers: {
    'Content-Type': 'application/json',
  }
})

axiosInstance.interceptors.request.use(
  (config) => {
    console.log('API 요청:', config.baseURL + config.url)
    return config
  },
  (error) => Promise.reject(error)
)


// 응답 인터셉터 설정
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    console.error('API 응답 오류:', error.response?.status, error.response?.data)
    
    if (error.response?.status === 401) {
      // 게스트 API 요청의 경우 재시도하지 않고 로그인 페이지로 이동
      if (error.config.url.includes('/api/v1/guest/')) {
        window.location.href = '/login'
        return Promise.reject(error)
      }

      // 일반 토큰 재발급 시도
      try {
        await axiosInstance.post('/api/v1/auth/reissue')
        // 재발급 성공시 원래 요청 재시도
        return axiosInstance(error.config)
      } catch {
        // 재발급 실패시 로그인 페이지로 이동
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance