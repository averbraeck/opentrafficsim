package org.opentrafficsim.draw.colorer;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;

/**
 * Color objects based on their current acceleration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public class AccelerationColorer<T> extends AbstractLegendBarColorer<T, Acceleration> implements Serializable
{

    /** */
    private static final long serialVersionUID = 201500001L;

    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("m/s\u00b2", 1);

    /**
     * Constructor.
     * @param valueFunction value function
     * @param boundPaintScale bound paint scale
     */
    public AccelerationColorer(final Function<? super T, Acceleration> valueFunction, final BoundsPaintScale boundPaintScale)
    {
        super(valueFunction, (v) -> boundPaintScale.getPaint(v.si),
                LegendColorer.fromBoundsPaintScale(boundPaintScale, FORMAT.getDoubleFormat()), boundPaintScale);
    }

    /**
     * Constructor.
     * @param valueFunction value function
     * @param minimumAcceleration minimum acceleration
     * @param maximumAcceleration maximum acceleration
     */
    public AccelerationColorer(final Function<? super T, Acceleration> valueFunction, final Acceleration minimumAcceleration,
            final Acceleration maximumAcceleration)
    {
        this(valueFunction,
                new BoundsPaintScale(new double[] {minimumAcceleration.si, 2.0 * minimumAcceleration.si / 3.0,
                        minimumAcceleration.si / 3.0, 0.0, maximumAcceleration.si / 2.0, maximumAcceleration.si},
                        Colors.ULTRA));
    }

    /**
     * Constructor constructing a scale from -6.0m/s/s to 2m/s/s.
     * @param valueFunction value function
     */
    public AccelerationColorer(final Function<? super T, Acceleration> valueFunction)
    {
        this(valueFunction, Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2.0));
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public final String getName()
    {
        return "Acceleration";
    }

}
