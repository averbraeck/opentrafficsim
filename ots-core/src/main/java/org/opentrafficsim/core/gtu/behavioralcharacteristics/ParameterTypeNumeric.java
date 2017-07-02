package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;
import java.util.IllegalFormatException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.simulation.language.Throw;

/**
 * Wrapper class for parameters of any quantity in JUnits, or double, integer, etc.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Class of the value.
 */
public class ParameterTypeNumeric<T extends Number> extends AbstractParameterType<T> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160400L;

    

    /** List of default constraint for ParameterTypes. */
    public enum NumericConstraint implements Constraint<Number>
    {

        /** Checks for &gt;0. */
        POSITIVE("Value of parameter '%s' must be above zero.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() <= 0.0;
            }
        },

        /** Checks for &lt;0. */
        NEGATIVE("Value of parameter '%s' must be below zero.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() >= 0.0;
            }
        },

        /** Checks for &ge;0. */
        POSITIVEZERO("Value of parameter '%s' may not be below zero.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() < 0.0;
            }
        },

        /** Checks for &le;0. */
        NEGATIVEZERO("Value of parameter '%s' may not be above zero.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() > 0.0;
            }
        },

        /** Checks for &ne;0. */
        NONZERO("Value of parameter '%s' may not be zero.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() == 0.0;
            }
        },

        /** Checks for range [0...1]. */
        UNITINTERVAL("Value of parameter '%s' must be in range [0...1]")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() < 0.0 || value.doubleValue() > 1.0;
            }
        },

        /** Checks for &ge;1. */
        ATLEASTONE("Value of parameter '%s' may not be below one.")
        {
            /** {@inheritDoc} */
            @Override
            public boolean fails(final Number value)
            {
                return value.doubleValue() < 1.0;
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
        NumericConstraint(final String failMessage)
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
        public String failMessage()
        {
            return this.failMessage;
        }

    }

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass)
    {
        super(id, description, valueClass);
    }

    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        super(id, description, valueClass, defaultValue);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass,
            final NumericConstraint constraint)
    {
        super(id, description, valueClass, constraint);
    }

    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param constraint Constraint for parameter values.
     */
    public ParameterTypeNumeric(final String id, final String description, final Class<T> valueClass, final T defaultValue,
            final NumericConstraint constraint)
    {
        super(id, description, valueClass, defaultValue, constraint);
    }

    /** {@inheritDoc} */
    public String printValue(final BehavioralCharacteristics behavioralCharacteristics) throws ParameterException
    {
        return behavioralCharacteristics.getParameter(this).toString();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "ParameterTypeNumeric [id=" + getId() + ", description=" + getDescription() + "]";
    }

}
