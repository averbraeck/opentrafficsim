package org.opentrafficsim.core.value.vdouble.matrix;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

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
 * @version Jul 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DoubleMatrixSparseTest extends DoubleMatrixTest
{

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixTest#createDoubleMatrixAbs(double[][], org.opentrafficsim.core.unit.Unit)
     */
    @Override
    protected <U extends Unit<U>> DoubleMatrixAbs<U> createDoubleMatrixAbs(double[][] in, U u) throws ValueException
    {
        return new DoubleMatrixAbsSparse<U>(in, u);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixTest#createDoubleMatrixAbs(org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs[][])
     */
    @Override
    protected <U extends Unit<U>> DoubleMatrixAbs<U> createDoubleMatrixAbs(DoubleScalarAbs<U>[][] in)
            throws ValueException
    {
        return new DoubleMatrixAbsSparse<U>(in);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixTest#createDoubleMatrixRel(double[][], org.opentrafficsim.core.unit.Unit)
     */
    @Override
    protected <U extends Unit<U>> DoubleMatrixRel<U> createDoubleMatrixRel(double[][] in, U u) throws ValueException
    {
        return new DoubleMatrixRelSparse<U>(in, u);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.matrix.DoubleMatrixTest#createDoubleMatrixRel(org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel[][])
     */
    @Override
    protected <U extends Unit<U>> DoubleMatrixRel<U> createDoubleMatrixRel(DoubleScalarRel<U>[][] in)
            throws ValueException
    {
        return new DoubleMatrixRelSparse<U>(in);
    }

}
