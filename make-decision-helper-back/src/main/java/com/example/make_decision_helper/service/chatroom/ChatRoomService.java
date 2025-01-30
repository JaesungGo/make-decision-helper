package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.dto.CreateRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.user.CustomUserDetails;

import java.util.List;

public interface ChatRoomService {

    RoomResponse joinRoom(JoinRoomRequest request, CustomUserDetails userDetails);

    void leaveRoom(Long roomId, CustomUserDetails userDetails);
    RoomResponse findRoom(Long roomId);


}
