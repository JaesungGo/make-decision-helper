<template>
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/" class="logo">Meeting Helper</router-link>
        </div>
        <nav class="header-nav">
          <template v-if="!isAuthenticated">
            <router-link to="/login" class="nav-link">로그인</router-link>
            <router-link to="/signup" class="nav-link primary">회원가입</router-link>
          </template>
          <a href="#" v-else @click.prevent="handleLogout" class="nav-link">로그아웃</a>
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
    background-color: var(--color-background);
    border-bottom: 1px solid var(--color-border);
    position: sticky;
    top: 0;
    z-index: 1000;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
  }

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: var(--space-16) var(--space-24);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    font-size: var(--text-title3);
    font-weight: var(--font-semibold);
    color: var(--color-primary);
    text-decoration: none;
  }

  .header-nav {
    display: flex;
    gap: var(--space-12);
    align-items: center;
  }

  .nav-link {
    text-decoration: none;
    color: var(--color-text);
    font-size: var(--text-subhead);
    font-weight: var(--font-medium);
    padding: var(--space-8) var(--space-16);
    border-radius: var(--radius-lg);
    transition: all 0.2s ease;
  }

  .nav-link:hover {
    background-color: var(--color-gray-100);
  }

  .nav-link.primary {
    background-color: var(--color-primary);
    color: white;
  }

  .nav-link.primary:hover {
    background-color: var(--color-primary-dark);
  }

  @media (max-width: 768px) {
    .header-content {
      padding: var(--space-12) var(--space-16);
    }

    .nav-link {
      padding: var(--space-6) var(--space-12);
      font-size: var(--text-footnote);
    }
  }
  </style>
