package com.togetherly.demo.service.group;

import com.togetherly.demo.data.group.response.MessageResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import java.util.List;
import java.util.UUID;

public interface MessageService {
    MessageResponse sendMessage(UUID userId, UUID groupId, String content)
            throws NotFound, InvalidOperation;
    List<MessageResponse> getMessages(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation;
}
