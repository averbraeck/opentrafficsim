package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.rmi.RemoteException;
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

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 24, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlot extends JFrame implements ActionListener, XYDataset
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

    /** Minimum position on this TrajectoryPlot. */
    private final DoubleScalar.Rel<LengthUnit> minimumPosition;

    /**
     * @return minimumPosition
     */
    public final DoubleScalar.Rel<LengthUnit> getMinimumPosition()
    {
        return this.minimumPosition;
    }

    /**
     * @return maximumPosition
     */
    public final DoubleScalar.Rel<LengthUnit> getMaximumPosition()
    {
        return this.maximumPosition;
    }

    /** Maximum position on this TrajectoryPlot. */
    private final DoubleScalar.Rel<LengthUnit> maximumPosition;

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

    /** The ChartPanel for this TrajectoryPlot. */
    private final JFreeChart chartPanel;

    /** Area to show status information. */
    private final JLabel statusLabel;

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Create a new TrajectoryPlot.
     * @param caption String; the text to show above the TrajectoryPlot
     * @param sampleInterval DoubleScalarRel&lt;TimeUnit&gt;; the time between samples of this TrajectoryPlot
     * @param minimumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum position sampled by this TrajectoryPlot
     * @param maximumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum position sampled by this TrajectoryPlot
     */
    public TrajectoryPlot(final String caption, final DoubleScalar.Rel<TimeUnit> sampleInterval,
            final DoubleScalar.Rel<LengthUnit> minimumPosition, final DoubleScalar.Rel<LengthUnit> maximumPosition)
    {
        this.trajectories = new ArrayList<Trajectory>();
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
                .getSI());
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
                    setStatusText(" ");
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
                setStatusText(String.format("t=%.0fs, distance=%.0fm%s", domainValue, rangeValue, value));
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
     * Update the status text.
     * @param newText String; the new text to show
     */
    public final void setStatusText(final String newText)
    {
        this.statusLabel.setText(newText);
    }

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed).
     */
    public final void reGraph()
    {
        configureAxis(this.chartPanel.getXYPlot().getDomainAxis(), this.maximumTime.getSI());
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
    private ArrayList<Trajectory> trajectories;

    /**
     * Add the scheduled motion of a car to this TrajectoryPlot.
     * @param car Car; the Car that has determined it's next move
     * @throws RemoteException when communication fails
     */
    public final void addData(final Car<?> car) throws RemoteException
    {
        final DoubleScalar.Abs<TimeUnit> startTime = car.getLastEvaluationTime();
        final DoubleScalar.Rel<LengthUnit> startPosition = car.positionOfFront(startTime).getLongitudinalPosition();
        // Lookup this Car in the list of trajectories
        Trajectory carTrajectory = null;
        for (Trajectory t : this.trajectories)
        {
            if (t.getCurrentEndTime().getSI() == startTime.getSI()
                    && t.getCurrentEndPosition().getSI() == startPosition.getSI())
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
            carTrajectory = new Trajectory();
            this.trajectories.add(carTrajectory);
        }
        carTrajectory.addSegment(car);
    }

    /**
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
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
        private DoubleScalar.Abs<TimeUnit> currentEndTime;

        /**
         * @return currentEndTime
         */
        public final DoubleScalar.Abs<TimeUnit> getCurrentEndTime()
        {
            return this.currentEndTime;
        }

        /** Position of (current) end of trajectory. */
        private DoubleScalar.Rel<LengthUnit> currentEndPosition;

        /**
         * @return currentEndPosition
         */
        public final DoubleScalar.Rel<LengthUnit> getCurrentEndPosition()
        {
            return this.currentEndPosition;
        }

        /** Storage for the position of the car. */
        private ArrayList<Double> positions = new ArrayList<Double>();

        /** Time sample of first sample in positions (successive entries will each be one sampleTime later). */
        private int firstSample;

        /**
         * Add a trajectory segment and update the currentEndTime and currentEndPosition.
         * @param car Car; the Car whose currently committed trajectory segment must be added
         * @throws RemoteException when communication fails
         */
        public final void addSegment(final Car<?> car) throws RemoteException
        {
            final int startSample = (int) Math.ceil(car.getLastEvaluationTime().getSI() / getSampleInterval().getSI());
            final int endSample = (int) (Math.ceil(car.getNextEvaluationTime().getSI() / getSampleInterval().getSI()));
            for (int sample = startSample; sample < endSample; sample++)
            {
                DoubleScalar.Abs<TimeUnit> sampleTime =
                        new DoubleScalar.Abs<TimeUnit>(sample * getSampleInterval().getSI(), TimeUnit.SECOND);
                DoubleScalar.Rel<LengthUnit> position = car.positionOfFront(sampleTime).getLongitudinalPosition();
                if (position.getSI() < getMinimumPosition().getSI())
                {
                    continue;
                }
                if (position.getSI() > getMaximumPosition().getSI())
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
                this.positions.add(position.getSI());
            }
            this.currentEndTime = car.getNextEvaluationTime();
            this.currentEndPosition = car.positionOfFront(this.currentEndTime).getLongitudinalPosition();
            if (car.getNextEvaluationTime().getSI() > getMaximumTime().getSI())
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
