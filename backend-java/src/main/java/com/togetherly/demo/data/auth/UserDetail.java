package com.togetherly.demo.data.auth;

import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Bridge between our User entity and Spring Security's authentication system.
 *
 * Spring Security doesn't know about our User class. It only understands
 * "UserDetails" — its own interface. This class wraps our User to satisfy
 * Spring Security's requirements.
 *
 * WHY NOT A RECORD?
 * - Implements UserDetails (requires specific getter method names like getUsername())
 * - Has mutable fields (currentAccessToken set after construction during JWT filter)
 * - Records are final — can't be proxied by some frameworks
 *
 * TWO CONSTRUCTORS:
 * 1. From User entity — used during login (password authentication)
 * 2. From JWT claims — used during request filtering (token already validated)
 */
@Getter
public class UserDetail implements UserDetails {
    private final String id;
    private final String username;
    private final String password;
    private final boolean isActive;
    private final Role role;
    private final List<GrantedAuthority> authorities;

    // Set by the JWT filter after token validation
    private String currentAccessToken;
    private String currentAccessTokenID;

    /** From User entity — used during login. */
    public UserDetail(User user) {
        this.id = user.getId().toString();
        this.username = user.getUserName();
        this.password = user.getPassword();
        this.isActive = user.isActive();
        this.role = user.getRole();
        this.authorities = mapRolesToAuthorities(Collections.singleton(user.getRole()));
    }

    /** From JWT claims — used during request filtering (no password needed). */
    public UserDetail(
            String id,
            String username,
            boolean isActive,
            Role role,
            String accessToken,
            String accessTokenID) {
        this.id = id;
        this.username = username;
        this.password = null; // not needed for token-based auth
        this.isActive = isActive;
        this.currentAccessToken = accessToken;
        this.currentAccessTokenID = accessTokenID;
        this.role = role;
        this.authorities = mapRolesToAuthorities(Collections.singleton(role));
    }

    private List<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .collect(Collectors.toList());
    }

    // --- Spring Security UserDetails interface methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetail that)) return false;
        return isActive == that.isActive
                && id.equals(that.id)
                && username.equals(that.username)
                && role == that.role
                && authorities.equals(that.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, isActive, role, authorities);
    }
}
