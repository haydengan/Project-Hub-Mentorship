package com.togetherly.demo.repository.group;

import com.togetherly.demo.model.group.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByGroupIdOrderByCreatedAtAsc(UUID groupId);
    List<Message> findTop50ByGroupIdOrderByCreatedAtDesc(UUID groupId);
}
