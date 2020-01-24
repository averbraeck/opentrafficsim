package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalarRel;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> the unit type
 * @param <T> type of the value
 */
public class PersistentStatistic<U extends Unit<U>, T extends AbstractDoubleScalarRel<U, T>>
{
    /** Wrapped persistent. */
    private final Persistent<U, T, ?> persistent;

    /**
     * Constructor.
     * @param persistent Persistent&lt;?,T,?&gt;; the persistent statistic that gathers the data
     */
    public PersistentStatistic(final Persistent<U, T, ?> persistent)
    {
        this.persistent = persistent.copy();
    }

    /**
     * @param alpha double; confidence level
     * @return both-side confidence interval
     */
    public ConfidenceInterval<T> getConfidenceInterval(final double alpha)
    {
        return this.persistent.getConfidenceInterval(alpha);
    }

    /**
     * @param alpha double; confidence level
     * @param side IntervalSide; side of confidence interval
     * @return confidence interval
     */
    public ConfidenceInterval<T> getConfidenceInterval(final double alpha, final IntervalSide side)
    {
        return this.persistent.getConfidenceInterval(alpha, side);
    }

    /**
     * @return sum.
     */
    public T getSum()
    {
        return this.persistent.getSum();
    }

    /**
     * @return min.
     */
    public T getMin()
    {
        return this.persistent.getMin();
    }

    /**
     * @return max.
     */
    public T getMax()
    {
        return this.persistent.getMax();
    }

    /**
     * @return mean.
     */
    public T getMean()
    {
        return this.persistent.getMean();
    }

    /**
     * @return stDev.
     */
    public T getStDev()
    {
        return this.persistent.getStDev();
    }

    /**
     * @return variance.
     */
    public double getVariance()
    {
        return this.persistent.getVariance();
    }

    /**
     * @return n.
     */
    public long getN()
    {
        return this.persistent.getN();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "PersistentStatistic [persistent=" + this.persistent + "]";
    }

}
