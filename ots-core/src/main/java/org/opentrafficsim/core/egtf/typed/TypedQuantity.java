package org.opentrafficsim.core.egtf.typed;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.AbstractScalar;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.matrix.FrequencyMatrix;
import org.djunits.value.vdouble.matrix.LinearDensityMatrix;
import org.djunits.value.vdouble.matrix.SpeedMatrix;
import org.djunits.value.vdouble.matrix.base.DoubleMatrixInterface;
import org.djunits.value.vdouble.matrix.data.DoubleMatrixDataDense;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.egtf.Converter;
import org.opentrafficsim.core.egtf.Quantity;

/**
 * Quantities for a strongly-typed context.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <U> unit of data
 * @param <T> data type
 * @param <K> grid output format
 */
public class TypedQuantity<U extends Unit<U>, T extends AbstractScalar<U, T>, K extends DoubleMatrixInterface<U, ?, ?, ?>>
        extends Quantity<T, K>
{
    /** Standard quantity for speed. */
    public static final Quantity<Speed, SpeedMatrix> SPEED = new TypedQuantity<>("Speed", true, new Converter<SpeedMatrix>()
    {
        @Override
        public SpeedMatrix convert(final double[][] data)
        {
            try
            {
                return new SpeedMatrix(new DoubleMatrixDataDense(data), SpeedUnit.SI);
            }
            catch (ValueRuntimeException exception)
            {
                // should not happen
                throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
            }
        }
    });

    /** Standard quantity for flow. */
    public static final Quantity<Frequency, FrequencyMatrix> FLOW = new TypedQuantity<>("Flow", new Converter<FrequencyMatrix>()
    {
        @Override
        public FrequencyMatrix convert(final double[][] data)
        {
            try
            {
                return new FrequencyMatrix(new DoubleMatrixDataDense(data), FrequencyUnit.SI);
            }
            catch (ValueRuntimeException exception)
            {
                // should not happen
                throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
            }
        }
    });

    /** Standard quantity for density. */
    public static final Quantity<LinearDensity, LinearDensityMatrix> DENSITY =
            new TypedQuantity<>("Density", new Converter<LinearDensityMatrix>()
            {
                @Override
                public LinearDensityMatrix convert(final double[][] data)
                {
                    try
                    {
                        return new LinearDensityMatrix(new DoubleMatrixDataDense(data), LinearDensityUnit.SI);
                    }
                    catch (ValueRuntimeException exception)
                    {
                        // should not happen
                        throw new RuntimeException("Unexcepted exception: data is null when converting.", exception);
                    }
                }
            });

    /**
     * Constructor.
     * @param name String; name
     * @param converter Converter&lt;K&gt;; converter for output format
     */
    public TypedQuantity(final String name, final Converter<K> converter)
    {
        super(name, false, converter);
    }

    /**
     * Constructor. Private so only the default SPEED quantity is speed.
     * @param name String; name
     * @param speed boolean; whether this quantity is speed
     * @param converter Converter&lt;K&gt;; converter for output format
     */
    protected TypedQuantity(final String name, final boolean speed, final Converter<K> converter)
    {
        super(name, speed, converter);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TypedQuantity []";
    }

}
