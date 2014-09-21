package org.opentrafficsim.core.value.vdouble.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> Unit of the vector
 */
public interface ReadOnlyDoubleVectorFunctions<U extends Unit<U>>
{
    /**
     * @return the size of the vector as an int.
     */
    int size();

    /**
     * @return the number of cells having non-zero values; ignores tolerance.
     */
    int cardinality();

    /**
     * @param index position to get the value for in the SI unit in which it has been stored.
     * @return value at position index.
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    double getSI(int index) throws ValueException;

    /**
     * @param index position to get the value for in the original unit of creation.
     * @return value at position index.
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    double getInUnit(int index) throws ValueException;

    /**
     * @param index position to get the value for in the SI unit in which it has been stored.
     * @param targetUnit the unit for the result.
     * @return value at position index.
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    double getInUnit(int index, U targetUnit) throws ValueException;

    /**
     * @param index index position to get the value that has been stored.
     * @return a strongly typed value from the cell
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    DoubleScalar<U> get(int index) throws ValueException;

    /**
     * @return sum of all values of the vector.
     */
    double zSum();

}
