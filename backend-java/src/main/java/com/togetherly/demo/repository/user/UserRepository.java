package com.togetherly.demo.repository.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.togetherly.demo.model.auth.Role;
import com.togetherly.demo.model.auth.User;

/**
 * Data access for the User entity 
 *
 * HOW THIS WORKS:
 * This is an INTERFACE — you never write a class that implements it.
 * Spring Data JPA reads these method signatures at startup and auto-generates
 * the implementation (SQL queries) at runtime.
 *
 * JpaRepository<User, UUID> means:
 *   - User   = the entity type this repository manages
 *   - UUID   = the type of the entity's primary key (id field)
 *
 * You get these methods FOR FREE (inherited from JpaRepository):
 *   save(user)          → INSERT or UPDATE
 *   findById(uuid)      → SELECT by primary key
 *   findAll()           → SELECT * (all users)
 *   delete(user)        → DELETE
 *   count()             → SELECT COUNT(*)
 *   ... and many more
 *
 * The methods below are CUSTOM queries derived from method names:
 *
 *   getByUserName("alice")
 *   → SELECT * FROM account_user WHERE user_name = 'alice'
 *
 *   getByEmail("alice@example.com")
 *   → SELECT * FROM account_user WHERE email = 'alice@example.com'
 *
 *   getByRole(Role.ADMIN)
 *   → SELECT * FROM account_user WHERE role = 'ADMIN'
 *
 *   findAll(Pageable) — returns a Page with pagination (page number, size, total count)
 *
 * Optional<User> means: returns empty() if no user found, instead of null.
 * This prevents NullPointerException — you check with .isPresent() or .orElseThrow().
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    @Override
    Optional<User> findById(UUID id);

    Optional<User> getByUserName(String username);

    Optional<User> getByEmail(String email);

    List<User> getByRole(Role role);

    @Override
    Page<User> findAll(Pageable pageable);
}
