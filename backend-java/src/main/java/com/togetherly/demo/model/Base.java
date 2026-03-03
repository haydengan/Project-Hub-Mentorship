package com.togetherly.demo.model;

import com.togetherly.demo.model.generator.UUIDv7Id;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all entities. Provides a UUID primary key.
 *
 * ANNOTATION GUIDE:
 *
 * @MappedSuperclass — This class is NOT a table itself. It just provides fields
 *   that child classes (User, VerifyToken, etc.) will inherit into THEIR tables.
 *   Without this, JPA would ignore the `id` field in subclasses.
 *
 * @Getter/@Setter (Lombok) — Auto-generates only getters and setters.
 *   We intentionally DON'T use @Data here because @Data also generates
 *   equals(), hashCode(), and toString() — which are problematic for JPA:
 *     - equals()/hashCode() using the generated ID breaks before the entity is persisted
 *     - toString() can trigger lazy loading of related entities
 *   Child entities should define their own equals()/hashCode() using business keys.
 *
 * @Id — Marks this field as the primary key.
 *
 * @UUIDv7Id — Our custom annotation (see model/generator/UUIDv7Id.java).
 *   Tells Hibernate to use UUIDv7Generator to create IDs on insert.
 *   This is the Hibernate 7 way (replaces the removed @GenericGenerator).
 *
 * @Column(...) — Configures the DB column:
 *   unique=true     → adds a UNIQUE constraint
 *   updatable=false → prevents UPDATE from changing the ID after insert
 *   nullable=false  → adds a NOT NULL constraint
 */
@MappedSuperclass
@Getter
@Setter
public class Base {
    @Id
    @UUIDv7Id
    @Column(unique = true, updatable = false, nullable = false)
    protected UUID id;
}
