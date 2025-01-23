<template>
    <div class="signup-container">
      <div class="signup-form">
        <h2 class="text-2xl font-bold mb-6">회원가입</h2>

        <div class="form-group">
          <label for="email">이메일</label>
          <input
            id="email"
            v-model="email"
            type="email"
            required
            class="form-input"
            :class="{ 'error': errors.email }"
          >
          <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            class="form-input"
            :class="{ 'error': errors.password }"
          >
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>

        <button
          @click="handleSignup"
          class="signup-button"
          :disabled="isLoading"
        >
          {{ isLoading ? '가입 중...' : '회원가입' }}
        </button>

        <p class="text-center mt-4">
          이미 계정이 있으신가요?
          <router-link to="/login" class="text-blue-500">로그인</router-link>
        </p>
      </div>
    </div>
  </template>

  <script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'

  const router = useRouter()
  const authStore = useAuthStore()

  const email = ref('')
  const password = ref('')
  const errors = ref({})
  const isLoading = ref(false)

  const validateForm = () => {
    errors.value = {}

    if (!email.value) {
      errors.value.email = '이메일을 입력해주세요.'
    }
    if (!password.value) {
      errors.value.password = '비밀번호를 입력해주세요.'
    }

    return Object.keys(errors.value).length === 0
  }

  const handleSignup = async () => {
    if (!validateForm()) return

    try {
      isLoading.value = true
      const { success, error } = await authStore.signup(
        email.value,
        password.value
      )

      if (success) {
        await router.push('/login')
      } else {
        errors.value.form = error
      }
    } catch (err) {
      console.error('회원가입 중 오류가 발생했습니다:', err)
      errors.value.form = '회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.'
    } finally {
      isLoading.value = false
    }
  }
  </script>

  <style scoped>
  .signup-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    padding: 2rem;
  }

  .signup-form {
    width: 100%;
    max-width: 400px;
    padding: 2rem;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .form-group {
    margin-bottom: 1rem;
  }

  .form-input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    margin-top: 0.25rem;
  }

  .form-input.error {
    border-color: #ef4444;
  }

  .error-message {
    color: #ef4444;
    font-size: 0.875rem;
    margin-top: 0.25rem;
  }


  </style>
