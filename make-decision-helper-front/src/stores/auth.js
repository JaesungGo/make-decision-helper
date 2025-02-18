import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/plugins/axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)

  const login = async (email, password) => {
    try {
      const response = await axios.post('/api/v1/auth/login', {
        email,
        password
      })

      user.value = response.data
      isAuthenticated.value = true

      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '로그인에 실패했습니다.'
      }
    }
  }

  const signup = async (email, password) => {
    try {
      const response = await axios.post('/api/v1/auth/signup', {
        email,
        password
      })
      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '회원가입에 실패했습니다.'
      }
    }
  }

  const logout = async () => {
    try {
      await axios.post('/api/v1/auth/logout')

      // 로그아웃 시 상태 초기화
      user.value = null
      isAuthenticated.value = false

      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '로그아웃에 실패했습니다.'
      }
    }
  }

  const checkAuth = async () => {
    try {
      const response = await axios.get('/api/v1/auth/me')
      user.value = response.data
      isAuthenticated.value = true
      return true
    } catch (error) {
      // 인증 실패 시 토큰 재발급 시도
      if (error.response?.data?.message === "유저 인증에 실패했습니다.") {
        try {
          const reissueResult = await reissueToken()
          if (reissueResult.success) {
            // 토큰 재발급 성공 시 다시 한 번 인증 체크
            const retryResponse = await axios.get('/api/v1/auth/me')
            user.value = retryResponse.data
            isAuthenticated.value = true
            return true
          }
        } catch (reissueError) {
          console.error('토큰 재발급 실패:', reissueError)
        }
      }

      // 모든 시도 실패 시
      user.value = null
      isAuthenticated.value = false
      return false
    }
  }

  const reissueToken = async () => {
    try {
      await axios.post('/api/v1/auth/reissue')
      return { success: true }
    } catch (error) {
      // 토큰 재발급 실패 시 로그아웃 처리
      user.value = null
      isAuthenticated.value = false
      return {
        success: false,
        error: error.response?.data?.message || '토큰 재발급에 실패했습니다.'
      }
    }
  }

  const joinAsGuest = async (inviteCode, nickname) => {
    try {
      const response = await axios.post('/api/v1/guest/rooms/join', {
        inviteCode,
        nickname
      })
      return { success: true, data: response.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '게스트 입장에 실패했습니다.'
      }
    }
  }

  const checkGuestAuth = async (roomId) => {
    try {
      console.log('Checking guest auth for room:', roomId)
      const response = await axios.get(`/api/v1/guest/rooms/${roomId}`)
      console.log('Guest auth response:', response)
      isAuthenticated.value = false
      user.value = null
      return true
    } catch (error) {
      console.error('Guest auth error:', error)
      return false
    }
  }

  return {
    user,
    isAuthenticated,
    login,
    signup,
    logout,
    reissueToken,
    joinAsGuest,
    checkAuth,
    checkGuestAuth
  }
})
