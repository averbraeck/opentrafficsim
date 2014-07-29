package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends JFrame implements MouseMotionListener, ActionListener, XYDataset
{
    /** */
    private static final long serialVersionUID = 20140724L;

    /** Sample interval of this TrajectoryPlot. */
    protected final DoubleScalarRel<TimeUnit> sampleInterval;

    /** Minimum position on this TrajectoryPlot. */
    protected final DoubleScalarAbs<LengthUnit> minimumPosition;

    /** Maximum position on this TrajectoryPlot. */
    protected final DoubleScalarAbs<LengthUnit> maximumPosition;

    /** Maximum of the time axis. */
    protected DoubleScalarAbs<TimeUnit> maximumTime = new DoubleScalarAbs<TimeUnit>(300, TimeUnit.SECOND);

    /** The ChartPanel for this ContourPlot. */
    protected final JFreeChart chartPanel;

    /** Area to show status information. */
    protected final JLabel statusLabel;

    /** List of parties interested in changes of this ContourPlot. */
    transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Create a new TrajectoryPlot.
     * @param caption String; the text to show above the TrajectoryPlot
     * @param sampleInterval DoubleScalarRel&lt;TimeUnit&gt;; the time between samples of this TrajectoryPlot
     * @param minimumPosition DoubleScalarAbs&lt;LengthUnit&gt;; the minimum position sampled by this TrajectoryPlot
     * @param maximumPosition DoubleScalarAbs&lt;LengthUnit&gt;; the maximum position sampled by this TrajectoryPlot
     */
    public TrajectoryPlot(final String caption, final DoubleScalarRel<TimeUnit> sampleInterval,
            final DoubleScalarAbs<LengthUnit> minimumPosition, final DoubleScalarAbs<LengthUnit> maximumPosition)
    {
        this.sampleInterval = sampleInterval;
        this.minimumPosition = minimumPosition;
        this.maximumPosition = maximumPosition;
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        this.chartPanel =
                ChartFactory.createXYLineChart(caption, "", "", this, PlotOrientation.VERTICAL, false, false, false);
        NumberAxis xAxis = new NumberAxis("\u2192 " + "time [s]");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        this.chartPanel.getXYPlot().setDomainAxis(xAxis);
        this.chartPanel.getXYPlot().setRangeAxis(yAxis);
        configureAxis(this.chartPanel.getXYPlot().getRangeAxis(), DoubleScalar.minus(maximumPosition, minimumPosition)
                .getValueSI());
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        renderer.setBaseShape(new Line2D.Float(0, 0, 0, 0));
        ChartPanel cp = new ChartPanel(this.chartPanel);
        cp.setFillZoomRectangle(true);
        cp.setMouseWheelEnabled(true);
        cp.addMouseMotionListener(this);
        setPreferredSize(new java.awt.Dimension(500, 270));
        cp.addMouseMotionListener(this);
        this.add(cp, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        this.add(this.statusLabel, BorderLayout.SOUTH);
        this.reGraph(); // fixes the domain axis
        // TODO ensure that shapes for all the data points don't get allocated.
        // Currently many megabytes of memory become allocated for Ellipses.
    }

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed).
     */
    private void reGraph()
    {
        configureAxis(this.chartPanel.getXYPlot().getDomainAxis(), this.maximumTime.getValueSI());
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
    }

    /**
     * Notify interested parties of an event affecting this TrajectoryPlot.
     * @param event
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
            dcl.datasetChanged(event);
    }

    /**
     * Configure the range of an axis.
     * @param valueAxis
     * @param range
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

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        // not yet
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // ignored
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        XYPlot plot = (XYPlot) cp.getChart().getPlot();
        boolean showCrossHair = cp.getScreenDataArea().contains(mouseEvent.getPoint());
        if (cp.getHorizontalAxisTrace() != showCrossHair)
        {
            cp.setHorizontalAxisTrace(showCrossHair);
            cp.setVerticalAxisTrace(showCrossHair);
            plot.notifyListeners(new PlotChangeEvent(plot));
        }
        if (showCrossHair)
        {
            Point2D p = cp.translateScreenToJava2D(mouseEvent.getPoint());
            PlotRenderingInfo pi = cp.getChartRenderingInfo().getPlotInfo();
            double t = plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge());
            double distance = plot.getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge());
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
                                        ": vehicle %s; location on measurement path at t=%.1fs: longitudinal %.1fm, lateral %.1fm",
                                        so.toString(), t, bestPosition.x, bestPosition.y);
                    }
            }
            else
                value = "";
             */
            this.statusLabel.setText(String.format("t=%.0fs, distance=%.0fm%s", t, distance, value));
        }
        else
            this.statusLabel.setText(" ");
    }

    /** All stored trajectories. */
    ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

    /**
     * @param car Car; the Car that has determined it's next move
     */
    public void addData(final Car car)
    {
        DoubleScalarAbs<TimeUnit> startTime = car.getLastEvaluationTime();
        DoubleScalarAbs<LengthUnit> startPosition = car.position(startTime);
        // Lookup this Car in the list of trajectories
        Trajectory carTrajectory = null;
        for (Trajectory t : this.trajectories)
            if (t.currentEndTime.getValueSI() == startTime.getValueSI()
                    && t.currentEndPosition.getValueSI() == startPosition.getValueSI())
            {
                if (null != carTrajectory)
                    System.err.println("Whoops; we've got another match");
                carTrajectory = t;
            }
        if (null == carTrajectory)
            this.trajectories.add(carTrajectory = new Trajectory());
        carTrajectory.addSegment(car);
    }

    /**
     * <p>
     * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
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
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 24, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Trajectory
    {
        /** Time of (current) end of trajectory. */
        DoubleScalarAbs<TimeUnit> currentEndTime;

        /** Position of (current) end of trajectory. */
        DoubleScalarAbs<LengthUnit> currentEndPosition;

        /** Storage for the position of the car. */
        ArrayList<Double> positions = new ArrayList<Double>();

        /** Time sample of first sample in positions (successive entries will each be one sampleTime later). */
        int firstSample;

        /**
         * Add a trajectory segment and update the currentEndTime and currentEndPosition.
         * @param car Car; the Car whose currently committed trajectory segment must be added
         */
        public void addSegment(final Car car)
        {
            int startSample =
                    (int) Math.ceil(car.getLastEvaluationTime().getValueSI()
                            / TrajectoryPlot.this.sampleInterval.getValueSI());
            int endSample =
                    (int) (Math.ceil(car.getNextEvaluationTime().getValueSI()
                            / TrajectoryPlot.this.sampleInterval.getValueSI()));
            for (int sample = startSample; sample < endSample; sample++)
            {
                DoubleScalarAbs<TimeUnit> sampleTime =
                        new DoubleScalarAbs<TimeUnit>(sample * TrajectoryPlot.this.sampleInterval.getValueSI(),
                                TimeUnit.SECOND);
                DoubleScalarAbs<LengthUnit> position = car.position(sampleTime);
                if (position.getValueSI() < TrajectoryPlot.this.minimumPosition.getValueSI())
                    continue;
                if (position.getValueSI() > TrajectoryPlot.this.maximumPosition.getValueSI())
                    continue;
                if (this.positions.size() == 0)
                    this.firstSample = sample;
                while (sample - startSample > this.positions.size())
                    this.positions.add(null); // insert nulls as place holders for unsampled data (because vehicle was
                                              // temporarily out of range?)
                this.positions.add(position.getValueSI());
            }
            this.currentEndTime = car.getNextEvaluationTime();
            this.currentEndPosition = car.position(this.currentEndTime);
            if (car.getNextEvaluationTime().getValueSI() > TrajectoryPlot.this.maximumTime.getValueSI())
                TrajectoryPlot.this.maximumTime = car.getNextEvaluationTime();
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
            return (item + this.firstSample) * TrajectoryPlot.this.sampleInterval.getValueSI();
        }

        /**
         * @param item Integer; the sample number
         * @return Double; the position indexed by item
         */
        public double getDistance(final int item)
        {
            return this.positions.get(item);
        }
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
     */
    @Override
    public int getSeriesCount()
    {
        return this.trajectories.size();
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
     */
    @Override
    public Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        if (seriesKey instanceof Integer)
            return (Integer) seriesKey;
        return -1;
    }

    /**
     * @see org.jfree.data.general.Dataset#addChangeListener(org.jfree.data.general.DatasetChangeListener)
     */
    @Override
    public void addChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /**
     * @see org.jfree.data.general.Dataset#removeChangeListener(org.jfree.data.general.DatasetChangeListener)
     */
    @Override
    public void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
    }

    /**
     * @see org.jfree.data.general.Dataset#getGroup()
     */
    @Override
    public DatasetGroup getGroup()
    {
        return this.datasetGroup;
    }

    /**
     * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
     */
    @Override
    public void setGroup(final DatasetGroup group)
    {
        this.datasetGroup = group;
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getDomainOrder()
     */
    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getItemCount(int)
     */
    @Override
    public int getItemCount(final int series)
    {
        if ((series < 0) || (series >= this.trajectories.size()))
            return 0;
        return this.trajectories.get(series).size();
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getX(int, int)
     */
    @Override
    public Number getX(final int series, final int item)
    {
        if ((series < 0) || (series >= this.trajectories.size()))
            return null;
        return this.trajectories.get(series).getTime(item);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
     */
    @Override
    public double getXValue(final int series, final int item)
    {
        if ((series < 0) || (series >= this.trajectories.size()))
            return Double.NaN;
        return this.trajectories.get(series).getTime(item);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getY(int, int)
     */
    @Override
    public Number getY(final int series, final int item)
    {
        if ((series < 0) || (series >= this.trajectories.size()))
            return null;
        return this.trajectories.get(series).getDistance(item);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
     */
    @Override
    public double getYValue(final int series, final int item)
    {
        if ((series < 0) || (series >= this.trajectories.size()))
            return Double.NaN;
        return this.trajectories.get(series).getDistance(item);
    }

    /**
     * Main for stand alone running.
     * @param args String[]; the program arguments (not used)
     */
    public static void main(final String[] args)
    {
        JOptionPane.showMessageDialog(null, "TrajectoryPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(5000, LengthUnit.METER);
        DoubleScalarRel<TimeUnit> sampleInterval = new DoubleScalarRel<TimeUnit>(0.5, TimeUnit.SECOND);
        TrajectoryPlot tp = new TrajectoryPlot("Trajectories", sampleInterval, minimumDistance, maximumDistance);
        tp.setTitle("Flow Contour Graph");
        tp.setBounds(0, 0, 600, 400);
        tp.pack();
        tp.setVisible(true);
        DEVSSimulator simulator = new DEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        int carsCreated = 0;
        ArrayList<Car> cars = new ArrayList<Car>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        double idmPlusTick = 0.5;
        while (thisTick < endTime)
        {
            // System.out.println("thisTick is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                Car car =
                        new Car(++carsCreated, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
                cars.add(0, car);
                // System.out.println(String.format("thisTick=%.1f, there are now %d vehicles", thisTick, cars.size()));
                nextSourceTick += headway;
            }
            if (thisTick == nextMoveTick)
            {
                // Time to move all vehicles forward (this works even though they do not have simultaneous clock ticks)
                // Debugging
                /*-
                if (thisTick == 700)
                {
                    DoubleScalarAbs<TimeUnit> now = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    for (int i = 0; i < cars.size(); i++)
                        System.out.println(cars.get(i).toString(now));
                }
                 */
                /*
                 * TODO: Currently all cars have to be moved "manually". This functionality should go to the simulator.
                 */
                for (int carIndex = 0; carIndex < cars.size(); carIndex++)
                {
                    DoubleScalarAbs<TimeUnit> now = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    Car car = cars.get(carIndex);
                    if (car.position(now).getValueSI() > 5000)
                    {
                        cars.remove(carIndex);
                        break;
                    }
                    Collection<Car> leaders = new ArrayList<Car>();
                    if (carIndex < cars.size() - 1)
                        leaders.add(cars.get(carIndex + 1));
                    if (thisTick >= 300 && thisTick < 500)
                    {
                        // Add a stationary car at 4000m to simulate an opening bridge
                        Car block =
                                new Car(99999, simulator, carFollowingModel, now, new DoubleScalarAbs<LengthUnit>(4000,
                                        LengthUnit.METER), new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                        leaders.add(block);
                    }
                    CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(car, leaders, speedLimit);
                    car.setState(cfmr);
                    // Add the movement of this Car to the trajectory plot
                    tp.addData(car);
                }
                nextMoveTick += idmPlusTick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify the trajectory plot that the underlying data has changed
        tp.reGraph();
    }
}
