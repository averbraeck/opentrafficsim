package org.opentrafficsim.core.value.vdouble.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.DenseData;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
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
 * @version Jun 18, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit
 */
public class DoubleVectorRelDense<U extends Unit<U>> extends DoubleVectorRel<U> implements DenseData
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @param unit the unit of the values
     */
    public DoubleVectorRelDense(final double[] values, final U unit)
    {
        super(values, unit);
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    public DoubleVectorRelDense(final DoubleScalarRel<U>[] values) throws ValueException
    {
        super(values);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#createMatrix1D(int)
     */
    protected final DoubleMatrix1D createMatrix1D(final int size)
    {
        return new DenseDoubleMatrix1D(size);
    }

    /**
     * @see org.opentrafficsim.core.value.vdouble.vector.DoubleVector#copy()
     */
    @Override
    public final DoubleVectorRelDense<U> copy()
    {
        DoubleVectorRelDense<U> v = new DoubleVectorRelDense<U>(this.vectorSI.toArray(), this.unit.getStandardUnit());
        v.unit = this.unit;
        return v;
    }

    /**
     * @return the internally stored vector from the Colt library, converted to SI units.
     */
    public final DenseDoubleMatrix1D getColtDenseDoubleMatrix1D()
    {
        return (DenseDoubleMatrix1D) this.vectorSI;
    }

}
