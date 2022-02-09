package org.opentrafficsim.base.parameters.constraint;

import java.util.IllegalFormatException;

import org.djutils.exceptions.Throw;

/**
 * List of default constraint for ParameterTypes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 sep. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum NumericConstraint implements Constraint<Number>
{

    /** Checks for &gt;0. */
    POSITIVE("Value of parameter '%s' must be above zero.")
    {
        /** {@inheritDoc} */
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() > 0.0;
        }
    },

    /** Checks for &lt;0. */
    NEGATIVE("Value of parameter '%s' must be below zero.")
    {
        /** {@inheritDoc} */
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() < 0.0;
        }
    },

    /** Checks for &ge;0. */
    POSITIVEZERO("Value of parameter '%s' may not be below zero.")
    {
        /** {@inheritDoc} */
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() >= 0.0;
        }
    },

    /** Checks for &le;0. */
    NEGATIVEZERO("Value of parameter '%s' may not be above zero.")
    {
        /** {@inheritDoc} */
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() <= 0.0;
        }
    },

    /** Checks for &ne;0. */
    NONZERO("Value of parameter '%s' may not be zero.")
    {
        /** {@inheritDoc} */
        @Override
        public boolean accept(final Number value)
        {
            return value.doubleValue() != 0.0;
        }
    },

    /** Checks for &ge;1. */
    ATLEASTONE("Value of parameter '%s' may not be below one.")
    {
        /** {@inheritDoc} */
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
     * @param failMessage Message for value failure, pointing to a parameter using '%s'.
     */
    // @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
    NumericConstraint(final String failMessage)
    {
        Throw.whenNull(failMessage, "Default parameter constraint '%s' has null as fail message as given to the constructor,"
                + " which is not allowed.", this);
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
    @Override
    public String failMessage()
    {
        return this.failMessage;
    }

}
