package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
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
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.TimedEvent;

/**
 * Trajectory plot.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends JFrame
        implements ActionListener, XYDataset, MultipleViewerChart, LaneBasedGTUSampler, EventListenerInterface

{
    /** */
    private static final long serialVersionUID = 20140724L;

    /** Sample interval of this TrajectoryPlot. */
    private final double sampleInterval;

    /**
     * @return sampleInterval
     */
    public final double getSampleInterval()
    {
        return this.sampleInterval;
    }

    /** The series of Lanes that provide the data for this TrajectoryPlot. */
    private final ArrayList<Lane> path;

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
    private Time maximumTime = new Time(300, TimeUnit.SECOND);

    /**
     * @return maximumTime
     */
    public final Time getMaximumTime()
    {
        return this.maximumTime;
    }

    /**
     * @param maximumTime set maximumTime
     */
    public final void setMaximumTime(final Time maximumTime)
    {
        this.maximumTime = maximumTime;
    }

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /** Name of the chart. */
    private final String caption;

    /**
     * Create a new TrajectoryPlot.
     * @param caption String; the text to show above the TrajectoryPlot
     * @param sampleInterval DoubleScalarRel&lt;TimeUnit&gt;; the time between samples of this TrajectoryPlot
     * @param path ArrayList&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     */
    public TrajectoryPlot(final String caption, final Duration sampleInterval, final List<Lane> path)
    {
        this.sampleInterval = sampleInterval.si;
        this.path = new ArrayList<Lane>(path); // make a defensive copy
        double[] endLengths = new double[path.size()];
        double cumulativeLength = 0;
        for (int i = 0; i < path.size(); i++)
        {
            Lane lane = path.get(i);
            lane.addListener(this, Lane.GTU_ADD_EVENT, true);
            lane.addListener(this, Lane.GTU_REMOVE_EVENT, true);
            try
            {
                // register the current GTUs on the lanes (if any) for statistics sampling.
                for (LaneBasedGTU gtu : lane.getGtuList())
                {
                    notify(new TimedEvent<OTSSimTimeDouble>(Lane.GTU_ADD_EVENT, lane, new Object[] { gtu.getId(), gtu },
                            gtu.getSimulator().getSimulatorTime()));
                }
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
            cumulativeLength += lane.getLength().getSI();
            endLengths[i] = cumulativeLength;
        }
        this.cumulativeLengths = endLengths;
        this.caption = caption;
        createChart(this);
        this.reGraph(); // fixes the domain axis
    }

    /** the GTUs that might be of interest to gather statistics about. */
    private Set<LaneBasedGTU> gtusOfInterest = new HashSet<>();

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void notify(final EventInterface event) throws RemoteException
    {
        LaneBasedGTU gtu;
        if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            gtu = (LaneBasedGTU) content[1];
            if (!this.gtusOfInterest.contains(gtu))
            {
                this.gtusOfInterest.add(gtu);
                gtu.addListener(this, LaneBasedGTU.MOVE_EVENT);
            }
        }
        else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            gtu = (LaneBasedGTU) content[1];
            boolean interest = false;
            for (Lane lane : gtu.getLanes().keySet())
            {
                if (this.path.contains(lane))
                {
                    interest = true;
                }
            }
            if (!interest)
            {
                this.gtusOfInterest.remove(gtu);
                gtu.removeListener(this, LaneBasedGTU.MOVE_EVENT);
            }
        }
        else if (event.getType().equals(LaneBasedGTU.MOVE_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            Lane lane = (Lane) content[6];
            Length posOnLane = (Length) content[7];
            gtu = (LaneBasedGTU) event.getSource();
            if (this.path.contains(lane))
            {
                addData(gtu, lane, posOnLane.si);
            }
        }
    }

    /**
     * Create the visualization.
     * @param container JFrame; the JFrame that will be filled with chart and the status label
     * @return JFreeChart; the visualization
     */
    private JFreeChart createChart(final JFrame container)
    {
        final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        container.add(statusLabel, BorderLayout.SOUTH);
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        final JFreeChart result =
                ChartFactory.createXYLineChart(this.caption, "", "", this, PlotOrientation.VERTICAL, false, false, false);
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
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) result.getXYPlot().getRenderer();
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

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed).
     */
    public final void reGraph()
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            if (dcl instanceof XYPlot)
            {
                configureAxis(((XYPlot) dcl).getDomainAxis(), this.maximumTime.getSI());
            }
        }
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
    }

    /**
     * Notify interested parties of an event affecting this TrajectoryPlot.
     * @param event DatasetChangedEvent
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            dcl.datasetChanged(event);
        }
    }

    /**
     * Configure the range of an axis.
     * @param valueAxis ValueAxis
     * @param range double; the upper bound of the axis
     */
    private static void configureAxis(final ValueAxis valueAxis, final double range)
    {
        valueAxis.setUpperBound(range);
        valueAxis.setLowerMargin(0);
        valueAxis.setUpperMargin(0);
        valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        valueAxis.setAutoRange(true);
        valueAxis.setAutoRangeMinimumSize(range);
        valueAxis.centerRange(range / 2);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // not yet
    }

    /** All stored trajectories. */
    private HashMap<String, Trajectory> trajectories = new HashMap<String, Trajectory>();

    /** Quick access to the Nth trajectory. */
    private ArrayList<Trajectory> trajectoryIndices = new ArrayList<Trajectory>();

    /**
     * Add data for a GTU on a lane to this graph.
     * @param gtu the gtu to add the data for
     * @param lane the lane on which the GTU is registered
     * @param posOnLane the position on the lane as a double si Length
     */
    protected final void addData(final LaneBasedGTU gtu, final Lane lane, final double posOnLane)
    {
        int index = this.path.indexOf(lane);
        if (index < 0)
        {
            // error -- silently ignore for now. Graphs should not cause errors.
            System.err.println("TrajectoryPlot: GTU " + gtu.getId() + " is not registered on lane " + lane.toString());
            return;
        }
        double lengthOffset = index == 0 ? 0 : this.cumulativeLengths[index - 1];

        String key = gtu.getId();
        Trajectory carTrajectory = this.trajectories.get(key);
        if (null == carTrajectory)
        {
            // Create a new Trajectory for this GTU
            carTrajectory = new Trajectory(key);
            this.trajectoryIndices.add(carTrajectory);
            this.trajectories.put(key, carTrajectory);
        }
        try
        {
            carTrajectory.addSegment(gtu, lane, lengthOffset, posOnLane);
        }
        catch (NetworkException | GTUException exception)
        {
            // error -- silently ignore for now. Graphs should not cause errors.
            System.err.println("TrajectoryPlot: GTU " + gtu.getId() + " on lane " + lane.toString() + " caused exception "
                    + exception.getMessage());
        }
    }

    /**
     * Store trajectory data.
     * <p>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Trajectory implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20140000L;

        /** Time of (current) end of trajectory. */
        private Time currentEndTime;

        /**
         * Retrieve the last registered time of this Trajectory.
         * @return currentEndTime
         */
        public final Time getCurrentEndTime()
        {
            return this.currentEndTime;
        }

        /** Last registered position in trajectory. */
        private Double lastPosition;

        /**
         * Retrieve the current end position of this Trajectory.
         * @return currentEndPosition
         */
        public final Double getLastPosition()
        {
            return this.lastPosition;
        }

        /** ID of the GTU. */
        private final Object id;

        /**
         * Retrieve the id of this Trajectory.
         * @return Object; the id of this Trajectory
         */
        public final Object getId()
        {
            return this.id;
        }

        /** Storage for the position of the car. */
        private ArrayList<Double> positions = new ArrayList<Double>();

        /** Time sample of first sample in positions (successive entries will each be one sampleTime later). */
        private int firstSample;

        /**
         * Construct a Trajectory.
         * @param id Object; Id of the new Trajectory
         */
        Trajectory(final Object id)
        {
            this.id = id;
        }

        /**
         * Add a trajectory segment and update the currentEndTime and currentEndPosition.
         * @param gtu AbstractLaneBasedGTU; the GTU whose currently committed trajectory segment must be added
         * @param lane Lane; the Lane that the positionOffset is valid for
         * @param positionOffset double; offset needed to convert the position in the current Lane to a position on the
         *            trajectory
         * @param posOnLane the position on the lane in meters (si)
         * @throws NetworkException when car is not on lane anymore
         * @throws GTUException on problems obtaining data from the GTU
         */
        public final void addSegment(final LaneBasedGTU gtu, final Lane lane, final double positionOffset,
                final double posOnLane) throws NetworkException, GTUException
        {
            // for now, just sample ONE data point.
            Double position = posOnLane + positionOffset;
            final int sample = (int) Math.ceil(gtu.getOperationalPlan().getStartTime().si / getSampleInterval());
            if (this.positions.size() == 0)
            {
                this.firstSample = sample;
            }
            while (sample - this.firstSample > this.positions.size())
            {
                // insert nulls as place holders for unsampled data (usually because vehicle was in a parallel Lane)
                this.positions.add(null);
            }
            if (this.lastPosition != null && Math.abs(this.lastPosition - position) > 0.9 * getCumulativeLength(-1))
            {
                // wrap around... probably circular lane.
                position = null;
            }
            this.positions.add(position);
            this.lastPosition = position;

            this.currentEndTime = gtu.getOperationalPlan().getEndTime();
            if (this.currentEndTime.gt(getMaximumTime()))
            {
                setMaximumTime(this.currentEndTime);
            }

            /*-
            try
            {
                final int startSample =
                        (int) Math.ceil(car.getOperationalPlan().getStartTime().getSI() / getSampleInterval());
                final int endSample =
                        (int) (Math.ceil(car.getOperationalPlan().getEndTime().getSI() / getSampleInterval()));
                for (int sample = startSample; sample < endSample; sample++)
                {
                    Time sampleTime = new Time(sample * getSampleInterval(), TimeUnit.SI);
                    Double position = car.position(lane, car.getReference(), sampleTime).getSI() + positionOffset;
                    if (this.positions.size() > 0 && null != this.currentEndPosition
                            && position < this.currentEndPosition.getSI() - 0.001)
                    {
                        if (0 != positionOffset)
                        {
                            // System.out.println("Already added " + car);
                            break;
                        }
                        // System.out.println("inserting null for " + car);
                        position = null; // Wrapping on circular path?
                    }
                    if (this.positions.size() == 0)
                    {
                        this.firstSample = sample;
                    }
                    while (sample - this.firstSample > this.positions.size())
                    {
                        // System.out.println("Inserting nulls");
                        this.positions.add(null); // insert nulls as place holders for unsampled data (usually because
                                                  // vehicle was temporarily in a parallel Lane)
                    }
                    if (null != position && this.positions.size() > sample - this.firstSample)
                    {
                        // System.out.println("Skipping sample " + car);
                        continue;
                    }
                    this.positions.add(position);
                }
                this.currentEndTime = car.getOperationalPlan().getEndTime();
                this.currentEndPosition = new Length(
                        car.position(lane, car.getReference(), this.currentEndTime).getSI() + positionOffset, LengthUnit.SI);
                if (car.getOperationalPlan().getEndTime().gt(getMaximumTime()))
                {
                    setMaximumTime(car.getOperationalPlan().getEndTime());
                }
            }
            catch (Exception e)
            {
                // TODO lane change causes error...
                System.err.println("Trajectoryplot caught unexpected Exception: " + e.getMessage());
                e.printStackTrace();
            }
            */
        }

        /**
         * Retrieve the number of samples in this Trajectory.
         * @return Integer; number of positions in this Trajectory
         */
        public int size()
        {
            return this.positions.size();
        }

        /**
         * @param item Integer; the sample number
         * @return Double; the time of the sample indexed by item
         */
        public double getTime(final int item)
        {
            return (item + this.firstSample) * getSampleInterval();
        }

        /**
         * @param item Integer; the sample number
         * @return Double; the position indexed by item
         */
        public double getDistance(final int item)
        {
            Double distance = this.positions.get(item);
            if (null == distance)
            {
                return Double.NaN;
            }
            return this.positions.get(item);
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Trajectory [currentEndTime=" + this.currentEndTime + ", currentEndPosition=" + this.lastPosition
                    + ", id=" + this.id + ", positions.size=" + this.positions.size() + ", firstSample=" + this.firstSample
                    + "]";
        }
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return this.trajectories.size();
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
    public final void addChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
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

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return this.trajectoryIndices.get(series).size();
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
        return this.trajectoryIndices.get(series).getTime(item);
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
        return this.trajectoryIndices.get(series).getDistance(item);
    }

    /** {@inheritDoc} */
    @Override
    public final JFrame addViewer()
    {
        JFrame result = new JFrame(this.caption);
        result.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JFreeChart newChart = createChart(result);
        newChart.setTitle((String) null);
        addChangeListener(newChart.getPlot());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrajectoryPlot [sampleInterval=" + this.sampleInterval + ", path=" + this.path + ", cumulativeLengths.length="
                + this.cumulativeLengths.length + ", maximumTime=" + this.maximumTime + ", caption=" + this.caption
                + ", trajectories.size=" + this.trajectories.size() + "]";
    }

}
