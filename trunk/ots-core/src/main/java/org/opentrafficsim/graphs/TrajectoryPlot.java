package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

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
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

/**
 * Trajectory plot.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends JFrame implements ActionListener, XYDataset, MultipleViewerChart,
        LaneBasedGTUSampler
{
    /** */
    private static final long serialVersionUID = 20140724L;

    /** Sample interval of this TrajectoryPlot. */
    private final DoubleScalar.Rel<TimeUnit> sampleInterval;

    /**
     * @return sampleInterval
     */
    public final DoubleScalar.Rel<TimeUnit> getSampleInterval()
    {
        return this.sampleInterval;
    }

    /** The series of Lanes that provide the data for this TrajectoryPlot. */
    private final ArrayList<Lane> path;

    /** The cumulative lengths of the elements of path. */
    private final DoubleVector.Rel.Dense<LengthUnit> cumulativeLengths;

    /**
     * Retrieve the cumulative length of the sampled path at the end of a path element.
     * @param index int; the index of the path element; if -1, the total length of the path is returned
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the cumulative length at the end of the specified path element
     */
    public final DoubleScalar.Rel<LengthUnit> getCumulativeLength(final int index)
    {
        int useIndex = -1 == index ? this.cumulativeLengths.size() - 1 : index;
        try
        {
            return this.cumulativeLengths.get(useIndex);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        return null; // NOTREACHED
    }

    /** Maximum of the time axis. */
    private DoubleScalar.Abs<TimeUnit> maximumTime = new DoubleScalar.Abs<TimeUnit>(300, TimeUnit.SECOND);

    /**
     * @return maximumTime
     */
    public final DoubleScalar.Abs<TimeUnit> getMaximumTime()
    {
        return this.maximumTime;
    }

    /**
     * @param maximumTime set maximumTime
     */
    public final void setMaximumTime(final DoubleScalar.Abs<TimeUnit> maximumTime)
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
    public TrajectoryPlot(final String caption, final DoubleScalar.Rel<TimeUnit> sampleInterval, final List<Lane> path)
    {
        this.sampleInterval = sampleInterval;
        this.path = new ArrayList<Lane>(path); // make a copy
        double[] endLengths = new double[path.size()];
        double cumulativeLength = 0;
        DoubleVector.Rel.Dense<LengthUnit> lengths = null;
        for (int i = 0; i < path.size(); i++)
        {
            Lane lane = path.get(i);
            lane.addSampler(this);
            cumulativeLength += lane.getLength().getSI();
            endLengths[i] = cumulativeLength;
        }
        try
        {
            lengths = new DoubleVector.Rel.Dense<LengthUnit>(endLengths, LengthUnit.SI);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        this.cumulativeLengths = lengths;
        this.caption = caption;
        createChart(this);
        this.reGraph(); // fixes the domain axis
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
                ChartFactory.createXYLineChart(this.caption, "", "", this, PlotOrientation.VERTICAL, false, false,
                        false);
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
        DoubleScalar.Rel<LengthUnit> minimumPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.SI);
        DoubleScalar.Rel<LengthUnit> maximumPosition = getCumulativeLength(-1);
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

    /** {@inheritDoc} */
    public final void addData(final AbstractLaneBasedGTU<?> car, final Lane lane) throws NetworkException,
            RemoteException
    {
        // final DoubleScalar.Abs<TimeUnit> startTime = car.getLastEvaluationTime();
        // System.out.println("addData car: " + car + ", lastEval: " + startTime);
        // Convert the position of the car to a position on path.
        // Find a (the first) lane that car is on that is in our path.
        double lengthOffset = 0;
        int index = this.path.indexOf(lane);
        if (index >= 0)
        {
            if (index > 0)
            {
                try
                {
                    lengthOffset = this.cumulativeLengths.getSI(index - 1);
                }
                catch (ValueException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            throw new Error("Car is not on any lane in the path");
        }
        // System.out.println("lane index is " + index + " car is " + car);
        // final DoubleScalar.Rel<LengthUnit> startPosition =
        // DoubleScalar.plus(new DoubleScalar.Rel<LengthUnit>(lengthOffset, LengthUnit.SI),
        // car.position(lane, car.getReference(), startTime)).immutable();
        String key = car.getId().toString();
        Trajectory carTrajectory = this.trajectories.get(key);
        if (null == carTrajectory)
        {
            // Create a new Trajectory for this GTU
            carTrajectory = new Trajectory(key);
            this.trajectoryIndices.add(carTrajectory);
            this.trajectories.put(key, carTrajectory);
            // System.out.println("Creating new trajectory");
        }
        carTrajectory.addSegment(car, lane, lengthOffset);
    }

    /**
     * Store trajectory data.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage. $LastChangedDate:
     * 2015-07-15 11:18:39 +0200 (Wed, 15 Jul 2015) $, @version $Revision$, by $Author$, initial
     * versionJul 24, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Trajectory
    {
        /** Time of (current) end of trajectory. */
        private DoubleScalar.Abs<TimeUnit> currentEndTime;

        /**
         * Retrieve the current end time of this Trajectory.
         * @return currentEndTime
         */
        public final DoubleScalar.Abs<TimeUnit> getCurrentEndTime()
        {
            return this.currentEndTime;
        }

        /** Position of (current) end of trajectory. */
        private DoubleScalar.Rel<LengthUnit> currentEndPosition;

        /**
         * Retrieve the current end position of this Trajectory.
         * @return currentEndPosition
         */
        public final DoubleScalar.Rel<LengthUnit> getCurrentEndPosition()
        {
            return this.currentEndPosition;
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
        public Trajectory(final Object id)
        {
            this.id = id;
        }

        /**
         * Add a trajectory segment and update the currentEndTime and currentEndPosition.
         * @param car AbstractLaneBasedGTU&lt;>&gt;; the GTU whose currently committed trajectory segment must be added
         * @param lane Lane; the Lane that the positionOffset is valid for
         * @param positionOffset double; offset needed to convert the position in the current Lane to a position on the
         *            trajectory
         * @throws NetworkException when car is not on lane anymore
         * @throws RemoteException when communication fails
         */
        public final void addSegment(final AbstractLaneBasedGTU<?> car, final Lane lane, final double positionOffset)
                throws NetworkException, RemoteException
        {
            final int startSample = (int) Math.ceil(car.getLastEvaluationTime().getSI() / getSampleInterval().getSI());
            final int endSample = (int) (Math.ceil(car.getNextEvaluationTime().getSI() / getSampleInterval().getSI()));
            for (int sample = startSample; sample < endSample; sample++)
            {
                DoubleScalar.Abs<TimeUnit> sampleTime =
                        new DoubleScalar.Abs<TimeUnit>(sample * getSampleInterval().getSI(), TimeUnit.SECOND);
                Double position = car.position(lane, car.getReference(), sampleTime).getSI() + positionOffset;
                if (this.positions.size() > 0 && position < this.currentEndPosition.getSI() - 0.001)
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
                /*-
                if (sample - this.firstSample > this.positions.size())
                {
                    System.out.println("Inserting " + (sample - this.positions.size()) 
                            + " nulls; this is trajectory number " + trajectoryIndices.indexOf(this));
                }
                 */
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
            this.currentEndTime = car.getNextEvaluationTime();
            this.currentEndPosition =
                    new DoubleScalar.Rel<LengthUnit>(car.position(lane, car.getReference(), this.currentEndTime)
                            .getSI() + positionOffset, LengthUnit.METER);
            if (car.getNextEvaluationTime().gt(getMaximumTime()))
            {
                setMaximumTime(car.getNextEvaluationTime());
            }
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
            return (item + this.firstSample) * getSampleInterval().getSI();
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

}
