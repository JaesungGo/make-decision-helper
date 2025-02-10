<template>
    <div class="room-container">
      <!-- 좌측: 채팅방 목록 -->
      <div class="room-list">
        <div class="room-list-header">
          <h2>채팅방</h2>
          </div>
            <div v-for="room in myRooms"
                :key="room.roomId"
                class="room-item"
                @click="navigateToRoom(room.roomId)"
                :class="{ 'active': room.roomId === route.params.roomId }">
          <div class="room-info">
              <h3>{{ room.title }}</h3>
              <p>{{ room.currentParticipants }}/{{ room.maxParticipants }}</p>
          </div>
        </div>
      </div>

      <!-- 중앙: 채팅 영역 -->
      <div class="chat-area">
        <div class="chat-header">
          <h2>{{ currentRoom?.title || '채팅방' }} + {{ currentRoom?.inviteCode}}</h2>
          <div class="chat-actions">
            <button @click="handleLeaveRoom" class="leave-button">나가기</button>
          </div>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <div v-for="message in messages" :key="message.messageId" class="message-wrapper">
            <div :class="['message', { 'my-message': message.senderId === currentUser?.id }]">
              <div class="message-info">
                <span class="sender">{{ message.senderNickname }}</span>
                <span class="time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-content">
                {{ message.content }}
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <textarea
            v-model="newMessage"
            @keypress.enter.prevent="sendMessage"
            placeholder="메시지를 입력하세요..."
            rows="1"
          ></textarea>
          <button @click="sendMessage" :disabled="!newMessage.trim()">전송</button>
        </div>
      </div>

      <!-- 우측: 사이드바 -->
      <div class="sidebar">
        <div class="sidebar-tabs">
          <button
            @click="activeTab = 'calendar'"
            :class="['tab-button', { active: activeTab === 'calendar' }]"
          >
            일정
          </button>
          <button
            @click="activeTab = 'map'"
            :class="['tab-button', { active: activeTab === 'map' }]"
          >
            장소
          </button>
        </div>

        <div class="sidebar-content">
          <Calendar v-if="activeTab === 'calendar'" />
          <div v-else-if="activeTab === 'map'" class="map-container">
            <GoogleMap
              :api-key="GOOGLE_MAPS_API_KEY"
              style="width: 100%; height: 300px"
              :center="mapCenter"
              :zoom="15"
            >
              <Marker v-if="selectedLocation" :options="markerOptions" />
            </GoogleMap>
            <div v-if="selectedLocation" class="selected-location">
              <h4>선택된 장소</h4>
              <p>{{ selectedLocation.name }}</p>
              <p class="location-address">{{ selectedLocation.address }}</p>
            </div>
          </div>
        </div>
      </div>

    </div>
  </template>

  <script setup>
  import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import { useRoomStore } from '@/stores/room'
  import socketService from '@/services/socket'
  import axios from '@/plugins/axios'
  import Calendar from '@/components/Calendar.vue'
  import { GoogleMap, Marker } from 'vue3-google-map'

  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()
  const roomStore = useRoomStore()

  const messages = ref([])
  const newMessage = ref('')
  const messagesContainer = ref(null)
  const currentUser = ref(authStore.user)
  const currentRoom = ref(null)
  const myRooms = ref([])

  const activeTab = ref('calendar')
  const selectedLocation = ref(null)
  const mapCenter = ref({ lat: 37.5665, lng: 126.9780 }) // 서울 중심 좌표

  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

  const navigateToRoom = (roomId) => {
    router.push(`/room/${roomId}`)
  }

  // 채팅방 변경 감지 및 데이터 로드 함수
  const loadRoomData = async (roomId) => {
    // 채팅방 정보 로드
    const result = await roomStore.getRoomInfo(roomId)
    if (!result.success) {
      alert('채팅방을 찾을 수 없습니다.')
      router.push('/')
      return
    }

    currentRoom.value = result.data

    // 이전 메시지 로드
    await loadPreviousMessages()

    // 웹소켓 재연결
    socketService.disconnect()
    socketService.connect(roomId, handleMessageReceived)
  }

  // route.params.roomId 변경 감지
  watch(() => route.params.roomId, async (newRoomId) => {
    if (newRoomId) {
      await loadRoomData(newRoomId)
    }
  })

  onMounted(async () => {
    const roomId = route.params.roomId
    if (roomId) {
      await loadRoomData(roomId)
    }

    // 채팅방 목록 로드
    try {
      const roomsResult = await roomStore.getMyRooms()
      if (roomsResult.success) {
        myRooms.value = roomsResult.data
      }
    } catch (error) {
      console.error('채팅방 목록 로드 실패:', error)
    }
  })

  onUnmounted(() => {
    socketService.disconnect()
  })

  const loadPreviousMessages = async () => {
    try {
      const response = await axios.get(`/api/v1/chat/rooms/${route.params.roomId}/messages/recent`)
      messages.value = response.data.data
      await nextTick()
      scrollToBottom()
    } catch (error) {
      console.error('메시지 로드 실패:', error)
    }
  }

  const handleMessageReceived = (message) => {
    messages.value.push(message)
    nextTick(() => scrollToBottom())
  }

  const sendMessage = async () => {
    if (!newMessage.value.trim()) return

    socketService.sendMessage(route.params.roomId, newMessage.value)
    newMessage.value = ''
  }

  const handleLeaveRoom = async () => {
    const result = await roomStore.leaveRoom(route.params.roomId)
    if (result.success) {
      router.push('/')
    } else {
      alert('채팅방을 나가는데 실패했습니다.')
    }
  }

  const scrollToBottom = () => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }

  const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  </script>

  <style scoped>
  .room-container {
    display: grid;
    grid-template-columns: minmax(250px, 20%) minmax(400px, 1fr) minmax(300px, 25%);
    height: calc(100vh - 64px);
    gap: 1px;
    background-color: var(--color-border);
  }

  .room-container .room-list {
    background: var(--color-background);
    border-right: 1px solid var(--color-border);
    overflow-y: auto;
    height: 100%;
    padding: var(--space-16);
  }

  .room-container .room-item {
    padding: var(--space-12) var(--space-16);
    margin: var(--space-8) 0;
    border-radius: var(--radius-lg);
    cursor: pointer;
    transition: all 0.2s ease;
    background: var(--color-background);
    border: 1px solid transparent;
  }

  .room-container .room-item:hover {
    background: var(--color-background-soft);
    border-color: var(--color-border);
  }

  .room-container .room-item.active {
    background: var(--color-background-soft);
    border-left: 3px solid var(--color-primary);
    padding-left: calc(var(--space-16) - 3px);
  }

  .room-list-header {
    padding-bottom: var(--space-12);
    border-bottom: 1px solid var(--color-border);
    margin-bottom: var(--space-16);
  }

  .room-list-header h2 {
    font-size: var(--text-title3);
    font-weight: var(--font-semibold);
    color: var(--color-text);
  }

  .room-list-header .button-group {
    display: flex;
    gap: var(--space-16);
    margin-bottom: var(--space-32);
    justify-content: center;
  }

  .room-list-header .create-button, .room-list-header .join-button {
    min-width: 200px;
    padding: var(--space-16) var(--space-24);
    font-size: var(--text-title3);
    box-shadow: var(--shadow-sm);
    transform: translateY(0);
    transition: all 0.2s ease;
  }

  .room-list-header .create-button:hover, .room-list-header .join-button:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .room-info {
    display: flex;
    flex-direction: column;
    gap: var(--space-4);
  }

  .room-info h3 {
    font-size: var(--text-subhead);
    font-weight: var(--font-medium);
    margin-bottom: var(--space-4);
  }

  .room-info p {
    font-size: var(--text-caption);
    color: var(--color-text-light);
  }

  .chat-area {
    display: flex;
    flex-direction: column;
    max-width: 100%;
    background: var(--color-background);
    border-right: 1px solid var(--color-border);
  }

  .chat-header {
    padding: var(--space-12) var(--space-16);
    border-bottom: 1px solid var(--color-border);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--color-background);
  }

  .chat-header h2 {
    font-size: var(--text-subhead);
    font-weight: var(--font-medium);
    color: var(--color-text);
  }

  .leave-button {
    padding: var(--space-6) var(--space-12);
    border: none;
    border-radius: var(--radius-lg);
    background-color: transparent;
    color: var(--color-error);
    font-size: var(--text-footnote);
    font-weight: var(--font-medium);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .leave-button:hover {
    background-color: var(--color-error);
    color: white;
  }

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: var(--space-16);
    display: flex;
    flex-direction: column;
    gap: var(--space-12);
  }

  .message-wrapper {
    display: flex;
    flex-direction: column;
  }

  .message {
    max-width: 70%;
    padding: var(--space-12);
    border-radius: var(--radius-lg);
    background: var(--color-background-soft);
  }

  .my-message {
    align-self: flex-end;
    background: var(--color-primary);
    color: white;
  }

  .message-info {
    margin-bottom: var(--space-2);
  }

  .sender {
    font-size: var(--text-caption1);
    font-weight: var(--font-medium);
    color: var(--color-text-light);
    margin-bottom: var(--space-4);
  }

  .time {
    font-size: var(--text-caption2);
    color: var(--color-text-mute);
    margin-left: var(--space-8);
  }

  .message-content {
    line-height: 1.4;
  }

  .message-reactions {
    display: flex;
    gap: var(--space-2);
    margin-top: var(--space-4);
  }

  .reaction-button {
    padding: var(--space-2) var(--space-4);
    border: 1px solid transparent;
    border-radius: var(--radius-lg);
    background-color: transparent;
    font-size: var(--text-caption2);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .reaction-button:hover {
    background-color: var(--color-background-mute);
  }

  .reaction-button.active {
    background-color: var(--color-primary-light);
    color: white;
  }

  .chat-input {
    padding: var(--space-16);
    border-top: 1px solid var(--color-border);
    display: flex;
    gap: var(--space-12);
    background: var(--color-background);
  }

  .chat-input textarea {
    flex: 1;
    resize: none;
    padding: var(--space-12);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    font-family: inherit;
  }

  .chat-input button {
    padding: var(--space-8) var(--space-16);
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: var(--radius-lg);
    font-weight: var(--font-medium);
    transition: all 0.2s ease;
  }

  .sidebar {
    background-color: var(--color-background);
    border-left: 1px solid var(--color-border);
    padding: var(--space-16);
  }

  .sidebar-tabs {
    display: flex;
    gap: var(--space-4);
    margin-bottom: var(--space-16);
  }

  .tab-button {
    flex: 1;
    padding: var(--space-8) var(--space-12);
    border: none;
    border-radius: var(--radius-lg);
    background: transparent;
    font-size: var(--text-footnote);
    color: var(--color-text-light);
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .tab-button:hover {
    background-color: var(--color-background-soft);
  }

  .tab-button.active {
    color: var(--color-primary);
    background-color: var(--color-background-soft);
    font-weight: var(--font-medium);
  }

  .map-container {
    padding: var(--space-16);
    background-color: var(--color-background);
    border-radius: var(--radius-lg);
  }

  .selected-location {
    margin-top: var(--space-16);
    padding: var(--space-12);
    background-color: var(--color-background-soft);
    border-radius: var(--radius-lg);
  }

  .location-address {
    color: var(--color-text-light);
    font-size: var(--text-caption1);
    margin-top: var(--space-4);
  }

  .room-card {
    position: relative;
    overflow: hidden;
  }

  .expired-room {
    opacity: 0.6;
    background-color: var(--color-background-mute);
  }

  .expired-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: var(--space-4) var(--space-8);
    background-color: var(--color-error);
    color: white;
    border-radius: var(--radius-full);
    font-size: var(--text-caption2);
  }

  .enter-button {
    background-color: var(--color-primary);
    color: white;
    padding: var(--space-8) var(--space-16);
    border-radius: var(--radius-lg);
    font-weight: var(--font-medium);
    transition: all 0.2s ease;
  }

  @media (max-width: 1024px) {
    .room-container {
      grid-template-columns: 1fr;
    }

    .room-list, .sidebar {
      display: none;
    }
  }

  @media (max-width: 768px) {
    .chat-header,
    .messages-container,
    .chat-input {
      padding: var(--space-12);
    }

    .message {
      max-width: 85%;
    }
  }
  </style>

