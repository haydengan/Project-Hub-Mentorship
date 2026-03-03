package com.togetherly.demo.model.generator;

import java.lang.reflect.Member;
import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;

import com.fasterxml.uuid.Generators;

/**
 * Custom Hibernate ID generator that produces UUID v7 (time-based epoch).
 * - Implements BeforeExecutionGenerator
 * - Gets registered via the @UUIDv7Id annotation (which uses @IdGeneratorType)
 */
public class UUIDv7Generator implements BeforeExecutionGenerator {

    // This constructor is required by @IdGeneratorType.
    // Hibernate calls it automatically, passing the annotation instance and metadata.
    public UUIDv7Generator(UUIDv7Id config, Member idMember, GeneratorCreationContext context) {
        // No configuration needed — we always generate UUIDv7
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        // Generators.timeBasedEpochGenerator() comes from the java-uuid-generator library.
        // It creates a UUID v7: the first 48 bits are a Unix timestamp in milliseconds,
        // the rest is random. This means UUIDs sort chronologically.
        return Generators.timeBasedEpochGenerator().generate();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        // Only generate an ID on INSERT, not on UPDATE
        return EnumSet.of(EventType.INSERT);
    }
}
