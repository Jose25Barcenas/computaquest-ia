package com.computaquest.repository;

import com.computaquest.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByUserOrderByCreatedAtDesc(String userId);
    Optional<Chat> findByIdAndUser(String chatId, String userId);
}
