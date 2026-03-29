package com.togetherly.demo.service.group;

import com.togetherly.demo.data.group.response.MessageResponse;
import com.togetherly.demo.exception.InvalidOperation;
import com.togetherly.demo.exception.NotFound;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.model.group.Message;
import com.togetherly.demo.repository.group.GroupMemberRepository;
import com.togetherly.demo.repository.group.GroupRepository;
import com.togetherly.demo.repository.group.MessageRepository;
import com.togetherly.demo.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired private MessageRepository messageRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private UserRepository userRepository;

    @Override
    public MessageResponse sendMessage(UUID userId, UUID groupId, String content)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        if (groupMemberRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            throw new InvalidOperation("you are not a member of this group !");
        }

        Message msg = new Message();
        msg.setGroupId(groupId);
        msg.setUserId(userId);
        msg.setContent(content);
        messageRepository.saveAndFlush(msg);

        String username = userRepository.findById(userId)
                .map(User::getUserName).orElse("Unknown");

        return new MessageResponse(
                msg.getId().toString(),
                groupId.toString(),
                userId.toString(),
                username,
                msg.getContent(),
                msg.getCreatedAt().toString());
    }

    @Override
    public List<MessageResponse> getMessages(UUID userId, UUID groupId)
            throws NotFound, InvalidOperation {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFound("group not found !"));
        if (groupMemberRepository.findByUserIdAndGroupId(userId, groupId).isEmpty()) {
            throw new InvalidOperation("you are not a member of this group !");
        }

        List<Message> messages = new ArrayList<>(
                messageRepository.findTop50ByGroupIdOrderByCreatedAtDesc(groupId));
        // Reverse so oldest first
        Collections.reverse(messages);

        return messages.stream().map(msg -> {
            String username = userRepository.findById(msg.getUserId())
                    .map(User::getUserName).orElse("Unknown");
            return new MessageResponse(
                    msg.getId().toString(),
                    msg.getGroupId().toString(),
                    msg.getUserId().toString(),
                    username,
                    msg.getContent(),
                    msg.getCreatedAt().toString());
        }).toList();
    }
}
