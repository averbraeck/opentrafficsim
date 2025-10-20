package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

/**
 * Represents a single context datum within a {@link ContextCategory}.
 * <p>
 * Supports any Java type including typed units (e.g., {@code Speed}, {@code Length}),
 * primitives, or enumerations.
 * </p>
 */
public final class ContextValue<T> {

    private final String key;
    private final T value;

    public ContextValue(final String key, final T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return this.key; }
    public T getValue() { return this.value; }

    @Override
    public String toString() {
        return this.key + "=" + this.value;
    }
}
