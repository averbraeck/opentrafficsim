package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * Methods that operate on FloatMatrix but do not modify the contents of the FloatMatrix.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of the matrix
 */
public interface ReadOnlyFloatMatrixFunctions<U extends Unit<U>>
{
    /**
     * Retrieve the number of rows of the matrix.
     * @return int; the number of rows of the matrix
     */
    int rows();

    /**
     * Retrieve the number of columns of the matrix.
     * @return int; the number of columns of the matrix
     */
    int columns();

    /**
     * Count the number of cells that have a non-zero value (ignores tolerance).
     * @return int; the number of cells having non-zero value
     */
    int cardinality();

    /**
     * Retrieve the value stored at a specified row and column in the standard SI unit.
     * @param row int; row of the value to retrieve
     * @param column int; column of the value to retrieve
     * @return float; value at position row, column in the standard SI unit
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    float getSI(int row, int column) throws ValueException;

    /**
     * Retrieve the value stored at a specified row and column in the original unit.
     * @param row int; row of the value to retrieve
     * @param column int; column of the value to retrieve
     * @return float; value at position row, column in the original unit
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    float getInUnit(int row, int column) throws ValueException;

    /**
     * Retrieve the value stored at a specified row and column converted into a specified unit.
     * @param row int; row of the value to retrieve
     * @param column int; column of the value to retrieve
     * @param targetUnit U; the unit for the result
     * @return float; value at position row, column converted into the specified unit
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    float getInUnit(int row, int column, U targetUnit) throws ValueException;

    /**
     * Retrieve the value stored at a specified row and column as a FloatScalar.
     * @param row int; row of the value to retrieve
     * @param column int; column of the value to retrieve
     * @return FloatScalar&lt;U&gt;; the strongly typed value of the selected cell
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    FloatScalar<U> get(int row, int column) throws ValueException;

    /**
     * Compute the sum of all values of this matrix.
     * @return float; the sum of all values of this matrix
     */
    float zSum();

    /**
     * Compute the determinant of the matrix.
     * @return float; the determinant of the matrix
     * @throws ValueException when matrix is neither sparse, nor dense, or not square
     */
    float det() throws ValueException;
    
}
