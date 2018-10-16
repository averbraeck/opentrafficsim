package org.opentrafficsim.graphs;

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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
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
public class XFundamentalDiagram extends XAbstractPlot implements XYDataset
{

    /** */
    private static final long serialVersionUID = 20101016L;

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
        for (int series = 0; series < source.getSeriesCount(); series++)
        {
            this.seriesLabels.add(series, source.getSeriesLabel(series));
        }
        setChart(createChart());

        // setup updater to do the actual work in another thread
        this.graphUpdater = new XGraphUpdater<>("Fundamental diagram worker", Thread.currentThread(), (t) -> 
        {
            if (this.source != null)
            {
                this.source.increaseTime(t);
            }
        });
    }

    /**
     * Constructor using a sampler as source.
     * @param caption String; caption
     * @param domainQuantity Quantity; initial quantity on the domain axis
     * @param rangeQuantity Quantity; initial quantity on the range axis
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler RoadSampler; sampler
     * @param positions List&lt;DirectedLanePosition&gt;; positions to measure
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     */
    @SuppressWarnings("parameternumber")
    public XFundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final OTSSimulatorInterface simulator, final RoadSampler sampler, final List<DirectedLanePosition> positions,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        this(caption, domainQuantity, rangeQuantity, simulator,
                sourceFromSampler(sampler, positions, aggregateLanes, aggregationTime));
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("\u2192 " + this.domainQuantity.label());
        NumberAxis yAxis = new NumberAxis("\u2192 " + this.rangeQuantity.label());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(); // XYDotRenderer doesn't support different markers
        renderer.setDefaultLinesVisible(false);
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, this.source.getSeriesCount() > 1);
    }

    /** {@inheritDoc} */
    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        super.addPopUpMenuItems(popupMenu);
        popupMenu.insert(new JPopupMenu.Separator(), 0);
        createChangeQuantityButton(popupMenu, false);
        createChangeQuantityButton(popupMenu, true);
    }

    /**
     * Creates a button in the menu to change the quantity on an axis.
     * @param popupMenu JPopupMenu; the menu to add the buttons to
     * @param domain boolean; whether this is for the domain or the range axis
     */
    private void createChangeQuantityButton(final JPopupMenu popupMenu, final boolean domain)
    {
        JMenuItem button = new JMenuItem(domain ? "Change domain quantity" : "Change range quantity");
        popupMenu.insert(button, 0);
        button.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if (domain)
                {
                    Quantity old = XFundamentalDiagram.this.domainQuantity;
                    XFundamentalDiagram.this.domainQuantity = XFundamentalDiagram.this.otherQuantity;
                    XFundamentalDiagram.this.otherQuantity = old;
                    getChart().getXYPlot().getDomainAxis()
                            .setLabel("\u2192 " + XFundamentalDiagram.this.domainQuantity.label());
                    getChart().getXYPlot().zoomDomainAxes(0.0, null, null);
                }
                else
                {
                    Quantity old = XFundamentalDiagram.this.rangeQuantity;
                    XFundamentalDiagram.this.rangeQuantity = XFundamentalDiagram.this.otherQuantity;
                    XFundamentalDiagram.this.otherQuantity = old;
                    getChart().getXYPlot().getRangeAxis()
                            .setLabel("\u2192 " + XFundamentalDiagram.this.rangeQuantity.label());
                    getChart().getXYPlot().zoomRangeAxes(0.0, null, null);
                }
            }
        });
        notifyPlotChange();
    }

    /** {@inheritDoc} */
    @Override
    protected void increaseTime(final Time time)
    {
        if (this.graphUpdater != null) // null during construction
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
        return this.source.getSeriesCount();
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
                + this.otherQuantity.format(this.domainQuantity.computeOther(this.rangeQuantity, domainValue, rangeValue));
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
                return "Density [veh/km]";
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
                return "Flow [veh/h]";
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
                return "Speed [km/h]";
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
         * The update interval, which is also the aggregation period.
         * @return Duration; update interval, which is also the aggregation period
         */
        Duration getUpdateInterval();

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
        int getSeriesCount();

        /**
         * Returns a legend label for the series.
         * @param series int; series number
         * @return String; legend label for the series
         */
        String getSeriesLabel(int series);

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
     * @param sampler RoadSampler; sampler
     * @param positions List&lt;DirectedLanePosition&gt;; positions to measure
     * @param aggregateLanes boolean; whether to aggregate the positions
     * @param aggregationTime Duration; aggregation time (and update time)
     * @return Source; source for a fundamental diagram from a sampler and positions
     */
    @SuppressWarnings("methodlength")
    public static Source sourceFromSampler(final RoadSampler sampler, final List<DirectedLanePosition> positions,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        int nSeries = aggregateLanes ? 1 : positions.size();

        // these maps allow to skip trajectories that were confirmed to hit the cross-section or not
        // it's essentially a list of all trajectory numbers, where all consecutive up to 'last' are reduced to 1 number
        Map<KpiLaneDirection, Integer> lastConsecutivelyAssignedTrajectories = new LinkedHashMap<>();
        Map<KpiLaneDirection, SortedSet<Integer>> assignedTrajectories = new LinkedHashMap<>();

        // create and register kpi lane directions
        List<KpiLaneDirection> lanes = new ArrayList<>();
        List<Length> poss = new ArrayList<>();
        for (DirectedLanePosition pos : positions)
        {
            KpiLaneDirection laneDirection = new KpiLaneDirection(new LaneData(pos.getLane()),
                    pos.getGtuDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
            sampler.registerSpaceTimeRegion(new SpaceTimeRegion(laneDirection, Length.ZERO, pos.getLane().getLength(),
                    Time.ZERO, Time.createSI(Double.MAX_VALUE)));
            lanes.add(laneDirection);

            // info per kpi lane direaction 
            poss.add(laneDirection.getPositionInDirection(pos.getPosition()));
            lastConsecutivelyAssignedTrajectories.put(laneDirection, -1);
            assignedTrajectories.put(laneDirection, new TreeSet<>());
        }

        // create the source
        return new Source()
        {
            // internal data
            private int period = -1;
            private int[][] count = new int[nSeries][10];
            private double[][] speed = new double[nSeries][10];

            /** {@inheritDoc} */
            @Override
            public Duration getUpdateInterval()
            {
                return aggregationTime;
            }

            /** {@inheritDoc} */
            @Override
            public Duration getDelay()
            {
                return Duration.createSI(1.0);
            }

            /** {@inheritDoc} */
            @Override
            public void increaseTime(final Time time)
            {
                // ensure capacity
                int nextPeriod = this.period + 1;
                if (nextPeriod >= this.count[0].length - 1)
                {
                    for (int i = 0; i < nSeries; i++)
                    {
                        this.count[i] = XPlotUtil.ensureCapacity(this.count[i], nextPeriod + 1);
                        this.speed[i] = XPlotUtil.ensureCapacity(this.speed[i], nextPeriod + 1);
                    }
                }
                
                // loop positions and trajectories
                Time startTime = time.minus(aggregationTime);
                double v = 0.0;
                int c = 0;
                for (int i = 0; i < lanes.size(); i++)
                {
                    TrajectoryGroup trajectoryGroup = sampler.getTrajectoryGroup(lanes.get(i));
                    int last = lastConsecutivelyAssignedTrajectories.get(lanes.get(i));
                    SortedSet<Integer> assigned = assignedTrajectories.get(lanes.get(i));
                    if (!aggregateLanes)
                    {
                        v = 0.0;
                        c = 0;
                    }
                    Length x = poss.get(i);
                    int j = 0;
                    for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                    {
                        // we can skip all assigned trajectories, which are all up to and including 'last' and all in 'assigned'
                        try
                        {
                            if (j > last && !assigned.contains(j))
                            {
                                // quickly filter
                                if (XPlotUtil.considerTrajectory(trajectory, startTime, time)
                                        && XPlotUtil.considerTrajectory(trajectory, x, x))
                                {
                                    // detailed check
                                    Time t = trajectory.getTimeAtPosition(x);
                                    if (t.si >= startTime.si && t.si < time.si)
                                    {
                                        c++; // is this allowed in java ;)?
                                        v += trajectory.getSpeedAtPosition(x).si;
                                    }
                                    assigned.add(j);
                                }
                                else if (trajectory.getX(0) >= x.si
                                        || trajectory.getT(trajectory.size() - 1) < time.si - getDelay().si)
                                {
                                    // we need this additional check to allow us to ignore initial GTU's beyond the measurement
                                    // point, and trajectories that ended some time ago but never crossed the point
                                    assigned.add(j);
                                }
                            }
                            j++;
                        }
                        catch (SamplingException exception)
                        {
                            throw new RuntimeException("Unexpected exception while counting trajectories.", exception);
                        }
                    }
                    if (!aggregateLanes)
                    {
                        this.count[i][nextPeriod] = c;
                        this.speed[i][nextPeriod] = c == 0 ? Float.NaN : v / c;
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
                        lastConsecutivelyAssignedTrajectories.put(lanes.get(i), last);
                    }
                }
                if (aggregateLanes)
                {
                    this.count[0][nextPeriod] = c;
                    this.speed[0][nextPeriod] = c == 0 ? Float.NaN : v / c;
                }
                this.period = nextPeriod;
            }

            /** {@inheritDoc} */
            @Override
            public int getSeriesCount()
            {
                return nSeries;
            }

            /** {@inheritDoc} */
            @Override
            public String getSeriesLabel(final int series)
            {
                if (aggregateLanes)
                {
                    return "Aggregate";
                }
                return lanes.get(series).getLaneData().getId();
            }

            /** {@inheritDoc} */
            @Override
            public int getItemCount(final int series)
            {
                return this.period + 1;
            }

            /** {@inheritDoc} */
            @Override
            public double getFlow(final int series, final int item)
            {
                return this.count[series][item] / aggregationTime.si;
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
                return this.speed[series][item];
            }

        };
    }

}
