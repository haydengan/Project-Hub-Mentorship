package com.togetherly.demo.data.group.response;

public record MessageResponse(
        String id,
        String groupId,
        String userId,
        String username,
        String content,
        String createdAt) {}
