package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;
import java.util.IllegalFormatException;

import org.djunits.value.vdouble.scalar.DoubleScalarInterface;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.Type;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
public abstract class AbstractParameterType<T extends DoubleScalarInterface> extends Type<AbstractParameterType<?>>
        implements Serializable, Identifiable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    /** Short name of parameter. */
    private final String id;

    /** Parameter description or full name. */
    private final String description;

    /** Class of the value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Class<T> valueClass;

    /** Default value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final T defaultValue;

    /** Default constraint. */
    private final Constraint constraint;

    /** List of default constraint for ParameterTypes. */
    public enum Constraint
    {

        /** Checks for &gt;0. */
        POSITIVE("Value of parameter '%s' must be above zero.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value <= 0.0;
            }
        },

        /** Checks for &lt;0. */
        NEGATIVE("Value of parameter '%s' must be below zero.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value >= 0.0;
            }
        },

        /** Checks for &ge;0. */
        POSITIVEZERO("Value of parameter '%s' may not be below zero.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value < 0.0;
            }
        },

        /** Checks for &le;0. */
        NEGATIVEZERO("Value of parameter '%s' may not be above zero.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value > 0.0;
            }
        },

        /** Checks for &ne;0. */
        NONZERO("Value of parameter '%s' may not be zero.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value == 0.0;
            }
        },

        /** Checks for range [0...1]. */
        UNITINTERVAL("Value of parameter '%s' must be in range [0...1]")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value < 0.0 || value > 1.0;
            }
        },

        /** Checks for &ge;1. */
        ATLEASTONE("Value of parameter '%s' may not be below one.")
        {
            /** {@inheritDoc} */
            @Override
            boolean fails(final double value)
            {
                return value < 1.0;
            }
        };

        /** Message for value failure, pointing to a parameter using '%s'. */
        private final String failMessage;

        /**
         * Constructor with message for value failure, pointing to a parameter using '%s'.
         * @param failMessage Message for value failure, pointing to a parameter using '%s'.
         */
        @SuppressWarnings("redundantmodifier")
        @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
        Constraint(final String failMessage)
        {
            Throw.whenNull(failMessage,
                    "Default parameter constraint '%s' has null as fail message as given to the constructor,"
                            + " which is not allowed.",
                    this);
            try
            {
                // return value can be ignored
                String.format(failMessage, "dummy");
            }
            catch (IllegalFormatException ife)
            {
                throw new RuntimeException("Default parameter constraint " + this.toString()
                        + " has an illegal formatting of the fail message as given to the constructor."
                        + " It should contain a single '%s'.", ife);
            }
            this.failMessage = failMessage;
        }

        /**
         * Returns a message for value failure, pointing to a parameter using '%s'.
         * @return Message for value failure, pointing to a parameter using '%s'.
         */
        String failMessage()
        {
            return this.failMessage;
        }

        /**
         * Checks whether the value fails to comply with constraints.
         * @param value Value to check.
         * @return Whether the value fails to comply with constraints.
         */
        abstract boolean fails(double value);

    }

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
     * Constructor without default value, with constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param constraint Constraint for parameter values.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass,
            final Constraint constraint)
    {
        this(id, description, valueClass, null, constraint, false);
    }

    /**
     * Constructor with default value and constraint.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint constraint)
    {
        this(id, description, valueClass, defaultValue, constraint, true);
    }

    /**
     * Protected constructor with default value and constraint, which may check the default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Check for parameter values.
     * @param hasDefaultValue Whether to check the default value for null.
     */
    protected AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final Constraint constraint, final boolean hasDefaultValue)
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
    public abstract Object getDefaultValue() throws ParameterException;

    /**
     * Checks the default constraints given with the parameter type.
     * @param value The value to check.
     * @throws ParameterException If the value does not comply with constraints.
     */
    protected final void checkConstraint(final T value) throws ParameterException
    {
        if (this.constraint == null)
        {
            return;
        }
        Throw.when(this.constraint.fails(value.getSI()), ParameterException.class, this.constraint.failMessage(), this.id);
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
        result = prime * result + ((this.constraint == null) ? 0 : this.constraint.hashCode());
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
        if (this.constraint != other.constraint)
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
