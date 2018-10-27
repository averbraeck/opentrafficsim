package org.opentrafficsim.base.means;

/**
 * Compute the geometric (weighted) mean of a set of values. Geometric mean can not handle negative or zero values.
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
public class GeometricMean<V extends Number, W extends Number> extends AbstractMean<GeometricMean<V, W>, V, W>
{

    /** {@inheritDoc} */
    @Override
    public final double getMean()
    {
        return Math.exp(getSum() / getSumOfWeights());
    }

    /** {@inheritDoc} */
    @Override
    public final GeometricMean<V, W> add(final V value, final W weight)
    {
        increment(Math.log(value.doubleValue()) * weight.doubleValue(), weight.doubleValue());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GeometricMean [current sum of logarithmic values=" + getSum() + ", current sum of weights="
                + getSumOfWeights() + ", current geometric mean=" + getMean() + "]";
    }

}
