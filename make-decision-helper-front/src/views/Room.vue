<template>
    <div class="room-container">
      <!-- 좌측: 채팅방 목록 -->
      <div class="room-list">
        <div class="room-list-header">
          <h2>채팅방</h2>
        </div>
        <!-- 추후 채팅방 목록 구현 -->
      </div>

      <!-- 중앙: 채팅 영역 -->
      <div class="chat-area">
        <div class="chat-header">
          <h2>{{ currentRoom?.title || '채팅방' }}</h2>
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
              <div class="message-reactions">
                <button
                  v-for="type in reactionTypes"
                  :key="type"
                  @click="handleReaction(message.messageId, type)"
                  :class="['reaction-button', { active: hasReaction(message.messageId, type) }]"
                >
                  {{ getReactionEmoji(type) }}
                  {{ getReactionCount(message.messageId, type) }}
                </button>
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
  import { ref, onMounted, onUnmounted, nextTick } from 'vue'
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

  const reactionTypes = ['LIKE', 'NEUTRAL', 'DISLIKE']

  const activeTab = ref('calendar')
  const selectedLocation = ref(null)
  const mapCenter = ref({ lat: 37.5665, lng: 126.9780 }) // 서울 중심 좌표

  const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY

  onMounted(async () => {
    const roomId = route.params.roomId

    // 채팅방 정보 로드
    const result = await roomStore.getRoomInfo(roomId)
    if (!result.success) {
      alert('채팅방을 찾을 수 없습니다.')
      router.push('/')
      return
    }
    currentRoom.value = result.data

    // 이전 메시지 로드
    loadPreviousMessages()

    // 웹소켓 연결
    socketService.connect(
      roomId,
      handleMessageReceived,
      handleReactionReceived
    )
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

  const handleReactionReceived = (reaction) => {
    const message = messages.value.find(m => m.messageId === reaction.messageId)
    if (message) {
      // 반응 업데이트 로직
      message.reactions = message.reactions || {}
      message.reactions[reaction.type] = (message.reactions[reaction.type] || 0) + 1
    }
  }

  const sendMessage = async () => {
    if (!newMessage.value.trim()) return

    socketService.sendMessage(route.params.roomId, newMessage.value)
    newMessage.value = ''
  }

  const handleReaction = async (messageId, type) => {
    socketService.sendReaction(route.params.roomId, { messageId, type })
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

  const getReactionEmoji = (type) => {
    switch (type) {
      case 'LIKE': return '👍'
      case 'NEUTRAL': return '😐'
      case 'DISLIKE': return '👎'
      default: return ''
    }
  }

  const hasReaction = (messageId, type) => {
    const message = messages.value.find(m => m.messageId === messageId)
    return message?.reactions?.[type] > 0
  }

  const getReactionCount = (messageId, type) => {
    const message = messages.value.find(m => m.messageId === messageId)
    return message?.reactions?.[type] || 0
  }
  </script>

  <style scoped>
  .room-container {
    display: grid;
    grid-template-columns: 250px 1fr 300px;
    height: calc(100vh - 64px);
    background-color: #f5f5f5;
  }

  .room-list {
    background: white;
    border-right: 1px solid #eee;
    overflow-y: auto;
  }

  .room-list-header {
    padding: 1rem;
    border-bottom: 1px solid #eee;
  }

  .chat-area {
    display: flex;
    flex-direction: column;
    background: white;
  }

  .chat-header {
    padding: 1rem;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .leave-button {
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 4px;
    background-color: #ef4444;
    color: white;
    cursor: pointer;
  }

  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 1rem;
  }

  .message-wrapper {
    margin-bottom: 1rem;
  }

  .message {
    max-width: 70%;
    padding: 0.75rem;
    border-radius: 8px;
    background-color: #f0f0f0;
  }

  .my-message {
    margin-left: auto;
    background-color: #4CAF50;
    color: white;
  }

  .message-info {
    margin-bottom: 0.25rem;
    font-size: 0.875rem;
  }

  .sender {
    font-weight: 500;
  }

  .time {
    margin-left: 0.5rem;
    color: #666;
  }

  .my-message .time {
    color: rgba(255, 255, 255, 0.8);
  }

  .message-reactions {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.5rem;
  }

  .reaction-button {
    padding: 0.25rem 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    background: white;
    cursor: pointer;
  }

  .reaction-button.active {
    background-color: #e5e5e5;
  }

  .chat-input {
    padding: 1rem;
    border-top: 1px solid #eee;
    display: flex;
    gap: 0.5rem;
  }

  .chat-input textarea {
    flex: 1;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    resize: none;
  }

  .chat-input button {
    padding: 0 1.5rem;
    border: none;
    border-radius: 4px;
    background-color: #4CAF50;
    color: white;
    cursor: pointer;
  }

  .chat-input button:disabled {
    background-color: #9ca3af;
    cursor: not-allowed;
  }

  .sidebar {
    background: white;
    border-left: 1px solid #eee;
    padding: 1rem;
  }

  .sidebar-tabs {
    display: flex;
    border-bottom: 1px solid #eee;
    margin-bottom: 1rem;
  }

  .tab-button {
    flex: 1;
    padding: 0.75rem;
    border: none;
    background: none;
    cursor: pointer;
    color: #666;
    font-weight: 500;
  }

  .tab-button.active {
    color: #4CAF50;
    border-bottom: 2px solid #4CAF50;
  }

  .map-container {
    padding: 1rem;
  }

  .selected-location {
    margin-top: 1rem;
    padding: 1rem;
    background: #f9fafb;
    border-radius: 4px;
  }

  .location-address {
    color: #666;
    font-size: 0.875rem;
    margin-top: 0.25rem;
  }

  /* 반응형 디자인 */
  @media (max-width: 1024px) {
    .room-container {
      grid-template-columns: 1fr;
    }

    .room-list, .sidebar {
      display: none;
    }
  }

  @media (max-width: 768px) {
    .chat-header {
      padding: 0.75rem;
    }

    .messages-container {
      padding: 0.75rem;
    }

    .message {
      max-width: 85%;
    }

    .chat-input {
      padding: 0.75rem;
    }
  }
  </style>
