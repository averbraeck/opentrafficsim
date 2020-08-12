/**
 * 
 */
package org.opentrafficsim.swing.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.djunits.value.vdouble.scalar.Duration;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphUtil;

/**
 * Embed a FundamentalDiagram in a Swing JPanel.
 * <P>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SwingFundamentalDiagram extends SwingPlot
{

    /**  */
    private static final long serialVersionUID = 20190823L;

    /**
     * Construct a new Swing container for FundamentalDiagram plot.
     * @param plot FundamentalDiagram; the plot to embed
     */
    public SwingFundamentalDiagram(final FundamentalDiagram plot)
    {
        super(plot);
    }

    /** {@inheritDoc} */
    @Override
    protected ChartMouseListener getChartMouseListener()
    {
        ChartMouseListener toggle = !getPlot().hasLineFD() && getPlot().getSource().getNumberOfSeries() < 2 ? null : GraphUtil
            .getToggleSeriesByLegendListener(getPlot().getLegend(), getPlot().getLaneVisible());
        return new ChartMouseListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("unchecked")
            @Override
            public void chartMouseClicked(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseClicked(event); // forward as we use two listeners
                }
                // remove any line annotations
                for (XYAnnotation annotation : ((List<XYAnnotation>) getPlot().getChart().getXYPlot().getAnnotations()))
                {
                    if (annotation instanceof XYLineAnnotation)
                    {
                        getPlot().getChart().getXYPlot().removeAnnotation(annotation);
                    }
                }
                // add line annotation for each item in series if the user clicked in an item
                if (event.getEntity() instanceof XYItemEntity)
                {
                    XYItemEntity itemEntity = (XYItemEntity) event.getEntity();
                    int series = itemEntity.getSeriesIndex();
                    for (int i = 0; i < getPlot().getItemCount(series) - 1; i++)
                    {
                        XYLineAnnotation annotation = new XYLineAnnotation(getPlot().getXValue(series, i), getPlot().getYValue(
                            series, i), getPlot().getXValue(series, i + 1), getPlot().getYValue(series, i + 1), new BasicStroke(
                                1.0f), Color.WHITE);
                        getPlot().getChart().getXYPlot().addAnnotation(annotation);
                    }
                }
                else if (event.getEntity() instanceof AxisEntity)
                {
                    if (((AxisEntity) event.getEntity()).getAxis().equals(getPlot().getChart().getXYPlot().getDomainAxis()))
                    {
                        Quantity old = getPlot().getDomainQuantity();
                        getPlot().setDomainQuantity(getPlot().getOtherQuantity());
                        getPlot().setOtherQuantity(old);
                        getPlot().getChart().getXYPlot().getDomainAxis().setLabel(getPlot().getDomainQuantity().label());
                        getPlot().getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                    }
                    else
                    {
                        Quantity old = getPlot().getRangeQuantity();
                        getPlot().setRangeQuantity(getPlot().getOtherQuantity());
                        getPlot().setOtherQuantity(old);
                        getPlot().getChart().getXYPlot().getRangeAxis().setLabel(getPlot().getRangeQuantity().label());
                        getPlot().getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                    }
                }
            }

            /** {@inheritDoc} */
            @SuppressWarnings("unchecked")
            @Override
            public void chartMouseMoved(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseMoved(event); // forward as we use two listeners
                }
                boolean clearText = true;
                // set text annotation and status text to time of item
                if (event.getEntity() instanceof XYItemEntity)
                {
                    // create time info for status label
                    XYItemEntity itemEntity = (XYItemEntity) event.getEntity();
                    int series = itemEntity.getSeriesIndex();
                    if (!getPlot().hasLineFD() || series != getPlot().getSeriesCount() - 1)
                    {
                        clearText = false;
                        int item = itemEntity.getItem();
                        double t = item * getPlot().getSource().getUpdateInterval().si;
                        getPlot().setTimeInfo(String.format(", %.0fs", t));
                        double x = getPlot().getXValue(series, item);
                        double y = getPlot().getYValue(series, item);
                        Range domain = getPlot().getChart().getXYPlot().getDomainAxis().getRange();
                        Range range = getPlot().getChart().getXYPlot().getRangeAxis().getRange();
                        TextAnchor anchor;
                        if (range.getUpperBound() - y < y - range.getLowerBound())
                        {
                            // upper half
                            if (domain.getUpperBound() - x < x - domain.getLowerBound())
                            {
                                // upper right quadrant
                                anchor = TextAnchor.TOP_RIGHT;
                            }
                            else
                            {
                                // upper left quadrant, can't use TOP_LEFT as text will be under mouse pointer
                                if ((range.getUpperBound() - y) / (range.getUpperBound() - range.getLowerBound()) < (x - domain
                                    .getLowerBound()) / (domain.getUpperBound() - domain.getLowerBound()))
                                {
                                    // closer to top (at least relatively) so move text down
                                    anchor = TextAnchor.TOP_RIGHT;
                                }
                                else
                                {
                                    // closer to left (at least relatively) so move text right
                                    anchor = TextAnchor.BOTTOM_LEFT;
                                }
                            }
                        }
                        else if (domain.getUpperBound() - x < x - domain.getLowerBound())
                        {
                            // lower right quadrant
                            anchor = TextAnchor.BOTTOM_RIGHT;
                        }
                        else
                        {
                            // lower left quadrant
                            anchor = TextAnchor.BOTTOM_LEFT;
                        }
                        XYTextAnnotation textAnnotation = new XYTextAnnotation(String.format("%.0fs", t), x, y);
                        textAnnotation.setTextAnchor(anchor);
                        textAnnotation.setFont(textAnnotation.getFont().deriveFont(14.0f).deriveFont(Font.BOLD));
                        getPlot().getChart().getXYPlot().addAnnotation(textAnnotation);
                    }
                }
                // remove texts when mouse is elsewhere, or on FD line
                if (clearText)
                {
                    for (XYAnnotation annotation : ((List<XYAnnotation>) getPlot().getChart().getXYPlot().getAnnotations()))
                    {
                        if (annotation instanceof XYTextAnnotation)
                        {
                            getPlot().getChart().getXYPlot().removeAnnotation(annotation);
                        }
                    }
                    getPlot().setTimeInfo("");
                }
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        super.addPopUpMenuItems(popupMenu);
        popupMenu.insert(new JPopupMenu.Separator(), 0);

        JMenu updMenu = new JMenu("Update frequency");
        ButtonGroup updGroup = new ButtonGroup();
        for (int f : getPlot().getSource().getPossibleUpdateFrequencies())
        {
            String format = "%dx";
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, f));
            item.setSelected(f == 1);
            item.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {

                    if ((int) (.5 + getPlot().getSource().getAggregationPeriod().si / getPlot().getSource()
                        .getUpdateInterval().si) != f)
                    {
                        Duration interval = Duration.instantiateSI(getPlot().getSource().getAggregationPeriod().si / f);
                        for (FundamentalDiagram diagram : getPlot().getSource().getDiagrams())
                        {
                            diagram.setUpdateInterval(interval);
                        }
                        // the above setUpdateInterval also recalculates the virtual last update time
                        // add half an interval to avoid any rounding issues
                        getPlot().getSource().setUpdateInterval(interval, getPlot().getUpdateTime().plus(interval.times(0.5)));

                        for (FundamentalDiagram diagram : getPlot().getSource().getDiagrams())
                        {
                            diagram.getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                            diagram.getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                            diagram.notifyPlotChange();
                        }
                    }
                }
            });
            updGroup.add(item);
            updMenu.add(item);
        }
        popupMenu.insert(updMenu, 0);

        JMenu aggMenu = new JMenu("Aggregation period");
        ButtonGroup aggGroup = new ButtonGroup();
        for (double t : getPlot().getSource().getPossibleAggregationPeriods())
        {
            double t2 = t;
            String format = "%.0f s";
            if (t >= 60.0)
            {
                t2 = t / 60.0;
                format = "%.0f min";
            }
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, t2));
            item.setSelected(t == getPlot().getSource().getAggregationPeriod().si);
            item.addActionListener(new ActionListener()
            {

                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    if (getPlot().getSource().getAggregationPeriod().si != t)
                    {
                        int n = (int) (0.5 + getPlot().getSource().getAggregationPeriod().si / getPlot().getSource()
                            .getUpdateInterval().si);
                        Duration period = Duration.instantiateSI(t);
                        Duration interval = period.divide(n);
                        for (FundamentalDiagram diagram : getPlot().getSource().getDiagrams())
                        {
                            diagram.setUpdateInterval(interval);
                        }
                        // add half an interval to avoid any rounding issues
                        getPlot().getSource().setAggregationPeriod(period);
                        getPlot().getSource().setUpdateInterval(period.divide(n), getPlot().getUpdateTime().plus(period.divide(
                            n).times(0.5)));
                        for (FundamentalDiagram diagram : getPlot().getSource().getDiagrams())
                        {
                            diagram.getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                            diagram.getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                            diagram.notifyPlotChange();
                        }
                    }
                }

            });
            aggGroup.add(item);
            aggMenu.add(item);
        }
        popupMenu.insert(aggMenu, 0);
    }

    /**
     * Retrieve the plot.
     * @return AbstractPlot; the plot
     */
    @Override
    public FundamentalDiagram getPlot()
    {
        return (FundamentalDiagram) super.getPlot();
    }

}
