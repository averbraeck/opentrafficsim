import cern.colt.matrix.tfloat.FloatMatrix2D;
import cern.colt.matrix.tfloat.algo.DenseFloatAlgebra;
import cern.colt.matrix.tfloat.algo.SparseFloatAlgebra;
import cern.colt.matrix.tfloat.algo.decomposition.DenseFloatLUDecomposition;
import cern.colt.matrix.tfloat.algo.decomposition.SparseFloatLUDecomposition;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseCCFloatMatrix2D;

/**
 * Demonstrate the determinant has wrong sign problem.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version6 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class DeterminantProblem
{
    /**
     * This class should never be instantiated.
     */
    private DeterminantProblem()
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

        FloatMatrix2D sparseFloatMatrix2D = new SparseCCFloatMatrix2D(values.length, values[0].length);
        FloatMatrix2D denseFloatMatrix2D = new DenseFloatMatrix2D(values.length, values[0].length);

        for (int row = 0; row < values.length; row++)
        {
            for (int column = 0; column < values[row].length; column++)
            {
                sparseFloatMatrix2D.set(row, column, values[row][column]);
                denseFloatMatrix2D.set(row, column, values[row][column]);
            }
        }
        System.out.println("sparse matrix: " + sparseFloatMatrix2D.toString());
        float sparseDeterminant = new SparseFloatAlgebra().det(sparseFloatMatrix2D);
        System.out.println("determinant returned by det() is " + sparseDeterminant
                + " (prints -15.0; correct value is 15.0)");
        System.out.println("");
        System.out.println("dense matrix: " + denseFloatMatrix2D.toString());
        float denseDeterminant = new DenseFloatAlgebra().det(denseFloatMatrix2D);
        System.out.println("determinant returned by det() is " + denseDeterminant + " (prints 15.0 which is OK)");

        System.out.println("");
        System.out.println("The L and U matrices of the LU decompositions look fine and are identical (except for "
                + "one being sparse and the other dense).");
        SparseFloatLUDecomposition sparseLU = new SparseFloatLUDecomposition(sparseFloatMatrix2D, 0, true);
        DenseFloatLUDecomposition denseLU = new DenseFloatLUDecomposition(denseFloatMatrix2D);

        System.out.println("sparse L: " + sparseLU.getL());
        System.out.println("dense L: " + denseLU.getL());
        System.out.println("");
        System.out.println("sparse U: " + sparseLU.getU());
        System.out.println("dense U: " + denseLU.getU());

        sparseLU.det();
        denseLU.det();
    }

}
