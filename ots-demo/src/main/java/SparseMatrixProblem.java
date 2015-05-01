import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.algo.SparseFloatAlgebra;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;

/**
 * Demonstrate the COLT Matrix must be sparse problem.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 26, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class SparseMatrixProblem
{
    /**
     * This class should never be instantiated.
     */
    private SparseMatrixProblem()
    {
        // This class should never be instantiated.
    }

    /**
     * Execute the demo.
     * @param args String[]; not used
     */
    public static void main(final String[] args)
    {
        float[][] values = {{1, 2, 3}, {3, 5, 7}, {5, 10, 0}};
        Float determinant = null;

        FloatMatrix2D floatMatrix2D = new SparseFloatMatrix2D(values.length, values[0].length);
        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[row].length; column++)
            {
                floatMatrix2D.set(row, column, values[row][column]);
            }
        }
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
