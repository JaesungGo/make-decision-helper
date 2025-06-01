import axios from '@/plugins/axios'

// API 엔드포인트 상수
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/v1/auth/login',
    SIGNUP: '/v1/auth/signup',
    LOGOUT: '/v1/auth/logout',
    REISSUE: '/v1/auth/reissue',
    ME: '/v1/auth/me'
  },
  ROOM: {
    CREATE: '/v1/rooms',
    GET_INFO: (roomId) => `/v1/rooms/${roomId}`,
    JOIN: (roomId) => `/v1/chat/rooms/${roomId}/join`,
    JOIN_BY_INVITE: '/v1/rooms/join',
    LEAVE: (roomId) => `/v1/rooms/${roomId}/leave`,
    GET_MY_ROOMS: '/v1/rooms/my-rooms'
  },
  GUEST: {
    JOIN: '/v1/guest/rooms/join',
    GET_ROOM: (roomId) => `/v1/guest/rooms/${roomId}`,
    LEAVE: (roomId) => `/v1/guest/rooms/${roomId}/leave`
  },
  CHAT: {
    GET_MESSAGES: (roomId) => `/v1/chat/rooms/${roomId}/messages`,
    GET_RECENT_MESSAGES: (roomId) => `/v1/chat/rooms/${roomId}/messages/recent`
  }
}

// API 요청 함수들
export const api = {
  auth: {
    login: (email, password) => 
      axios.post(API_ENDPOINTS.AUTH.LOGIN, { email, password }),
    
    signup: (email, password) => 
      axios.post(API_ENDPOINTS.AUTH.SIGNUP, { email, password }),
    
    logout: () => 
      axios.post(API_ENDPOINTS.AUTH.LOGOUT),
    
    reissue: () => 
      axios.post(API_ENDPOINTS.AUTH.REISSUE),
    
    me: () => 
      axios.get(API_ENDPOINTS.AUTH.ME)
  },
  
  room: {
    create: (params) => 
      axios.post(API_ENDPOINTS.ROOM.CREATE, {
        roomName: params.roomName,
        maxParticipants: params.maxParticipants,
        duration: params.durationHours,
        nickname: params.nickname
      }),
    
    getInfo: (roomId) => 
      axios.get(API_ENDPOINTS.ROOM.GET_INFO(roomId)),
    
    join: (roomId, nickname) => 
      axios.post(API_ENDPOINTS.ROOM.JOIN(roomId), { nickname }),
    
    joinByInvite: (inviteCode, nickname) =>
      axios.post(API_ENDPOINTS.ROOM.JOIN_BY_INVITE, { inviteCode, nickname }),
    
    leave: (roomId) =>
      axios.delete(API_ENDPOINTS.ROOM.LEAVE(roomId)),
    
    getMyRooms: () =>
      axios.get(API_ENDPOINTS.ROOM.GET_MY_ROOMS)
  },
  
  guest: {
    join: (inviteCode, nickname) => 
      axios.post(API_ENDPOINTS.GUEST.JOIN, { inviteCode, nickname }),
    
    getRoom: (roomId) => 
      axios.get(API_ENDPOINTS.GUEST.GET_ROOM(roomId)),
    
    leave: (roomId) =>
      axios.delete(API_ENDPOINTS.GUEST.LEAVE(roomId))
  },
  
  chat: {
    getMessages: (roomId, page = 0, size = 20, lastMessageId = null) => 
      axios.get(API_ENDPOINTS.CHAT.GET_MESSAGES(roomId), {
        params: { page, size, lastMessageId }
      }),
    
    getRecentMessages: (roomId, limit = 50) => 
      axios.get(API_ENDPOINTS.CHAT.GET_RECENT_MESSAGES(roomId), {
        params: { limit }
      })
  }
} 