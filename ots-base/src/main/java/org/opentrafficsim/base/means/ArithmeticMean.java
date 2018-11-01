package org.opentrafficsim.base.means;

/**
 * Compute arithmetic (weighted) mean of a set of values.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 26, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <V> value type
 * @param <W> weight type
 */
public class ArithmeticMean<V extends Number, W extends Number> extends AbstractMean<ArithmeticMean<V, W>, V, W>
{
    /** {@inheritDoc} */
    @Override
    public final double getMean()
    {
        return getSum() / getSumOfWeights();
    }

    /** {@inheritDoc} */
    @Override
    public final ArithmeticMean<V, W> addImpl(final V value, final Number weight)
    {
        increment(weight.doubleValue() * value.doubleValue(), weight.doubleValue());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ArithmeticMean [current sum=" + getSum() + ", current sum of weights=" + getSumOfWeights()
                + ", current arithmetic mean=" + getMean() + "]";
    }

}
