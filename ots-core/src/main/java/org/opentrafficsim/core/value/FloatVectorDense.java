package org.opentrafficsim.core.value;

import org.opentrafficsim.core.unit.Unit;

import cern.colt.function.tfloat.FloatFloatFunction;
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
 * @version Jun 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Peter Knoppers</a>
 * @param <U> The unit for this value type
 * @param <A> absolute or relative value
 */
public /*abstract*/ class FloatVectorDense<U extends Unit<U>, A extends AbsoluteRelative> /*implements FloatFunctions*/
{
    /** */
    private static final long serialVersionUID = 20140615L;

    /** the internal storage for the vector; internally they are stored in SI units */
    private DenseFloatMatrix1D vectorSI;

    /** the unit */
    protected final U unit;

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     * @param unit the unit of the values
     */
    public FloatVectorDense(float[] values, final U unit)
    {
        if (unit.equals(unit.getStandardUnit()))
        {
            this.vectorSI = new DenseFloatMatrix1D(values);
        }
        else
        {
            this.vectorSI = new DenseFloatMatrix1D(values.length);
            float conversionFactor = (float) unit.getConversionFactorToStandardUnit();
            for (int index = 0; index < values.length; index++)
            {
                this.vectorSI.set(index, values[index] * conversionFactor);
            }
        }
        this.unit = unit;
    }

    /**
     * Construct the vector and store the values in SI units.
     * @param values an array of values for the constructor
     */
    public FloatVectorDense(FloatScalar<U>[] values)
    {
        this.vectorSI = new DenseFloatMatrix1D(values.length);
        if (values.length > 0)
        {
            for (int index = 0; index < values.length; index++)
            {
                this.vectorSI.set(index, values[index].getValueSI());
            }
            this.unit = values[0].getUnit();
        }
        else
        {
            this.unit = null;
            // TODO: error: vector with 0 elements
        }
    }

    public String toString()
    {
        return "[" + this.unit + "] " + this.vectorSI.toString();
    }

    /**
     * @return the internally stored vector from the Colt library, converted to SI units.
     */
    public DenseFloatMatrix1D getColtDenseFloatMatrix1D()
    {
        return this.vectorSI;
    }

    /**
     * Add a vector with absolute values x and a vector with relative values y.
     * @param x absolute vector 1
     * @param y relative vector 2
     * @param targetUnit unit in which the results will be displayed
     * @return new Vector with absolute elements sum of x[i] and y[i]
     */
    public static <U extends Unit<U>, A extends Absolute, R extends Relative> FloatVectorDense<U, A> addAR(
            FloatVectorDense<U, A> x, FloatVectorDense<U, R> y, U targetUnit)
    {
        DenseFloatMatrix1D c =
                (DenseFloatMatrix1D) new DenseFloatMatrix1D(x.getColtDenseFloatMatrix1D().toArray()).assign(
                        y.getColtDenseFloatMatrix1D(), new PlusUnitFunction<U>(x.getUnit(), y.getUnit(), targetUnit));
        return new FloatVectorDense<U, A>(c.toArray(), targetUnit);
    }

    /**
     * Add a vector with relative values x and a vector with absolute values y.
     * @param x absolute vector 1
     * @param y relative vector 2
     * @param targetUnit unit in which the results will be displayed
     * @return new Vector with absolute elements sum of x[i] and y[i]
     */
    public static <U extends Unit<U>, R extends Relative, A extends Absolute> FloatVectorDense<U, A> addRA(
            FloatVectorDense<U, R> x, FloatVectorDense<U, A> y, U targetUnit)
    {
        DenseFloatMatrix1D c =
                (DenseFloatMatrix1D) new DenseFloatMatrix1D(x.getColtDenseFloatMatrix1D().toArray()).assign(
                        y.getColtDenseFloatMatrix1D(), new PlusUnitFunction<U>(x.getUnit(), y.getUnit(), targetUnit));
        return new FloatVectorDense<U, A>(c.toArray(), targetUnit);
    }

    public static <U extends Unit<U>, R extends Relative> FloatVectorDense<U, R> addRR(FloatVectorDense<U, R> a,
            FloatVectorDense<U, R> b, U targetUnit)
    {
        DenseFloatMatrix1D c =
                (DenseFloatMatrix1D) new DenseFloatMatrix1D(a.getColtDenseFloatMatrix1D().toArray()).assign(
                        b.getColtDenseFloatMatrix1D(), new PlusUnitFunction<U>(a.getUnit(), b.getUnit(), targetUnit));
        return new FloatVectorDense<U, R>(c.toArray(), targetUnit);
    }

    /**
     * @return unit
     */
    public U getUnit()
    {
        return this.unit;
    }

    private static class PlusUnitFunction<U extends Unit<U>> implements FloatFloatFunction
    {
        U xUnit;

        U yUnit;

        U targetUnit;

        public PlusUnitFunction(U xUnit, U yUnit, U targetUnit)
        {
            this.xUnit = xUnit;
            this.yUnit = yUnit;
            this.targetUnit = targetUnit;
        }

        /**
         * @see cern.colt.function.tfloat.FloatFloatFunction#apply(float, float)
         */
        @Override
        public float apply(float x, float y)
        {
            double r =
                    this.xUnit.getConversionFactorToStandardUnit() * x + this.yUnit.getConversionFactorToStandardUnit()
                            * y;
            r = r / this.targetUnit.getConversionFactorToStandardUnit();
            return (float) r;
        }

    }
}
