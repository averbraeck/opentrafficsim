package org.opentrafficsim.core.value.vfloat.vector;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Dense;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarRel;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix1D;

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
public class FloatVectorRelDense<U extends Unit<U>> extends FloatVectorRel<U> implements Dense
{
    /** */
    private static final long serialVersionUID = 20140618L;

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @param unit the unit of the values
     */
    public FloatVectorRelDense(final float[] values, final U unit)
    {
        super(values, unit);
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @throws ValueException exception thrown when array with zero elements is offered
     */
    public FloatVectorRelDense(final FloatScalarRel<U>[] values) throws ValueException
    {
        super(values);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.FloatVector#createMatrix1D(int)
     */
    protected final FloatMatrix1D createMatrix1D(final int size)
    {
        return new DenseFloatMatrix1D(size);
    }

    /**
     * @see org.opentrafficsim.core.value.vfloat.vector.FloatVector#copy()
     */
    @Override
    public final FloatVector<U> copy()
    {
        FloatVectorRelDense<U> v = new FloatVectorRelDense<U>(this.vectorSI.toArray(), this.unit.getStandardUnit());
        v.unit = this.unit;
        return v;
    }

    /**
     * @return the internally stored vector from the Colt library, converted to SI units.
     */
    public final DenseFloatMatrix1D getColtDenseFloatMatrix1D()
    {
        return (DenseFloatMatrix1D) this.vectorSI;
    }

}
