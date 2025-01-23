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
    padding: 2rem;
    background-color: #f5f5f5;
  }

  .room-create-form {
    width: 100%;
    max-width: 480px;
    padding: 2rem;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .title {
    font-size: 1.5rem;
    font-weight: 600;
    text-align: center;
    margin-bottom: 2rem;
    color: #333;
  }

  .form-group {
    margin-bottom: 1.5rem;
  }

  .form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #333;
  }

  .form-select {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
    background-color: white;
  }

  .submit-button {
    width: 100%;
    padding: 1rem;
    border: none;
    border-radius: 8px;
    background-color: #4CAF50;
    color: white;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .submit-button:hover {
    background-color: #45a049;
  }

  .submit-button:disabled {
    background-color: #9ca3af;
    cursor: not-allowed;
  }

  @media (max-width: 768px) {
    .room-create-container {
      padding: 1rem;
    }

    .room-create-form {
      padding: 1.5rem;
    }
  }
  </style>
