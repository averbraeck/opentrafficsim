package org.opentrafficsim.draw.egtf.typed;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.Unit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.base.Scalar;
import org.djunits.value.vdouble.matrix.FrequencyMatrix;
import org.djunits.value.vdouble.matrix.LinearDensityMatrix;
import org.djunits.value.vdouble.matrix.SpeedMatrix;
import org.djunits.value.vdouble.matrix.base.DoubleMatrix;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.draw.egtf.Converter;
import org.opentrafficsim.draw.egtf.Quantity;

/**
 * Quantities for a strongly-typed context.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <U> unit of data
 * @param <T> data type
 * @param <K> grid output format
 */
public class TypedQuantity<U extends Unit<U>, T extends Scalar<U, T>, K extends DoubleMatrix<U, ?, ?, ?>> extends Quantity<T, K>
{
    /** Standard quantity for speed. */
    public static final Quantity<Speed, SpeedMatrix> SPEED = new TypedQuantity<>("Speed", true, new Converter<SpeedMatrix>()
    {
        @Override
        public SpeedMatrix convert(final double[][] data)
        {
            try
            {
                return new SpeedMatrix(data, SpeedUnit.SI);
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
                return new FrequencyMatrix(data, FrequencyUnit.SI);
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
                        return new LinearDensityMatrix(data, LinearDensityUnit.SI);
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
