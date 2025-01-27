<template>
    <div class="login-container">
      <div class="login-form">
        <h1 class="login-title">환영합니다</h1>
        <p class="login-subtitle">로그인하거나 초대 코드로 시작하세요</p>

        <div class="social-buttons">
          <button class="social-button google">
            <img src="@/assets/google-icon.png" alt="Google" />
            Google로 계속하기
          </button>
          <button class="social-button kakao">
            <img src="@/assets/kakao-icon.png" alt="Kakao" />
            카카오로 계속하기
          </button>
        </div>

        <div class="divider">
          <span>또는</span>
        </div>

        <form @submit.prevent="handleSubmit" class="form-content">
          <div v-if="isGuestMode" class="form-group">
            <input
              v-model="inviteCode"
              type="text"
              class="form-input"
              :class="{ 'has-error': errors.inviteCode }"
              placeholder="초대 코드를 입력하세요"
            >
            <input
              v-model="nickname"
              type="text"
              class="form-input"
              :class="{ 'has-error': errors.nickname }"
              placeholder="닉네임을 입력하세요"
            >
          </div>
          <div v-else class="form-group">
            <input
              v-model="email"
              type="email"
              class="form-input"
              :class="{ 'has-error': errors.email }"
              placeholder="이메일을 입력하세요"
            >
            <input
              v-model="password"
              type="password"
              class="form-input"
              :class="{ 'has-error': errors.password }"
              placeholder="비밀번호를 입력하세요"
            >
          </div>

          <button type="submit" class="submit-button" :disabled="isLoading">
            {{ submitButtonText }}
          </button>
        </form>

        <div class="toggle-mode">
          <button @click="toggleMode" class="toggle-button">
            {{ isGuestMode ? '이메일로 로그인' : '초대 코드로 입장' }}
          </button>
        </div>

        <p v-if="!isGuestMode" class="signup-link">
          계정이 없으신가요? <router-link to="/signup">회원가입</router-link>
        </p>
      </div>
    </div>
  </template>

  <script setup>
  import { ref, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'

  const router = useRouter()
  const authStore = useAuthStore()

  const isGuestMode = ref(true)
  const email = ref('')
  const password = ref('')
  const inviteCode = ref('')
  const nickname = ref('')
  const errors = ref({})
  const isLoading = ref(false)

  const submitButtonText = computed(() => {
    if (isLoading.value) return '처리 중...'
    return isGuestMode.value ? '게스트로 입장' : '로그인'
  })

  const toggleMode = () => {
    isGuestMode.value = !isGuestMode.value
    errors.value = {}
  }

  const validateForm = () => {
    errors.value = {}

    if (isGuestMode.value) {
      if (!inviteCode.value) {
        errors.value.inviteCode = '초대 코드를 입력해주세요.'
        return false
      }
      if (!nickname.value) {
        errors.value.nickname = '닉네임을 입력해주세요.'
        return false
      }
    } else {
      if (!email.value) {
        errors.value.email = '이메일을 입력해주세요.'
        return false
      }
      if (!password.value) {
        errors.value.password = '비밀번호를 입력해주세요.'
        return false
      }
    }
    return true
  }

  const handleSubmit = async () => {
    if (!validateForm()) return

    isLoading.value = true

    try {
      if (isGuestMode.value) {
        // 게스트로 입장
        const result = await authStore.joinAsGuest(inviteCode.value, nickname.value)
        if (result.success) {
          router.push(`/room/${result.data.roomId}`)
        } else {
          errors.value.form = result.error
        }
      } else {
        // 일반 로그인
        const result = await authStore.login(email.value, password.value)
        if (result.success) {
          router.push('/')
        } else {
          errors.value.form = result.error
        }
      }
    } catch (error) {
      console.error('로그인 오류:', error)
      errors.value.form = '처리 중 오류가 발생했습니다.'
    } finally {
      isLoading.value = false
    }
  }
  </script>

  <style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    padding: 1rem;
    background-color: #f5f5f5;
  }

  .login-form {
    width: 100%;
    max-width: 400px;
    padding: 2rem;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .login-title {
    font-size: 1.5rem;
    font-weight: 600;
    text-align: center;
    margin-bottom: 0.5rem;
  }

  .login-subtitle {
    color: #666;
    text-align: center;
    margin-bottom: 2rem;
  }

  .social-buttons {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
  }

  .social-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: 0.875rem;
    border-radius: 8px;
    border: 1px solid #ddd;
    background: white;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .social-button img {
    width: 20px;
    height: 20px;
    margin-right: 0.75rem;
  }

  .social-button.kakao {
    background-color: #FEE500;
    border: none;
  }

  .divider {
    display: flex;
    align-items: center;
    text-align: center;
    margin: 1.5rem 0;
  }

  .divider::before,
  .divider::after {
    content: '';
    flex: 1;
    border-bottom: 1px solid #ddd;
  }

  .divider span {
    padding: 0 1rem;
    color: #666;
    font-size: 0.875rem;
  }

  .form-input {
    width: 100%;
    padding: 0.875rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
    margin-bottom: 0.75rem;
  }

  .form-input.has-error {
    border-color: #ff4444;
  }

  .submit-button {
    width: 100%;
    padding: 0.875rem;
    border: none;
    border-radius: 8px;
    background-color: #1a73e8;
    color: white;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    margin-top: 1rem;
  }

  .submit-button:disabled {
    background-color: #9ca3af;
  }

  .toggle-mode {
    text-align: center;
    margin-top: 1rem;
  }

  .toggle-button {
    background: none;
    border: none;
    color: #1a73e8;
    font-size: 0.875rem;
    cursor: pointer;
  }

  .signup-link {
    text-align: center;
    margin-top: 1.5rem;
    font-size: 0.875rem;
    color: #666;
  }

  .signup-link a {
    color: #1a73e8;
    text-decoration: none;
    font-weight: 500;
  }

  @media (max-width: 480px) {
    .login-form {
      padding: 1.5rem;
    }

    .social-button {
      padding: 0.75rem;
    }
  }
  </style>
