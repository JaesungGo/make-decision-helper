package com.example.make_decision_helper.controller.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
import com.example.make_decision_helper.service.chatroom.GuestChatRoomService;
import com.example.make_decision_helper.service.user.GuestUserService;
import com.example.make_decision_helper.util.ApiResponse;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController @Slf4j
@RequiredArgsConstructor
@Transactional
@RequestMapping("/api/guest")
public class GuestUserController {

    private final GuestUserService guestUserService;
    private final ChatRoomService chatRoomService;
    private final GuestChatRoomService guestChatRoomService;

    /**
     * 게스트용 채팅참여 컨트롤러 반환된 GuestToken 쿠키가 소켓 연결시 사용
     * @param request (초대코드, 닉네임)
     * @return GuestJoinResponse (방이름, 닉네임)
     */
    @PostMapping("/rooms/join")
    public ResponseEntity<ApiResponse<RoomResponse>> createGuestCookie(@Valid @RequestBody JoinRoomRequest request){

        try {

            ChatRoom findRoomByInviteCode = chatRoomService.findRoomByInviteCode(request.getInviteCode());
            String guestToken = guestUserService.createGuestToken(findRoomByInviteCode, request.getNickname());

            ResponseCookie cookie = CookieUtil.createCookie("guestToken", guestToken, 360000L);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,cookie.toString())
                    .body(ApiResponse.success(guestChatRoomService.joinRoom(request)));
        } catch (Exception e) {
            log.error("게스트 방 참가 실패");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> joinRoom(@CookieValue(name = "guestToken",required = false) String guestToken){
        try{
            guestChatRoomService.leaveRoom(guestToken);
            return ResponseEntity.ok().body(ApiResponse.success(null, "방 나가기 성공"));
        } catch (Exception e){
            log.error("게스트 방 나가기 실패");
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
