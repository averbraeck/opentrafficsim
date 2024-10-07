package org.opentrafficsim.kpi.sampling.indicator;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <U> the unit type
 * @param <T> type of the value
 */
public class PersistentStatistic<U extends Unit<U>, T extends DoubleScalarRel<U, T>>
{
    /** Wrapped persistent. */
    private final Persistent<U, T, ?> persistent;

    /**
     * Constructor.
     * @param persistent the persistent statistic that gathers the data
     */
    public PersistentStatistic(final Persistent<U, T, ?> persistent)
    {
        this.persistent = persistent.copy();
    }

    /**
     * @param alpha confidence level
     * @return both-side confidence interval
     */
    public ConfidenceInterval<T> getConfidenceInterval(final double alpha)
    {
        return this.persistent.getConfidenceInterval(alpha);
    }

    /**
     * @param alpha confidence level
     * @param side side of confidence interval
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
