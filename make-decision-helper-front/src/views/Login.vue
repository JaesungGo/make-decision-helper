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
        console.log('Guest join result:', result)
        if (result.success) {
          const roomId = result.data.data.roomId
          console.log(roomId)
          if (roomId) {
            await router.push(`/room/${roomId}`)
          } else {
            alert('방 정보를 찾을 수 없습니다.')
          }
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
    min-height: calc(100vh - 64px);
    padding: var(--space-24);
    background-color: var(--color-background-soft);
  }

  .login-form {
    width: 100%;
    max-width: 400px;
    padding: var(--space-32);
    background: var(--color-background);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-lg);
  }

  .login-title {
    font-size: var(--text-title2);
    font-weight: var(--font-bold);
    text-align: center;
    color: var(--color-text);
    margin-bottom: var(--space-8);
  }

  .login-subtitle {
    color: var(--color-text-light);
    text-align: center;
    margin-bottom: var(--space-24);
    font-size: var(--text-subhead);
  }

  .social-buttons {
    display: flex;
    flex-direction: column;
    gap: var(--space-12);
    margin-bottom: var(--space-24);
  }

  .social-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    padding: var(--space-12);
    border-radius: var(--radius-lg);
    font-size: var(--text-body);
    font-weight: var(--font-medium);
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid var(--color-border);
  }

  .social-button.kakao {
    background-color: #FEE500;
  }

  .form-input {
    width: 100%;
    padding: var(--space-12);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-size: var(--text-body);
    margin-bottom: var(--space-12);
    background-color: var(--color-background-soft);
  }

  .form-input:focus {
    border-color: var(--color-primary);
    outline: none;
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
  }

  .toggle-button {
    background: none;
    border: none;
    color: var(--color-primary);
    font-size: var(--text-footnote);
    cursor: pointer;
    padding: var(--space-8);
  }

  @media (max-width: 480px) {
    .login-form {
      padding: var(--space-24);
    }
  }
  </style>
