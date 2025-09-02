package org.opentrafficsim.draw.colorer;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.function.Function;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;

/**
 * Color object depending on their speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public class SpeedColorer<T> extends AbstractLegendBarColorer<T, Speed> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("km/h", 0);

    /**
     * Constructor.
     * @param valueFunction value function
     * @param boundPaintScale bounds paint scale, based on values in km/h
     */
    public SpeedColorer(final Function<? super T, Speed> valueFunction, final BoundsPaintScale boundPaintScale)
    {
        super(valueFunction, (v) -> boundPaintScale.getPaint(v.getInUnit(SpeedUnit.KM_PER_HOUR)),
                LegendColorer.fromBoundsPaintScale(boundPaintScale, FORMAT.getDoubleFormat()), boundPaintScale);
    }

    /**
     * Constructor.
     * @param valueFunction value function
     * @param maximumSpeed maximum speed
     */
    public SpeedColorer(final Function<? super T, Speed> valueFunction, final Speed maximumSpeed)
    {
        this(valueFunction, new BoundsPaintScale(new double[] {0.0, maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR) / 2.0,
                maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR)}, Colors.reverse(Colors.GREEN_RED)));
    }

    /**
     * Constructor constructing a range to 150km/h.
     * @param valueFunction value function
     */
    public SpeedColorer(final Function<? super T, Speed> valueFunction)
    {
        this(valueFunction, new Speed(150.0, SpeedUnit.KM_PER_HOUR));
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public final String getName()
    {
        return "Speed";
    }

}
