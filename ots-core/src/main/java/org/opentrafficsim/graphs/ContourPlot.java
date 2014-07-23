package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RefineryUtilities;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbs;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbsDense;

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
 * @version Jul 16, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlot extends JFrame implements MouseMotionListener, ActionListener
{
    /**
     * Enumeration of the possible contour graphs.
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
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public enum Type {
        /** Density contour graph */
        DENSITY,
        /** Speed contour graph */
        SPEED,
        /** Flow contour graph */
        FLOW,
    }

    /** */
    private static final long serialVersionUID = 20140716L;

    /** The ChartPanel for this ContourPlot */
    protected final ChartPanel chartPanel;

    /** Area to show status information */
    protected final JLabel statusLabel;

    /** Time granularity in seconds */
    protected double timeGranularity = timeGranularities[3];

    /** Distance granularity in meters */
    protected double distanceGranularity = distanceGranularities[1];

    /** List of parties interested in changes of this ContourPlot */
    transient EventListenerList listenerList = new EventListenerList();

    /** Minimum distance used in this ContourPlot */
    protected final double minimumDistance;

    /** Maximum distance used in this ContourPlot */
    protected final double maximumDistance;

    /** Time range of this ContourPlot (automatically extended when data is added) */
    protected double timeRange = 300;

    /** Granularity values for the distancePopupMenu */
    protected final static double[] distanceGranularities = {10, 20, 50, 100, 200, 500, 1000};

    /** Granularity values for the timePopupMenu */
    protected final static double[] timeGranularities = {1, 2, 5, 10, 20, 30, 60, 120, 300, 600};

    /** Number of distance bins used */
    protected final int distanceBinCount;

    /**
     * Create a new ContourPlot
     * @param caption String; text to show above the contour plot
     * @param type Contourplot.Type; the type of this contour plot
     * @param minimumDistance double; the minimum distance value used in this contour plot
     * @param maximumDistance double; the maximum distance value used in this contour plot
     */
    public ContourPlot(String caption, Type type, final double minimumDistance, final double maximumDistance)
    {
        this.minimumDistance = distanceGranularities[0] * Math.floor(minimumDistance / distanceGranularities[0]);
        this.maximumDistance = distanceGranularities[0] * Math.ceil(maximumDistance / distanceGranularities[0]);
        this.distanceBinCount = (int) ((maximumDistance - minimumDistance) / distanceGranularities[0]);
        this.setLayout(new BorderLayout());
        String valueFormat;
        double[] boundaries = new double[3];
        ContourDataset contourDataSet = null;
        String legendFormat;
        double legendStep;

        switch (type)
        {
            case DENSITY:
                valueFormat = "density %.1f veh/km";
                boundaries[0] = 120;
                boundaries[1] = 10;
                boundaries[2] = 0;
                legendFormat = "%.1f veh/km";
                legendStep = 20;
                contourDataSet = new DensityContourDataset();
                break;
            case FLOW:
                valueFormat = "flow %.0f veh/hour";
                boundaries[0] = 2500;
                boundaries[1] = 1500;
                boundaries[2] = 0;
                legendFormat = "%.0f veh/hour";
                legendStep = 500;
                contourDataSet = new FlowContourDataset();
                break;
            case SPEED:
                valueFormat = "speed %.1f km/h";
                boundaries[0] = 0;
                boundaries[1] = 40;
                boundaries[2] = 150;
                legendFormat = "%.1f km/h";
                legendStep = 20;
                contourDataSet = new SpeedContourDataset();
                break;
            default:
                throw new Error("Bad switch; Cannot happen");
        }
        this.chartPanel =
                new ChartPanel(createChart(caption, "\u2192 Distance", ", " + valueFormat, contourDataSet, boundaries,
                        legendFormat, legendStep));
        this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        this.chartPanel.addMouseMotionListener(this);
        this.add(this.chartPanel, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ");
        this.add(this.statusLabel, BorderLayout.SOUTH);
        JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        popupMenu.insert(
                buildMenu("Distance granularity", "%.0f m", "setDistanceGranularity", distanceGranularities,
                        this.distanceGranularity), 0);
        popupMenu.insert(
                buildMenu("Time granularity", "%.0f s", "setTimeGranularity", timeGranularities, this.timeGranularity),
                1);
        this.reGraph();
    }

    /**
     * Change the upper limit of the time range.
     * @param newUpperLimit double; the new upper limit for the time range
     */
    public void adjustTimeRange(double newUpperLimit)
    {
        this.timeRange = newUpperLimit;

    }

    /** The sub-menu that sets the distance granularity */
    protected JMenu distanceGranularityMenu;

    /** The sub-menu that sets the time granularity */
    protected JMenu timeGranularityMenu;

    /**
     * Create a JMenu to let the user set the granularity of the XYBlockChart.
     * @param caption String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param commandPrefix String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new
     *            JMenu
     * @return
     */
    private JMenu buildMenu(String caption, String format, String commandPrefix, double[] values, double currentValue)
    {
        JMenu result = new JMenu(caption);
        ButtonGroup group = new ButtonGroup();
        for (double value : values)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, value));
            item.setSelected(value == currentValue);
            item.setActionCommand(commandPrefix + String.format(Locale.US, " %f", value));
            item.addActionListener(this);
            result.add(item);
            group.add(item);
        }
        return result;
    }

    /**
     * Storage for the contour data of this ContourPlot
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
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    abstract class ContourDataset implements XYZDataset
    {
        // Implements everything except getSeriesKey and adds virtual method incrementData and computeZValue and
        // non-virtual
        // method addData.
        @Override
        public int getSeriesCount()
        {
            return 1;
        }

        /**
         * Retrieve the number of cells to use along the distance axis.
         * @return Integer; the number of cells to use along the distance axis
         */
        protected int distances()
        {
            return (int) Math.ceil((ContourPlot.this.maximumDistance - ContourPlot.this.minimumDistance)
                    / ContourPlot.this.distanceGranularity);
        }

        /**
         * Retrieve the number of cells to use along the time axis.
         * @return Integer; the number of cells to use along the time axis
         */
        private int times()
        {
            return (int) Math.ceil(ContourPlot.this.timeRange / ContourPlot.this.timeGranularity);
        }

        @Override
        public int getItemCount(int series)
        {
            return distances() * times();
        }

        @Override
        public Number getX(int series, int item)
        {
            return new Double(getXValue(series, item));
        }

        @Override
        public double getXValue(int series, int item)
        {
            return item / distances() * ContourPlot.this.timeGranularity;
        }

        @Override
        public Number getY(int series, int item)
        {
            return new Double(getYValue(series, item));
        }

        @Override
        public double getYValue(int series, int item)
        {
            return item % distances() * ContourPlot.this.distanceGranularity;
        }

        @Override
        public Number getZ(int series, int item)
        {
            return new Double(getZValue(series, item));
        }

        @Override
        public void addChangeListener(DatasetChangeListener listener)
        {
            ContourPlot.this.listenerList.add(DatasetChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(DatasetChangeListener listener)
        {
            ContourPlot.this.listenerList.remove(DatasetChangeListener.class, listener);
        }

        @Override
        public DatasetGroup getGroup()
        {
            return null;
        }

        @Override
        public void setGroup(DatasetGroup group)
        {
            // ignore
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int indexOf(Comparable seriesKey)
        {
            return 0;
        }

        @Override
        public DomainOrder getDomainOrder()
        {
            return DomainOrder.ASCENDING;
        }

        /**
         * Add a fragment of a trajectory to this ContourPlot.
         * @param fromTime Double; start time of the sub-trajectory
         * @param toTime Double; end time of the sub-trajectory
         * @param car Car; the GTU that is being sampled TODO: replace Car by GTU
         */
        public void addData(Car car)
        {
            DoubleScalarAbs<TimeUnit> fromTime = car.getLastEvaluationTime();
            DoubleScalarAbs<TimeUnit> toTime = car.getNextEvaluationTime();
            if (toTime.getValueSI() > ContourPlot.this.timeRange)
                adjustTimeRange(toTime.getValueSI());
            if (toTime.getValueSI() <= fromTime.getValueSI()) // degenerate sample???
                return;
            // System.out.println(String.format("addData: fromTime=%.1f, toTime=%.1f, fromDist=%.2f, toDist=%.2f",
            // fromTime.getValueSI(), toTime.getValueSI(), car.position(fromTime).getValueSI(),
            // car.position(toTime).getValueSI()));
            double relativeFromDistance =
                    (car.position(fromTime).getValueSI() - ContourPlot.this.minimumDistance) / distanceGranularities[0];
            double relativeToDistance =
                    (car.position(toTime).getValueSI() - ContourPlot.this.minimumDistance) / distanceGranularities[0];
            double relativeFromTime = (fromTime.getValueSI() - 0) / timeGranularities[0];
            double relativeToTime = (toTime.getValueSI() - 0) / timeGranularities[0];
            int fromTimeBin = (int) Math.floor(relativeFromTime);
            int toTimeBin = (int) Math.floor(relativeToTime) + 1;
            double relativeMeanSpeed =
                    (relativeToDistance - relativeFromDistance) / (relativeToTime - relativeFromTime);
            for (int timeBin = fromTimeBin; timeBin < toTimeBin; timeBin++)
            {
                if (timeBin < 0)
                    continue;
                double binEndTime = timeBin + 1;
                if (binEndTime > relativeToTime)
                    binEndTime = relativeToTime;
                if (binEndTime <= relativeFromTime)
                    continue; // no time spent in this timeBin
                double binDistanceStart =
                        (car.position(
                                new DoubleScalarAbs<TimeUnit>(relativeFromTime * timeGranularities[0], TimeUnit.SECOND))
                                .getValueSI() - ContourPlot.this.minimumDistance)
                                / distanceGranularities[0];
                double binDistanceEnd =
                        (car.position(new DoubleScalarAbs<TimeUnit>(binEndTime * timeGranularities[0], TimeUnit.SECOND))
                                .getValueSI() - ContourPlot.this.minimumDistance)
                                / distanceGranularities[0];

                // Compute the time in each distanceBin (assuming constant speed during the timeBin)
                for (int distanceBin = (int) Math.floor(binDistanceStart); distanceBin <= binDistanceEnd; distanceBin++)
                {
                    double relativeDuration = 1;
                    if (relativeFromTime > timeBin)
                        relativeDuration -= relativeFromTime - timeBin;
                    if (distanceBin == (int) Math.floor(binDistanceEnd))
                    {
                        // This GTU does not move out of this distanceBin before the binEndTime
                        if (binEndTime < timeBin + 1)
                            relativeDuration -= timeBin + 1 - binEndTime;
                    }
                    else
                    {
                        // This GTU moves out of this distanceBin before the binEndTime
                        // Interpolate the time when this GTU crosses into the next distanceBin
                        // Using f.i. Newton-Rhapson interpolation would yield a more precise result...
                        double timeToBinBoundary = (distanceBin + 1 - binDistanceStart) / relativeMeanSpeed;
                        double endTime = relativeFromTime + timeToBinBoundary;
                        relativeDuration -= timeBin + 1 - endTime;
                    }
                    final double duration = relativeDuration * timeGranularities[0];
                    final double distance = duration * relativeMeanSpeed * distanceGranularities[0];
                    // System.out.println(String.format("tb=%d, db=%d, t=%.2f, d=%.2f", timeBin, distanceBin, duration,
                    // distance));
                    incrementData(timeBin, distanceBin, duration, distance);
                    relativeFromTime += relativeDuration;
                    binDistanceStart = distanceBin + 1;
                }
                relativeFromTime = timeBin + 1;
            }

        }

        /**
         * Increment the data of one bin.
         * @param timeBin Integer; the rank of the bin on the time-scale
         * @param distanceBin Integer; the rank of the bin on the distance-scale
         * @param duration Double; the time spent in this bin
         * @param distanceCovered Double; the distance covered in this bin
         */
        public abstract void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered);

        @Override
        public double getZValue(int series, int item)
        {
            int timeBinGroup = item / distances();
            int distanceBinGroup = item % distances();
            // System.out.println(String.format("getZValue(s=%d, i=%d) -> tbg=%d, dbg=%d", series, item, timeBinGroup,
            // distanceBinGroup));
            final int timeGroupSize = (int) (ContourPlot.this.timeGranularity / timeGranularities[0]);
            final int firstTimeBin = timeBinGroup * timeGroupSize;
            if (firstTimeBin * timeGranularities[0] >= ContourPlot.this.timeRange)
                return Double.NaN;
            final int distanceGroupSize = (int) (ContourPlot.this.distanceGranularity / distanceGranularities[0]);
            final int firstDistanceBin = distanceBinGroup * distanceGroupSize;
            if (firstDistanceBin * distanceGranularities[0] >= ContourPlot.this.maximumDistance)
                return Double.NaN;
            return computeZValue(firstTimeBin, firstTimeBin + timeGroupSize, firstDistanceBin, firstDistanceBin
                    + distanceGroupSize);
        }

        /**
         * Combine values in a range of time bins and distance bins to obtain a density value
         * @param firstTimeBin Integer; the first time bin to use
         * @param endTimeBin Integer; one higher than the last time bin to use
         * @param firstDistanceBin Integer; the first distance bin to use
         * @param endDistanceBin Integer; one higher than the last distance bin to use
         * @return Double; the density value (or Double.NaN if no value can be computed)
         */
        public abstract double computeZValue(int firstTimeBin, int endTimeBin, int firstDistanceBin, int endDistanceBin);

    }

    /**
     * Store the data needed for a density contour graph.
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
     * @version Jul 17, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class DensityContourDataset extends ContourDataset
    {
        /** Storage for the total time spent in each cell */
        private ArrayList<DoubleVectorAbs<TimeUnit>> cumulativeTimes = new ArrayList<DoubleVectorAbs<TimeUnit>>();

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getSeriesKey(int series)
        {
            return "density";
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#incrementData(int, int, double, double)
         */
        @Override
        public void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered)
        {
            if (timeBin < 0 || distanceBin < 0 || 0 == duration || distanceBin >= ContourPlot.this.distanceBinCount)
                return;
            while (timeBin >= this.cumulativeTimes.size())
                this.cumulativeTimes.add(new DoubleVectorAbsDense<TimeUnit>(
                        new double[ContourPlot.this.distanceBinCount], TimeUnit.SECOND));
            DoubleVectorAbs<TimeUnit> values = this.cumulativeTimes.get(timeBin);
            try
            {
                values.setSI(distanceBin, values.getSI(distanceBin) + duration);
            }
            catch (ValueException exception)
            {
                System.err.println("Error in incrementData:");
                exception.printStackTrace();
            }
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#computeZValue(int, int, int, int)
         */
        @Override
        public double computeZValue(int firstTimeBin, int endTimeBin, int firstDistanceBin, int endDistanceBin)
        {
            double cumulativeTimeInSI = 0;
            if (firstTimeBin >= this.cumulativeTimes.size())
                return Double.NaN;
            try
            {
                for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
                {
                    if (timeBinIndex >= this.cumulativeTimes.size())
                        break;
                    DoubleVectorAbs<TimeUnit> values = this.cumulativeTimes.get(timeBinIndex);
                    for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                    {
                        // System.out.println("distanceBinIndex is " + distanceBinIndex);
                        cumulativeTimeInSI += values.getSI(distanceBinIndex);
                    }
                }
            }
            catch (ValueException exception)
            {
                System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                        firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
                exception.printStackTrace();
            }
            return 1000 * cumulativeTimeInSI / ContourPlot.this.timeGranularity / ContourPlot.this.distanceGranularity;
        }

    }

    /**
     * Store the data needed for a flow contour graph.
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
     * @version Jul 17, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class FlowContourDataset extends ContourDataset
    {
        /** Storage for the total length traveled in each cell */
        private ArrayList<DoubleVectorAbs<LengthUnit>> cumulativeLengths = new ArrayList<DoubleVectorAbs<LengthUnit>>();

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getSeriesKey(int series)
        {
            return "flow";
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#incrementData(int, int, double, double)
         */
        @Override
        public void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered)
        {
            if (timeBin < 0 || distanceBin < 0 || 0 == duration || distanceBin >= ContourPlot.this.distanceBinCount)
                return;
            while (timeBin >= this.cumulativeLengths.size())
                this.cumulativeLengths.add(new DoubleVectorAbsDense<LengthUnit>(
                        new double[ContourPlot.this.distanceBinCount], LengthUnit.METER));
            DoubleVectorAbs<LengthUnit> values = this.cumulativeLengths.get(timeBin);
            try
            {
                values.setSI(distanceBin, values.getSI(distanceBin) + distanceCovered);
            }
            catch (ValueException exception)
            {
                System.err.println("Error in incrementData:");
                exception.printStackTrace();
            }
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#computeZValue(int, int, int, int)
         */
        @Override
        public double computeZValue(int firstTimeBin, int endTimeBin, int firstDistanceBin, int endDistanceBin)
        {
            double cumulativeLengthInSI = 0;
            if (firstTimeBin >= this.cumulativeLengths.size())
                return Double.NaN;
            try
            {
                for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
                {
                    if (timeBinIndex >= this.cumulativeLengths.size())
                        break;
                    DoubleVectorAbs<LengthUnit> values = this.cumulativeLengths.get(timeBinIndex);
                    for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                    {
                        // System.out.println("distanceBinIndex is " + distanceBinIndex);
                        cumulativeLengthInSI += values.getSI(distanceBinIndex);
                    }
                }
            }
            catch (ValueException exception)
            {
                System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                        firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
                exception.printStackTrace();
            }
            // System.out.println("cumLength " + cumulativeLengthInSI + " timegran=" + timeGranularity + ", distgran=" +
            // distanceGranularity);
            return 3600 * cumulativeLengthInSI / ContourPlot.this.timeGranularity
                    / ContourPlot.this.distanceGranularity;
        }

    }

    /**
     * Store the data needed for a speed contour graph.
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
     * @version Jul 17, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class SpeedContourDataset extends ContourDataset
    {
        /** Storage for the total time spent in each cell */
        private ArrayList<DoubleVectorAbs<TimeUnit>> cumulativeTimes = new ArrayList<DoubleVectorAbs<TimeUnit>>();

        /** Storage for the total length traveled in each cell */
        private ArrayList<DoubleVectorAbs<LengthUnit>> cumulativeLengths = new ArrayList<DoubleVectorAbs<LengthUnit>>();

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getSeriesKey(int series)
        {
            return "speed";
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#incrementData(int, int, double, double)
         */
        @Override
        public void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered)
        {
            if (timeBin < 0 || distanceBin < 0 || 0 == duration || distanceBin >= ContourPlot.this.distanceBinCount)
                return;
            while (timeBin >= this.cumulativeTimes.size())
            {
                this.cumulativeTimes.add(new DoubleVectorAbsDense<TimeUnit>(
                        new double[ContourPlot.this.distanceBinCount], TimeUnit.SECOND));
                this.cumulativeLengths.add(new DoubleVectorAbsDense<LengthUnit>(
                        new double[ContourPlot.this.distanceBinCount], LengthUnit.METER));
            }
            DoubleVectorAbs<TimeUnit> timeValues = this.cumulativeTimes.get(timeBin);
            DoubleVectorAbs<LengthUnit> lengthValues = this.cumulativeLengths.get(timeBin);
            try
            {
                timeValues.setSI(distanceBin, timeValues.getSI(distanceBin) + duration);
                lengthValues.setSI(distanceBin, lengthValues.getSI(distanceBin) + distanceCovered);
            }
            catch (ValueException exception)
            {
                System.err.println("Error in incrementData:");
                exception.printStackTrace();
            }
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataset#computeZValue(int, int, int, int)
         */
        @Override
        public double computeZValue(int firstTimeBin, int endTimeBin, int firstDistanceBin, int endDistanceBin)
        {
            double cumulativeTimeInSI = 0;
            double cumulativeLengthInSI = 0;
            if (firstTimeBin >= this.cumulativeTimes.size())
                return Double.NaN;
            try
            {
                for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
                {
                    if (timeBinIndex >= this.cumulativeTimes.size())
                        break;
                    DoubleVectorAbs<TimeUnit> timeValues = this.cumulativeTimes.get(timeBinIndex);
                    DoubleVectorAbs<LengthUnit> lengthValues = this.cumulativeLengths.get(timeBinIndex);
                    for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                    {
                        // System.out.println("distanceBinIndex is " + distanceBinIndex);
                        cumulativeTimeInSI += timeValues.getSI(distanceBinIndex);
                        cumulativeLengthInSI += lengthValues.getSI(distanceBinIndex);
                    }
                }
            }
            catch (ValueException exception)
            {
                System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                        firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
                exception.printStackTrace();
            }
            if (0 == cumulativeTimeInSI)
                return Double.NaN;
            return 3600d / 1000 * cumulativeLengthInSI / cumulativeTimeInSI;
        }

    }

    /**
     * Create a XYBlockChart.
     * @param caption String; text to show above the chart
     * @param contourType String; type of value plotted in the chart
     * @param valueFormat String; format string used to render the value in the status bar
     * @param dataset XYZDataset with the values to render
     * @param boundaries double[]; array of three boundary values corresponding to Red, Yellow and Green
     * @param legendStep value difference for successive colors in the legend
     * @return JFreeChart; the new XYBlockChart
     */
    private static JFreeChart createChart(String caption, String contourType, String valueFormat, XYZDataset dataset,
            double[] boundaries, String legendFormat, double legendStep)
    {
        NumberAxis xAxis = new NumberAxis("\u2192 time");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis(contourType);
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        ColorPaintScale paintScale = new ColorPaintScale(valueFormat, boundaries, colorValues);
        renderer.setPaintScale(paintScale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = paintScale.getLowerBound() + i * legendStep;
            if (value > paintScale.getUpperBound())
                break;
            legend.add(new LegendItem(String.format(legendFormat, value), paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.66f);
        JFreeChart chart = new JFreeChart(caption, plot);
        // chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    /**
     * Create a continuous color paint scale for contour plots.
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
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class ColorPaintScale implements PaintScale
    {
        /** Boundary values for this ColorPaintScale */
        private double[] bounds;

        /** Color values to use at the boundary values */
        private Color[] boundColors;

        /** Format string to render values in a human readable format (used in tool tip texts) */
        final String format;

        /**
         * Create a new ColorPaintScale.
         * @param format Format string to render the value under the mouse in a human readable format
         * @param bounds Double[] array of boundary values (must be ordered by increasing value)
         * @param boundColors Color[] array of the colors to use at the boundary values
         */
        ColorPaintScale(String format, double bounds[], Color boundColors[])
        {
            this.format = format;
            if (bounds.length < 2)
                throw new Error("bounds must have >= 2 entries");
            if (bounds.length != boundColors.length)
                throw new Error("bounds must have same length as boundColors");
            this.bounds = new double[bounds.length];
            this.boundColors = new Color[bounds.length];
            // Store the bounds and boundColors in order of increasing bound value.
            // This is as inefficient as bubble sorting...
            for (int nextBound = 0; nextBound < bounds.length; nextBound++)
            {
                // Find the lowest not-yet used bound
                double currentLowest = Double.POSITIVE_INFINITY;
                int bestIndex = -1;
                int index;
                for (index = 0; index < bounds.length; index++)
                    if (bounds[index] < currentLowest && (nextBound == 0 || bounds[index] > this.bounds[nextBound - 1]))
                    {
                        bestIndex = index;
                        currentLowest = bounds[index];
                    }
                if (bestIndex < 0)
                    throw new Error("duplicate value in bounds");
                this.bounds[nextBound] = bounds[bestIndex];
                this.boundColors[nextBound] = boundColors[bestIndex];
            }
        }

        @Override
        public double getLowerBound()
        {
            return this.bounds[0];
        }

        /**
         * Create a mixed color. Depending on the value of ratio the result varies from <i>low</i> to <i>high</i>.
         * @param ratio Double; value between 0.0 and 1.0.
         * @param low Integer; this value is returned when ratio equals 0.0
         * @param high Integer; this value is returned when ratio equals 1.0
         * @return Integer; the ratio-weighted average of <i>low</i> and <i>high</i>
         */
        private static int mixComponent(double ratio, int low, int high)
        {
            double mix = low * (1 - ratio) + high * ratio;
            int result = (int) mix;
            if (result < 0)
                result = 0;
            if (result > 255)
                result = 255;
            return result;
        }

        /**
         * @see org.jfree.chart.renderer.PaintScale#getPaint(double)
         */
        @Override
        public Paint getPaint(double value)
        {
            int bucket;
            for (bucket = 0; bucket < this.bounds.length - 1; bucket++)
                if (value < this.bounds[bucket + 1])
                    break;
            if (bucket >= this.bounds.length - 1)
                bucket = this.bounds.length - 2;
            double ratio = (value - this.bounds[bucket]) / (this.bounds[bucket + 1] - this.bounds[bucket]);

            Color mix =
                    new Color(mixComponent(ratio, this.boundColors[bucket].getRed(),
                            this.boundColors[bucket + 1].getRed()), mixComponent(ratio,
                            this.boundColors[bucket].getGreen(), this.boundColors[bucket + 1].getGreen()),
                            mixComponent(ratio, this.boundColors[bucket].getBlue(),
                                    this.boundColors[bucket + 1].getBlue()));
            return mix;
        }

        /**
         * @see org.jfree.chart.renderer.PaintScale#getUpperBound()
         */
        @Override
        public double getUpperBound()
        {
            return this.bounds[this.bounds.length - 1];
        }

    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        // not used
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent mouseEvent)
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
            XYDataset dataset = plot.getDataset();
            String value = "";
            double roundedTime = t;
            double roundedDistance = distance;
            for (int item = dataset.getItemCount(0); --item >= 0;)
            {
                double x = dataset.getXValue(0, item);
                if ((x + this.timeGranularity / 2 < t) || (x - this.timeGranularity / 2 >= t))
                    continue;
                double y = dataset.getYValue(0, item);
                if ((y + this.distanceGranularity / 2 < distance) || (y - this.distanceGranularity / 2 >= distance))
                    continue;
                roundedTime = x;
                roundedDistance = y;
                double valueUnderMouse = ((XYZDataset) dataset).getZValue(0, item);
                // System.out.println("Value under mouse is " + valueUnderMouse);
                if (Double.isNaN(valueUnderMouse))
                    break;
                String format = ((ColorPaintScale) (((XYBlockRenderer) (plot.getRenderer(0))).getPaintScale())).format;
                value = String.format(format, valueUnderMouse);
            }
            this.statusLabel
                    .setText(String.format("time %.0fs, distance %.0fm%s", roundedTime, roundedDistance, value));
        }
        else
            this.statusLabel.setText(" ");
    }

    /**
     * Main for stand alone running
     * @param args
     */
    public static void main(String[] args)
    {
        ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();
        ContourPlot cp = new ContourPlot("Flow Contour Graph", Type.FLOW, 0, 5000);
        cp.setTitle("Flow Contour Graph");
        cp.setBounds(0, 0, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);
        cp = new ContourPlot("Speed Contour Graph", Type.SPEED, 0, 5000);
        cp.setTitle("Speed Contour Graph");
        cp.setBounds(100, 50, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);
        cp = new ContourPlot("Density Contour Graph", Type.DENSITY, 0, 5000);
        cp.setTitle("Density Contour Graph");
        cp.setBounds(200, 100, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);
        DEVSSimulator simulator = new DEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        final double tick = 0.5;
        int carsCreated = 0;
        ArrayList<Car> cars = new ArrayList<Car>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        while (thisTick < endTime)
        {
            // System.out.println("timeStep is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                Car car =
                        new Car(++carsCreated, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
                cars.add(0, car);
                // System.out.println(String.format("TimeStep=%.1f, there are now %d vehicles", thisTick, cars.size()));
                nextSourceTick += headway;
            }
            if (thisTick == nextMoveTick)
            {
                // Time to move all vehicles forward (even though they do not have simultaneous clock ticks)
                /*
                 * Debugging if (thisTick == 700) { DoubleScalarAbs<TimeUnit> now = new
                 * DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND); for (int i = 0; i < cars.size(); i++)
                 * System.out.println(cars.get(i).toString(now)); }
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
                    // Add the movements of this Car to the contour plots
                    for (ContourPlot contourPlot : contourPlots)
                    {
                        ContourDataset dataSet =
                                (ContourDataset) ((XYPlot) contourPlot.chartPanel.getChart().getPlot()).getDataset();
                        dataSet.addData(car);
                    }
                }
                nextMoveTick += tick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify all contour plots that the underlying data has changed
        for (ContourPlot contourPlot : contourPlots)
            contourPlot.reGraph();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        String[] fields = command.split("[ ]");
        if (fields.length == 2)
        {
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            double value;
            try
            {
                value = nf.parse(fields[1]).doubleValue();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                return;
            }
            if (fields[0].equalsIgnoreCase("setDistanceGranularity"))
            {
                this.distanceGranularity = value;
            }
            else if (fields[0].equalsIgnoreCase("setTimeGranularity"))
            {
                this.timeGranularity = value;
            }
            reGraph();
        }
        else
            throw new Error("Unknown ActionEvent");
    }

    /**
     * Redraw this ContourGraph (after the underlying data, or a granularity setting has been changed).
     */
    private void reGraph()
    {
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
        XYPlot plot = this.chartPanel.getChart().getXYPlot();
        plot.notifyListeners(new PlotChangeEvent(plot));
        XYBlockRenderer blockRenderer = (XYBlockRenderer) plot.getRenderer();
        blockRenderer.setBlockHeight(this.distanceGranularity);
        blockRenderer.setBlockWidth(this.timeGranularity);
    }

    /**
     * Notify interested parties of an event affecting this ContourPlot
     * @param event
     */
    private void notifyListeners(DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
            dcl.datasetChanged(event);
    }

}
