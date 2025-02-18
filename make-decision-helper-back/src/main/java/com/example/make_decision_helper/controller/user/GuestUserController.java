package com.example.make_decision_helper.controller.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
import com.example.make_decision_helper.service.user.GuestUserService;
import com.example.make_decision_helper.util.ApiResponse;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController @Slf4j
@RequestMapping("/api/v1/guest/rooms")
public class GuestUserController {

    private final GuestUserService guestUserService;
    private final ChatRoomService guestChatRoomService;

    public GuestUserController(GuestUserService guestUserService,
                               @Qualifier("guestChatRoomServiceImpl") ChatRoomService guestChatRoomService) {
        this.guestUserService = guestUserService;
        this.guestChatRoomService = guestChatRoomService;
    }

    /**
     * 게스트용 채팅참여 컨트롤러 반환된 GuestToken 쿠키가 소켓 연결시 사용
     * @param request (초대코드, 닉네임)
     * @return GuestJoinResponse (방이름, 닉네임)
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<RoomResponse>> createGuestCookie(
            @Valid @RequestBody JoinRoomRequest request) {
        try {
            String guestToken = guestUserService.createGuestToken(request.getInviteCode(), request.getNickname());
            ResponseCookie cookie = CookieUtil.createCookie("guestToken", guestToken, 360000L);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.success(
                        guestChatRoomService.joinRoom(request, null)
                    ));
        } catch (Exception e) {
            log.error("게스트 방 참가 실패:{}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @CookieValue(name = "guestToken", required = false) String guestToken) {
        try {
            guestChatRoomService.leaveRoom(roomId, null);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(null, "방 나가기 성공"));
        } catch (Exception e) {
            log.error("게스트 방 나가기 실패");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(guestChatRoomService.findRoom(roomId)));
        } catch (AccessDeniedException e) {
            log.error("채팅방 접근 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("채팅방 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
