package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Methods that modify the data stored in a matrix.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
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
 * @version Sep 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit of the matrix
 */
public interface WriteDoubleMatrixFunctions<U extends Unit<U>>
{
    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param valueSI the value to store in the cell
     * @throws ValueException if row or column is invalid.
     */
    void setSI(int row, int column, double valueSI) throws ValueException;

    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param value DoubleScalar; the value to store in the cell
     * @throws ValueException if row or column is invalid.
     */
    void set(int row, int column, DoubleScalar<U> value) throws ValueException;

    /**
     * Store a value in a cell of this matrix.
     * @param row integer; row of the entry where the value must be stored
     * @param column integer; column of the entry where the value must be store
     * @param value double; the value to store in the cell
     * @param valueUnit U; the unit of the provided value
     * @throws ValueException if row or column is invalid.
     */
    void setInUnit(int row, int column, double value, U valueUnit) throws ValueException;

    /** 
     * normalize the matrix, i.e. make the sum of all elements equal to 1.
     * @throws ValueException if the sum of the values is zero, and normalization is not possible
     */
    void normalize() throws ValueException;

}
