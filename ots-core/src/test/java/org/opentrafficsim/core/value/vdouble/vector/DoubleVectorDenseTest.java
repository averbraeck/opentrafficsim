package org.opentrafficsim.core.value.vdouble.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
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
 * @version Jun 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleVectorDenseTest extends DoubleVectorTest
{

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVectorTest#createDoubleVector(double[],
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(final double[] in, final U u)
    {
        return new DoubleVector.Abs.Dense<U>(in, u);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVectorTest#createDoubleVectorAbs(org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs[])
     */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Abs<U> createDoubleVectorAbs(final DoubleScalar.Abs<U>[] in) throws ValueException
    {
        return new DoubleVector.Abs.Dense<U>(in);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVectorTest#createDoubleVector(double[],
     *      org.opentrafficsim.core.unit.Unit)
     */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(final double[] in, final U u)
    {
        return new DoubleVector.Rel.Dense<U>(in, u);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVectorTest#createDoubleVectorRel(org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel[])
     */
    @Override
    protected final <U extends Unit<U>> DoubleVector.Rel<U> createDoubleVectorRel(final DoubleScalar.Rel<U>[] in) throws ValueException
    {
        return new DoubleVector.Rel.Dense<U>(in);
    }

}
