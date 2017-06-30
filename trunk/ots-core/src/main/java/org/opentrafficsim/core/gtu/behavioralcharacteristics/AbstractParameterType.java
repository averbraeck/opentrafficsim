package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.Type;

import nl.tudelft.simulation.language.Throw;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of the value.
 */
public abstract class AbstractParameterType<T> extends Type<AbstractParameterType<?>> implements Serializable, Identifiable
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
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private final Class<T> valueClass;

    /** Default value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final T defaultValue;

    /**
     * Constructor without default value and constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass)
    {
        this(id, description, valueClass, null, null, false);
    }

    /**
     * Constructor without default value and constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param constraint Constraint to check the value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass,
            final Constraint<? super T> constraint)
    {
        this(id, description, valueClass, null, constraint, false);
    }

    /**
     * Constructor with default value, without constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        this(id, description, valueClass, defaultValue, null, true);
    }

    /**
     * Constructor with default value, without constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint to check the value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint<? super T> constraint)
    {
        this(id, description, valueClass, defaultValue, constraint, true);
    }

    /**
     * Protected constructor with default value and constraint, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint to check the value.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    protected AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
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
        }
    }

    /**
     * Returns the parameter id.
     * @return id Parameter id.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the parameter description.
     * @return description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the class of the value.
     * @return valueClass Class of the value.
     */
    public final Class<T> getValueClass()
    {
        return this.valueClass;
    }
    
    /**
     * Returns whether this parameter type has a default value.
     * @return Whether this parameter type has a default value.
     */
    public final boolean hasDefaultValue()
    {
        return this.defaultValue != null;
    }

    /**
     * Returns the default value.
     * @return defaultValue Default value.
     * @throws ParameterException If no default value was set.
     */
    public final T getDefaultValue() throws ParameterException
    {
        Throw.when(null == this.defaultValue, ParameterException.class, "No default value was set for '%s'.", getId());
        return this.defaultValue;
    }

    /**
     * Checks the default constraints given with the parameter type.
     * @param value The value to check.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public final void checkConstraint(final T value) throws ParameterException
    {
        if (this.constraint == null)
        {
            return;
        }
        Throw.when(this.constraint.fails(value), ParameterException.class, this.constraint.failMessage(), this.getId());
    }

    /**
     * Method to overwrite for checks with constraints.
     * @param value Value to check with constraints.
     * @param bc Set of behavioral characteristics.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(final T value, final BehavioralCharacteristics bc) throws ParameterException
    {
        //
    }

    /**
     * Print the given value from the map in BehavioralCharachteristics in a presentable format.
     * @param behavioralCharacteristics Behavioral characteristics to get the value from.
     * @return Printable string of value.
     * @throws ParameterException If the parameter is not present.
     */
    public abstract String printValue(BehavioralCharacteristics behavioralCharacteristics) throws ParameterException;

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
        AbstractParameterType<?> other = (AbstractParameterType<?>) obj;
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

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "AbstractParameterType [id=" + this.id + ", description=" + this.description + ", valueClass=" + this.valueClass
                + "]";
    }

}
