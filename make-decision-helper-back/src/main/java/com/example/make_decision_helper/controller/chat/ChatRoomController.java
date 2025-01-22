package com.example.make_decision_helper.controller.chat;


import com.example.make_decision_helper.domain.chatroom.dto.*;
import com.example.make_decision_helper.service.chatroom.ChatRoomService;
import com.example.make_decision_helper.service.chatroom.InviteCodeService;
import com.example.make_decision_helper.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
            String inviteCode = inviteCodeService.createInviteCode();
            return ResponseEntity.ok().body(ApiResponse.success(chatRoomService.createRoom(createRoomRequest, inviteCode)));
        } catch (Exception e){
            log.error("방 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(@Valid @RequestBody JoinRoomRequest joinRoomRequest){

        return null;
    }


    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> exitRoom(@PathVariable Long roomId){

        return null;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> roomInfo(@PathVariable Long roomId){

        return null;
    }

    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<ApiResponse<RoomResponse>> roomInfoWithCode(){

        return null;
    }
}
