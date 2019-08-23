/**
 * 
 */
package org.opentrafficsim.draw.graphs;

import org.jfree.chart.ChartMouseListener;

/**
 * Embed a TrajectoryPlot in a Swing JPanel.
 * <P>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SwingTrajectoryPlot extends SwingSpaceTimePlot
{

    /**  */
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
