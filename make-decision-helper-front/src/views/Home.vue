<template>
  <div class="home-container">
    <h2 class="section-title">홈</h2>
    <div class="button-group">
      <button @click="showCreateRoomModal = true" class="create-button">방 만들기</button>
      <button @click="showJoinRoomModal = true" class="join-button">초대코드로 입장하기</button>
    </div>

    <!-- 방 만들기 모달 -->
    <div v-if="showCreateRoomModal" class="modal">
      <div class="modal-content">
        <span class="close" @click="showCreateRoomModal = false">&times;</span>
        <h2>새로운 방 만들기</h2>
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
    </div>

    <!-- 초대 코드로 입장하기 모달 -->
    <div v-if="showJoinRoomModal" class="modal">
      <div class="modal-content">
        <span class="close" @click="showJoinRoomModal = false">&times;</span>
        <h2>초대 코드로 입장하기</h2>
        <div class="join-room-form">
          <div class="form-group">
            <input
              v-model="inviteCode"
              type="text"
              class="form-input"
              placeholder="초대 코드를 입력하세요"
            >
          </div>
          <div class="form-group">
            <input
              v-model="nickname"
              type="text"
              class="form-input"
              placeholder="닉네임을 입력하세요"
            >
          </div>
          <button @click="joinRoom" class="join-button" :disabled="isLoading">
            {{ isLoading ? '입장 중...' : '입장하기' }}
          </button>
        </div>
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
        <div v-else v-for="room in myRooms" :key="room.roomId"
        :class="['room-card', { 'expired-room': room.isExpired }]">
          <div class="room-info">
            <h3>{{ room.title }}</h3>
            <p>참여자: {{ room.currentParticipants }}/{{ room.maxParticipants }}</p>
            <span v-if="room.isExpired" class="expired-badge">만료됨</span>
          </div>
          <button @click="enterRoom(room.roomId)"
                  class="enter-button"
                  :disabled="room.isExpired">
            {{ room.isExpired ? '만료됨' : '입장' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '@/stores/room'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const roomStore = useRoomStore()
const authStore = useAuthStore()

const roomName = ref('')
const nickname = ref('')
const maxParticipants = ref(4)
const durationHours = ref(24)
const isLoading = ref(false)
const myRooms = ref([])
const showCreateRoomModal = ref(false)
const showJoinRoomModal = ref(false)
const isGuestMode = ref(false)
const inviteCode = ref('')
const errors = ref({})

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
    console.error('채팅방 생성에 실패했습니다.', error)
    errors.value.form = '처리 중 오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

const joinRoom = async () => {
  isLoading.value = true
  try {
    const result = await roomStore.getRoomByInviteCode({
      inviteCode: inviteCode.value,
      nickname: nickname.value
  })
    if (result.success) {
      await router.push(`/room/${result.data.roomId}`)
    } else {
      alert(result.error)
    }
  } catch (error) {
    alert('채팅방 입장에 실패했습니다.')
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
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  position: relative;
}

.close {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
}

.home-container {
  max-width: 800px;
  margin: var(--space-32) auto;
  padding: 0 var(--space-16);
}

.section-title {
  font-size: var(--text-title3);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-16);
  color: var(--color-text);
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

.button-group {
  display: flex;
  gap: var(--space-16);
  margin: var(--space-32) 0;
  justify-content: center;
}

.create-button, .join-button {
  min-width: 180px;
  padding: var(--space-12) var(--space-24);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--text-subhead);
  font-weight: var(--font-medium);
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}

.create-button:hover, .join-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  background: var(--color-primary-dark);
}

.create-button:disabled, .join-button:disabled {
  background-color: var(--color-gray-400);
  cursor: not-allowed;
}

.no-rooms {
  text-align: center;
  color: #666;
  padding: 2rem;
}

.room-card {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-16) var(--space-20);
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-12);
  transition: all 0.2s ease;
}

.room-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.room-info h3 {
  font-size: var(--text-headline);
  font-weight: var(--font-semibold);
  color: var(--color-text);
  margin-bottom: var(--space-4);
}

.room-info p {
  color: var(--color-text-light);
  font-size: var(--text-footnote);
}

.room-header {
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.status-badge {
  font-size: var(--text-caption2);
  padding: var(--space-2) var(--space-6);
  background-color: var(--color-gray-400);
  color: white;
  border-radius: var(--radius-lg);
}

.inactive-room {
  opacity: 0.7;
  background-color: var(--color-background-mute);
  border-color: var(--color-border);
}

.inactive-room:hover {
  border-color: var(--color-border);
  box-shadow: none;
}

.enter-button:disabled {
  background-color: var(--color-gray-400);
  cursor: not-allowed;
  opacity: 0.7;
}

.enter-button:disabled:hover {
  background-color: var(--color-gray-400);
}

.enter-button {
  padding: var(--space-8) var(--space-16);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-lg);
  font-weight: var(--font-medium);
  transition: all 0.2s ease;
}

.enter-button:hover {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
}

.expired-room {
  opacity: 0.7;
  background: var(--color-background-mute);
}

.expired-badge {
  position: absolute;
  top: var(--space-8);
  right: var(--space-8);
  padding: var(--space-4) var(--space-8);
  background: var(--color-error);
  color: white;
  border-radius: var(--radius-full);
  font-size: var(--text-caption);
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
