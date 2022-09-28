package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;

/**
 * Contour plot for density.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContourPlotDensity extends AbstractContourPlot<LinearDensity>
{

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;?&gt;; data pool
     */
    public ContourPlotDensity(final String caption, final OTSSimulatorInterface simulator, final ContourDataSource<?> dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new LinearDensity(30.0, LinearDensityUnit.PER_KILOMETER),
                "%.0f/km", "density %.1f veh/km");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 20.0 / 1000, 150.0 / 1000};
        Color[] colorValues = BoundsPaintScale.GREEN_RED;
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.DENSITY_CONTOUR;
    }

    /** {@inheritDoc} */
    @Override
    protected double scale(final double si)
    {
        return LinearDensityUnit.PER_KILOMETER.getScale().fromStandardUnit(si);
    }

    /** {@inheritDoc} */
    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getTotalTime(item) / (cellLength * cellSpan);
    }

    /** {@inheritDoc} */
    @Override
    protected ContourDataType<LinearDensity, ?> getContourDataType()
    {
        return null; // density is present by default
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContourPlotDensity []";
    }

}
