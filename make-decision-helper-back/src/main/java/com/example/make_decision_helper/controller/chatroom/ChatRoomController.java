package com.example.make_decision_helper.controller.chatroom;


import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatroom.dto.*;
import com.example.make_decision_helper.domain.user.CustomUserDetails;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
import com.example.make_decision_helper.service.chatroom.InviteCodeService;
import com.example.make_decision_helper.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final InviteCodeService inviteCodeService;

    @Transactional
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest){
        try{
            InviteCode inviteCode = inviteCodeService.createInviteCode();
            return ResponseEntity.ok().body(
                    ApiResponse.success(
                            chatRoomService.createRoom(createRoomRequest, inviteCode.getInviteCode())
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
                            chatRoomService.joinRoomWithInviteCode(joinRoomRequest, (CustomUserDetails) userDetails)
                    )
            );
        }catch (Exception e){
            log.error("방 참가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> exitRoom(@PathVariable Long roomId){

        return null;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long roomId) {
        try {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(chatRoomService.findRoom(roomId)));
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

    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<ApiResponse<RoomResponse>> roomInfoWithCode(){

        return null;
    }
}
