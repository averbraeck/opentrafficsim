package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;

/**
 * Contour plot for flow.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContourPlotFlow extends AbstractContourPlot<Frequency>
{

    /**
     * Constructor.
     * @param caption String; caption
     * @param scheduler PlotScheduler; scheduler.
     * @param dataPool ContourDataSource; data pool
     */
    public ContourPlotFlow(final String caption, final PlotScheduler scheduler, final ContourDataSource dataPool)
    {
        super(caption, scheduler, dataPool, createPaintScale(), new Frequency(500.0, FrequencyUnit.PER_HOUR), "%.0f/h",
                "flow %.1f veh/h");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 500.0 / 3600, 1000.0 / 3600, 1500.0 / 3600, 2000.0 / 3600, 2500.0 / 3600, 3000.0 / 3600};
        Color[] colorValues = BoundsPaintScale.hue(7);
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.FLOW_CONTOUR;
    }

    /** {@inheritDoc} */
    @Override
    protected double scale(final double si)
    {
        return FrequencyUnit.PER_HOUR.getScale().fromStandardUnit(si);
    }

    /** {@inheritDoc} */
    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getTotalDistance(item) / (cellLength * cellSpan);
    }

    /** {@inheritDoc} */
    @Override
    protected ContourDataType<Frequency, ?> getContourDataType()
    {
        return null; // flow is present by default
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContourPlotFlow []";
    }

}
