package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;

import java.util.List;

public interface UserChatRoomService extends ChatRoomService{

    List<RoomResponse> findUserRooms();
}
