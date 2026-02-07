package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;

/**
 * Wrapper class for two typed values that represent a confidence interval.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of the values
 */
@Deprecated // non-scalar statistics not yet implemented
public class ConfidenceInterval<T extends DoubleScalarRel<?, T>>
{

    /** Lower confidence value. */
    private final T lowerValue;

    /** Upper confidence value. */
    private final T upperValue;

    /**
     * Constructor.
     * @param lowerValue lower confidence value
     * @param upperValue upper confidence value
     */
    public ConfidenceInterval(final T lowerValue, final T upperValue)
    {
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
    }

    /**
     * Returns lower value.
     * @return lowerValue.
     */
    public T getLowerValue()
    {
        return this.lowerValue;
    }

    /**
     * Returns upper value.
     * @return upperValue.
     */
    public T getUpperValue()
    {
        return this.upperValue;
    }

    @Override
    public final String toString()
    {
        return "ConfidenceInterval [lowerValue=" + this.lowerValue + ", upperValue=" + this.upperValue + "]";
    }

}
