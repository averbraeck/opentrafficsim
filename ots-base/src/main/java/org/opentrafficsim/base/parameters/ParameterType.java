package org.opentrafficsim.base.parameters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.djutils.base.Identifiable;
import org.djutils.data.Column;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.base.collections.Indexed;
import org.opentrafficsim.base.collections.IndexedMap;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 * @param <T> Type of the value.
 */
public class ParameterType<T> implements Identifiable, Type<ParameterType<T>>, Indexed
{

    /** Indices store. */
    private static final Map<String, Integer> INDICES = new HashMap<>();

    /** Index supplier. */
    private static final AtomicInteger NEXT_INDEX = new AtomicInteger();

    /** Short name of parameter. */
    private final String id;

    /** Parameter description or full name. */
    private final String description;

    /** Default constraint. */
    private final Constraint<? super T> constraint;

    /** Class of the value. */
    private final Class<T> valueClass;

    /** Default value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final T defaultValue;

    /** Cached hash code. */
    private final int hashCode;

    /** Index. */
    private final int index;

    /**
     * Returns the highest index given to a parameter type so far. This is useful for pre-caching {@link IndexedMap} instances
     * containing parameter type keys.
     * @return the highest index given to a parameter type so far
     */
    public static int maxIndex()
    {
        return Math.max(0, NEXT_INDEX.get() - 1);
    }

    /**
     * Construct a new AbstractParameterType with default value, without constraint.
     * @param id short name of the new AbstractParameterType
     * @param description description or full name of the new AbstractParameterType
     * @param valueClass class of the value of the new AbstractParameterType
     * @param defaultValue default value of the new AbstractParameterType
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        this(id, description, valueClass, defaultValue, null);
    }

    /**
     * Construct a new AbstractParameterType with default value and constraint.
     * @param id short name of the new AbstractParameterType
     * @param description description or full name of the new AbstractParameterType
     * @param valueClass class of the value of the new AbstractParameterType
     * @param defaultValue default value of the new AbstractParameterType
     * @param constraint constraint that applies to the value of the new AbstractParameterType
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint<? super T> constraint)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(valueClass, "Value class may not be null.");
        Throw.whenNull(defaultValue, "Default values of parameter types may not be null.");
        this.id = id.intern();
        this.description = description;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        try
        {
            checkConstraint(this.defaultValue);
        }
        catch (ParameterException pe)
        {
            throw new OtsRuntimeException(
                    "Default value of parameter '" + this.id + "' does not comply with default constraints.", pe);
        }
        try
        {
            // Forward empty set of parameters. At creation time of parameter types, values cannot be checked with values of
            // other parameter types.
            check(defaultValue, new ParameterSet());
        }
        catch (ParameterException pe)
        {
            throw new OtsRuntimeException(
                    "Default value of parameter '" + getId() + "' does not comply with custom constraints.", pe);
        }
        this.hashCode = this.id.hashCode();
        this.index = INDICES.computeIfAbsent(this.id, (i) -> NEXT_INDEX.getAndIncrement());
    }

    /**
     * Retrieve the id of this AbstractParameterType.
     * @return the id of this AbstractParameterType
     */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the description of this AbstractParameterType.
     * @return the description of this AbstractParameterType
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * Retrieve the class of the value of this AbstractParameterType.
     * @return the class of the value of this AbstractParameterType
     */
    public final Class<T> getValueClass()
    {
        return this.valueClass;
    }

    /**
     * Retrieve the the default value of this AbstractParameterType.
     * @return the default value of this AbstractParameterType
     */
    public final T getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Check that a value complies with the constraint of this AbstractParameterType.
     * @param value the value to check
     * @throws ParameterException if the value does not comply with constraints
     */
    public final void checkConstraint(final T value) throws ParameterException
    {
        if (this.constraint == null)
        {
            return;
        }
        Throw.when(!this.constraint.accept(value), ParameterException.class, this.constraint.failMessage(), this.getId());
    }

    /**
     * Default implementation of check method. This default implementation will never throw any Exception.
     * @param value the value to check
     * @param params Set of parameters to check
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final T value, final Parameters params) throws ParameterException
    {
        // Default implementation does nothing
    }

    /**
     * Print the given value from the map in Parameters in a presentable format. The default implementation simply returns the
     * output of toString().
     * @param parameters Parameters to get the value from
     * @return readable representation of the value
     * @throws ParameterException If the parameter is not present
     */
    public String printValue(final Parameters parameters) throws ParameterException
    {
        return parameters.getParameter(this).toString();
    }

    /**
     * Returns a data column that can hold values of this parameter type.
     * @param unit unit, which may also be {@code null} or "-"
     * @return column that can hold values of this parameter type
     */
    public Column<T> getColumn(final String unit)
    {
        return new Column<T>(this.id, this.description, this.valueClass, unit);
    }

    @Override
    public int index()
    {
        return this.index;
    }

    @Override
    public int hashCode()
    {
        return this.hashCode;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ParameterType<?> other = (ParameterType<?>) obj;
        return this.id.equals(other.id);
    }

    /**
     * Retrieve the constraint.
     * @return the constraint of this AbstractParameterType
     */
    public final Constraint<? super T> getConstraint()
    {
        return this.constraint;
    }

    @Override
    public String toString()
    {
        return "ParameterType [id=" + this.id + ", description=" + this.description + ", valueClass=" + this.valueClass + "]";
    }

}
