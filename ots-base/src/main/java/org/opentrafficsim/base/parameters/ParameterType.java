package org.opentrafficsim.base.parameters;

import java.io.Serializable;

import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.Type;
import org.opentrafficsim.base.parameters.constraint.Constraint;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> Type of the value.
 */
public class ParameterType<T> implements Serializable, Identifiable, Type<ParameterType<T>>
{

    /** */
    private static final long serialVersionUID = 20160400L;

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

    /**
     * Construct a new AbstractParameterType without default value and constraint.
     * @param id short name of the new AbstractParameterType
     * @param description description or full name of the new AbstractParameterType
     * @param valueClass class of the value of the new AbstractParameterType
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass)
    {
        this(id, description, valueClass, null, null, false);
    }

    /**
     * Construct a new AbstractParameterType without default value and with constraint.
     * @param id short name of the new AbstractParameterType
     * @param description description or full name of the new AbstractParameterType
     * @param valueClass class of the value of the new AbstractParameterType
     * @param constraint constraint that applies to the value of the new AbstractParameterType
     */
    public ParameterType(final String id, final String description, final Class<T> valueClass,
            final Constraint<? super T> constraint)
    {
        this(id, description, valueClass, null, constraint, false);
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
        this(id, description, valueClass, defaultValue, null, true);
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
        this(id, description, valueClass, defaultValue, constraint, true);
    }

    /**
     * Protected constructor with default value and constraint, which may check the default value.
     * @param id short name of the new AbstractParameterType
     * @param description description or full name of the new AbstractParameterType
     * @param valueClass class of the value of the new AbstractParameterType
     * @param defaultValue default value of the new AbstractParameterType
     * @param constraint constraint that applies to the value of the new AbstractParameterType
     * @param hasDefaultValue if true a check is performed to ensure that the default value is not null and does not violate the
     *            constraint
     */
    private ParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint<? super T> constraint, final boolean hasDefaultValue)
    {
        Throw.whenNull(id, "Id may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(valueClass, "Value class may not be null.");
        if (hasDefaultValue)
        {
            Throw.whenNull(defaultValue, "Default values of parameter types may not be null.");
        }
        this.id = id;
        this.description = description;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
        this.constraint = constraint;
        if (this.defaultValue != null)
        {
            try
            {
                checkConstraint(this.defaultValue);
            }
            catch (ParameterException pe)
            {
                throw new RuntimeException(
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
                throw new RuntimeException(
                        "Default value of parameter '" + getId() + "' does not comply with custom constraints.", pe);
            }
        }
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
     * Returns whether this parameter type has a default value.
     * @return true if this AbstractParameterType type has a default value; false if it does not have a default value
     */
    public final boolean hasDefaultValue()
    {
        return this.defaultValue != null;
    }

    /**
     * Retrieve the the default value of this AbstractParameterType.
     * @return the default value of this AbstractParameterType
     * @throws ParameterException if this AbstractParameterType does not have a default value
     */
    public final T getDefaultValue() throws ParameterException
    {
        Throw.when(null == this.defaultValue, ParameterException.class, "No default value was set for '%s'.", getId());
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

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.defaultValue == null) ? 0 : this.defaultValue.hashCode());
        result = prime * result + this.description.hashCode();
        result = prime * result + this.id.hashCode();
        result = prime * result + this.valueClass.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
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
        if (!this.id.equals(other.id))
        {
            return false;
        }
        if (!this.description.equals(other.description))
        {
            return false;
        }
        if (!this.valueClass.equals(other.valueClass))
        {
            return false;
        }
        if (this.defaultValue == null)
        {
            if (other.defaultValue != null)
            {
                return false;
            }
        }
        else if (!this.defaultValue.equals(other.defaultValue))
        {
            return false;
        }
        return true;
    }

    /**
     * Retrieve the constraint.
     * @return the constraint of this AbstractParameterType
     */
    public final Constraint<? super T> getConstraint()
    {
        return this.constraint;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "ParameterType [id=" + this.id + ", description=" + this.description + ", valueClass=" + this.valueClass + "]";
    }

}
