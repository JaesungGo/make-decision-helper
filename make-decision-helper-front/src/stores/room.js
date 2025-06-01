import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/api'

export const useRoomStore = defineStore('room', () => {
  const currentRoom = ref(null)

  const createRoom = async (params) => {
    try {
      const response = await api.room.create(params)
      currentRoom.value = response.data.data
      return { success: true, data: response.data.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 생성에 실패했습니다.'
      }
    }
  }

  const joinRoom = async (roomId, nickname) => {
    try {
      const response = await api.room.join(roomId, nickname)
      return { success: true, data: response.data.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 참여에 실패했습니다.'
      }
    }
  }

  const getRoomInfo = async (roomId) => {
    try {
      const response = await api.room.getInfo(roomId)
      currentRoom.value = response.data.data
      return { success: true, data: response.data.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 정보 조회에 실패했습니다.'
      }
    }
  }

  const getRoomByInviteCode = async (params) => {
    try {
      const response = await api.room.join(params.inviteCode, params.nickname)
      return { success: true, data: response.data.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 정보 조회에 실패했습니다.'
      }
    }
  }

  const leaveRoom = async (roomId) => {
    try {
      await api.room.leave(roomId)
      currentRoom.value = null
      return { success: true }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 나가기에 실패했습니다.'
      }
    }
  }

  const getMyRooms = async () => {
    try {
      const response = await api.room.getMyRooms()
      return { success: true, data: response.data.data }
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || '채팅방 목록 조회에 실패했습니다.'
      }
    }
  }

  return {
    currentRoom,
    createRoom,
    joinRoom,
    getRoomByInviteCode,
    getRoomInfo,
    leaveRoom,
    getMyRooms
  }
})
