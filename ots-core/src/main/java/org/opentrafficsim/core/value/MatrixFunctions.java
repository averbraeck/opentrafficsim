package org.opentrafficsim.core.value;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface MatrixFunctions
{
    /**
     * normalize the matrix, i.e. make the sum of all elements equal to 1.
     * @throws ValueException if the sum of the values is zero, and normalization is not possible
     */
    void normalize() throws ValueException;

    /**
     * @return the number of rows of the matrix as an int.
     */
    int rows();

    /**
     * @return the number of columns of the matrix as an int.
     */
    int columns();

    /**
     * @return the number of cells having non-zero values; ignores tolerance.
     */
    int cardinality();
}
