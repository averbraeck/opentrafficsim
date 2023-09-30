package org.opentrafficsim.draw.egtf;

import java.util.Arrays;
import java.util.Map;

/**
 * Class containing processed output data.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FilterDouble implements Filter
{

    /** Grid locations of output data. */
    private final double[] location;

    /** Grid times of output data. */
    private final double[] time;

    /** Map of all filtered data. */
    private final Map<Quantity<?, ?>, double[][]> map;

    /**
     * Constructor.
     * @param location double[]; grid locations of output data
     * @param time double[]; grid times of output data
     * @param map Map&lt;Quantity&lt;?, ?&gt;, double[][]&gt;; filtered data
     */
    protected FilterDouble(final double[] location, final double[] time, final Map<Quantity<?, ?>, double[][]> map)
    {
        this.location = location;
        this.time = time;
        this.map = map;
    }

    /** {@inheritDoc} */
    @Override
    public double[] getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public double[] getTime()
    {
        return this.time;
    }

    /** {@inheritDoc} */
    @Override
    public double[][] getSI(final Quantity<?, ?> quantity)
    {
        return this.map.get(quantity);
    }

    /** {@inheritDoc} */
    @Override
    public <K> K get(final Quantity<?, K> quantity)
    {
        if (!this.map.containsKey(quantity))
        {
            throw new IllegalStateException(String.format("Filter does not contain data for quantity %s", quantity.getName()));
        }
        return quantity.convert(this.map.get(quantity));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Filter [location=" + Arrays.toString(this.location) + ", time=" + Arrays.toString(this.time) + "]";
    }

}
