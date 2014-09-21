package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends JFrame implements ActionListener, XYDataset
{
    /** */
    private static final long serialVersionUID = 20140724L;

    /** Sample interval of this TrajectoryPlot. */
    protected final DoubleScalar.Rel<TimeUnit> sampleInterval;

    /** Minimum position on this TrajectoryPlot. */
    protected final DoubleScalar.Abs<LengthUnit> minimumPosition;

    /** Maximum position on this TrajectoryPlot. */
    protected final DoubleScalar.Abs<LengthUnit> maximumPosition;

    /** Maximum of the time axis. */
    protected DoubleScalar.Abs<TimeUnit> maximumTime = new DoubleScalar.Abs<TimeUnit>(300, TimeUnit.SECOND);

    /** The ChartPanel for this TrajectoryPlot. */
    protected final JFreeChart chartPanel;

    /** Area to show status information. */
    protected final JLabel statusLabel;

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Create a new TrajectoryPlot.
     * @param caption String; the text to show above the TrajectoryPlot
     * @param sampleInterval DoubleScalarRel&lt;TimeUnit&gt;; the time between samples of this TrajectoryPlot
     * @param minimumPosition DoubleScalarAbs&lt;LengthUnit&gt;; the minimum position sampled by this TrajectoryPlot
     * @param maximumPosition DoubleScalarAbs&lt;LengthUnit&gt;; the maximum position sampled by this TrajectoryPlot
     */
    public TrajectoryPlot(final String caption, final DoubleScalar.Rel<TimeUnit> sampleInterval,
            final DoubleScalar.Abs<LengthUnit> minimumPosition, final DoubleScalar.Abs<LengthUnit> maximumPosition)
    {
        this.sampleInterval = sampleInterval;
        this.minimumPosition = minimumPosition;
        this.maximumPosition = maximumPosition;
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        this.chartPanel = ChartFactory.createXYLineChart(caption, "", "", this, PlotOrientation.VERTICAL, false, false, false);
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
        configureAxis(this.chartPanel.getXYPlot().getRangeAxis(), MutableDoubleScalar.minus(maximumPosition, minimumPosition)
                .getValueSI());
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        renderer.setBaseShape(new Line2D.Float(0, 0, 0, 0));
        final ChartPanel cp = new ChartPanel(this.chartPanel);
        cp.setMouseWheelEnabled(true);
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    TrajectoryPlot.this.statusLabel.setText(" ");
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
                                            ": vehicle %s; location on measurement path at t=%.1fs: longitudinal %.1fm, lateral %.1fm",
                                            so.toString(), t, bestPosition.x, bestPosition.y);
                        }
                }
                else
                    value = "";
                 */
                TrajectoryPlot.this.statusLabel.setText(String.format("t=%.0fs, distance=%.0fm%s", domainValue, rangeValue,
                        value));
            }
        };
        cp.addMouseMotionListener(ph);
        cp.addMouseListener(ph);
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
    public final void reGraph()
    {
        configureAxis(this.chartPanel.getXYPlot().getDomainAxis(), this.maximumTime.getValueSI());
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
    ArrayList<Trajectory> trajectories = new ArrayList<Trajectory>();

    /**
     * Add the scheduled motion of a car to this TrajectoryPlot.
     * @param car Car; the Car that has determined it's next move
     */
    public final void addData(final Car car)
    {
        final DoubleScalar.Abs<TimeUnit> startTime = car.getLastEvaluationTime();
        final DoubleScalar.Abs<LengthUnit> startPosition = car.getPosition(startTime);
        // Lookup this Car in the list of trajectories
        Trajectory carTrajectory = null;
        for (Trajectory t : this.trajectories)
        {
            if (t.currentEndTime.getValueSI() == startTime.getValueSI()
                    && t.currentEndPosition.getValueSI() == startPosition.getValueSI())
            {
                if (null != carTrajectory)
                {
                    System.err.println("Whoops; we've got another match");
                }
                carTrajectory = t;
            }
        }
        if (null == carTrajectory)
        {
            this.trajectories.add(carTrajectory = new Trajectory());
        }
        carTrajectory.addSegment(car);
    }

    /**
     * <p>
     * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
     * disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
     * disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
     * promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
     * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
     * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
     * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services;
     * loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in
     * contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
     * software, even if advised of the possibility of such damage.
     * @version Jul 24, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Trajectory
    {
        /** Time of (current) end of trajectory. */
        DoubleScalar.Abs<TimeUnit> currentEndTime;

        /** Position of (current) end of trajectory. */
        DoubleScalar.Abs<LengthUnit> currentEndPosition;

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
            final int startSample =
                    (int) Math.ceil(car.getLastEvaluationTime().getValueSI() / TrajectoryPlot.this.sampleInterval.getValueSI());
            final int endSample =
                    (int) (Math
                            .ceil(car.getNextEvaluationTime().getValueSI() / TrajectoryPlot.this.sampleInterval.getValueSI()));
            for (int sample = startSample; sample < endSample; sample++)
            {
                DoubleScalar.Abs<TimeUnit> sampleTime =
                        new DoubleScalar.Abs<TimeUnit>(sample * TrajectoryPlot.this.sampleInterval.getValueSI(),
                                TimeUnit.SECOND);
                DoubleScalar.Abs<LengthUnit> position = car.getPosition(sampleTime);
                if (position.getValueSI() < TrajectoryPlot.this.minimumPosition.getValueSI())
                {
                    continue;
                }
                if (position.getValueSI() > TrajectoryPlot.this.maximumPosition.getValueSI())
                {
                    continue;
                }
                if (this.positions.size() == 0)
                {
                    this.firstSample = sample;
                }
                while (sample - startSample > this.positions.size())
                {
                    this.positions.add(null); // insert nulls as place holders for unsampled data (because vehicle was
                                              // temporarily out of range?)
                }
                this.positions.add(position.getValueSI());
            }
            this.currentEndTime = car.getNextEvaluationTime();
            this.currentEndPosition = car.getPosition(this.currentEndTime);
            if (car.getNextEvaluationTime().getValueSI() > TrajectoryPlot.this.maximumTime.getValueSI())
            {
                TrajectoryPlot.this.maximumTime = car.getNextEvaluationTime();
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
        return this.trajectories.get(series).size();
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
        return this.trajectories.get(series).getTime(item);
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
        return this.trajectories.get(series).getDistance(item);
    }

}
