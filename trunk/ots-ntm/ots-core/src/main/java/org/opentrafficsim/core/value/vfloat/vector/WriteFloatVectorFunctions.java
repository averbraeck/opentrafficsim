package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * Methods that modify the data stored in a vector.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of the vector
 */
public interface WriteFloatVectorFunctions<U extends Unit<U>>
{
    /**
     * @param index position to set the value in the SI unit in which it has been stored.
     * @param valueSI the value to store in the cell
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    void setSI(int index, float valueSI) throws ValueException;

    /**
     * @param index position to set the value in the original unit of creation.
     * @param value the strongly typed value to store in the cell
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    void set(int index, FloatScalar<U> value) throws ValueException;

    /**
     * @param index position to set the value in the provided unit.
     * @param value the value to store in the cell
     * @param valueUnit the unit of the value.
     * @throws ValueException if index &lt; 0 or index &gt;= vector.size().
     */
    void setInUnit(int index, float value, U valueUnit) throws ValueException;

    /**
     * normalize the vector, i.e. make the sum of all elements equal to 1.
     * @throws ValueException if the sum of the values is zero, and normalization is not possible
     */
    void normalize() throws ValueException;

}
