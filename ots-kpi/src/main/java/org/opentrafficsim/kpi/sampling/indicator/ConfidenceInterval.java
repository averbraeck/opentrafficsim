package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;

/**
 * Wrapper class for two typed values that represent a confidence interval.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of the values
 */
public class ConfidenceInterval<T extends AbstractDoubleScalarRel<?, T>>
{

    /** Lower confidence value. */
    private final T lowerValue;

    /** Upper confidence value. */
    private final T upperValue;

    /**
     * @param lowerValue T; lower confidence value
     * @param upperValue T; upper confidence value
     */
    ConfidenceInterval(final T lowerValue, final T upperValue)
    {
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
    }

    /**
     * @return lowerValue.
     */
    public T getLowerValue()
    {
        return this.lowerValue;
    }

    /**
     * @return upperValue.
     */
    public T getUpperValue()
    {
        return this.upperValue;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ConfidenceInterval [lowerValue=" + this.lowerValue + ", upperValue=" + this.upperValue + "]";
    }

}
