package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;

/**
 * Trajectory plot.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends AbstractOTSPlot implements XYDataset, LaneBasedGTUSampler// , EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 20140724L;

    /** Sample interval of this TrajectoryPlot. */
    private final Duration sampleInterval;

    /** The simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * @return sampleInterval if this TrajectoryPlot samples at a fixed rate, or null if this TrajectoryPlot samples on the GTU
     *         move events
     */
    public final Duration getSampleInterval()
    {
        return this.sampleInterval;
    }

    /** The cumulative lengths of the elements of path. */
    private final double[] cumulativeLengths;

    /**
     * Retrieve the cumulative length of the sampled path at the end of a path element.
     * @param index int; the index of the path element; if -1, the total length of the path is returned
     * @return double; the cumulative length at the end of the specified path element in meters (si)
     */
    public final double getCumulativeLength(final int index)
    {
        return index == -1 ? this.cumulativeLengths[this.cumulativeLengths.length - 1] : this.cumulativeLengths[index];
    }

    /** Maximum of the time axis. */
    private Time maximumTime = new Time(300, TimeUnit.BASE);

    /**
     * Retrieve the maximum time.
     * @return Time; the maximum time
     */
    public final Time getMaximumTime()
    {
        return this.maximumTime;
    }

    /**
     * Set the maximum time.
     * @param maximumTime Time; set the maximum time
     */
    public final void setMaximumTime(final Time maximumTime)
    {
        this.maximumTime = maximumTime;
    }

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /** The underlying sampler. */
    private RoadSampler roadSampler;

    /** The lanes that make up the path. */
    private List<KpiLaneDirection> lanes;

    /** Mapping from series rank number to trajectory. */
    private List<TrajectoryAndLengthOffset> curves = null;

    /** Re generate the mapping on the next call to getSeriesCount. */
    private boolean shouldGenerateNewCurves = true;

    /**
     * Create a new TrajectoryPlot.
     * @param caption String; the text to show above the TrajectoryPlot
     * @param sampleInterval DoubleScalarRel&lt;TimeUnit&gt;; the time between samples of this TrajectoryPlot, or null in which
     *            case the GTUs are sampled whenever they fire a MOVE_EVENT
     * @param path ArrayList&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     */
    public TrajectoryPlot(final String caption, final Duration sampleInterval, final List<Lane> path,
            final OTSDEVSSimulatorInterface simulator)
    {
        super(caption, path);
        this.roadSampler = null == sampleInterval ? new RoadSampler(simulator)
                : new RoadSampler(simulator, Frequency.createSI(1 / sampleInterval.si));
        this.lanes = new ArrayList<>();
        for (Lane lane : path)
        {
            KpiLaneDirection kpiLaneDirection =
                    new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
            SpaceTimeRegion spaceTimeRegion = new SpaceTimeRegion(kpiLaneDirection, Length.ZERO, lane.getLength(),
                    Time.ZERO, Time.createSI(Double.MAX_VALUE));
            this.roadSampler.registerSpaceTimeRegion(spaceTimeRegion);
            this.lanes.add(kpiLaneDirection);
        }
        this.sampleInterval = sampleInterval;
        this.simulator = simulator;
        double[] endLengths = new double[path.size()];
        double cumulativeLength = 0;
        for (int i = 0; i < path.size(); i++)
        {
            Lane lane = path.get(i);
            cumulativeLength += lane.getLength().getSI();
            endLengths[i] = cumulativeLength;
        }
        this.cumulativeLengths = endLengths;
        setChart(createChart(this));
        this.reGraph(); // fixes the domain axis
    }

    /**
     * Derived from example on stackoverflow.
     * http://stackoverflow.com/questions/7283902/setting-different-color-to-particular-row-in-series-jfreechart/7285922#7285922
     */
    private class MyRenderer extends XYLineAndShapeRenderer
    {

        /** */
        private static final long serialVersionUID = 20170503L;

        /**
         * Construct a new MyRenderer.
         * @param lines boolean; draw connecting lines
         * @param shapes boolean; draw shapes at the points that define the lines
         */
        MyRenderer(final boolean lines, final boolean shapes)
        {
            super(lines, shapes);
        }

        @Override
        public Paint getItemPaint(final int row, final int col)
        {
            @SuppressWarnings("synthetic-access")
            TrajectoryAndLengthOffset tal = getTrajectory(row);
            String gtuId = tal.getTrajectory().getGtuId();
            int colorIndex = 0;
            for (int pos = gtuId.length(); --pos >= 0;)
            {
                Character c = gtuId.charAt(pos);
                if (Character.isDigit(c))
                {
                    colorIndex = c - '0';
                    break;
                }
            }
            return IDGTUColorer.LEGEND.get(colorIndex).getColor();
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "MyRenderer []";
        }
    }

    /** {@inheritDoc} */
    @Override
    public final GraphType getGraphType()
    {
        return GraphType.TRAJECTORY;
    }

    /** {@inheritDoc} */
    @Override
    protected final JFreeChart createChart(final JFrame container)
    {
        final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        container.add(statusLabel, BorderLayout.SOUTH);
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        final JFreeChart result =
                ChartFactory.createXYLineChart(getCaption(), "", "", this, PlotOrientation.VERTICAL, false, false, false);
        // Overrule the default background paint because some of the lines are invisible on top of this default.
        result.getPlot().setBackgroundPaint(new Color(0.9f, 0.9f, 0.9f));
        FixCaption.fixCaption(result);
        NumberAxis xAxis = new NumberAxis("\u2192 " + "time [s]");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        result.getXYPlot().setDomainAxis(xAxis);
        result.getXYPlot().setRangeAxis(yAxis);
        Length minimumPosition = Length.ZERO;
        Length maximumPosition = new Length(getCumulativeLength(-1), LengthUnit.SI);
        configureAxis(result.getXYPlot().getRangeAxis(), DoubleScalar.minus(maximumPosition, minimumPosition).getSI());
        // final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) result.getXYPlot().getRenderer();
        MyRenderer renderer = new MyRenderer(false, true);
        result.getXYPlot().setRenderer(renderer);
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        renderer.setBaseShape(new Line2D.Float(0, 0, 0, 0));
        final ChartPanel cp = new ChartPanel(result);
        cp.setMouseWheelEnabled(true);
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    statusLabel.setText(" ");
                    return;
                }
                String value = "";
                /*-
                XYDataset dataset = plot.getDataset();
                double bestDistance = Double.MAX_VALUE;
                Trajectory bestTrajectory = null;
                final int mousePrecision = 5;
                java.awt.geom.Point2D.Double mousePoint = new java.awt.geom.Point2D.Double(t, distance);
                double lowTime =
                        plot.getDomainAxis().java2DToValue(p.getX() - mousePrecision, pi.getDataArea(),
                                plot.getDomainAxisEdge()) - 1;
                double highTime =
                        plot.getDomainAxis().java2DToValue(p.getX() + mousePrecision, pi.getDataArea(),
                                plot.getDomainAxisEdge()) + 1;
                double lowDistance =
                        plot.getRangeAxis().java2DToValue(p.getY() + mousePrecision, pi.getDataArea(),
                                plot.getRangeAxisEdge()) - 20;
                double highDistance =
                        plot.getRangeAxis().java2DToValue(p.getY() - mousePrecision, pi.getDataArea(),
                                plot.getRangeAxisEdge()) + 20;
                // System.out.println(String.format("Searching area t[%.1f-%.1f], x[%.1f,%.1f]", lowTime, highTime,
                // lowDistance, highDistance));
                for (Trajectory trajectory : this.trajectories)
                {
                    java.awt.geom.Point2D.Double[] clippedTrajectory =
                            trajectory.clipTrajectory(lowTime, highTime, lowDistance, highDistance);
                    if (null == clippedTrajectory)
                        continue;
                    java.awt.geom.Point2D.Double prevPoint = null;
                    for (java.awt.geom.Point2D.Double trajectoryPoint : clippedTrajectory)
                    {
                        if (null != prevPoint)
                        {
                            double thisDistance = Planar.distancePolygonToPoint(clippedTrajectory, mousePoint);
                            if (thisDistance < bestDistance)
                            {
                                bestDistance = thisDistance;
                                bestTrajectory = trajectory;
                            }
                        }
                        prevPoint = trajectoryPoint;
                    }
                }
                if (null != bestTrajectory)
                {
                    for (SimulatedObject so : indices.keySet())
                        if (this.trajectories.get(indices.get(so)) == bestTrajectory)
                        {
                            Point2D.Double bestPosition = bestTrajectory.getEstimatedPosition(t);
                            if (null == bestPosition)
                                continue;
                            value =
                                    String.format(
                                            Main.locale,
                                            ": vehicle %s; location on measurement path at t=%.1fs: "
                                            + "longitudinal %.1fm, lateral %.1fm",
                                            so.toString(), t, bestPosition.x, bestPosition.y);
                        }
                }
                else
                    value = "";
                 */
                statusLabel.setText(String.format("t=%.0fs, distance=%.0fm%s", domainValue, rangeValue, value));
            }
        };
        cp.addMouseMotionListener(ph);
        cp.addMouseListener(ph);
        container.add(cp, BorderLayout.CENTER);
        // TODO ensure that shapes for all the data points don't get allocated.
        // Currently JFreeChart allocates many megabytes of memory for Ellipses that are never drawn.
        JPopupMenu popupMenu = cp.getPopupMenu();
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(StandAloneChartWindow.createMenuItem(this));
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final void reGraph()
    {

        SwingUtilities.invokeLater(new Runnable()
        {

            @SuppressWarnings({ "synthetic-access", "unqualified-field-access" })
            @Override
            public void run()
            {
                for (DatasetChangeListener dcl : getListenerList().getListeners(DatasetChangeListener.class))
                {
                    if (dcl instanceof XYPlot)
                    {
                        Time simulatorTime = simulator.getSimulatorTime().getTime();
                        if (getMaximumTime().lt(simulatorTime))
                        {
                            setMaximumTime(simulatorTime);
                        }
                        configureAxis(((XYPlot) dcl).getDomainAxis(), maximumTime.getSI());
                    }
                }
                shouldGenerateNewCurves = true;
                notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
            }
        });
    }

    /**
     * Configure the range of an axis.
     * @param valueAxis ValueAxis
     * @param range double; the upper bound of the axis
     */
    static void configureAxis(final ValueAxis valueAxis, final double range)
    {
        valueAxis.setUpperBound(range);
        valueAxis.setLowerMargin(0);
        valueAxis.setUpperMargin(0);
        valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        valueAxis.setAutoRange(true);
        valueAxis.setAutoRangeMinimumSize(range);
        valueAxis.centerRange(range / 2);
        // System.out.println("centerRange is " + (range / 2));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // not yet
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        if (null == this.curves || this.shouldGenerateNewCurves)
        {
            List<TrajectoryAndLengthOffset> newCurves = new ArrayList<>();
            double cumulativeLength = 0;
            for (KpiLaneDirection kld : this.lanes)
            {
                TrajectoryGroup tg = this.roadSampler.getTrajectoryGroup(kld);
                if (null == tg)
                {
                    continue;
                }
                for (org.opentrafficsim.kpi.sampling.Trajectory trajectory : tg.getTrajectories())
                {
                    newCurves.add(new TrajectoryAndLengthOffset(trajectory, cumulativeLength));
                }
                cumulativeLength += kld.getLaneData().getLength().si;
            }
            this.curves = newCurves;
            this.shouldGenerateNewCurves = false;
        }
        return this.curves.size();
    }

    /** {@inheritDoc} */
    @Override
    public final Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public final int indexOf(final Comparable seriesKey)
    {
        if (seriesKey instanceof Integer)
        {
            return (Integer) seriesKey;
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public final DatasetGroup getGroup()
    {
        return this.datasetGroup;
    }

    /** {@inheritDoc} */
    @Override
    public final void setGroup(final DatasetGroup group)
    {
        this.datasetGroup = group;
    }

    /** {@inheritDoc} */
    @Override
    public final DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /**
     * Storage for a trajectory and a length.
     */
    class TrajectoryAndLengthOffset
    {
        /** The trajectory. */
        private final org.opentrafficsim.kpi.sampling.Trajectory trajectory;

        /** The length. */
        private final double lengthOffset;

        /**
         * Construct a new TrajectoryAndLengthOffset object.
         * @param trajectory org.opentrafficsim.kpi.sampling.Trajectory; the trajectory
         * @param lengthOffset double; the length from the beginning of the sampled path to the start of the lane to which the
         *            trajectory belongs
         */
        TrajectoryAndLengthOffset(final org.opentrafficsim.kpi.sampling.Trajectory trajectory,
                final double lengthOffset)
        {
            this.trajectory = trajectory;
            this.lengthOffset = lengthOffset;
        }

        /**
         * Retrieve the trajectory.
         * @return org.opentrafficsim.kpi.sampling.Trajectory; the trajectory
         */
        public org.opentrafficsim.kpi.sampling.Trajectory getTrajectory()
        {
            return this.trajectory;
        }

        /**
         * Retrieve the lengthOffset.
         * @return double; the lengthOffset
         */
        public double getLengthOffset()
        {
            return this.lengthOffset;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TrajectoryAndLengthOffset [trajectory=" + this.trajectory + ", lengthOffset=" + this.lengthOffset + "]";
        }

    }

    /**
     * Retrieve the Nth trajectory.
     * @param index int; the index of the requested trajectory
     * @return org.opentrafficsim.kpi.sampling.Trajectory; the Nth trajectory, or null if the provided index is out of range
     */
    private TrajectoryAndLengthOffset getTrajectory(final int index)
    {
        if (index < 0)
        {
            System.err.println("Negative index (" + index + ")");
            return null;
        }
        while (null == this.curves)
        {
            getSeriesCount();
        }
        if (index >= this.curves.size())
        {
            System.err.println("index out of range (" + index + " >= " + this.curves.size() + ")");
            return null;
        }
        return this.curves.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return getTrajectory(series).getTrajectory().size();
    }

    /** {@inheritDoc} */
    @Override
    public final Number getX(final int series, final int item)
    {
        double v = getXValue(series, item);
        if (Double.isNaN(v))
        {
            return null;
        }
        return v;
    }

    /** {@inheritDoc} */
    @Override
    public final double getXValue(final int series, final int item)
    {
        TrajectoryAndLengthOffset tal = getTrajectory(series);
        try
        {
            return tal.getTrajectory().getT(item);
        }
        catch (SamplingException exception)
        {
            exception.printStackTrace();
            System.out.println("index out of bounds: item=" + item + ", limit=" + tal.getTrajectory().size());
            return Double.NaN;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Number getY(final int series, final int item)
    {
        double v = getYValue(series, item);
        if (Double.isNaN(v))
        {
            return null;
        }
        return v;
    }

    /** {@inheritDoc} */
    @Override
    public final double getYValue(final int series, final int item)
    {
        TrajectoryAndLengthOffset tal = getTrajectory(series);
        try
        {
            return tal.getTrajectory().getX(item) + tal.getLengthOffset();
        }
        catch (SamplingException exception)
        {
            exception.printStackTrace();
            System.out.println("index out of bounds: item=" + item + ", limit=" + tal.getTrajectory().size());
            return Double.NaN;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrajectoryPlot [sampleInterval=" + this.sampleInterval + ", path=" + getPath() + ", cumulativeLengths.length="
                + this.cumulativeLengths.length + ", maximumTime=" + this.maximumTime + ", caption=" + getCaption() + "]";
    }

}
