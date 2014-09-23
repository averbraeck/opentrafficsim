package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * Methods that modify the data stored in a matrix.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of the matrix
 */
public interface WriteFloatMatrixFunctions<U extends Unit<U>>
{
    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param valueSI the value to store in the cell
     * @throws ValueException if row or column is invalid.
     */
    void setSI(int row, int column, float valueSI) throws ValueException;

    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param value FloatScalar; the value to store in the cell
     * @throws ValueException if row or column is invalid.
     */
    void set(int row, int column, FloatScalar<U> value) throws ValueException;

    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param value float; the value to store in the cell
     * @param valueUnit U; the unit of the provided value
     * @throws ValueException if row or column is invalid.
     */
    void setInUnit(int row, int column, float value, U valueUnit) throws ValueException;

    /**
     * normalize the matrix, i.e. make the sum of all elements equal to 1.
     * @throws ValueException if the sum of the values is zero, and normalization is not possible
     */
    void normalize() throws ValueException;

}
