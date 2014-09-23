package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FloatVectorSparseTest extends FloatVectorTest
{

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> FloatVector.Abs<U> createFloatVectorAbs(final float[] in, final U u)
    {
        return new FloatVector.Abs.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> FloatVector.Abs<U> createFloatVectorAbs(final FloatScalar.Abs<U>[] in)
            throws ValueException
    {
        return new FloatVector.Abs.Sparse<U>(in);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> FloatVector.Rel<U> createFloatVectorRel(final float[] in, final U u)
    {
        return new FloatVector.Rel.Sparse<U>(in, u);
    }

    /** {@inheritDoc} */
    @Override
    protected final <U extends Unit<U>> FloatVector.Rel<U> createFloatVectorRel(final FloatScalar.Rel<U>[] in)
            throws ValueException
    {
        return new FloatVector.Rel.Sparse<U>(in);
    }

}
