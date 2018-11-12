package org.opentrafficsim.core.egtf;

/**
 * Interface for filtered data.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param quantity Quantity; quantity
     * @return double[][]; filtered data as SI values
     */
    double[][] getSI(Quantity<?, ?> quantity);

    /**
     * Returns the filtered data in output format.
     * @param quantity Quantity; quantity.
     * @return K; filtered data in output format
     * @param <K> output format.
     */
    <K> K get(Quantity<?, K> quantity);

}
