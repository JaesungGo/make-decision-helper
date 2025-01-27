<template>
  <div class="home-container">
    <!-- 새로운 방 만들기 섹션 -->
    <div class="create-room-section">
      <h2 class="section-title">새로운 방 만들기</h2>
      <div class="create-room-form">
        <div class="form-group">
          <input
            v-model="roomName"
            type="text"
            class="form-input"
            placeholder="방 이름을 입력하세요"
          >
        </div>
        <div class="form-group">
          <input
            v-model="nickname"
            type="text"
            class="form-input"
            placeholder="방에서 사용할 닉네임을 입력하세요"
          >
        </div>
        <div class="form-row">
          <div class="form-group half">
            <select v-model="maxParticipants" class="form-select">
              <option v-for="n in 10" :key="n" :value="n">{{ n }}명</option>
            </select>
          </div>
          <div class="form-group half">
            <select v-model="durationHours" class="form-select">
              <option v-for="n in 24" :key="n" :value="n">{{ n }}시간</option>
            </select>
          </div>
        </div>
        <button @click="handleSubmit" class="create-button" :disabled="isLoading">
          {{ isLoading ? '생성 중...' : '채팅방 만들기' }}
        </button>
      </div>
    </div>

    <!-- 참여중인 방 목록 섹션 -->
    <div class="room-list-section">
      <h2 class="section-title">참여중인 방</h2>
      <div class="room-list">
        <div v-if="myRooms.length === 0" class="no-rooms">
          <p>가족들과 함께하는 공간</p>
          <p>주말 모임 계획</p>
        </div>
        <div v-else v-for="room in myRooms" :key="room.roomId" class="room-card">
          <div class="room-info">
            <h3>{{ room.title }}</h3>
            <p>참여자: {{ room.currentParticipants }}/{{ room.maxParticipants }}</p>
          </div>
          <button @click="enterRoom(room.roomId)" class="enter-button">입장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '@/stores/room'

const router = useRouter()
const roomStore = useRoomStore()

const roomName = ref('')
const nickname = ref('')
const maxParticipants = ref(4)
const durationHours = ref(24)
const isLoading = ref(false)
const myRooms = ref([])

const handleSubmit = async () => {
  if (!roomName.value.trim() || !nickname.value.trim()) {
    alert('방 이름과 닉네임을 모두 입력해주세요.')
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

const enterRoom = (roomId) => {
  router.push(`/room/${roomId}`)
}

onMounted(async () => {
  try {
    const result = await roomStore.getMyRooms()
    if (result.success) {
      myRooms.value = result.data
    } else {
      console.error('채팅방 목록 로드 실패:', result.error)
    }
  } catch (error) {
    console.error('채팅방 목록 로드 중 오류 발생:', error)
  }
})
</script>

<style scoped>
.home-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
}

.create-room-section, .room-list-section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.create-room-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  width: 100%;
}

.form-group.half {
  width: 50%;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-input, .form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.create-button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4F46E5;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
}

.create-button:disabled {
  background-color: #9CA3AF;
  cursor: not-allowed;
}

.no-rooms {
  text-align: center;
  color: #666;
  padding: 2rem;
}

.room-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.room-info h3 {
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.room-info p {
  color: #666;
  font-size: 0.9rem;
}

.enter-button {
  padding: 0.5rem 1rem;
  background-color: #4F46E5;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

@media (max-width: 640px) {
  .form-row {
    flex-direction: column;
  }

  .form-group.half {
    width: 100%;
  }
}
</style>
