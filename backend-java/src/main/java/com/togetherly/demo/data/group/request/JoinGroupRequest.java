package com.togetherly.demo.data.group.request;

import jakarta.validation.constraints.NotEmpty;

/**
 * Sent by the client to join a group using an invite code.
 */
public record JoinGroupRequest(
        @NotEmpty(message = "invite code cannot be empty !") String inviteCode) {}
