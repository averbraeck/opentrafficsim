package org.opentrafficsim.core.egtf;

import java.util.Map;

/**
 * Class containing processed output data.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Filter
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
     * @param map Map; filtered data
     */
    Filter(final double[] location, final double[] time, final Map<Quantity<?, ?>, double[][]> map)
    {
        this.location = location;
        this.time = time;
        this.map = map;
    }

    /**
     * Returns the grid location.
     * @return double[]; grid location
     */
    public double[] getLocation()
    {
        return this.location;
    }

    /**
     * Returns the grid time.
     * @return double[]; grid time
     */
    public double[] getTime()
    {
        return this.time;
    }

    /**
     * Returns filtered data as SI values.
     * @param quantity Quantity; quantity
     * @return double[][]; filtered data as SI values
     */
    public double[][] getSI(final Quantity<?, ?> quantity)
    {
        return this.map.get(quantity);
    }

    /**
     * Returns the filtered data in output format.
     * @param quantity Quantity; quantity.
     * @return K; filtered data in output format
     * @param <K> output format.
     */
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
        return "Filter [location=" + this.location + ", time=" + this.time + "]";
    }

}
