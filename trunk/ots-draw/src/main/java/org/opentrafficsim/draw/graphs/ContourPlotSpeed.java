package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;

/**
 * Contour plot for speed.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ContourPlotSpeed extends AbstractContourPlot<Speed>
{

    /** */
    private static final long serialVersionUID = 20181004L;

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;?&gt;; data pool
     */
    public ContourPlotSpeed(final String caption, final OTSSimulatorInterface simulator, final ContourDataSource<?> dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new Speed(30.0, SpeedUnit.KM_PER_HOUR), "%.0fkm/h",
                "speed %.1f km/h");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {0.0, 30.0 / 3.6, 60.0 / 3.6, 110.0 / 3.6, 160.0 / 3.6};
        Color[] colorValues = BoundsPaintScale.reverse(BoundsPaintScale.GREEN_RED_DARK);
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.SPEED_CONTOUR;
    }

    /** {@inheritDoc} */
    @Override
    protected double scale(final double si)
    {
        return SpeedUnit.KM_PER_HOUR.getScale().fromStandardUnit(si);
    }

    /** {@inheritDoc} */
    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().getSpeed(item);
    }

    /** {@inheritDoc} */
    @Override
    protected ContourDataType<Speed, ?> getContourDataType()
    {
        return null; // speed is present by default
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContourPlotSpeed []";
    }

}
