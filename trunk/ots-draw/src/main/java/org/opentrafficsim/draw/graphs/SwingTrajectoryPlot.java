/**
 * 
 */
package org.opentrafficsim.draw.graphs;

import org.jfree.chart.ChartMouseListener;

/**
 * @author pknoppers
 *
 */
public class SwingTrajectoryPlot extends SwingSpaceTimePlot
{

    /**
     * @param plot
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
