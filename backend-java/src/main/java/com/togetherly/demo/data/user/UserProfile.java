package com.togetherly.demo.data.user;

import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;

/**
 * Public profile data returned to clients. Excludes sensitive fields (password, loginAttempts).
 *
 * Factory method from(User) converts the entity → DTO.
 * This pattern keeps the entity out of the API response (don't expose DB internals).
 */
public record UserProfile(
        String id,
        String username,
        String email,
        Role role,
        Boolean isActive,
        String registeredAt) {

    public static UserProfile from(User user) {
        return new UserProfile(
                user.getId().toString(),
                user.getUserName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreateAt().toString());
    }
}
