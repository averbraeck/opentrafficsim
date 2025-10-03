package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;

/**
 * Contour plot for speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContourPlotSpeed extends AbstractContourPlot<Speed>
{

    /**
     * Constructor.
     * @param caption caption
     * @param scheduler scheduler.
     * @param dataPool data pool
     */
    public ContourPlotSpeed(final String caption, final PlotScheduler scheduler, final ContourDataSource dataPool)
    {
        // speed is present by default, hence null contour data type
        super(caption, scheduler, dataPool, null, createPaintScale(), new Speed(30.0, SpeedUnit.KM_PER_HOUR), "%.0fkm/h",
                "speed %.1f km/h");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 30.0 / 3.6, 60.0 / 3.6, 110.0 / 3.6, 160.0 / 3.6};
        Color[] colorValues = Colors.reverse(Colors.GREEN_RED_DARK);
        return new BoundsPaintScale(boundaries, colorValues);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.SPEED_CONTOUR;
    }

    @Override
    protected double scale(final double si)
    {
        return SpeedUnit.KM_PER_HOUR.getScale().fromStandardUnit(si);
    }

    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getSpeed(item);
    }

    @Override
    public String toString()
    {
        return "ContourPlotSpeed []";
    }

}
