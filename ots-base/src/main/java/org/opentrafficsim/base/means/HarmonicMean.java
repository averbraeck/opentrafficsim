package org.opentrafficsim.base.means;

/**
 * Compute the harmonic (weighted) mean of a set of values.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 26, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <V> value type
 * @param <W> weight type
 */
public class HarmonicMean<V extends Number, W extends Number> extends AbstractMean<HarmonicMean<V, W>, V, W>
{

    /** {@inheritDoc} */
    @Override
    public final double getMean()
    {
        return getSumOfWeights() / getSum();
    }

    /** {@inheritDoc} */
    @Override
    public final HarmonicMean<V, W> add(final V value, final W weight)
    {
        increment(weight.doubleValue() / value.doubleValue(), weight.doubleValue());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HarmonicMean [current sum of reciprocal values=" + getSum() + ", current sum of weights="
                + getSumOfWeights() + ", current harmonic mean=" + getMean() + "]";
    }

}
