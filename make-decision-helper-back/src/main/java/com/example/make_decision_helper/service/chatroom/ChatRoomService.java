package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.user.CustomUserDetails;


public interface ChatRoomService {


    RoomResponse joinRoom(JoinRoomRequest request, CustomUserDetails userDetails);
    void leaveRoom(Long roomId, CustomUserDetails userDetails);
    RoomResponse findRoom(Long roomId);


}
