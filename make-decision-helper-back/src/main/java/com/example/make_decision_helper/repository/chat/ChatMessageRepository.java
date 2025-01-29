package com.example.make_decision_helper.repository.chat;

import com.example.make_decision_helper.domain.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query(value = "{'roomId': ?0, '_id': { $lt: ?1}}", sort = "{'sendAt': -1}")
    List<ChatMessage> findByRoomIdAndIdLessThan(Long roomId, String lastMessageId, Pageable pageable);
    
    @Query(value = "{'roomId': ?0}", sort = "{'sentAt': -1}")
    List<ChatMessage> findByRoomId(Long roomId, Pageable pageable);
    
    @Query(value = "{'roomId': ?0}", count = true)
    long countByRoomId(Long roomId);
    
    void deleteByRoomId(Long roomId);
} 