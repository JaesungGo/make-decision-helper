import axios from './config'

export const authService = {
  login: async (email, password) => {
    const response = await axios.post('/v1/auth/login', { email, password })
    return response.data
  },

  signup: async (email, password) => {
    const response = await axios.post('/v1/auth/signup', { email, password })
    return response.data
  },

  logout: async () => {
    const response = await axios.post('/v1/auth/logout')
    return response.data
  },

  checkAuth: async () => {
    const response = await axios.get('/v1/auth/me')
    return response.data
  },

  reissueToken: async () => {
    const response = await axios.post('/v1/auth/reissue')
    return response.data
  },

  joinAsGuest: async (inviteCode, nickname) => {
    const response = await axios.post('/v1/guest/rooms/join', { inviteCode, nickname })
    return response.data
  },

  checkGuestAuth: async (roomId) => {
    const response = await axios.get(`/v1/guest/rooms/${roomId}`)
    return response.data
  }
} 