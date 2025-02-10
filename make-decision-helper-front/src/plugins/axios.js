import axios from 'axios'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
  timeout: 5000
})

// 응답 인터셉터 설정
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
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
