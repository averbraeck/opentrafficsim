package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Methods that modify the data stored in a DoubleMatrix.
 * <p>
 * This file was generated by the OpenTrafficSim value classes generator, 26 jun, 2015
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 26 jun, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of the matrix
 */
public interface WriteDoubleMatrixFunctions<U extends Unit<U>>
{
    /**
     * Replace the value at row, column by the supplied value which is expressed in the standard SI unit.
     * @param row int; row of the value to replace
     * @param column int; column of the value to replace
     * @param valueSI double; the value to store (expressed in the standard SI unit)
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    void setSI(int row, int column, double valueSI) throws ValueException;

    /**
     * Replace the value at row, column by the supplied value which is in a compatible unit.
     * @param row int; row of the value to replace
     * @param column int; column of the value to replace
     * @param value DoubleScalar&lt;U&gt;; the strongly typed value to store
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    void set(int row, int column, DoubleScalar<U> value) throws ValueException;

    /**
     * Replace the value at row, column by the supplied value which is expressed in a supplied (compatible) unit.
     * @param row int; row of the value to replace
     * @param column int; column of the value to replace
     * @param value double; the value to store (which is expressed in valueUnit)
     * @param valueUnit U; unit of the supplied value
     * @throws ValueException when row or column out of range (row &lt; 0 or row &gt;= rows() or column &lt; 0 or column
     *             &gt;= columns())
     */
    void setInUnit(int row, int column, double value, U valueUnit) throws ValueException;

    /**
     * Normalize the matrix, i.e. scale the values to make the sum equal to 1.
     * @throws ValueException when the sum of the values is zero and normalization is not possible
     */
    void normalize() throws ValueException;

}
