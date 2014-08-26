import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.algo.SparseFloatAlgebra;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;

/**
 * Demonstrate the COLT Matrix must be sparse problem.
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
 * @version Aug 26, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SparseMatrixProblem
{

    /**
     * Execute the demo.
     * @param args String[]; not used
     */
    public static void main(String args[])
    {
        float[][] values = {{1, 2, 3}, {3, 5, 7}, {5, 10, 0}};
        Float determinant = null;

        FloatMatrix2D floatMatrix2D;
        floatMatrix2D = new SparseFloatMatrix2D(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
            for (int column = 0; column < values[row].length; column++)
                floatMatrix2D.set(row, column, values[row][column]);
        System.out.println("matrix: " + floatMatrix2D.toString());
        System.out.println("calling SparseFloatAlgebra().det(this.matrixSI)");
        try
        {
            determinant = new SparseFloatAlgebra().det(floatMatrix2D);
        }
        catch (IllegalArgumentException exception)
        {
            exception.printStackTrace();
        }
        System.out.println("determinant is " + determinant + " (should be 15.0)");
    }
}
