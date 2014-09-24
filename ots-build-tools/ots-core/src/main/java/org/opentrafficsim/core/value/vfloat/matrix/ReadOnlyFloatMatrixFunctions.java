package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public interface ReadOnlyFloatMatrixFunctions<U extends Unit<U>>
{
    /**
     * @return the number of rows of the matrix.
     */
    int rows();

    /**
     * @return the number of columns of the matrix.
     */
    int columns();

    /**
     * @return the number of cells having non-zero values; ignores tolerance.
     */
    int cardinality();

    /**
     * Retrieve the value at a specified row and column in the SI unit in which is stored.
     * @param row integer; row of the value to retrieve
     * @param column integer; column to get the value to retrieve
     * @return float; value at position row, column.
     * @throws ValueException if row or column out of range.
     */
    float getSI(int row, int column) throws ValueException;

    /**
     * Retrieve the value at a specified row and column in the unit of creation.
     * @param row integer; row of the value to retrieve
     * @param column integer; column to get the value to retrieve
     * @return float; value at position row, column.
     * @throws ValueException if row or column out of range.
     */
    float getInUnit(int row, int column) throws ValueException;

    /**
     * Retrieve the value at a specified row and column in a specified unit.
     * @param row integer; row of the value to retrieve
     * @param column integer; column to get the value to retrieve
     * @param targetUnit U; the unit to express the value in
     * @return float; value at position row, column.
     * @throws ValueException if row or column out of range.
     */
    float getInUnit(int row, int column, U targetUnit) throws ValueException;

    /**
     * Retrieve the value at a specified row and column as a FloatScalar.
     * @param row integer; row of the value to retrieve
     * @param column integer; column to get the value to retrieve
     * @return FloatScalar; value at position row, column.
     * @throws ValueException if row or column out of range.
     */
    FloatScalar<U> get(int row, int column) throws ValueException;

    /**
     * @return sum of all values of the vector.
     */
    float zSum();

    /**
     * Compute the determinant of the matrix.
     * @return the determinant of the matrix
     * @throws ValueException if matrix is neither sparse, nor dense, or not square
     */
    float det() throws ValueException;
}
