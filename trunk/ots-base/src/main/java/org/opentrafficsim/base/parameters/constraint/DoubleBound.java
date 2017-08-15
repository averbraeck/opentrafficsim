package org.opentrafficsim.base.parameters.constraint;

import nl.tudelft.simulation.language.Throw;

import org.opentrafficsim.base.parameters.ParameterTypeNumeric.NumericRangeConstraint;

/**
 * Continuous constraints with upper and lower bound
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 14, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DoubleBound implements NumericRangeConstraint
{
    /** The lower bound of this NumericRangeConstraint. */
    private final Number lowerBound;

    /** The upper bound of this NumericRangeConstraint. */
    private final Number upperBound;

    /** Is the lower bound value valid? */
    private final boolean includesLowerBound;

    /** Is the upper bound value valid? */
    private final boolean includesUpperBound;

    /** Checks for range [0...1]. */
    public static final DoubleBound UNITINTERVAL = new DoubleBound(0, 1, true, true);

    /** {@inheritDoc} */
    @Override
    public final boolean fails(final Number value)
    {
        // System.out.println("double value check value=" + value + " " + this.failMessage());
        boolean result =
                value.doubleValue() < this.lowerBound.doubleValue() || value.doubleValue() > this.upperBound.doubleValue()
                        || (!this.includesLowerBound) && value == this.lowerBound || (!this.includesUpperBound)
                        && value == this.upperBound;
        // System.out.println("result is " + result);
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String failMessage()
    {
        return String.format("Value must be in range %s (%s) .. %s (%s)", this.lowerBound, this.includesLowerBound
                ? "inclusive" : "exclusive", this.upperBound, this.includesUpperBound ? "inclusive" : "exclusive");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean includesLowerBound()
    {
        return this.includesLowerBound;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean includesUpperBound()
    {
        return this.includesUpperBound;
    }

    /** {@inheritDoc} */
    @Override
    public final Number getLowerBound()
    {
        return this.lowerBound;
    }

    /** {@inheritDoc} */
    @Override
    public final Number getUpperBound()
    {
        return this.upperBound;
    }

    /**
     * Construct a new DoubleBound constraint.
     * @param lowerBound Number; lower bound of the constraint
     * @param upperBound Number; upper bound of the constraint
     * @param includesLowerBound boolean; if true; the lower bound value is valid; if false; the lower bound value is not valid
     * @param includesUpperBound boolean; if true; the upper bound value is valid; if false; the upper bound value is not valid
     * @throws IllegalArgumentException when no value exists that satisfies the range
     */
    public DoubleBound(final Number lowerBound, final Number upperBound, final boolean includesLowerBound,
            final boolean includesUpperBound) throws IllegalArgumentException
    {
        Throw.when(upperBound.doubleValue() < lowerBound.doubleValue(), IllegalArgumentException.class,
                "Invalid range (upper bound %f < lower bound)", upperBound, lowerBound);
        Throw.when((!includesLowerBound) && (!includesUpperBound) && lowerBound.doubleValue() == upperBound.doubleValue(),
                IllegalArgumentException.class, "Invalid range (upperBound == lowerBound and both ends of range are invalid)");
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.includesLowerBound = includesLowerBound;
        this.includesUpperBound = includesUpperBound;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DoubleBound [lowerBound=" + this.lowerBound + ", upperBound=" + this.upperBound + ", includesLowerBound="
                + this.includesLowerBound + ", includesUpperBound=" + this.includesUpperBound + "]";
    }

}
