package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;

/**
 * Contour plot for density.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContourPlotDensity extends AbstractContourPlot<LinearDensity>
{

    /**
     * Constructor.
     * @param caption caption
     * @param scheduler scheduler.
     * @param dataPool data pool
     */
    public ContourPlotDensity(final String caption, final PlotScheduler scheduler, final ContourDataSource dataPool)
    {
        // density is present by default, hence null contour data type
        super(caption, scheduler, dataPool, null, createPaintScale(), new LinearDensity(30.0, LinearDensityUnit.PER_KILOMETER),
                "%.0f/km", "density %.1f veh/km");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 20.0 / 1000, 150.0 / 1000};
        Color[] colorValues = Colors.GREEN_RED;
        return new BoundsPaintScale(boundaries, colorValues);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.DENSITY_CONTOUR;
    }

    @Override
    protected double scale(final double si)
    {
        return LinearDensityUnit.PER_KILOMETER.getScale().fromStandardUnit(si);
    }

    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getTotalTime(item) / (cellLength * cellSpan);
    }

    @Override
    public String toString()
    {
        return "ContourPlotDensity []";
    }

}
