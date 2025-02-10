<template>
    <div class="room-create-container">
      <div class="room-create-form">
        <h1 class="title">새로운 채팅방 만들기</h1>

        <form @submit.prevent="handleSubmit" class="form-content">
          <div class="form-group">
            <label>방 이름</label>
            <input
              v-model="roomName"
              type="text"
              class="form-input"
              placeholder="방 이름을 입력하세요"
              required
            >
          </div>

          <div class="form-group">
            <label>닉네임</label>
            <input
              v-model="nickname"
              type="text"
              class="form-input"
              placeholder="채팅방에서 사용할 닉네임을 입력하세요"
              required
            >
          </div>

          <div class="form-group">
            <label>최대 참여 인원</label>
            <select v-model="maxParticipants" class="form-select">
              <option v-for="n in 10" :key="n" :value="n">{{ n }}명</option>
            </select>
          </div>

          <div class="form-group">
            <label>방 유지 시간</label>
            <div class="duration-input">
              <select v-model="durationHours" class="form-select">
                <option v-for="n in 24" :key="n" :value="n">{{ n }}시간</option>
              </select>
            </div>
          </div>

          <button type="submit" class="submit-button" :disabled="isLoading">
            {{ isLoading ? '생성 중...' : '채팅방 만들기' }}
          </button>
        </form>
      </div>
    </div>
  </template>

  <script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { useRoomStore } from '@/stores/room'

  const router = useRouter()
  const roomStore = useRoomStore()

  const roomName = ref('')
  const nickname = ref('')
  const maxParticipants = ref(4)
  const durationHours = ref(24)
  const isLoading = ref(false)

  const handleSubmit = async () => {
    if (!roomName.value.trim()) {
      alert('방 이름을 입력해주세요.')
      return
    }

    if (!nickname.value.trim()) {
      alert('닉네임을 입력해주세요.')
      return
    }

    isLoading.value = true
    try {
      const result = await roomStore.createRoom({
        roomName: roomName.value,
        nickname: nickname.value,
        maxParticipants: maxParticipants.value,
        durationHours: durationHours.value
      })

      if (result.success) {
        await navigator.clipboard.writeText(result.data.inviteCode)
        alert('초대 코드가 클립보드에 복사되었습니다: ' + result.data.inviteCode)
        router.push(`/room/${result.data.roomId}`)
      } else {
        alert(result.error)
      }
    } catch (error) {
      alert('채팅방 생성에 실패했습니다.')
    } finally {
      isLoading.value = false
    }
  }
  </script>

  <style scoped>
  .room-create-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: calc(100vh - 64px);
    padding: var(--space-24);
    background-color: var(--color-background-soft);
  }

  .room-create-form {
    width: 100%;
    max-width: 480px;
    padding: var(--space-32);
    background: var(--color-background);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-lg);
  }

  .title {
    font-size: var(--text-title2);
    font-weight: var(--font-bold);
    text-align: center;
    color: var(--color-text);
    margin-bottom: var(--space-24);
  }

  .form-group {
    margin-bottom: var(--space-20);
  }

  .form-group label {
    display: block;
    margin-bottom: var(--space-4);
    font-size: var(--text-subhead);
    color: var(--color-text);
    font-weight: var(--font-medium);
  }

  .form-input, .form-select {
    width: 100%;
    padding: var(--space-12);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-size: var(--text-body);
    background-color: var(--color-background-soft);
    transition: all 0.2s ease;
  }

  .form-input:focus, .form-select:focus {
    border-color: var(--color-primary);
    outline: none;
    box-shadow: 0 0 0 2px var(--color-primary-light);
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

  @media (max-width: 768px) {
    .room-create-container {
      padding: var(--space-16);
    }

    .room-create-form {
      padding: var(--space-24);
    }
  }
  </style>
