import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/plugins/axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)

  const login = async (email, password) => {
    try {
      const response = await axios.post(import.meta.env.VITE_API_BASE_URL + '/api/v1/auth/login', {
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

  const signup = async (email, password, nickname) => {
    try {
      const response = await axios.post(import.meta.env.VITE_API_BASE_URL + '/api/v1/auth/signup', {
        email,
        password,
        nickname
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
      const response = await axios.get(import.meta.env.VITE_API_BASE_URL + '/api/v1/auth/me')
      if (response.data.status === 'SUCCESS') {
        user.value = response.data.data
        isAuthenticated.value = true
        return true
      }
      user.value = null 
      isAuthenticated.value = false
      return false
    } catch (error) {
      if (error.response?.status !== 400) {
        console.error('인증 확인 실패:', error)
      }
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

  // 초대 코드로 채팅방 참여
  const joinWithInviteCode = async (inviteCode, nickname) => {
    try {
      const response = await axios.post('/api/v1/rooms/join', {
        inviteCode,
        nickname
      })
      return { success: true, data: response.data }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || '입장에 실패했습니다.' 
      }
    }
  }

  return {
    user,
    isAuthenticated,
    login,
    signup,
    logout,
    reissueToken,
    joinWithInviteCode,
    checkAuth
  }
})