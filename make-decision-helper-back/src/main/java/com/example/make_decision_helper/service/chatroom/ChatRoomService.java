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
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;

    /**
     * 채팅방 생성 (로그인 사용자만 가능)
     */
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String inviteCode){

        if(request.getDuration() <=0 || request.getDuration() > 24){
            throw new InvalidRequestStateException("방 유지시간은 1시간 이상 24시간 사이여야 합니다.");
        }

        String userEmail = SecurityUtil.getCurrentUserEmail()
                .orElseThrow(()-> new RuntimeException("로그인이 필요합니다."));

        User hostUser = userRepository.findByEmail(userEmail)
                .orElseThrow(()-> new RuntimeException("유저를 찾을 수 없습니다"));

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


        ChatRoom saveChatRoom = chatRoomRepository.save(chatRoom);
        log.debug("방 생성 완료 방장:{} 방ID:{}",hostUser.getId(),saveChatRoom.getId());

        ChatUser chatUser = ChatUser.builder()
                .chatRoom(saveChatRoom)
                .type(ChatUserType.HOST)
                .user(hostUser)
                .build();

        chatUserRepository.save(chatUser);

        return RoomResponse.builder()
                .inviteCode(findInviteCode.getInviteCode())
                .hostNickName(chatUser.getNickname())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getParticipants().size())
                .expiration(chatRoom.getExpirationTime())
                .status(chatRoom.getRoomStatus())
                .chatUsers(chatRoom.getParticipants())
                .build();

    }


    /**
     * 초대코드 기반으로 채팅방 참여
     * @param joinRoomRequest
     * @param userDetails
     * @return roomResponse
     */
    @Transactional
    public RoomResponse joinRoomWithInviteCode(JoinRoomRequest joinRoomRequest, CustomUserDetails userDetails){

        InviteCode inviteCode = inviteCodeRepository.findByInviteCode(joinRoomRequest.getInviteCode()).orElseThrow(() -> new NoSuchElementException("초대 코드를 찾을 수 없습니다."));

        ChatRoom roomByInviteCode = inviteCode.getChatRoom();

        if(!roomByInviteCode.isActive() || roomByInviteCode.getParticipants().size() >= roomByInviteCode.getMaxParticipants()){
            throw new InvalidRequestStateException("현재 입장할 수 없습니다");
        }

        if(LocalDateTime.now().isAfter(roomByInviteCode.getExpirationTime())){
            roomByInviteCode.changeRoomStatus(ChatRoom.RoomStatus.EXPIRED);
            chatRoomRepository.save(roomByInviteCode);
        }

        ChatUser chatUser;
        // 로그인 사용자가 존재할 경우
        if( userDetails != null ){
            User findUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(()-> new NoSuchElementException("사용자를 찾을 수 없습니다."));

            chatUser = ChatUser.builder()
                    .chatRoom(roomByInviteCode)
                    .user(findUser)
                    .type(ChatUserType.USER)
                    .nickname(joinRoomRequest.getNickname())
                    .build();
        }
        // 게스트 사용자일 경우
        else {
            chatUser = ChatUser.builder()
                    .chatRoom(roomByInviteCode)
                    .type(ChatUserType.GUEST)
                    .nickname(joinRoomRequest.getNickname())
                    .build();
        }

        chatUserRepository.save(chatUser);

        return RoomResponse.builder()
                .inviteCode(inviteCode.getInviteCode())
                .hostNickName(chatUser.getNickname())
                .maxParticipants(roomByInviteCode.getMaxParticipants())
                .currentParticipants(roomByInviteCode.getParticipants().size())
                .expiration(roomByInviteCode.getExpirationTime())
                .status(roomByInviteCode.getRoomStatus())
                .chatUsers(roomByInviteCode.getParticipants())
                .build();

    }

    /**
     * 초대코드로 방찾기
     * @param stringInviteCode
     * @return ChatRoom
     */
    @Transactional
    public ChatRoom findRoomByInviteCode(String stringInviteCode){
        InviteCode inviteCode = InviteCode.builder()
                .inviteCode(stringInviteCode)
                .build();
        return chatRoomRepository.findChatRoomByInviteCode(inviteCode)
                .orElseThrow(() -> new NoSuchElementException("방을 찾을 수 없습니다."));
    }

}
