package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.MatrixFunctions;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit
 */
public interface FloatMatrixFunctions<U extends Unit<U>> extends MatrixFunctions
{
    /**
     * @param row row for which to get the value for in the SI unit in which it has been stored.
     * @param column column for which to get the value for in the SI unit in which it has been stored.
     * @return value at row,column index.
     * @throws ValueException if row or column out of bounds.
     */
    float getSI(final int row, final int column) throws ValueException;

    /**
     * @param row row for which to get the value for in the original unit of creation.
     * @param column column for which to get the value for in the original unit of creation.
     * @return value at row,column index.
     * @throws ValueException if row or column out of bounds.
     */
    float getInUnit(final int row, final int column) throws ValueException;

    /**
     * @param row row for which to get the value for in the SI unit in which it has been stored.
     * @param column column for which to get the value for in the SI unit in which it has been stored.
     * @param targetUnit the unit for the result.
     * @return value at position index.
     * @throws ValueException if row or column out of bounds.
     */
    float getInUnit(final int row, final int column, U targetUnit) throws ValueException;

    /**
     * @param row row position to get the value that has been stored.
     * @param column column position to get the value that has been stored.
     * @return a strongly typed value from the cell
     * @throws ValueException if index < 0 or index >= vector.size().
     */
    FloatScalar<U> get(final int row, final int column) throws ValueException;

    /**
     * @param row row position to set the value in the SI unit in which it has been stored.
     * @param column column position to set the value in the SI unit in which it has been stored.
     * @param valueSI the value to store in the cell
     * @throws ValueException if index < 0 or index >= vector.size().
     */
    void setSI(final int row, final int column, float valueSI) throws ValueException;

    /**
     * @param row row position to set the value in the original unit of creation.
     * @param column column position to set the value in the original unit of creation.
     * @param value the strongly typed value to store in the cell
     * @throws ValueException if index < 0 or index >= vector.size().
     */
    void set(final int row, final int column, FloatScalar<U> value) throws ValueException;

    /**
     * @param row row position to set the value in the provided unit.
     * @param column column position to set the value in the provided unit.
     * @param value the value to store in the cell
     * @param valueUnit the unit of the value.
     * @throws ValueException if index < 0 or index >= vector.size().
     */
    void setInUnit(final int row, final int column, float value, U valueUnit) throws ValueException;

    /**
     * @return sum of all values within the matrix.
     */
    float zSum();

    /**
     * @return the determinant of the matrix
     * @throws ValueException if matrix is neither sparse, nor dense
     */
    float det() throws ValueException;
}
