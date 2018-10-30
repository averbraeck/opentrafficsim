package org.opentrafficsim.core.graphs;

import java.awt.Color;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

/**
 * Contour plot for density.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> sampler GTU data type
 */
public class ContourPlotDensity<G extends GtuDataInterface> extends AbstractContourPlot<LinearDensity, G>
{

    /** */
    private static final long serialVersionUID = 20181010L;

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;G&gt;; data pool
     */
    public ContourPlotDensity(final String caption, final OTSSimulatorInterface simulator, final ContourDataSource<G> dataPool)
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
        double[] boundaries = { 0.0, 20.0 / 1000, 150.0 / 1000 };
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
