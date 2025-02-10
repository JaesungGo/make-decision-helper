<template>
    <div class="signup-container">
      <div class="signup-form">
        <h2 class="signup-title">회원가입</h2>

        <div class="form-group">
          <label for="email" class="form-label">이메일</label>
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
          <label for="password" class="form-label">비밀번호</label>
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
          class="submit-button"
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
    padding: var(--space-24);
    background-color: var(--color-background-soft);
  }

  .signup-form {
    width: 100%;
    max-width: 400px;
    padding: var(--space-32);
    background: var(--color-background);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-lg);
  }

  .signup-title {
    font-size: var(--text-title2);
    font-weight: var(--font-bold);
    text-align: center;
    color: var(--color-text);
    margin-bottom: var(--space-8);
  }

  .form-group {
    margin-bottom: var(--space-16);
  }

  .form-label {
    display: block;
    margin-bottom: var(--space-4);
    font-size: var(--text-subhead);
    color: var(--color-text);
    font-weight: var(--font-medium);
  }

  .form-input {
    width: 100%;
    padding: var(--space-12);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-size: var(--text-body);
    background-color: var(--color-background-soft);
    transition: all 0.2s ease;
  }

  .form-input:focus {
    border-color: var(--color-primary);
    outline: none;
    box-shadow: 0 0 0 2px var(--color-primary-light);
  }

  .form-input.error {
    border-color: var(--color-error);
  }

  .error-message {
    color: var(--color-error);
    font-size: var(--text-caption1);
    margin-top: var(--space-4);
  }

  .submit-button {
    width: 100%;
    padding: var(--space-12);
    border: none;
    border-radius: var(--radius-lg);
    background-color: var(--color-primary);
    color: white;
    font-size: var(--text-body);
    font-weight: var(--font-semibold);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .submit-button:hover {
    background-color: var(--color-primary-dark);
  }

  .submit-button:disabled {
    background-color: var(--color-gray-400);
    cursor: not-allowed;
  }

  @media (max-width: 480px) {
    .signup-container {
      padding: var(--space-16);
    }

    .signup-form {
      padding: var(--space-24);
    }
  }
  </style>
