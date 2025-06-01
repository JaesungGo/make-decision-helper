import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)

  const login = async (email, password) => {
    try {
      const response = await api.auth.login(email, password)
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
      await api.auth.signup(email, password)
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
      await api.auth.logout()
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
      const response = await api.auth.me()
      user.value = response.data
      isAuthenticated.value = true
      return true
    } catch (error) {
      if (error.response?.data?.message === "유저 인증에 실패했습니다.") {
        try {
          const reissueResult = await reissueToken()
          if (reissueResult.success) {
            const retryResponse = await api.auth.me()
            user.value = retryResponse.data
            isAuthenticated.value = true
            return true
          }
        } catch (reissueError) {
          console.error('토큰 재발급 실패:', reissueError)
        }
      }
      user.value = null
      isAuthenticated.value = false
      return false
    }
  }

  const reissueToken = async () => {
    try {
      await api.auth.reissue()
      return { success: true }
    } catch (error) {
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
      const response = await api.guest.join(inviteCode, nickname)
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
      const response = await api.guest.getRoom(roomId)
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
