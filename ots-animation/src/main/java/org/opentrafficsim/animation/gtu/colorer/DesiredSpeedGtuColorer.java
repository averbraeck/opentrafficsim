package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.AbstractLegendBarColorer;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.draw.colorer.NumberFormatUnit;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Colorer of GTUs by desired speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DesiredSpeedGtuColorer extends AbstractLegendBarColorer<Gtu, Speed>
{
    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("km/h", 0);

    /**
     * Constructor.
     * @param boundsPaintScale bounds paint scale for value in km/h
     */
    public DesiredSpeedGtuColorer(final BoundsPaintScale boundsPaintScale)
    {
        super((gtu) -> gtu instanceof LaneBasedGtu gtuLane ? Optional.of(gtuLane.getDesiredSpeed()) : Optional.empty(),
                (v) -> v == null ? Color.WHITE : boundsPaintScale.getPaint(v.getInUnit(SpeedUnit.KM_PER_HOUR)),
                LegendColorer.fromBoundsPaintScale(boundsPaintScale, FORMAT.getDoubleFormat()), boundsPaintScale);
    }

    /**
     * Constructor.
     * @param minimumSpeed the speed at (and below) which the returned color will be red
     * @param maximumSpeed the speed at (and above) which the returned color will be green
     */
    public DesiredSpeedGtuColorer(final Speed minimumSpeed, final Speed maximumSpeed)
    {
        this(new BoundsPaintScale(new double[] {minimumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR),
                (minimumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR) + maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR)) / 2.0,
                maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR)}, Colors.reverse(Colors.GREEN_RED), Color.WHITE));
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public final String getName()
    {
        return "Desired speed";
    }

}
