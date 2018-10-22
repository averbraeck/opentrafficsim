package org.opentrafficsim.core.graphs;

import java.awt.Color;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Frequency;
import org.opentrafficsim.core.graphs.XContourDataPool.ContourDataType;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

/**
 * Contour plot for flow.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> sampler GTU data type
 */
public class XContourPlotFlow<G extends GtuDataInterface> extends XAbstractContourPlot<Frequency, G>
{

    /** */
    private static final long serialVersionUID = 20181010L;

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataPool&lt;G&gt;; data pool
     */
    public XContourPlotFlow(final String caption, final OTSSimulatorInterface simulator, final XContourDataPool<G> dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new Frequency(500.0, FrequencyUnit.PER_HOUR), "%.0f/h",
                "flow %.1f veh/h");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static XBoundsPaintScale createPaintScale()
    {
        double[] boundaries = { 0.0, 500.0 / 3600, 1000.0 / 3600, 1500.0 / 3600, 2000.0 / 3600, 2500.0 / 3600, 3000.0 / 3600 };
        Color[] colorValues = XBoundsPaintScale.hue(7);
        return new XBoundsPaintScale(boundaries, colorValues);
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

}
