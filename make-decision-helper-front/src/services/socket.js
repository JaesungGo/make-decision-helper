import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

class SocketService {
  constructor() {
    this.stompClient = null
    this.connected = false
  }

  connect(roomId, onMessageReceived) {
    // SockJS 인스턴스 생성 시 withCredentials 옵션 추가
    const socket = new SockJS(import.meta.env.VITE_API_BASE_URL + '/ws-stomp', null, {
      transports: ['websocket', 'xhr-streaming', 'xhr-polling'],
      withCredentials: true
    })

    this.stompClient = new Client({
      webSocketFactory: () => socket,
      debug: function (str) {
        console.log(str)
      },
      onConnect: () => {
        this.connected = true

        this.stompClient.subscribe(`/sub/chat/room/${roomId}`, (message) => {
          const receivedMessage = JSON.parse(message.body)
          onMessageReceived(receivedMessage)
        })
      },
      onDisconnect: () => {
        this.connected = false
      }
    })

    this.stompClient.activate()
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate()
      this.connected = false
    }
  }

  sendMessage(roomId, content) {
    if (!this.connected) return

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
