package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatroom.dto.CreateRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.chatuser.ChatUserType;
import com.example.make_decision_helper.domain.user.CustomUserDetails;
import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
import com.example.make_decision_helper.repository.chatuser.ChatUserRepository;
import com.example.make_decision_helper.repository.user.UserRepository;
import com.example.make_decision_helper.util.security.SecurityUtil;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service("userChatRoomServiceImpl")
@RequiredArgsConstructor
public class UserChatRoomServiceImpl implements UserChatRoomService{

    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;

    /**
     * 로그인 사용자 채팅방 생성
     * @param request
     * @param inviteCode
     * @return
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String inviteCode){

        checkDuration(request.getDuration());

        User hostUser = getUser();

        InviteCode findInviteCode = inviteCodeRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NoSuchElementException("초대 코드를 찾을 수 없습니다."));

        ChatRoom chatRoom = ChatRoom.builder()
                .title(request.getRoomName())
                .inviteCode(findInviteCode)
                .hostUser(hostUser)
                .maxParticipants(request.getMaxParticipants())
                .expirationTime(ChatRoom.setExpirationFromDuration(request.getDuration()))
                .roomStatus(ChatRoom.RoomStatus.ACTIVE)
                .active(true)
                .build();


        ChatUser chatUser = ChatUser.builder()
                .chatRoom(chatRoom)
                .type(ChatUserType.HOST)
                .user(hostUser)
                .nickname(request.getNickname())
                .build();

        hostUser.addChatRoom(chatRoom);

        hostUser.addChatUser(chatUser);

        chatRoom.addChatUser(chatUser);

        ChatRoom saveChatRoom = chatRoomRepository.save(chatRoom);
        log.debug("방 생성 완료 방장:{} 방ID:{}",hostUser.getId(),saveChatRoom.getId());

        ChatUser savedChatUser = chatUserRepository.save(chatUser);
        log.debug("방 참여 유저 저장 완료");

        return RoomResponse.from(chatRoom,savedChatUser,findInviteCode);

    }


    /**
     * 초대코드 기반으로 채팅방 참여
     * @param request
     * @param userDetails
     * @return roomResponse
     */
    @Override
    @Transactional
    public RoomResponse joinRoom(JoinRoomRequest request, CustomUserDetails userDetails) {

        InviteCode inviteCode = inviteCodeRepository.findByInviteCode(request.getInviteCode())
                .orElseThrow(() -> new NoSuchElementException("초대 코드를 찾을 수 없습니다."));

        ChatRoom roomByInviteCode = inviteCode.getChatRoom();

        roomIsNotActive(roomByInviteCode);

        validateRoomStatus(roomByInviteCode);

        User findUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(()-> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        if(roomByInviteCode.getParticipants().stream()
                .anyMatch(chatUser -> chatUser.getUser().equals(findUser))){
            throw new InvalidRequestStateException("현재 참여중인 채팅방입니다.");
        }

        ChatUser chatUser = ChatUser.builder()
                .chatRoom(roomByInviteCode)
                .user(findUser)
                .type(ChatUserType.USER)
                .nickname(request.getNickname())
                .build();

        roomByInviteCode.addChatUser(chatUser);

        findUser.addChatUser(chatUser);

        chatRoomRepository.save(roomByInviteCode);

        return RoomResponse.from(roomByInviteCode,chatUser,inviteCode);

    }

    /**
     * 로그인 사용자 채팅방 나가기
     * @param roomId
     * @param userDetails
     */
    @Override
    @Transactional
    public void leaveRoom(Long roomId, CustomUserDetails userDetails){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new NoSuchElementException("채팅방을 찾을 수 없습니다"));

        String userEmail = userDetails.getUsername();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new NoSuchElementException("유저를 찾을 수 없습니다"));
        ChatUser chatUser = chatUserRepository.findByChatRoomAndUser(chatRoom,user)
                .orElseThrow(()-> new NoSuchElementException("해당 유저가 참여중이 아닙니다"));


        if(chatUser.getType().equals(ChatUserType.HOST)){
            List<ChatUser> chatUsers = chatUserRepository.findByChatRoomOrderByJoinedAtAsc(chatRoom)
                    .orElseThrow(() -> new NoSuchElementException("채팅방에 남아있는 유저가 없습니다."));

        }

        chatRoom.removeChatUser(chatUser);

        chatUserRepository.delete(chatUser);

        if (chatRoom.getParticipants().isEmpty()) {
            chatRoom.changeRoomStatus(ChatRoom.RoomStatus.CLOSED);
            chatRoomRepository.save(chatRoom);
        }
    }

    /**
     * 초대코드로 방찾기
     * @param stringInviteCode
     * @return ChatRoom
     */
    @Transactional
    public ChatRoom findRoomByInviteCode(String stringInviteCode){
        InviteCode inviteCode = inviteCodeRepository.findByInviteCode(stringInviteCode)
                .orElseThrow(()-> new NoSuchElementException("초대 코드를 찾을 수 없습니다."));
        return chatRoomRepository.findChatRoomByInviteCode(inviteCode)
                .orElseThrow(() -> new NoSuchElementException("방을 찾을 수 없습니다."));
    }

    /**
     * 채팅방 조회 (참여자 확인)
     * @param roomId
     * @return RoomResponse
     */
    @Transactional(readOnly = true)
    public RoomResponse findRoom(Long roomId) {
        User currentUser = getUser();
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        ChatUser chatUser = chatUserRepository.findByChatRoomAndUser(chatRoom, currentUser)
                .orElseThrow(() -> new AccessDeniedException("해당 채팅방에 참여하지 않은 사용자입니다."));

        validateRoomStatus(chatRoom);

        return RoomResponse.from(chatRoom, chatUser, chatRoom.getInviteCode());
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findUserRooms() {
        User currentUser = getUser();
        return chatUserRepository.findByUser(currentUser);
    }

    private User getUser() {
        String userEmail = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(()-> new RuntimeException("로그인이 필요합니다."));
        return userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다"));
    }

    private void checkDuration(int duration) {
        if (duration <= 0 || duration > 24) {
            throw new InvalidRequestStateException("방 유지시간은 1시간 이상 24시간 사이여야 합니다.");
        }
    }

    private void validateRoomStatus(ChatRoom chatRoom) {
        if (!chatRoom.isActive() || chatRoom.getRoomStatus() != ChatRoom.RoomStatus.ACTIVE || isRoomFull(chatRoom)) {
            throw new InvalidRequestStateException("접근할 수 없는 채팅방입니다.");
        }
        checkRoomExpiration(chatRoom);
    }

    private boolean isRoomFull(ChatRoom chatRoom) {
        return chatRoom.getParticipants().size() >= chatRoom.getMaxParticipants();
    }

    private void checkRoomExpiration(ChatRoom chatRoom) {
        if (LocalDateTime.now().isAfter(chatRoom.getExpirationTime())) {
            chatRoom.changeRoomStatus(ChatRoom.RoomStatus.EXPIRED);
            chatRoomRepository.save(chatRoom);
        }
    }

    private void roomIsNotActive(ChatRoom roomByInviteCode) {
        if(!roomByInviteCode.getRoomStatus().equals(ChatRoom.RoomStatus.ACTIVE)){
            throw new InvalidRequestStateException("사용 불가능한 채팅방입니다");
        }
    }

}
