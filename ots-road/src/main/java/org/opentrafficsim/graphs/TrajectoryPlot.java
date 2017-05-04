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
        this.roadSampler =
                null == sampleInterval ? new RoadSampler(simulator) : new RoadSampler(simulator,
                        Frequency.createSI(1 / sampleInterval.si));
        this.lanes = new ArrayList<>();
        for (Lane lane : path)
        {
            KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
            SpaceTimeRegion spaceTimeRegion =
                    new SpaceTimeRegion(kpiLaneDirection, Length.ZERO, lane.getLength(), Time.ZERO,
                            Time.createSI(Double.MAX_VALUE));
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
            // lane.addListener(this, Lane.GTU_ADD_EVENT, true);
            // lane.addListener(this, Lane.GTU_REMOVE_EVENT, true);
            // try
            // {
            // // Register the GTUs currently (i.e. already) on the lane (if any) for statistics sampling.
            // for (LaneBasedGTU gtu : lane.getGtuList())
            // {
            // notify(new TimedEvent<OTSSimTimeDouble>(Lane.GTU_ADD_EVENT, lane, new Object[] { gtu.getId(), gtu }, gtu
            // .getSimulator().getSimulatorTime()));
            // }
            // }
            // catch (RemoteException exception)
            // {
            // exception.printStackTrace();
            // }
            cumulativeLength += lane.getLength().getSI();
            endLengths[i] = cumulativeLength;
        }
        this.cumulativeLengths = endLengths;
        setChart(createChart(this));
        this.reGraph(); // fixes the domain axis
        // if (null != this.sampleInterval)
        // {
        // try
        // {
        // this.simulator.scheduleEventRel(Duration.ZERO, this, this, "sample", null);
        // }
        // catch (SimRuntimeException exception)
        // {
        // exception.printStackTrace();
        // }
        // }
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
    }

    /** {@inheritDoc} */
    @Override
    public final GraphType getGraphType()
    {
        return GraphType.TRAJECTORY;
    }

    /**
     * Sample all the GTUs on the observed lanes.
     */
    // public final void sample()
    // {
    // Time now = this.simulator.getSimulatorTime().getTime();
    // for (LaneBasedGTU gtu : this.gtusOfInterest)
    // {
    // try
    // {
    // Map<Lane, Length> positions = gtu.positions(gtu.getReference(), now);
    // int hits = 0;
    // for (Lane lane : positions.keySet())
    // {
    // if (getPath().contains(lane))
    // {
    // Length position = positions.get(lane);
    // if (position.si >= 0 && position.si <= lane.getLength().si)
    // {
    // addData(gtu, lane, positions.get(lane).si);
    // hits++;
    // }
    // }
    // }
    // if (1 != hits)
    // {
    // System.err.println("GTU " + gtu + " scored " + hits + " (expected 1 hit)");
    // }
    // }
    // catch (GTUException exception)
    // {
    // exception.printStackTrace();
    // }
    // }
    // // Schedule the next sample
    // try
    // {
    // this.simulator.scheduleEventRel(this.sampleInterval, this, this, "sample", null);
    // }
    // catch (SimRuntimeException exception)
    // {
    // exception.printStackTrace();
    // }
    // }

    /** The GTUs that might be of interest to gather statistics about. */
    // private Set<LaneBasedGTU> gtusOfInterest = new HashSet<>();

    // /** {@inheritDoc} */
    // @Override
    // @SuppressWarnings("checkstyle:designforextension")
    // public void notify(final EventInterface event) throws RemoteException
    // {
    // LaneBasedGTU gtu;
    // if (event.getType().equals(Lane.GTU_ADD_EVENT))
    // {
    // Object[] content = (Object[]) event.getContent();
    // gtu = (LaneBasedGTU) content[1];
    // if (!this.gtusOfInterest.contains(gtu))
    // {
    // this.gtusOfInterest.add(gtu);
    // if (null == this.sampleInterval)
    // {
    // gtu.addListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
    // }
    // }
    // }
    // else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
    // {
    // Object[] content = (Object[]) event.getContent();
    // gtu = (LaneBasedGTU) content[1];
    // Lane lane = null;
    // try
    // {
    // lane = gtu.getReferencePosition().getLane();
    // }
    // catch (GTUException exception)
    // {
    // // ignore - lane will be null
    // }
    // if (lane == null || !getPath().contains(lane))
    // {
    // this.gtusOfInterest.remove(gtu);
    // if (null != this.sampleInterval)
    // {
    // gtu.removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
    // }
    // else
    // {
    // String key = gtu.getId();
    // VariableSampleRateTrajectory carTrajectory = (VariableSampleRateTrajectory) this.trajectories.get(key);
    // if (null != carTrajectory)
    // {
    // carTrajectory.recordGTULeftTrajectoryEvent();
    // }
    // }
    // }
    // }
    // else if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
    // {
    // Object[] content = (Object[]) event.getContent();
    // Lane lane = (Lane) content[6];
    // Length posOnLane = (Length) content[7];
    // gtu = (LaneBasedGTU) event.getSource();
    // if (getPath().contains(lane))
    // {
    // addData(gtu, lane, posOnLane.si);
    // }
    // }
    // }

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

    /** All stored trajectories. */
    // private HashMap<String, Trajectory> trajectories = new HashMap<String, Trajectory>();

    /** Quick access to the Nth trajectory. */
    // private ArrayList<Trajectory> trajectoryIndices = new ArrayList<Trajectory>();

    /**
     * Add data for a GTU on a lane to this graph.
     * @param gtu the gtu to add the data for
     * @param lane the lane on which the GTU is registered
     * @param posOnLane the position on the lane as a double si Length
     */
    // protected final void addData(final LaneBasedGTU gtu, final Lane lane, final double posOnLane)
    // {
    // int index = getPath().indexOf(lane);
    // if (index < 0)
    // {
    // // error -- silently ignore for now. Graphs should not cause errors.
    // System.err.println("TrajectoryPlot: GTU " + gtu.getId() + " is not registered on lane " + lane.toString());
    // return;
    // }
    // double lengthOffset = index == 0 ? 0 : this.cumulativeLengths[index - 1];
    //
    // String key = gtu.getId();
    // Trajectory carTrajectory = this.trajectories.get(key);
    // if (null == carTrajectory)
    // {
    // // Create a new Trajectory for this GTU
    // carTrajectory =
    // null == this.sampleInterval ? new VariableSampleRateTrajectory(key) : new FixedSampleRateTrajectory(key);
    // this.trajectoryIndices.add(carTrajectory);
    // this.trajectories.put(key, carTrajectory);
    // }
    // try
    // {
    // carTrajectory.addSample(gtu, lane, lengthOffset + posOnLane);
    // }
    // catch (NetworkException | GTUException exception)
    // {
    // // error -- silently ignore for now. Graphs should not cause errors.
    // System.err.println("TrajectoryPlot: GTU " + gtu.getId() + " on lane " + lane.toString() + " caused exception "
    // + exception.getMessage());
    // }
    // }

    // /**
    // * Common interface for both (all?) types of trajectories.
    // */
    // interface Trajectory
    // {
    // /**
    // * Retrieve the time of the last stored event.
    // * @return Time; the time of the last stored event
    // */
    // Time getCurrentEndTime();
    //
    // /**
    // * Retrieve the last recorded non-null position, or null if no non-null positions have been recorded yet.
    // * @return Double; the last recorded position of this Trajectory in meters
    // */
    // Double getLastPosition();
    //
    // /**
    // * Retrieve the id of this Trajectory.
    // * @return Object; the id of this Trajectory
    // */
    // String getId();
    //
    // /**
    // * Add a trajectory segment sample and update the currentEndTime and currentEndPosition.
    // * @param gtu AbstractLaneBasedGTU; the GTU whose currently committed trajectory segment must be added
    // * @param lane Lane; the Lane that the positionOffset is valid for
    // * @param position Double; distance in meters from the start of the trajectory
    // * @throws NetworkException when car is not on lane anymore
    // * @throws GTUException on problems obtaining data from the GTU
    // */
    // void addSample(LaneBasedGTU gtu, Lane lane, double position) throws NetworkException, GTUException;
    //
    // /**
    // * Retrieve the number of stored samples in this Trajectory.
    // * @return int; number of stored samples
    // */
    // int size();
    //
    // /**
    // * Return the time of the Nth stored sample.
    // * @param item int; the index of the sample
    // * @return double; the time of the sample
    // */
    // double getTime(int item);
    //
    // /**
    // * Return the distance of the Nth stored sample.
    // * @param item int; the index of the sample
    // * @return double; the distance of the sample
    // */
    // double getDistance(int item);
    //
    // }
    //
    // /**
    // * Store trajectory data for use with a variable sample rate.
    // * <p>
    // * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
    // */
    // class VariableSampleRateTrajectory implements Trajectory, Serializable
    // {
    // /** */
    // private static final long serialVersionUID = 20140000L;
    //
    // /** Time of (current) end of trajectory. */
    // private Time currentEndTime;
    //
    // /** ID of the GTU. */
    // private final String id;
    //
    // /** Storage for the samples of the GTU. */
    // private ArrayList<DistanceAndTime> samples = new ArrayList<DistanceAndTime>();
    //
    // /**
    // * Construct a new VariableSamplerateTrajectory.
    // * @param id String; id of the new Trajectory (id of the GTU)
    // */
    // VariableSampleRateTrajectory(final String id)
    // {
    // this.id = id;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public Time getCurrentEndTime()
    // {
    // return this.currentEndTime;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public Double getLastPosition()
    // {
    // return null;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public String getId()
    // {
    // return this.id;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public void addSample(final LaneBasedGTU gtu, final Lane lane, final double position) throws NetworkException,
    // GTUException
    // {
    // if (this.samples.size() > 0)
    // {
    // DistanceAndTime lastSample = this.samples.get(this.samples.size() - 1);
    // if (null != lastSample)
    // {
    // Double lastPosition = lastSample.getDistance();
    // if (null != lastPosition && Math.abs(lastPosition - position) > 0.9 * getCumulativeLength(-1))
    // {
    // // wrap around... probably circular lane, insert a GTU left trajectory event.
    // recordGTULeftTrajectoryEvent();
    // }
    // }
    // }
    // this.currentEndTime = gtu.getSimulator().getSimulatorTime().getTime();
    // this.samples.add(new DistanceAndTime(position, this.currentEndTime.si));
    // if (gtu.getSimulator().getSimulatorTime().getTime().gt(getMaximumTime()))
    // {
    // setMaximumTime(gtu.getSimulator().getSimulatorTime().getTime());
    // }
    // }
    //
    // /**
    // * Store that the GTU went off of the trajectory.
    // */
    // public void recordGTULeftTrajectoryEvent()
    // {
    // this.samples.add(null);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public int size()
    // {
    // return this.samples.size();
    // }
    //
    // /**
    // * Retrieve the Nth sample.
    // * @param item int; the number of the sample
    // * @return DistanceAndTime; the Nth sample (samples can be null to indicate that GTU went off the trajectory).
    // */
    // private DistanceAndTime getSample(final int item)
    // {
    // return this.samples.get(item);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public double getTime(final int item)
    // {
    // DistanceAndTime sample = getSample(item);
    // if (null == sample)
    // {
    // return Double.NaN;
    // }
    // return this.samples.get(item).getTime();
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public double getDistance(final int item)
    // {
    // DistanceAndTime sample = getSample(item);
    // if (null == sample)
    // {
    // return Double.NaN;
    // }
    // return sample.getDistance();
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public String toString()
    // {
    // return "VariableSampleRateTrajectory [id=" + this.id + ", currentEndTime=" + this.currentEndTime + "]";
    // }
    //
    // /**
    // * Store a position and a time.
    // */
    // class DistanceAndTime
    // {
    // /** The position [m]. */
    // private final double distance;
    //
    // /** The time [s]. */
    // private final double time;
    //
    // /**
    // * Construct a new DistanceAndTime object.
    // * @param distance double; the position
    // * @param time double; the time
    // */
    // DistanceAndTime(final double distance, final double time)
    // {
    // this.distance = distance;
    // this.time = time;
    // }
    //
    // /**
    // * Retrieve the position.
    // * @return double; the position
    // */
    // public double getDistance()
    // {
    // return this.distance;
    // }
    //
    // /**
    // * Retrieve the time.
    // * @return double; the time
    // */
    // public double getTime()
    // {
    // return this.time;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public String toString()
    // {
    // return "DistanceAndTime [distance=" + this.distance + ", time=" + this.time + "]";
    // }
    //
    // }
    // }
    //
    // /**
    // * Store trajectory data for use with a fixed sample rate.
    // * <p>
    // * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
    // */
    // class FixedSampleRateTrajectory implements Trajectory, Serializable
    // {
    // /** */
    // private static final long serialVersionUID = 20140000L;
    //
    // /** Time of (current) end of trajectory. */
    // private Time currentEndTime;
    //
    // /** ID of the GTU. */
    // private final String id;
    //
    // /** Storage for the position of the GTU. */
    // private ArrayList<Double> positions = new ArrayList<Double>();
    //
    // /** Sample number of sample with index 0 in positions (following entries will each be one sampleTime later). */
    // private int firstSample;
    //
    // /**
    // * Construct a FixedSampleRateTrajectory.
    // * @param id String; id of the new Trajectory (id of the GTU)
    // */
    // FixedSampleRateTrajectory(final String id)
    // {
    // this.id = id;
    // }
    //
    // /** {@inheritDoc} */
    // public final Time getCurrentEndTime()
    // {
    // return this.currentEndTime;
    // }
    //
    // /** {@inheritDoc} */
    // public final Double getLastPosition()
    // {
    // for (int i = this.positions.size(); --i >= 0;)
    // {
    // Double result = this.positions.get(i);
    // if (null != result)
    // {
    // return result;
    // }
    // }
    // return null;
    // }
    //
    // /** {@inheritDoc} */
    // public final String getId()
    // {
    // return this.id;
    // }
    //
    // /** {@inheritDoc} */
    // public final void addSample(final LaneBasedGTU gtu, final Lane lane, final double position) throws NetworkException,
    // GTUException
    // {
    // final int sample = (int) Math.ceil(gtu.getOperationalPlan().getStartTime().si / getSampleInterval().si);
    // if (0 == this.positions.size())
    // {
    // this.firstSample = sample;
    // }
    // while (sample - this.firstSample > this.positions.size())
    // {
    // // insert nulls as place holders for unsampled data (usually because vehicle was in a parallel Lane)
    // this.positions.add(null);
    // }
    // Double adjustedPosition = position;
    // Double lastPosition = this.positions.size() > 0 ? this.positions.get(this.positions.size() - 1) : null;
    // if (null != lastPosition && Math.abs(lastPosition - position) > 0.9 * getCumulativeLength(-1))
    // {
    // // wrap around... probably circular lane.
    // adjustedPosition = null;
    // }
    // this.positions.add(adjustedPosition);
    //
    // /*-
    // try
    // {
    // final int startSample =
    // (int) Math.ceil(car.getOperationalPlan().getStartTime().getSI() / getSampleInterval());
    // final int endSample =
    // (int) (Math.ceil(car.getOperationalPlan().getEndTime().getSI() / getSampleInterval()));
    // for (int sample = startSample; sample < endSample; sample++)
    // {
    // Time sampleTime = new Time(sample * getSampleInterval(), TimeUnit.SI);
    // Double position = car.position(lane, car.getReference(), sampleTime).getSI() + positionOffset;
    // if (this.positions.size() > 0 && null != this.currentEndPosition
    // && position < this.currentEndPosition.getSI() - 0.001)
    // {
    // if (0 != positionOffset)
    // {
    // // System.out.println("Already added " + car);
    // break;
    // }
    // // System.out.println("inserting null for " + car);
    // position = null; // Wrapping on circular path?
    // }
    // if (this.positions.size() == 0)
    // {
    // this.firstSample = sample;
    // }
    // while (sample - this.firstSample > this.positions.size())
    // {
    // // System.out.println("Inserting nulls");
    // this.positions.add(null); // insert nulls as place holders for unsampled data (usually because
    // // vehicle was temporarily in a parallel Lane)
    // }
    // if (null != position && this.positions.size() > sample - this.firstSample)
    // {
    // // System.out.println("Skipping sample " + car);
    // continue;
    // }
    // this.positions.add(position);
    // }
    // this.currentEndTime = car.getOperationalPlan().getEndTime();
    // this.currentEndPosition = new Length(
    // car.position(lane, car.getReference(), this.currentEndTime).getSI() + positionOffset, LengthUnit.SI);
    // }
    // catch (Exception e)
    // {
    // // TODO lane change causes error...
    // System.err.println("Trajectoryplot caught unexpected Exception: " + e.getMessage());
    // e.printStackTrace();
    // }
    // */
    // if (gtu.getSimulator().getSimulatorTime().getTime().gt(getMaximumTime()))
    // {
    // setMaximumTime(gtu.getSimulator().getSimulatorTime().getTime());
    // }
    // }
    //
    // /** {@inheritDoc} */
    // public int size()
    // {
    // return this.positions.size();
    // }
    //
    // /** {@inheritDoc} */
    // public double getTime(final int item)
    // {
    // return (item + this.firstSample) * getSampleInterval().si;
    // }
    //
    // /**
    // * @param item Integer; the sample number
    // * @return Double; the position indexed by item
    // */
    // public double getDistance(final int item)
    // {
    // Double distance = this.positions.get(item);
    // if (null == distance)
    // {
    // return Double.NaN;
    // }
    // return this.positions.get(item);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final String toString()
    // {
    // return "FixedSampleRateTrajectory [currentEndTime=" + this.currentEndTime + ", id=" + this.id + ", positions.size="
    // + this.positions.size() + ", firstSample=" + this.firstSample + "]";
    // }
    //
    // }

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
        // return this.trajectories.size();
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
        TrajectoryAndLengthOffset(final org.opentrafficsim.kpi.sampling.Trajectory trajectory, final double lengthOffset)
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
        // return this.trajectoryIndices.get(series).size();
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
        // return this.trajectoryIndices.get(series).getTime(item);
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
        // return this.trajectoryIndices.get(series).getDistance(item);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrajectoryPlot [sampleInterval=" + this.sampleInterval + ", path=" + getPath() + ", cumulativeLengths.length="
                + this.cumulativeLengths.length + ", maximumTime=" + this.maximumTime + ", caption=" + getCaption() + "]";
        // +
        // ", trajectories.size="
        // +
        // this.trajectories.size()
        // +
        // "]";
    }

}
