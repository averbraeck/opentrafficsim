package org.opentrafficsim.draw.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Period;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Fundamental diagram from various sources.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FundamentalDiagram extends AbstractBoundedPlot implements XYDataset
{

    /** */
    private static final long serialVersionUID = 20101016L;

    /** Aggregation periods. */
    public static final double[] DEFAULT_PERIODS = new double[] { 5.0, 10.0, 30.0, 60.0, 120.0, 300.0, 900.0 };

    /** Update frequencies (n * 1/period). */
    public static final int[] DEFAULT_UPDATE_FREQUENCIES = new int[] { 1, 2, 3, 5, 10 };

    /** Source providing the data. */
    private final FdSource source;

    /** Quantity on domain axis. */
    private Quantity domainQuantity;

    /** Quantity on range axis. */
    private Quantity rangeQuantity;

    /** The other, 3rd quantity. */
    private Quantity otherQuantity;

    /** Labels of series. */
    private final List<String> seriesLabels = new ArrayList<>();

    /** Updater for update times. */
    private final GraphUpdater<Time> graphUpdater;

    /** Property for chart listener to provide time info for status label. */
    private String timeInfo = "";

    /** Legend to change text color to indicate visibility. */
    private LegendItemCollection legend;

    /** Whether each lane is visible or not. */
    private final List<Boolean> laneVisible = new ArrayList<>();

    /**
     * Constructor.
     * @param caption String; caption
     * @param domainQuantity Quantity; initial quantity on the domain axis
     * @param rangeQuantity Quantity; initial quantity on the range axis
     * @param simulator OTSSimulatorInterface; simulator
     * @param source FdSource; source providing the data
     */
    public FundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final FdSource source)
    {
        super(caption, source.getUpdateInterval(), simulator, source.getDelay());
        Throw.when(domainQuantity.equals(rangeQuantity), IllegalArgumentException.class,
                "Domain and range quantity should not be equal.");
        this.domainQuantity = domainQuantity;
        this.rangeQuantity = rangeQuantity;
        Set<Quantity> quantities = EnumSet.allOf(Quantity.class);
        quantities.remove(domainQuantity);
        quantities.remove(rangeQuantity);
        this.otherQuantity = quantities.iterator().next();
        this.source = source;
        for (int series = 0; series < source.getNumberOfSeries(); series++)
        {
            this.seriesLabels.add(series, source.getName(series));
            this.laneVisible.add(true);
        }
        setChart(createChart());
        setLowerDomainBound(0.0);
        setLowerRangeBound(0.0);

        // setup updater to do the actual work in another thread
        this.graphUpdater = new GraphUpdater<>("Fundamental diagram worker", Thread.currentThread(), (t) ->
        {
            if (this.source != null)
            {
                this.source.increaseTime(t);
                notifyPlotChange();
            }
        });
    }

    /**
     * Constructor using a sampler as source.
     * @param caption String; caption
     * @param domainQuantity Quantity; initial quantity on the domain axis
     * @param rangeQuantity Quantity; initial quantity on the range axis
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler Sampler&lt;?&gt;; sampler
     * @param crossSection GraphCrossSection&lt;KpiLaneDirection&gt;; lanes
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     * @param harmonic boolean; harmonic mean
     */
    @SuppressWarnings("parameternumber")
    public FundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final Sampler<?> sampler,
            final GraphCrossSection<KpiLaneDirection> crossSection, final boolean aggregateLanes,
            final Duration aggregationTime, final boolean harmonic)
    {
        this(caption, domainQuantity, rangeQuantity, simulator,
                sourceFromSampler(sampler, crossSection, aggregateLanes, aggregationTime, harmonic));
    }

    /**
     * Constructor using a sampler as source.
     * @param caption String; caption
     * @param domainQuantity Quantity; initial quantity on the domain axis
     * @param rangeQuantity Quantity; initial quantity on the range axis
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler Sampler&lt;?&gt;; sampler
     * @param path GraphPath&lt;KpiLaneDirection&gt;; lanes
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     */
    @SuppressWarnings("parameternumber")
    public FundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final Sampler<?> sampler, final GraphPath<KpiLaneDirection> path,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        this(caption, domainQuantity, rangeQuantity, simulator,
                sourceFromSampler(sampler, path, aggregateLanes, aggregationTime));
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis(this.domainQuantity.label());
        NumberAxis yAxis = new NumberAxis(this.rangeQuantity.label());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer()
        {
            /** */
            private static final long serialVersionUID = 20181022L;

            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public boolean isSeriesVisible(final int series)
            {
                return FundamentalDiagram.this.laneVisible.get(series);
            }

        }; // XYDotRenderer doesn't support different markers
        renderer.setDefaultLinesVisible(false);
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        boolean showLegend = true;
        if (this.source.getNumberOfSeries() < 2)
        {
            plot.setFixedLegendItems(null);
            showLegend = false;
        }
        else
        {
            this.legend = new LegendItemCollection();
            for (int i = 0; i < this.source.getNumberOfSeries(); i++)
            {
                LegendItem li = new LegendItem(this.source.getName(i));
                li.setSeriesKey(i); // lane series, not curve series
                li.setShape(renderer.lookupLegendShape(i));
                li.setFillPaint(renderer.lookupSeriesPaint(i));
                this.legend.add(li);
            }
            plot.setFixedLegendItems(this.legend);
            showLegend = true;
        }
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
    }

    /** {@inheritDoc} */
    @Override
    protected ChartMouseListener getChartMouseListener()
    {
        ChartMouseListener toggle = this.source.getNumberOfSeries() < 2 ? null
                : GraphUtil.getToggleSeriesByLegendListener(this.legend, this.laneVisible);
        return new ChartMouseListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings({ "unchecked", "synthetic-access" })
            @Override
            public void chartMouseClicked(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseClicked(event); // forward as we use two listeners
                }
                // remove any line annotations
                for (XYAnnotation annotation : ((List<XYAnnotation>) getChart().getXYPlot().getAnnotations()))
                {
                    if (annotation instanceof XYLineAnnotation)
                    {
                        getChart().getXYPlot().removeAnnotation(annotation);
                    }
                }
                // add line annotation for each item in series if the user clicked in an item
                if (event.getEntity() instanceof XYItemEntity)
                {
                    XYItemEntity itemEntity = (XYItemEntity) event.getEntity();
                    int series = itemEntity.getSeriesIndex();
                    for (int i = 0; i < getItemCount(series) - 1; i++)
                    {
                        XYLineAnnotation annotation = new XYLineAnnotation(getXValue(series, i), getYValue(series, i),
                                getXValue(series, i + 1), getYValue(series, i + 1), new BasicStroke(1.0f), Color.WHITE);
                        getChart().getXYPlot().addAnnotation(annotation);
                    }
                }
                else if (event.getEntity() instanceof AxisEntity)
                {
                    if (((AxisEntity) event.getEntity()).getAxis().equals(getChart().getXYPlot().getDomainAxis()))
                    {
                        Quantity old = FundamentalDiagram.this.domainQuantity;
                        FundamentalDiagram.this.domainQuantity = FundamentalDiagram.this.otherQuantity;
                        FundamentalDiagram.this.otherQuantity = old;
                        getChart().getXYPlot().getDomainAxis().setLabel(FundamentalDiagram.this.domainQuantity.label());
                        getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                    }
                    else
                    {
                        Quantity old = FundamentalDiagram.this.rangeQuantity;
                        FundamentalDiagram.this.rangeQuantity = FundamentalDiagram.this.otherQuantity;
                        FundamentalDiagram.this.otherQuantity = old;
                        getChart().getXYPlot().getRangeAxis().setLabel(FundamentalDiagram.this.rangeQuantity.label());
                        getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                    }
                }
            }

            /** {@inheritDoc} */
            @SuppressWarnings({ "synthetic-access", "unchecked" })
            @Override
            public void chartMouseMoved(final ChartMouseEvent event)
            {
                if (toggle != null)
                {
                    toggle.chartMouseMoved(event); // forward as we use two listeners
                }
                // set text annotation and status text to time of item
                if (event.getEntity() instanceof XYItemEntity)
                {
                    // create time info for status label
                    XYItemEntity itemEntity = (XYItemEntity) event.getEntity();
                    int series = itemEntity.getSeriesIndex();
                    int item = itemEntity.getItem();
                    double t = item * FundamentalDiagram.this.source.getUpdateInterval().si;
                    FundamentalDiagram.this.timeInfo = String.format(", %.0fs", t);
                    double x = getXValue(series, item);
                    double y = getYValue(series, item);
                    Range domain = getChart().getXYPlot().getDomainAxis().getRange();
                    Range range = getChart().getXYPlot().getRangeAxis().getRange();
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
                    getChart().getXYPlot().addAnnotation(textAnnotation);
                }
                // remove texts when mouse is elsewhere
                else
                {
                    for (XYAnnotation annotation : ((List<XYAnnotation>) getChart().getXYPlot().getAnnotations()))
                    {
                        if (annotation instanceof XYTextAnnotation)
                        {
                            getChart().getXYPlot().removeAnnotation(annotation);
                        }
                    }
                    FundamentalDiagram.this.timeInfo = "";
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
        for (int f : this.source.getPossibleUpdateFrequencies())
        {
            String format = "%dx";
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, f));
            item.setSelected(f == 1);
            item.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public void actionPerformed(final ActionEvent e)
                {

                    if ((int) (.5 + FundamentalDiagram.this.source.getAggregationPeriod().si
                            / FundamentalDiagram.this.source.getUpdateInterval().si) != f)
                    {
                        Duration interval = Duration.createSI(FundamentalDiagram.this.source.getAggregationPeriod().si / f);
                        FundamentalDiagram.this.setUpdateInterval(interval);
                        // the above setUpdateInterval also recalculates the virtual last update time
                        // add half an interval to avoid any rounding issues
                        FundamentalDiagram.this.source.setUpdateInterval(interval,
                                FundamentalDiagram.this.getUpdateTime().plus(interval.multiplyBy(0.5)),
                                FundamentalDiagram.this);
                        getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                        getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                        notifyPlotChange();
                    }
                }
            });
            updGroup.add(item);
            updMenu.add(item);
        }
        popupMenu.insert(updMenu, 0);

        JMenu aggMenu = new JMenu("Aggregation period");
        ButtonGroup aggGroup = new ButtonGroup();
        for (double t : this.source.getPossibleAggregationPeriods())
        {
            double t2 = t;
            String format = "%.0f s";
            if (t >= 60.0)
            {
                t2 = t / 60.0;
                format = "%.0f min";
            }
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, t2));
            item.setSelected(t == this.source.getAggregationPeriod().si);
            item.addActionListener(new ActionListener()
            {

                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    if (FundamentalDiagram.this.source.getAggregationPeriod().si != t)
                    {
                        int n = (int) (0.5 + FundamentalDiagram.this.source.getAggregationPeriod().si
                                / FundamentalDiagram.this.source.getUpdateInterval().si);
                        Duration period = Duration.createSI(t);
                        FundamentalDiagram.this.setUpdateInterval(period.divideBy(n));
                        // add half an interval to avoid any rounding issues
                        FundamentalDiagram.this.source.setAggregationPeriod(period);
                        FundamentalDiagram.this.source.setUpdateInterval(period.divideBy(n),
                                FundamentalDiagram.this.getUpdateTime().plus(period.divideBy(n).multiplyBy(0.5)),
                                FundamentalDiagram.this);
                        getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                        getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                        notifyPlotChange();
                    }
                }

            });
            aggGroup.add(item);
            aggMenu.add(item);
        }
        popupMenu.insert(aggMenu, 0);
    }

    /** {@inheritDoc} */
    @Override
    protected void increaseTime(final Time time)
    {
        if (this.graphUpdater != null && time.si >= this.source.getAggregationPeriod().si) // null during construction
        {
            this.graphUpdater.offer(time);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getSeriesCount()
    {
        if (this.source == null)
        {
            return 0;
        }
        return this.source.getNumberOfSeries();
    }

    /** {@inheritDoc} */
    @Override
    public Comparable<String> getSeriesKey(final int series)
    {
        return this.seriesLabels.get(series);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        int index = this.seriesLabels.indexOf(seriesKey);
        return index < 0 ? 0 : index;
    }

    /** {@inheritDoc} */
    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public int getItemCount(final int series)
    {
        return this.source.getItemCount(series);
    }

    /** {@inheritDoc} */
    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public double getXValue(final int series, final int item)
    {
        return this.domainQuantity.getValue(this.source, series, item);
    }

    /** {@inheritDoc} */
    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public double getYValue(final int series, final int item)
    {
        return this.rangeQuantity.getValue(this.source, series, item);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.FUNDAMENTAL_DIAGRAM;
    }

    /** {@inheritDoc} */
    @Override
    protected String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return this.domainQuantity.format(domainValue) + ", " + this.rangeQuantity.format(rangeValue) + ", "
                + this.otherQuantity.format(this.domainQuantity.computeOther(this.rangeQuantity, domainValue, rangeValue))
                + this.timeInfo;
    }

    /**
     * Quantity enum defining density, flow and speed.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum Quantity
    {
        /** Density. */
        DENSITY
        {
            /** {@inheritDoc} */
            @Override
            public String label()
            {
                return "Density [veh/km] \u2192";
            }

            /** {@inheritDoc} */
            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/km", value);
            }

            /** {@inheritDoc} */
            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 1000 * src.getDensity(series, item);
            }

            /** {@inheritDoc} */
            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // .......................... speed = flow / density .. flow = density * speed
                return pairing.equals(FLOW) ? pairedValue / thisValue : thisValue * pairedValue;
            }
        },

        /** Flow. */
        FLOW
        {
            /** {@inheritDoc} */
            @Override
            public String label()
            {
                return "Flow [veh/h] \u2192";
            }

            /** {@inheritDoc} */
            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/h", value);
            }

            /** {@inheritDoc} */
            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 3600 * src.getFlow(series, item);
            }

            /** {@inheritDoc} */
            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // speed = flow * density ... density = flow / speed
                return thisValue / pairedValue;
            }
        },

        /** Speed. */
        SPEED
        {
            /** {@inheritDoc} */
            @Override
            public String label()
            {
                return "Speed [km/h] \u2192";
            }

            /** {@inheritDoc} */
            @Override
            public String format(final double value)
            {
                return String.format("%.1f km/h", value);
            }

            /** {@inheritDoc} */
            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 3.6 * src.getSpeed(series, item);
            }

            /** {@inheritDoc} */
            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // ............................. flow = speed * density .. density = flow / speed
                return pairing.equals(DENSITY) ? thisValue * pairedValue : pairedValue / thisValue;
            }
        };

        /**
         * Returns an axis label of the quantity.
         * @return String; axis label of the quantity
         */
        public abstract String label();

        /**
         * Formats a value for status display.
         * @param value double; value
         * @return String; formatted string including quantity
         */
        public abstract String format(double value);

        /**
         * Get scaled value in presentation unit.
         * @param src FdSource; the data source
         * @param series int; series number
         * @param item int; item number in series
         * @return double; scaled value in presentation unit
         */
        public abstract double getValue(FdSource src, int series, int item);

        /**
         * Compute the value of the 3rd quantity.
         * @param pairing Quantity; quantity on other axis
         * @param thisValue double; value of this quantity
         * @param pairedValue double; value of the paired quantity on the other axis
         * @return double; value of the 3rd quantity
         */
        public abstract double computeOther(Quantity pairing, double thisValue, double pairedValue);

    }

    /**
     * Data source for a fundamental diagram.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface FdSource
    {
        /**
         * Returns the possible intervals.
         * @return double[]; possible intervals
         */
        default double[] getPossibleAggregationPeriods()
        {
            return DEFAULT_PERIODS;
        }

        /**
         * Returns the possible frequencies, as a factor on 1 / 'aggregation interval'.
         * @return int[]; possible frequencies
         */
        default int[] getPossibleUpdateFrequencies()
        {
            return DEFAULT_UPDATE_FREQUENCIES;
        }

        /**
         * The update interval.
         * @return Duration; update interval
         */
        Duration getUpdateInterval();

        /**
         * Changes the update interval.
         * @param interval Duration; update interval
         * @param time Time; time until which data has to be recalculated
         * @param fd FundamentalDiagram; the fundamental diagram to notify when data is ready
         */
        void setUpdateInterval(Duration interval, Time time, FundamentalDiagram fd);

        /**
         * The aggregation period.
         * @return Duration; aggregation period
         */
        Duration getAggregationPeriod();

        /**
         * Changes the aggregation period.
         * @param period Duration; aggregation period
         */
        void setAggregationPeriod(Duration period);

        /**
         * Return the delay for graph updates so future influencing events have occurred, e.d. GTU move's.
         * @return Duration; graph delay
         */
        Duration getDelay();

        /**
         * Increase the time span.
         * @param time Time; time to increase to
         */
        void increaseTime(Time time);

        /**
         * Returns the number of series (i.e. lanes or 1 for aggregated).
         * @return int; number of series
         */
        int getNumberOfSeries();

        /**
         * Returns a name of the series.
         * @param series int; series number
         * @return String; name of the series
         */
        String getName(int series);

        /**
         * Returns the number of items in the series.
         * @param series int; series number
         * @return int; number of items in the series
         */
        int getItemCount(int series);

        /**
         * Return the SI flow value of item in series.
         * @param series int; series number
         * @param item int; item number in the series
         * @return double; SI flow value of item in series
         */
        double getFlow(int series, int item);

        /**
         * Return the SI density value of item in series.
         * @param series int; series number
         * @param item int; item number in the series
         * @return double; SI density value of item in series
         */
        double getDensity(int series, int item);

        /**
         * Return the SI speed value of item in series.
         * @param series int; series number
         * @param item int; item number in the series
         * @return double; SI speed value of item in series
         */
        double getSpeed(int series, int item);
    }

    /**
     * Creates a {@code Source} from a sampler and positions.
     * @param sampler Sampler&lt;?&gt;; sampler
     * @param crossSection GraphCrossSection&lt;KpiLaneDirection&gt;; cross section
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     * @param harmonic boolean; harmonic mean
     * @return Source; source for a fundamental diagram from a sampler and positions
     */
    @SuppressWarnings("methodlength")
    public static FdSource sourceFromSampler(final Sampler<?> sampler, final GraphCrossSection<KpiLaneDirection> crossSection,
            final boolean aggregateLanes, final Duration aggregationTime, final boolean harmonic)
    {
        return new CrossSectionSamplerFdSource<>(sampler, crossSection, aggregateLanes, aggregationTime, harmonic);
    }

    /**
     * Creates a {@code Source} from a sampler and positions.
     * @param sampler Sampler&lt;?&gt;; sampler
     * @param path GraphPath&lt;KpiLaneDirection&gt;; cross section
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     * @return Source; source for a fundamental diagram from a sampler and positions
     */
    public static FdSource sourceFromSampler(final Sampler<?> sampler, final GraphPath<KpiLaneDirection> path,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        return new PathSamplerFdSource<>(sampler, path, aggregateLanes, aggregationTime);
    }

    /**
     * Fundamental diagram source based on a cross section.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> underlying source type
     */
    private static class CrossSectionSamplerFdSource<S extends GraphCrossSection<? extends KpiLaneDirection>>
            extends AbstractSpaceSamplerFdSource<S>
    {
        /** Harmonic mean. */
        private final boolean harmonic;

        /**
         * Constructor.
         * @param sampler Sampler&lt;?&gt;; sampler
         * @param crossSection S; cross section
         * @param aggregateLanes boolean; whether to aggregate the lanes
         * @param aggregationPeriod Duration; initial aggregation {@link Period}
         * @param harmonic boolean; harmonic mean
         */
        CrossSectionSamplerFdSource(final Sampler<?> sampler, final S crossSection, final boolean aggregateLanes,
                final Duration aggregationPeriod, final boolean harmonic)
        {
            super(sampler, crossSection, aggregateLanes, aggregationPeriod);
            this.harmonic = harmonic;
        }

        /** {@inheritDoc} */
        @Override
        protected void getMeasurements(final Trajectory<?> trajectory, final Time startTime, final Time endTime,
                final Length length, final int series, final double[] measurements)
        {
            Length x = getSpace().position(series);
            if (GraphUtil.considerTrajectory(trajectory, x, x))
            {
                // detailed check
                Time t = trajectory.getTimeAtPosition(x);
                if (t.si >= startTime.si && t.si < endTime.si)
                {
                    measurements[0] = 1; // first = count
                    measurements[1] = // second = sum of (inverted) speeds
                            this.harmonic ? 1.0 / trajectory.getSpeedAtPosition(x).si : trajectory.getSpeedAtPosition(x).si;
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        protected double getVehicleCount(final double first, final double second)
        {
            return first; // is divided by aggregation period by caller
        }

        /** {@inheritDoc} */
        @Override
        protected double getSpeed(final double first, final double second)
        {
            return this.harmonic ? first / second : second / first;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "CrossSectionSamplerFdSource [harmonic=" + this.harmonic + "]";
        }

    }

    /**
     * Fundamental diagram source based on a path. Density, speed and flow over the entire path are calculated per lane.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> underlying source type
     */
    private static class PathSamplerFdSource<S extends GraphPath<? extends KpiLaneDirection>>
            extends AbstractSpaceSamplerFdSource<S>
    {
        /**
         * Constructor.
         * @param sampler Sampler&lt;?&gt;; sampler
         * @param path S; path
         * @param aggregateLanes boolean; whether to aggregate the lanes
         * @param aggregationPeriod Duration; initial aggregation period
         */
        PathSamplerFdSource(final Sampler<?> sampler, final S path, final boolean aggregateLanes,
                final Duration aggregationPeriod)
        {
            super(sampler, path, aggregateLanes, aggregationPeriod);
        }

        /** {@inheritDoc} */
        @Override
        protected void getMeasurements(final Trajectory<?> trajectory, final Time startTime, final Time endTime,
                final Length length, final int sereies, final double[] measurements)
        {
            SpaceTimeView stv = trajectory.getSpaceTimeView(Length.ZERO, length, startTime, endTime);
            measurements[0] = stv.getDistance().si; // first = total traveled distance
            measurements[1] = stv.getTime().si; // second = total traveled time
        }

        /** {@inheritDoc} */
        @Override
        protected double getVehicleCount(final double first, final double second)
        {
            return first / getSpace().getTotalLength().si; // is divided by aggregation period by caller
        }

        /** {@inheritDoc} */
        @Override
        protected double getSpeed(final double first, final double second)
        {
            return first / second;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "PathSamplerFdSource []";
        }

    }

    /**
     * Abstract class that deals with updating and recalculating the fundamental diagram.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> underlying source type
     */
    private abstract static class AbstractSpaceSamplerFdSource<S extends AbstractGraphSpace<? extends KpiLaneDirection>>
            implements FdSource
    {
        /** Period number of last calculated period. */
        private int periodNumber = -1;

        /** Update interval. */
        private Duration updateInterval;

        /** Aggregation period. */
        private Duration aggregationPeriod;

        /** Number of series. */
        private final int nSeries;

        /** First data. */
        private double[][] firstMeasurement;

        /** Second data. */
        private double[][] secondMeasurement;

        /** Whether the plot is in a process such that the data is invalid for the current draw of the plot. */
        private boolean invalid = false;

        /** The sampler. */
        private final Sampler<?> sampler;

        /** Space. */
        private final S space;

        /** Whether to aggregate the lanes. */
        private final boolean aggregateLanes;

        /** For each series (lane), the highest trajectory number (n) below which all trajectories were also handled (0:n). */
        private Map<KpiLaneDirection, Integer> lastConsecutivelyAssignedTrajectories = new LinkedHashMap<>();

        /** For each series (lane), a list of handled trajectories above n, excluding n+1. */
        private Map<KpiLaneDirection, SortedSet<Integer>> assignedTrajectories = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param sampler Sampler&lt;?&gt;; sampler
         * @param space S; space
         * @param aggregateLanes boolean; whether to aggregate the lanes
         * @param aggregationPeriod Duration; initial aggregation period
         */
        AbstractSpaceSamplerFdSource(final Sampler<?> sampler, final S space, final boolean aggregateLanes,
                final Duration aggregationPeriod)
        {
            this.sampler = sampler;
            this.space = space;
            this.aggregateLanes = aggregateLanes;
            this.nSeries = aggregateLanes ? 1 : space.getNumberOfSeries();
            // create and register kpi lane directions
            for (KpiLaneDirection laneDirection : space)
            {
                sampler.registerSpaceTimeRegion(new SpaceTimeRegion(laneDirection, Length.ZERO,
                        laneDirection.getLaneData().getLength(), Time.ZERO, Time.createSI(Double.MAX_VALUE)));

                // info per kpi lane direction
                this.lastConsecutivelyAssignedTrajectories.put(laneDirection, -1);
                this.assignedTrajectories.put(laneDirection, new TreeSet<>());
            }

            this.updateInterval = aggregationPeriod;
            this.aggregationPeriod = aggregationPeriod;
            this.firstMeasurement = new double[this.nSeries][10];
            this.secondMeasurement = new double[this.nSeries][10];
        }

        /**
         * Returns the space.
         * @return S; space
         */
        protected S getSpace()
        {
            return this.space;
        }

        /** {@inheritDoc} */
        @Override
        public Duration getUpdateInterval()
        {
            return this.updateInterval;
        }

        /** {@inheritDoc} */
        @Override
        public void setUpdateInterval(final Duration interval, final Time time, final FundamentalDiagram fd)
        {
            if (this.updateInterval != interval)
            {
                this.updateInterval = interval;
                recalculate(time, fd);
            }
        }

        /** {@inheritDoc} */
        @Override
        public Duration getAggregationPeriod()
        {
            return this.aggregationPeriod;
        }

        /** {@inheritDoc} */
        @Override
        public void setAggregationPeriod(final Duration period)
        {
            if (this.aggregationPeriod != period)
            {
                this.aggregationPeriod = period;
            }
        }

        /**
         * Recalculates the data after the aggregation or update time was changed.
         * @param time Time; time up to which recalculation is required
         * @param fd FundamentalDiagram; fundamental diagram to notify
         */
        private void recalculate(final Time time, final FundamentalDiagram fd)
        {
            new Thread(new Runnable()
            {
                @SuppressWarnings("synthetic-access")
                public void run()
                {
                    synchronized (AbstractSpaceSamplerFdSource.this)
                    {
                        // an active plot draw will now request data on invalid items
                        AbstractSpaceSamplerFdSource.this.invalid = true;
                        AbstractSpaceSamplerFdSource.this.periodNumber = -1;
                        AbstractSpaceSamplerFdSource.this.updateInterval = getUpdateInterval();
                        AbstractSpaceSamplerFdSource.this.firstMeasurement =
                                new double[AbstractSpaceSamplerFdSource.this.nSeries][10];
                        AbstractSpaceSamplerFdSource.this.secondMeasurement =
                                new double[AbstractSpaceSamplerFdSource.this.nSeries][10];
                        AbstractSpaceSamplerFdSource.this.lastConsecutivelyAssignedTrajectories.clear();
                        AbstractSpaceSamplerFdSource.this.assignedTrajectories.clear();
                        for (KpiLaneDirection lane : AbstractSpaceSamplerFdSource.this.space)
                        {
                            AbstractSpaceSamplerFdSource.this.lastConsecutivelyAssignedTrajectories.put(lane, -1);
                            AbstractSpaceSamplerFdSource.this.assignedTrajectories.put(lane, new TreeSet<>());
                        }
                        while ((AbstractSpaceSamplerFdSource.this.periodNumber + 1) * getUpdateInterval().si
                                + AbstractSpaceSamplerFdSource.this.aggregationPeriod.si <= time.si)
                        {
                            increaseTime(
                                    Time.createSI((AbstractSpaceSamplerFdSource.this.periodNumber + 1) * getUpdateInterval().si
                                            + AbstractSpaceSamplerFdSource.this.aggregationPeriod.si));
                            fd.notifyPlotChange();
                        }
                        AbstractSpaceSamplerFdSource.this.invalid = false;
                    }
                }
            }, "Fundamental diagram recalculation").start();
        }

        /** {@inheritDoc} */
        @Override
        public Duration getDelay()
        {
            return Duration.createSI(1.0);
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void increaseTime(final Time time)
        {
            if (time.si < this.aggregationPeriod.si)
            {
                // skip periods that fall below 0.0 time
                return;
            }

            // ensure capacity
            int nextPeriod = this.periodNumber + 1;
            if (nextPeriod >= this.firstMeasurement[0].length - 1)
            {
                for (int i = 0; i < this.nSeries; i++)
                {
                    this.firstMeasurement[i] = GraphUtil.ensureCapacity(this.firstMeasurement[i], nextPeriod + 1);
                    this.secondMeasurement[i] = GraphUtil.ensureCapacity(this.secondMeasurement[i], nextPeriod + 1);
                }
            }

            // loop positions and trajectories
            Time startTime = time.minus(this.aggregationPeriod);
            double first = 0;
            double second = 0.0;
            for (int series = 0; series < this.space.getNumberOfSeries(); series++)
            {
                Iterator<? extends KpiLaneDirection> it = this.space.iterator(series);
                while (it.hasNext())
                {
                    KpiLaneDirection lane = it.next();
                    TrajectoryGroup<?> trajectoryGroup = this.sampler.getTrajectoryGroup(lane);
                    int last = this.lastConsecutivelyAssignedTrajectories.get(lane);
                    SortedSet<Integer> assigned = this.assignedTrajectories.get(lane);
                    if (!this.aggregateLanes)
                    {
                        first = 0.0;
                        second = 0.0;
                    }

                    // Length x = this.crossSection.position(series);
                    int i = 0;
                    for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                    {
                        // we can skip all assigned trajectories, which are all up to and including 'last' and all in 'assigned'
                        try
                        {
                            if (i > last && !assigned.contains(i))
                            {
                                // quickly filter
                                if (GraphUtil.considerTrajectory(trajectory, startTime, time))
                                {
                                    double[] measurements = new double[2];
                                    getMeasurements(trajectory, startTime, time, lane.getLaneData().getLength(), series,
                                            measurements);
                                    first += measurements[0];
                                    second += measurements[1];
                                }
                                if (trajectory.getT(trajectory.size() - 1) < startTime.si - getDelay().si)
                                {
                                    assigned.add(i);
                                }
                            }
                            i++;
                        }
                        catch (SamplingException exception)
                        {
                            throw new RuntimeException("Unexpected exception while counting trajectories.", exception);
                        }
                    }
                    if (!this.aggregateLanes)
                    {
                        this.firstMeasurement[series][nextPeriod] = first;
                        this.secondMeasurement[series][nextPeriod] = second;
                    }

                    // consolidate list of assigned trajectories in 'all up to n' and 'these specific ones beyond n'
                    if (!assigned.isEmpty())
                    {
                        int possibleNextLastAssigned = assigned.first();
                        while (possibleNextLastAssigned == last + 1) // consecutive or very first
                        {
                            last = possibleNextLastAssigned;
                            assigned.remove(possibleNextLastAssigned);
                            possibleNextLastAssigned = assigned.isEmpty() ? -1 : assigned.first();
                        }
                        this.lastConsecutivelyAssignedTrajectories.put(lane, last);
                    }
                }
            }
            if (this.aggregateLanes)
            {
                // whatever we measured, it was summed and can be normalized per line like this
                this.firstMeasurement[0][nextPeriod] = first / this.space.getNumberOfSeries();
                this.secondMeasurement[0][nextPeriod] = second / this.space.getNumberOfSeries();
            }
            this.periodNumber = nextPeriod;
        }

        /** {@inheritDoc} */
        @Override
        public int getNumberOfSeries()
        {
            // if there is an active plot draw as the data is being recalculated, data on invalid items is requested
            // a call to getSeriesCount() indicates a new draw, and during a recalculation the data is limited but valid
            this.invalid = false;
            return this.nSeries;
        }

        /** {@inheritDoc} */
        @Override
        public String getName(final int series)
        {
            if (this.aggregateLanes)
            {
                return "Aggregate";
            }
            return this.space.getName(series);
        }

        /** {@inheritDoc} */
        @Override
        public int getItemCount(final int series)
        {
            return this.periodNumber + 1;
        }

        /** {@inheritDoc} */
        @Override
        public final double getFlow(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return getVehicleCount(this.firstMeasurement[series][item], this.secondMeasurement[series][item])
                    / this.aggregationPeriod.si;
        }

        /** {@inheritDoc} */
        @Override
        public final double getDensity(final int series, final int item)
        {
            return getFlow(series, item) / getSpeed(series, item);
        }

        /** {@inheritDoc} */
        @Override
        public final double getSpeed(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return getSpeed(this.firstMeasurement[series][item], this.secondMeasurement[series][item]);
        }

        /**
         * Returns the first and the second measurement of a trajectory. For a cross-section this is 1 and the vehicle speed if
         * the trajectory crosses the location, and for a path it is the traveled distance and the traveled time. If the
         * trajectory didn't cross the cross section or space-time range, both should be 0.
         * @param trajectory Trajectory&lt;?&gt;; trajectory
         * @param startTime Time; start time of aggregation period
         * @param endTime Time; end time of aggregation period
         * @param length Length; length of the section (to cut off possible lane overshoot of trajectories)
         * @param series int; series number in the section
         * @param measurements double[]; array with length 2 to place the first and second measurement in
         */
        protected abstract void getMeasurements(Trajectory<?> trajectory, Time startTime, Time endTime, Length length,
                int series, double[] measurements);

        /**
         * Returns the vehicle count of two related measurement values. For a cross section: vehicle count & sum of speeds (or
         * sum of inverted speeds for the harmonic mean). For a path: total traveled distance & total traveled time.
         * <p>
         * The value will be divided by the aggregation time to calculate flow. Hence, for a cross section the first measurement
         * should be returned, while for a path the first measurement divided by the section length should be returned. That
         * will end up to equate to {@code q = sum(x)/XT}.
         * @param first double; first measurement value
         * @param second double; second measurement value
         * @return double; flow
         */
        protected abstract double getVehicleCount(double first, double second);

        /**
         * Returns the speed of two related measurement values. For a cross section: vehicle count & sum of speeds (or sum of
         * inverted speeds for the harmonic mean). For a path: total traveled distance & total traveled time.
         * @param first double; first measurement value
         * @param second double; second measurement value
         * @return double; speed
         */
        protected abstract double getSpeed(double first, double second);

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "FundamentalDiagram [source=" + this.source + ", domainQuantity=" + this.domainQuantity + ", rangeQuantity="
                + this.rangeQuantity + ", otherQuantity=" + this.otherQuantity + ", seriesLabels=" + this.seriesLabels
                + ", graphUpdater=" + this.graphUpdater + ", timeInfo=" + this.timeInfo + ", legend=" + this.legend
                + ", laneVisible=" + this.laneVisible + "]";
    }

}
