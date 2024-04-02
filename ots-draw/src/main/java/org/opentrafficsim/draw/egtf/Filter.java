package org.opentrafficsim.draw.egtf;

/**
 * Interface for filtered data.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Filter
{

    /**
     * Returns the grid location.
     * @return double[]; grid location
     */
    double[] getLocation();

    /**
     * Returns the grid time.
     * @return double[]; grid time
     */
    double[] getTime();

    /**
     * Returns filtered data as SI values.
     * @param quantity Quantity&lt;?, ?&gt;; quantity
     * @return double[][]; filtered data as SI values
     */
    double[][] getSI(Quantity<?, ?> quantity);

    /**
     * Returns the filtered data in output format.
     * @param quantity Quantity&lt;?, K&gt;; quantity.
     * @return K; filtered data in output format
     * @param <K> output format.
     */
    <K> K get(Quantity<?, K> quantity);

}
