package org.opentrafficsim.swing.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.opentrafficsim.draw.graphs.FdDataSource;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphUtil;

/**
 * Embed a {@link FundamentalDiagram} in a swing {@link JPanel}.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SwingFundamentalDiagram extends SwingPlot implements EventListener
{
    /** */
    private static final long serialVersionUID = 20190823L;

    /** Whether to ignore events as this is itself the plot causing change of aggregationPeriod/updatesPerPeriod. */
    private boolean ignoreEvent = false;

    /** Aggregation period UI buttons. */
    private Map<JRadioButtonMenuItem, Double> aggregationPeriodButtons;

    /** Updates per period UI buttons. */
    private Map<JRadioButtonMenuItem, Integer> updatesPerPeriodButtons;

    /**
     * Construct a new Swing container for FundamentalDiagram plot.
     * @param plot the plot to embed
     */
    public SwingFundamentalDiagram(final FundamentalDiagram plot)
    {
        super(plot);
        plot.addListener(this, FdDataSource.UPDATES_PER_PERIOD);
        plot.addListener(this, FdDataSource.AGGREGATION_PERIOD);
    }

    @Override
    protected Optional<ChartMouseListener> getChartMouseListener()
    {
        ChartMouseListener toggle = !getPlot().hasLineFD() && getPlot().getNumberOfSeries() < 2 ? null
                : GraphUtil.getToggleSeriesByLegendListener(getPlot(), getPlot().getLegend(), getPlot().getLaneVisible());
        return Optional.of(new ChartMouseListener()
        {
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
                        double x0 = getPlot().getXValue(series, i);
                        double y0 = getPlot().getYValue(series, i);
                        double x1 = getPlot().getXValue(series, i + 1);
                        double y1 = getPlot().getYValue(series, i + 1);
                        if (!Double.isNaN(x0) && !Double.isNaN(y0) && !Double.isNaN(x1) && !Double.isNaN(y1))
                        {
                            XYLineAnnotation annotation =
                                    new XYLineAnnotation(x0, y0, x1, y1, new BasicStroke(1.0f), Color.WHITE);
                            getPlot().getChart().getXYPlot().addAnnotation(annotation);
                        }
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
                        double t = item * getPlot().getUpdateInterval().si;
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
                                if ((range.getUpperBound() - y)
                                        / (range.getUpperBound() - range.getLowerBound()) < (x - domain.getLowerBound())
                                                / (domain.getUpperBound() - domain.getLowerBound()))
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
        });
    }

    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        super.addPopUpMenuItems(popupMenu);
        popupMenu.insert(new JPopupMenu.Separator(), 0);

        this.aggregationPeriodButtons = new LinkedHashMap<>();
        this.updatesPerPeriodButtons = new LinkedHashMap<>();

        JMenu updMenu = new JMenu("Update frequency");
        ButtonGroup updGroup = new ButtonGroup();
        for (int f : getPlot().getPossibleUpdatesPerPeriod())
        {
            String format = "%dx";
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, f));
            this.updatesPerPeriodButtons.put(item, f);
            item.setSelected(f == getPlot().getDefaultUpdatesPerPeriod());
            item.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    SwingFundamentalDiagram.this.ignoreEvent = true;
                    getPlot().getChart().getXYPlot().clearAnnotations();
                    getPlot().setUpdatesPerPeriod(f);
                    SwingFundamentalDiagram.this.ignoreEvent = false;
                }
            });
            updGroup.add(item);
            updMenu.add(item);
        }
        popupMenu.insert(updMenu, 0);

        JMenu aggMenu = new JMenu("Aggregation period");
        ButtonGroup aggGroup = new ButtonGroup();
        for (Duration t : getPlot().getPossibleAggregationPeriods())
        {
            double t2 = t.si;
            String format = "%.0f s";
            if (t.si >= 60.0)
            {
                t2 = t.si / 60.0;
                format = "%.0f min";
            }
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, t2));
            this.aggregationPeriodButtons.put(item, t.si);
            item.setSelected(t.si == getPlot().getDefaultAggregationPeriod().si);
            item.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    SwingFundamentalDiagram.this.ignoreEvent = true;
                    getPlot().getChart().getXYPlot().clearAnnotations();
                    getPlot().setAggregationPeriod(t);
                    SwingFundamentalDiagram.this.ignoreEvent = false;
                }

            });
            aggGroup.add(item);
            aggMenu.add(item);
        }
        popupMenu.insert(aggMenu, 0);
    }

    /**
     * Retrieve the plot.
     * @return the plot
     */
    @Override
    public FundamentalDiagram getPlot()
    {
        return (FundamentalDiagram) super.getPlot();
    }

    @Override
    public void notify(final Event event)
    {
        if (this.ignoreEvent)
        {
            return;
        }
        if (event.getType().equals(FdDataSource.AGGREGATION_PERIOD))
        {
            Object[] payload = (Object[]) event.getContent();
            Duration aggregation = (Duration) payload[0];
            for (JRadioButtonMenuItem button : this.aggregationPeriodButtons.keySet())
            {
                button.setSelected(Math.abs(this.aggregationPeriodButtons.get(button) - aggregation.si) < 0.001);
            }
        }
        else if (event.getType().equals(FdDataSource.UPDATES_PER_PERIOD))
        {
            Object[] payload = (Object[]) event.getContent();
            int n = (int) payload[0];
            for (JRadioButtonMenuItem button : this.updatesPerPeriodButtons.keySet())
            {
                button.setSelected(this.updatesPerPeriodButtons.get(button) == n);
            }
        }
    }

}
