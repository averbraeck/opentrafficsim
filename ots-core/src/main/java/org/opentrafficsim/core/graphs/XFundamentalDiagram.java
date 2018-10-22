package org.opentrafficsim.core.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;
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
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.language.Throw;

/**
 * Fundamental diagram from various sources.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class XFundamentalDiagram extends XAbstractBoundedPlot implements XYDataset
{

    /** */
    private static final long serialVersionUID = 20101016L;

    /** Aggregation periods. */
    public static final double[] DEFAULT_PERIODS = new double[] { 5.0, 10.0, 30.0, 60.0, 120.0, 300.0, 900.0 };

    /** Update frequencies (n * 1/period). */
    public static final int[] DEFAULT_UPDATE_FREQUENCIES = new int[] { 1, 2, 3, 5, 10 };

    /** Source providing the data. */
    private final Source source;

    /** Quantity on domain axis. */
    private Quantity domainQuantity;

    /** Quantity on range axis. */
    private Quantity rangeQuantity;

    /** The other, 3rd quantity. */
    private Quantity otherQuantity;

    /** Labels of series. */
    private final List<String> seriesLabels = new ArrayList<>();

    /** Updater for update times. */
    private final XGraphUpdater<Time> graphUpdater;

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
     * @param source Source; source providing the data
     */
    public XFundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final Source source)
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
        this.graphUpdater = new XGraphUpdater<>("Fundamental diagram worker", Thread.currentThread(), (t) ->
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
     * @param crossSection List&lt;KpiLaneDirection&gt;; lanes
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     */
    @SuppressWarnings("parameternumber")
    public XFundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final Sampler<?> sampler,
            final GraphCrossSection<KpiLaneDirection> crossSection, final boolean aggregateLanes,
            final Duration aggregationTime)
    {
        this(caption, domainQuantity, rangeQuantity, simulator,
                sourceFromSampler(sampler, crossSection, aggregateLanes, aggregationTime));
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
                return XFundamentalDiagram.this.laneVisible.get(series);
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
                        Quantity old = XFundamentalDiagram.this.domainQuantity;
                        XFundamentalDiagram.this.domainQuantity = XFundamentalDiagram.this.otherQuantity;
                        XFundamentalDiagram.this.otherQuantity = old;
                        getChart().getXYPlot().getDomainAxis().setLabel(XFundamentalDiagram.this.domainQuantity.label());
                        getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                    }
                    else
                    {
                        Quantity old = XFundamentalDiagram.this.rangeQuantity;
                        XFundamentalDiagram.this.rangeQuantity = XFundamentalDiagram.this.otherQuantity;
                        XFundamentalDiagram.this.otherQuantity = old;
                        getChart().getXYPlot().getRangeAxis().setLabel(XFundamentalDiagram.this.rangeQuantity.label());
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
                    double t = item * XFundamentalDiagram.this.source.getUpdateInterval().si;
                    XFundamentalDiagram.this.timeInfo = String.format(", %.0fs", t);
                    XYTextAnnotation textAnnotation =
                            new XYTextAnnotation(String.format("%.0fs", t), getXValue(series, item), getYValue(series, item));
                    textAnnotation.setTextAnchor(TextAnchor.TOP_RIGHT);
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
                    XFundamentalDiagram.this.timeInfo = "";
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

                    if ((int) (.5 + XFundamentalDiagram.this.source.getAggregationPeriod().si
                            / XFundamentalDiagram.this.source.getUpdateInterval().si) != f)
                    {
                        Duration interval = Duration.createSI(XFundamentalDiagram.this.source.getAggregationPeriod().si / f);
                        XFundamentalDiagram.this.setUpdateInterval(interval);
                        // the above setUpdateInterval also recalculates the virtual last update time
                        // add half an interval to avoid any rounding issues
                        XFundamentalDiagram.this.source.setUpdateInterval(interval,
                                XFundamentalDiagram.this.getUpdateTime().plus(interval.multiplyBy(0.5)),
                                XFundamentalDiagram.this);
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
                    if (XFundamentalDiagram.this.source.getAggregationPeriod().si != t)
                    {
                        int n = (int) (0.5 + XFundamentalDiagram.this.source.getAggregationPeriod().si
                                / XFundamentalDiagram.this.source.getUpdateInterval().si);
                        Duration period = Duration.createSI(t);
                        XFundamentalDiagram.this.setUpdateInterval(period.divideBy(n));
                        // add half an interval to avoid any rounding issues
                        XFundamentalDiagram.this.source.setAggregationPeriod(period);
                        XFundamentalDiagram.this.source.setUpdateInterval(period.divideBy(n),
                                XFundamentalDiagram.this.getUpdateTime().plus(period.divideBy(n).multiplyBy(0.5)),
                                XFundamentalDiagram.this);
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
            public double getValue(final Source src, final int series, final int item)
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
            public double getValue(final Source src, final int series, final int item)
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
            public double getValue(final Source src, final int series, final int item)
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
         * @param src Source; the data source
         * @param series int; series number
         * @param item item; item number in series
         * @return double; scaled value in presentation unit
         */
        public abstract double getValue(Source src, int series, int item);

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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface Source
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
        void setUpdateInterval(Duration interval, Time time, XFundamentalDiagram fd);

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
     * @return Source; source for a fundamental diagram from a sampler and positions
     */
    @SuppressWarnings("methodlength")
    public static Source sourceFromSampler(final Sampler<?> sampler, final GraphCrossSection<KpiLaneDirection> crossSection,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        return new SamplerSource(sampler, crossSection, aggregateLanes, aggregationTime);
    }

    /**
     * Fundamental diagram source from sampler.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class SamplerSource implements Source
    {
        /** Period number of last calculated period. */
        private int periodNumber = -1;

        /** Update interval. */
        private Duration updateInterval;

        /** Aggregation period. */
        private Duration aggregationPeriod;

        /** Number of series. */
        private final int nSeries;

        /** Flow data. */
        private int[][] count;

        /** Speed data. */
        private double[][] speed;

        /** Whether the plot is in a process such that the data is invalid for the current draw of the plot. */
        private boolean invalid = false;

        /** The sampler. */
        private final Sampler<?> sampler;

        /** Lanes. */
        private final GraphCrossSection<KpiLaneDirection> crossSection;

        /** Whether to aggregate the lanes. */
        private final boolean aggregateLanes;

        /** For each series (lane), the highest trajectory number (n) below which all trajectories were also handled (0:n). */
        private Map<KpiLaneDirection, Integer> lastConsecutivelyAssignedTrajectories = new LinkedHashMap<>();

        /** For each series (lane), a list of handled trajectories above n, excluding n+1. */
        private Map<KpiLaneDirection, SortedSet<Integer>> assignedTrajectories = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param sampler Sampler&lt;?&gt;; sampler
         * @param crossSection GraphCrossSection&lt;KpiLaneDirection&gt;; cross section
         * @param aggregateLanes boolean; whether to aggregate the lanes
         * @param aggregationPeriod Duration; initial aggregation period
         */
        SamplerSource(final Sampler<?> sampler, final GraphCrossSection<KpiLaneDirection> crossSection,
                final boolean aggregateLanes, final Duration aggregationPeriod)
        {
            this.sampler = sampler;
            this.crossSection = crossSection;
            this.aggregateLanes = aggregateLanes;
            this.nSeries = aggregateLanes ? 1 : crossSection.getNumberOfSeries();
            // create and register kpi lane directions
            for (int i = 0; i < crossSection.getNumberOfSeries(); i++)
            {
                KpiLaneDirection laneDirection = crossSection.getSource(i);
                sampler.registerSpaceTimeRegion(new SpaceTimeRegion(laneDirection, Length.ZERO,
                        laneDirection.getLaneData().getLength(), Time.ZERO, Time.createSI(Double.MAX_VALUE)));

                // info per kpi lane direction
                this.lastConsecutivelyAssignedTrajectories.put(laneDirection, -1);
                this.assignedTrajectories.put(laneDirection, new TreeSet<>());
            }

            this.updateInterval = aggregationPeriod;
            this.aggregationPeriod = aggregationPeriod;
            this.count = new int[this.nSeries][10];
            this.speed = new double[this.nSeries][10];
        }

        /** {@inheritDoc} */
        @Override
        public Duration getUpdateInterval()
        {
            return this.updateInterval;
        }

        /** {@inheritDoc} */
        @Override
        public void setUpdateInterval(final Duration interval, final Time time, final XFundamentalDiagram fd)
        {
            if (Double.isInfinite(interval.si))
            {
                System.out.println("hmmm");
            }
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
        public void recalculate(final Time time, final XFundamentalDiagram fd)
        {
            new Thread(new Runnable()
            {
                @SuppressWarnings("synthetic-access")
                public void run()
                {
                    synchronized (SamplerSource.this)
                    {
                        SamplerSource.this.invalid = true; // an active plot draw will now request data on invalid items
                        SamplerSource.this.periodNumber = -1;
                        SamplerSource.this.updateInterval = getUpdateInterval();
                        SamplerSource.this.count = new int[SamplerSource.this.nSeries][10];
                        SamplerSource.this.speed = new double[SamplerSource.this.nSeries][10];
                        SamplerSource.this.lastConsecutivelyAssignedTrajectories.clear();
                        SamplerSource.this.assignedTrajectories.clear();
                        for (KpiLaneDirection lane : SamplerSource.this.crossSection)
                        {
                            SamplerSource.this.lastConsecutivelyAssignedTrajectories.put(lane, -1);
                            SamplerSource.this.assignedTrajectories.put(lane, new TreeSet<>());
                        }
                        while ((SamplerSource.this.periodNumber + 1) * getUpdateInterval().si
                                + SamplerSource.this.aggregationPeriod.si <= time.si)
                        {
                            increaseTime(Time.createSI((SamplerSource.this.periodNumber + 1) * getUpdateInterval().si
                                    + SamplerSource.this.aggregationPeriod.si));
                            fd.notifyPlotChange();
                        }
                        SamplerSource.this.invalid = false;
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
            if (nextPeriod >= this.count[0].length - 1)
            {
                for (int i = 0; i < this.nSeries; i++)
                {
                    this.count[i] = GraphUtil.ensureCapacity(this.count[i], nextPeriod + 1);
                    this.speed[i] = GraphUtil.ensureCapacity(this.speed[i], nextPeriod + 1);
                }
            }

            // loop positions and trajectories
            Time startTime = time.minus(this.aggregationPeriod);
            double v = 0.0;
            int c = 0;
            for (int series = 0; series < this.crossSection.getNumberOfSeries(); series++)
            {
                KpiLaneDirection lane = this.crossSection.getSource(series);
                TrajectoryGroup trajectoryGroup = this.sampler.getTrajectoryGroup(lane);
                int last = this.lastConsecutivelyAssignedTrajectories.get(lane);
                SortedSet<Integer> assigned = this.assignedTrajectories.get(lane);
                if (!this.aggregateLanes)
                {
                    v = 0.0;
                    c = 0;
                }
                Length x = this.crossSection.position(series);
                int i = 0;
                for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                {
                    // we can skip all assigned trajectories, which are all up to and including 'last' and all in 'assigned'
                    try
                    {
                        if (i > last && !assigned.contains(i))
                        {
                            // quickly filter
                            if (GraphUtil.considerTrajectory(trajectory, startTime, time)
                                    && GraphUtil.considerTrajectory(trajectory, x, x))
                            {
                                // detailed check
                                Time t = trajectory.getTimeAtPosition(x);
                                if (t.si >= startTime.si && t.si < time.si)
                                {
                                    c++; // is this allowed in java ;)?
                                    v += trajectory.getSpeedAtPosition(x).si;
                                }
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
                    this.count[series][nextPeriod] = c;
                    this.speed[series][nextPeriod] = c == 0 ? Float.NaN : v / c;
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
            if (this.aggregateLanes)
            {
                this.count[0][nextPeriod] = c / this.crossSection.getNumberOfSeries();
                this.speed[0][nextPeriod] = c == 0 ? Float.NaN : v / c;
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
            return this.crossSection.getName(series);
        }

        /** {@inheritDoc} */
        @Override
        public int getItemCount(final int series)
        {
            return this.periodNumber + 1;
        }

        /** {@inheritDoc} */
        @Override
        public double getFlow(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return this.count[series][item] / this.aggregationPeriod.si;
        }

        /** {@inheritDoc} */
        @Override
        public double getDensity(final int series, final int item)
        {
            return getFlow(series, item) / getSpeed(series, item);
        }

        /** {@inheritDoc} */
        @Override
        public double getSpeed(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return this.speed[series][item];
        }

    }

}
