package org.opentrafficsim.core.value.vfloat.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX
 * Delft, the Netherlands. All rights reserved.
 * 
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/">
 * www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is"
 * and any express or implied warranties, including, but not limited to, the
 * implied warranties of merchantability and fitness for a particular purpose
 * are disclaimed. In no event shall the copyright holder or contributors be
 * liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of
 * substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in
 * contract, strict liability, or tort (including negligence or otherwise)
 * arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * 
 * @version Sep 9, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <U> Unit
 */
public interface  ReadOnlyFloatMatrixFunctions<U extends Unit<U>>
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
