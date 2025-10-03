package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;

/**
 * Contour plot for flow.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContourPlotFlow extends AbstractContourPlot<Frequency>
{

    /**
     * Constructor.
     * @param caption caption
     * @param scheduler scheduler.
     * @param dataPool data pool
     */
    public ContourPlotFlow(final String caption, final PlotScheduler scheduler, final ContourDataSource dataPool)
    {
        // flow is present by default, hence null contour data type
        super(caption, scheduler, dataPool, null, createPaintScale(), new Frequency(500.0, FrequencyUnit.PER_HOUR), "%.0f/h",
                "flow %.1f veh/h");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 500.0 / 3600, 1000.0 / 3600, 1500.0 / 3600, 2000.0 / 3600, 2500.0 / 3600, 3000.0 / 3600};
        Color[] colorValues = Colors.hue(7);
        return new BoundsPaintScale(boundaries, colorValues);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.FLOW_CONTOUR;
    }

    @Override
    protected double scale(final double si)
    {
        return FrequencyUnit.PER_HOUR.getScale().fromStandardUnit(si);
    }

    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getTotalDistance(item) / (cellLength * cellSpan);
    }

    @Override
    public String toString()
    {
        return "ContourPlotFlow []";
    }

}
