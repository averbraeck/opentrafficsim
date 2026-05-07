package org.opentrafficsim.animation.data.gtu;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.animation.BoundsPaintScale;
import org.opentrafficsim.animation.Colors;
import org.opentrafficsim.animation.colorer.AbstractLegendBarColorer;
import org.opentrafficsim.animation.colorer.LegendColorer;
import org.opentrafficsim.animation.colorer.NumberFormatUnit;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.tactical.WithDesiredSpeed;

/**
 * Colorer of GTUs by desired speed.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
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
        super((gtu) -> gtu instanceof LaneBasedGtu gtuLane
                && gtuLane.getTacticalPlanner() instanceof WithDesiredSpeed speedPlanner
                        ? Optional.of(speedPlanner.getDesiredSpeed()) : Optional.empty(),
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
                .75 * minimumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR) + .25 * maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR),
                .5 * minimumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR) + .5 * maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR),
                .25 * minimumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR) + .75 * maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR),
                maximumSpeed.getInUnit(SpeedUnit.KM_PER_HOUR)}, Colors.reverse(Colors.GREEN_RED_DARK), Color.WHITE));
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
