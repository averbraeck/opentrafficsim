package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 26, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleMatrixSparseTest extends DoubleMatrixTest
{

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleMatrix.Abs<U> createDoubleMatrixAbs(final double[][] in, final U u)
            throws ValueException
    {
        return new DoubleMatrix.Abs.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleMatrix.Abs<U> createDoubleMatrixAbs(final DoubleScalar.Abs<U>[][] in)
            throws ValueException
    {
        return new DoubleMatrix.Abs.Sparse<U>(in);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleMatrix.Rel<U> createDoubleMatrixRel(final double[][] in, final U u)
            throws ValueException
    {
        return new DoubleMatrix.Rel.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleMatrix.Rel<U> createDoubleMatrixRel(final DoubleScalar.Rel<U>[][] in)
            throws ValueException
    {
        return new DoubleMatrix.Rel.Sparse<U>(in);
    }

}
