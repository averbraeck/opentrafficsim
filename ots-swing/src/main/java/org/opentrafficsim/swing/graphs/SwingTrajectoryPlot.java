/**
 * 
 */
package org.opentrafficsim.swing.graphs;

import org.jfree.chart.ChartMouseListener;
import org.opentrafficsim.draw.graphs.GraphUtil;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;

/**
 * Embed a TrajectoryPlot in a Swing JPanel.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SwingTrajectoryPlot extends SwingSpaceTimePlot
{
    /** */
    private static final long serialVersionUID = 20190823L;

    /**
     * Construct a new Swing container for a TrajectoryPlot.
     * @param plot TrajectoryPlot; the plot to embed
     */
    public SwingTrajectoryPlot(final TrajectoryPlot plot)
    {
        super(plot);
    }

    /** {@inheritDoc} This implementation creates a listener to disable and enable lanes through the legend. */
    @Override
    protected ChartMouseListener getChartMouseListener()
    {
        if (getPlot().getPath().getNumberOfSeries() < 2)
        {
            return null;
        }
        return GraphUtil.getToggleSeriesByLegendListener(getPlot().getLegend(), getPlot().getLaneVisible());
    }

    /**
     * Retrieve the plot.
     * @return AbstractPlot; the plot
     */
    @Override
    public TrajectoryPlot getPlot()
    {
        return (TrajectoryPlot) super.getPlot();
    }

}
