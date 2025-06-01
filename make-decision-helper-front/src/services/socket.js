import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

class SocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
  }

  getWebSocketUrl() {
    const IS_DEV = import.meta.env.DEV
    const API_URL = import.meta.env.VITE_APP_API_URL
    
    if (IS_DEV) {
      // 개발환경: proxy를 통한 WebSocket 연결
      return '/ws-stomp'
    } else {
      // 프로덕션환경: 전체 URL 사용
      if (API_URL && API_URL !== 'undefined') {
        return API_URL + '/ws-stomp'
      }
      console.error('❌ WebSocket URL을 결정할 수 없습니다!')
      return '/ws-stomp'
    }
  }

  connect(roomId, onMessageReceived) {
    const wsUrl = this.getWebSocketUrl()
    console.log('🔌 WebSocket 연결 시도:', wsUrl)
    
    // SockJS 인스턴스 생성
    const socket = new SockJS(wsUrl, null, {
      transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
      withCredentials: true
    })

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: function (str) {
        console.log('🔌 STOMP:', str)
      },
      onConnect: () => {
        console.log('✅ WebSocket 연결 성공')
        this.connected = true

        // 채팅방 구독
        this.stompClient.subscribe(`/sub/chat/room/${roomId}`, (message) => {
          const receivedMessage = JSON.parse(message.body)
          onMessageReceived(receivedMessage)
        })
      },
      onDisconnect: () => {
        console.log('❌ WebSocket 연결 해제')
        this.connected = false
      },
      onStompError: (frame) => {
        console.error('❌ STOMP 오류:', frame.headers['message'])
        console.error('추가 정보:', frame.body)
      }
    })

    this.stompClient.activate()
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate()
      this.connected = false
      console.log('🔌 WebSocket 연결 종료')
    }
  }

  sendMessage(roomId, content) {
    if (!this.connected) {
      console.warn('⚠️ WebSocket이 연결되지 않았습니다')
      return
    }

    this.stompClient.publish({
      destination: '/pub/chat/message',
      body: JSON.stringify({
        roomId,
        content
      })
    })
  }
}

export default new SocketService()