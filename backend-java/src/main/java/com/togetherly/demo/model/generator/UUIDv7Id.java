package com.togetherly.demo.model.generator;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation that tells Hibernate: "use UUIDv7Generator to generate IDs."
 *
 * HOW IT WORKS:
 * - @IdGeneratorType(UUIDv7Generator.class) is a META-ANNOTATION from Hibernate 6.5+.
 *   It links this annotation to our generator class.
 *
 * - @Retention(RUNTIME) means this annotation is available at runtime
 *   (Hibernate reads it via reflection when the app starts).
 *
 * - @Target({FIELD, METHOD}) means you can put this on a field or getter method.
 *
 * USAGE (in entity classes):
 *   @Id
 *   @UUIDv7Id        <-- just this one annotation, replaces 2 old ones
 *   private UUID id;
 *
 * OLD WAY (deprecated):
 *   @Id
 *   @GeneratedValue(generator = "UUIDv7")
 *   @GenericGenerator(name = "UUIDv7", type = UUIDv7Generator.class)
 *   private UUID id;
 */
@IdGeneratorType(UUIDv7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface UUIDv7Id {
}
