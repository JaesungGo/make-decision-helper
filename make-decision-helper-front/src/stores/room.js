import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/plugins/axios'

export const useRoomStore = defineStore('room', () => {
  const currentRoom = ref(null)

  const createRoom = async (params) => {
    try {
      const response = await axios.post('/api/v1/rooms', params)
      currentRoom.value = response.data
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || '채팅방 생성에 실패했습니다.' 
      }
    }
  }

  const joinRoom = async (inviteCode, nickname) => {
    try {
      const response = await axios.post('/api/v1/rooms/join', {
        inviteCode,
        nickname
      })
      currentRoom.value = response.data
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || '채팅방 참여에 실패했습니다.' 
      }
    }
  }

  const leaveRoom = async (roomId) => {
    try {
      await axios.delete(`/api/v1/rooms/${roomId}/leave`)
      currentRoom.value = null
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || '채팅방 나가기에 실패했습니다.' 
      }
    }
  }

  const getRoomInfo = async (roomId) => {
    try {
      const response = await axios.get(`/api/v1/rooms/${roomId}`)
      currentRoom.value = response.data
      return { 
        success: true, 
        data: response.data 
      }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || '채팅방 정보 조회에 실패했습니다.' 
      }
    }
  }

  return {
    currentRoom,
    createRoom,
    joinRoom,
    leaveRoom,
    getRoomInfo
  }
})