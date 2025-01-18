package com.example.make_decision_helper.controller.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.dto.GuestJoinRequest;
import com.example.make_decision_helper.domain.chatroom.dto.GuestJoinResponse;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController @Slf4j
@RequiredArgsConstructor
@Transactional
@RequestMapping("/api/v2/rooms")
public class GuestUserController {

    private final GuestUserService guestUserService;
    private final ChatRoomService chatRoomService;

    /**
     * 게스트용 채팅참여 컨트롤러 반환된 GuestToken 쿠키가 소켓 연결시 사용
     * @param request (초대코드, 닉네임)
     * @return GuestJoinResponse (방이름, 닉네임)
     */
    @PostMapping("/guest/join")
    public ResponseEntity<ApiResponse<GuestJoinResponse>> createGuestCookie(@Valid @RequestBody GuestJoinRequest request){

        try {
            ChatRoom findRoomByInviteCode = chatRoomService.findRoomByInviteCode(request.getInviteCode());
            String guestToken = guestUserService.guestToken(findRoomByInviteCode, request.getNickname());

            ResponseCookie cookie = CookieUtil.createCookie("guestToken", guestToken, 360000L);

            GuestJoinResponse response = GuestJoinResponse.builder()
                    .nickname(request.getNickname())
                    .roomName(findRoomByInviteCode.getTitle())
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,cookie.toString())
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
