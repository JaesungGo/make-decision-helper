import axios from '@/plugins/axios'

const API_URL = import.meta.env.VITE_API_BASE_URL + '/api/v1/auth'

console.log('API URL:', import.meta.env.VITE_API_BASE_URL)

export const authService = {
  async login(email, password) {
    const response = await axios.post(`${API_URL}/login`, {
      email,
      password
    })
    return response.data
  },

  async signup(email, password, nickname) {
    const response = await axios.post(`${API_URL}/signup`, {
      email,
      password,
      nickname
    })
    return response.data
  },

  async logout() {
    const response = await axios.post(`${API_URL}/logout`)
    return response.data
  },

  async reissueToken() {
    const response = await axios.post(`${API_URL}/reissue`)
    return response.data
  }
}