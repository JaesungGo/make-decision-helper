package com.example.make_decision_helper.controller.chatroom;


import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatroom.dto.*;
import com.example.make_decision_helper.domain.user.CustomUserDetails;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
import com.example.make_decision_helper.service.chatroom.InviteCodeService;
import com.example.make_decision_helper.service.chatroom.UserChatRoomServiceImpl;
import com.example.make_decision_helper.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
public class ChatRoomController {

    private final UserChatRoomServiceImpl userChatRoomService;
    private final InviteCodeService inviteCodeService;

    public ChatRoomController(UserChatRoomServiceImpl userChatRoomService,
                              InviteCodeService inviteCodeService) {
        this.userChatRoomService = userChatRoomService;
        this.inviteCodeService = inviteCodeService;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest){
        try{
            InviteCode inviteCode = inviteCodeService.createInviteCode();
            return ResponseEntity.ok().body(
                    ApiResponse.success(
                            userChatRoomService.createRoom(createRoomRequest, inviteCode.getInviteCode())
                    )
            );
        } catch (Exception e){
            log.error("방 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(@Valid @RequestBody JoinRoomRequest joinRoomRequest, @AuthenticationPrincipal UserDetails userDetails){
        try {
            return ResponseEntity.ok().body(
                    ApiResponse.success(
                            userChatRoomService.joinRoom(joinRoomRequest, (CustomUserDetails) userDetails)
                    )
            );
        }catch (Exception e){
            log.error("방 참가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> exitRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserDetails userDetails){
        try{
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            userChatRoomService.leaveRoom(roomId,customUserDetails);
            return ResponseEntity.ok().body(ApiResponse.success(null));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(userChatRoomService.findRoom(roomId)));
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

    @GetMapping("/my-rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getUserRooms() {
        try {
            List<RoomResponse> userRooms = userChatRoomService.findUserRooms();
            return ResponseEntity.ok()
                    .body(ApiResponse.success(userRooms, "참여 중인 방 목록 조회 성공"));
        } catch (Exception e) {
            log.error("참여 중인 방 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
