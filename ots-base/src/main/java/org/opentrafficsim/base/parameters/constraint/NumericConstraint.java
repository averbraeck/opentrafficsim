package org.opentrafficsim.base.parameters.constraint;

/**
 * List of default constraint for ParameterTypes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum NumericConstraint implements Constraint<Number>
{

    /** Checks for &gt;0. */
    POSITIVE("Value of parameter '%s' must be above zero.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() > 0.0;
        }
    },

    /** Checks for &lt;0. */
    NEGATIVE("Value of parameter '%s' must be below zero.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() < 0.0;
        }
    },

    /** Checks for &ge;0. */
    POSITIVEZERO("Value of parameter '%s' may not be below zero.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() >= 0.0;
        }
    },

    /** Checks for &le;0. */
    NEGATIVEZERO("Value of parameter '%s' may not be above zero.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() <= 0.0;
        }
    },

    /** Checks for &ne;0. */
    NONZERO("Value of parameter '%s' may not be zero.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() != 0.0;
        }
    },

    /** Checks for &ge;1. */
    ATLEASTONE("Value of parameter '%s' may not be below one.")
    {
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() >= 1.0;
        }
    };

    /** Message for value failure, pointing to a parameter using '%s'. */
    private final String failMessage;

    /**
     * Constructor with message for value failure, pointing to a parameter using '%s'.
     * @param failMessage Message for value failure, pointing to a parameter using '%s'
     */
    NumericConstraint(final String failMessage)
    {
        this.failMessage = failMessage;
    }

    @Override
    public String failMessage()
    {
        return this.failMessage;
    }

}
