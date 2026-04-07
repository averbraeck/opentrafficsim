package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

/**
 * Represents a single context datum within a {@link ContextCategory}.
 * <p>
 * This class acts as a generic container for atomic pieces of "world knowledge" in
 * <b>Layer 1 (Perception & Context)</b> of the MiRoVA architecture. It supports
 * any Java type, including strictly typed physical units (e.g., {@code Speed},
 * {@code Length} from DJUnits), primitives, or enumerations.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @param <T> the specific type of the stored context value
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public final class ContextValue<T> {

    /** The unique identifier key for this context value. */
    private final String key;

    /** The actual data value. */
    private final T value;

    /**
     * Initializes a new context value.
     *
     * @param key   the unique identifier key for this context datum
     * @param value the actual data value to store
     */
    public ContextValue(final String key, final T value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Retrieves the unique identifier key of this context value.
     *
     * @return the context key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Retrieves the actual data value.
     *
     * @return the stored context value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Returns a string representation of this context value.
     *
     * @return a string in the format "key=value"
     */
    @Override
    public String toString() {
        return this.key + "=" + this.value;
    }
}