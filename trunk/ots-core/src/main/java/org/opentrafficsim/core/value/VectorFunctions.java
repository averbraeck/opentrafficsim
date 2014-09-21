package org.opentrafficsim.core.value;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface VectorFunctions
{
    /**
     * normalize the vector, i.e. make the sum of all elements equal to 1.
     * @throws ValueException if the sum of the values is zero, and normalization is not possible
     */
    void normalize() throws ValueException;

    /**
     * @return the size of the vector as an int.
     */
    int size();

    /**
     * @return the number of cells having non-zero values; ignores tolerance.
     */
    int cardinality();

}
