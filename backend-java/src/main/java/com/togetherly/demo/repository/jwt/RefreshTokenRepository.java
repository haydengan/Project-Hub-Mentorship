package com.togetherly.demo.repository.jwt;

import com.togetherly.demo.model.auth.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for RefreshToken entities.
 *
 * CUSTOM QUERY METHODS:
 *
 *   deleteAllByExpireAtLessThan(now)
 *   → This uses a MANUAL JPQL query instead of a derived method name. Why?
 *     Because Spring Data's auto-generated deletes load each entity first
 *     (SELECT then DELETE one by one), which is slow for bulk cleanup.
 *     Writing JPQL directly does a single DELETE statement.
 *
 *   @Modifying — Required on any @Query that changes data (INSERT/UPDATE/DELETE).
 *     Without it, Spring assumes it's a SELECT and throws an error.
 *
 *   @Query("delete from RefreshToken r where r.expireAt < ?1")
 *     → "RefreshToken" here is the ENTITY name (not the table name).
 *     → "?1" means the first method parameter (dateTime).
 *     → JPQL (Java Persistence Query Language) looks like SQL but uses
 *       entity names and field names instead of table/column names.
 *
 *   getByIdAndExpireAtGreaterThan — same pattern as AccessTokenRepository.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Query("delete from RefreshToken r where r.expireAt < ?1")
    void deleteAllByExpireAtLessThan(Instant dateTime);

    Optional<RefreshToken> getByIdAndExpireAtGreaterThan(UUID id, Instant dateTime);
}
