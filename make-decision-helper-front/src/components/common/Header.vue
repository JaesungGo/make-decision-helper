<template>
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/" class="logo">Meeting Helper</router-link>
        </div>
        <nav class="header-nav">
          <router-link to="/room/create" v-if="isAuthenticated">방 만들기</router-link>
          <template v-if="!isAuthenticated">
            <router-link to="/login">로그인</router-link>
            <router-link to="/signup">회원가입</router-link>
          </template>
          <a href="#" v-else @click.prevent="handleLogout">로그아웃</a>
        </nav>
      </div>
    </header>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  
  const router = useRouter()
  const authStore = useAuthStore()
  const isAuthenticated = ref(false)
  
  onMounted(async () => {
    isAuthenticated.value = await authStore.checkAuth()
  })
  
  const handleLogout = async () => {
    const { success } = await authStore.logout()
    if (success) {
      router.push('/login')
    }
  }
  </script>
  
  <style scoped>
  .header {
    background-color: white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    position: sticky;
    top: 0;
    z-index: 1000;
  }
  
  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .logo {
    font-size: 1.5rem;
    font-weight: bold;
    color: #4CAF50;
    text-decoration: none;
  }
  
  .header-nav {
    display: flex;
    gap: 1.5rem;
    align-items: center;
  }
  
  .header-nav a {
    text-decoration: none;
    color: #666;
    font-weight: 500;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    transition: all 0.3s ease;
  }
  
  .header-nav a:hover {
    color: #4CAF50;
    background-color: #f5f5f5;
  }
  
  /* 반응형 디자인 */
  @media (max-width: 768px) {
    .header-content {
      padding: 1rem;
    }
  
    .logo {
      font-size: 1.25rem;
    }
  
    .header-nav {
      gap: 1rem;
    }
  
    .header-nav a {
      padding: 0.4rem 0.8rem;
      font-size: 0.9rem;
    }
  }
  </style>