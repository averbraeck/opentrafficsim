package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;

/**
 * Wrapper class for two typed values that represent a confidence interval.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
