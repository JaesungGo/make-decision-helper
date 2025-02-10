import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Signup from '../views/Signup.vue'
import Room from '../views/Room.vue'
import Home from '../views/Home.vue'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/signup',
    name: 'Signup',
    component: Signup
  },
  {
    path: '/room/:roomId',
    name: 'Room',
    component: Room,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 네비게이션 가드 설정
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    if (to.name === 'Room') {
      const isGuestAuthenticated = await authStore.checkGuestAuth(to.params.roomId)
      console.log(isGuestAuthenticated)
      if (isGuestAuthenticated) {
        next()
      } else {
        const isAuthenticated = await authStore.checkAuth()
        if (!isAuthenticated) {
          next('/login')
        } else {
          next()
        }
      }
      return
    }

    const isAuthenticated = await authStore.checkAuth()
    if (!isAuthenticated) {
      next('/login')
      return
    }
  }

  next()
})

export default router
