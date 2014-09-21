package org.opentrafficsim.core.value.vdouble.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleVectorSparseTest extends DoubleVectorTest
{

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(final double[] in, final U u)
    {
        return new DoubleVector.Abs.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(final DoubleScalar.Abs<U>[] in)
            throws ValueException
    {
        return new DoubleVector.Abs.Sparse<U>(in);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(final double[] in, final U u)
    {
        return new DoubleVector.Rel.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(final DoubleScalar.Rel<U>[] in)
            throws ValueException
    {
        return new DoubleVector.Rel.Sparse<U>(in);
    }

}
