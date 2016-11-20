package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.Throw;

/**
 * The Fundamental Diagram Graph; see <a href="http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow"> Wikipedia:
 * http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Jul 31, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramLane extends JFrame implements XYDataset, ActionListener
{
    /** */
    private static final long serialVersionUID = 20140701L;

    /** The ChartPanel for this Fundamental Diagram. */
    private JFreeChart chartPanel;

    /** Caption for this Fundamental Diagram. */
    private final String caption;

    /** Area to show status information. */
    private final JLabel statusLabel;

    /** Sample duration of the detector that generates this Fundamental Diagram. */
    private final Duration aggregationTime;

    /** Storage for the Samples. */
    private ArrayList<Sample> samples = new ArrayList<Sample>();

    /** Definition of the density axis. */
    private Axis densityAxis = new Axis(new LinearDensity(0, LinearDensityUnit.PER_KILOMETER), new LinearDensity(200,
            LinearDensityUnit.PER_KILOMETER), null, 0d, "Density [veh/km]", "Density", "density %.1f veh/km");

    /**
     * @return densityAxis
     */
    public final Axis getDensityAxis()
    {
        return this.densityAxis;
    }

    /** Definition of the speed axis. */
    private Axis speedAxis = new Axis(new Speed(0, SpeedUnit.KM_PER_HOUR), new Speed(120, SpeedUnit.KM_PER_HOUR), null, 0d,
            "Speed [km/h]", "Speed", "speed %.0f km/h");

    /**
     * @return speedAxis
     */
    public final Axis getSpeedAxis()
    {
        return this.speedAxis;
    }

    /**
     * @return flowAxis
     */
    public final Axis getFlowAxis()
    {
        return this.flowAxis;
    }

    /** Definition of the flow axis. */
    private Axis flowAxis = new Axis(new Frequency(0, FrequencyUnit.PER_HOUR), new Frequency(3000d, FrequencyUnit.HERTZ), null,
            0d, "Flow [veh/h]", "Flow", "flow %.0f veh/h");

    /** The currently shown X-axis. */
    private Axis xAxis;

    /** The currently shown Y-axis. */
    private Axis yAxis;

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /** The lane for which data is gathered. */
    private final Lane lane;

    /** The simulator to schedule sampling. */
    private final OTSDEVSSimulatorInterface simulator;

    /** Flow counter. */

    int flow = 0;

    /**
     * Retrieve the format string for the Y axis.
     * @return format string
     */
    public final String getYAxisFormat()
    {
        return this.yAxis.getFormat();
    }

    /**
     * Retrieve the format string for the X axis.
     * @return format string
     */
    public final String getXAxisFormat()
    {
        return this.xAxis.getFormat();
    }

    /**
     * Graph a Fundamental Diagram.
     * @param caption String; the caption shown above the graphing area.
     * @param aggregationTime DoubleScalarRel&lt;TimeUnit&gt;; the aggregation of the detector that generates the data for this
     *            Fundamental diagram
     * @param lane Lane; the Lane on which the traffic will be sampled
     * @param simulator the simulator to schedule the sampling on
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException in case scheduling of the sampler fails
     */
    public FundamentalDiagramLane(final String caption, final Duration aggregationTime, final Lane lane,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException, SimRuntimeException
    {
        if (aggregationTime.getSI() <= 0)
        {
            throw new Error("Aggregation time must be > 0 (got " + aggregationTime + ")");
        }
        this.aggregationTime = aggregationTime;
        this.caption = caption;
        this.lane = lane;
        this.simulator = simulator;
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        this.chartPanel =
                ChartFactory.createXYLineChart(this.caption, "", "", this, PlotOrientation.VERTICAL, false, false, false);
        FixCaption.fixCaption(this.chartPanel);
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.chartPanel.getXYPlot().getRenderer();
        renderer.setBaseShapesVisible(true);

        final ChartPanel cp = new ChartPanel(this.chartPanel);
        PointerHandler ph = new PointerHandler()
        {
            /** */
            private static final long serialVersionUID = 120140000;

            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    setStatusText(" ");
                    return;
                }
                String s1 = String.format(getXAxisFormat(), domainValue);
                String s2 = String.format(getYAxisFormat(), rangeValue);
                setStatusText(s1 + ", " + s2);
            }

        };
        cp.addMouseMotionListener(ph);
        cp.addMouseListener(ph);
        cp.setMouseWheelEnabled(true);
        final JMenu subMenu = new JMenu("Set layout");
        final ButtonGroup group = new ButtonGroup();
        final JRadioButtonMenuItem defaultItem = addMenuItem(subMenu, group, getDensityAxis(), this.flowAxis, true);
        addMenuItem(subMenu, group, this.flowAxis, this.speedAxis, false);
        addMenuItem(subMenu, group, this.densityAxis, this.speedAxis, false);
        actionPerformed(new ActionEvent(this, 0, defaultItem.getActionCommand()));
        final JPopupMenu popupMenu = cp.getPopupMenu();
        popupMenu.insert(subMenu, 0);
        this.add(cp, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        this.add(this.statusLabel, BorderLayout.SOUTH);
        simulator.scheduleEventRel(this.aggregationTime, this, this, "addData", null);
        new FlowSensor(lane);
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
     * @return aggregationTime
     */
    public final Duration getAggregationTime()
    {
        return this.aggregationTime;
    }

    /**
     * Build one JRadioButtonMenuItem for the sub menu of the context menu.
     * @param subMenu JMenu; the menu to which the new JRadioButtonMenuItem is added
     * @param group ButtonGroup; the buttonGroup for the new JRadioButtonMenuItem
     * @param xAxisToSelect Axis; the Axis that will become X-axis when this item is clicked
     * @param yAxisToSelect Axis; the Axis that will become Y-axis when this item is clicked
     * @param selected Boolean; if true, the new JRadioButtonMenuItem will be selected; if false, the new JRadioButtonMenuItem
     *            will <b>not</b> be selected
     * @return JRatioButtonMenuItem; the newly added item
     */
    private JRadioButtonMenuItem addMenuItem(final JMenu subMenu, final ButtonGroup group, final Axis xAxisToSelect,
            final Axis yAxisToSelect, final boolean selected)
    {
        final JRadioButtonMenuItem item =
                new JRadioButtonMenuItem(yAxisToSelect.getShortName() + " / " + xAxisToSelect.getShortName());
        item.setSelected(selected);
        item.setActionCommand(yAxisToSelect.getShortName() + "/" + xAxisToSelect.getShortName());
        item.addActionListener(this);
        subMenu.add(item);
        group.add(item);
        return item;
    }

    /**
     * Add the density and average speed on the lane to this Fundamental Diagram.
     * @throws SimRuntimeException when scheduling of next sampling time fails
     */
    public final void addData() throws SimRuntimeException
    {
        // collect (harmonic) mean speed and number of vehicles per meter on the lane
        double n = this.lane.getGtuList().size();
        double density = n / this.lane.getLength().si;
        if (density > 0.0)
        {
            double meanSpeed = 0.0;
            for (LaneBasedGTU gtu : this.lane.getGtuList())
            {
                meanSpeed += 1 / gtu.getSpeed().si;
            }
            meanSpeed = n / meanSpeed;
            this.samples.add(new Sample(meanSpeed, density, this.flow / this.aggregationTime.si));
            this.flow = 0;
        }
        this.simulator.scheduleEventRel(this.aggregationTime, this, this, "addData", null);
    }

    /**
     * Set up a JFreeChart axis.
     * @param valueAxis ValueAxis; the axis to set up
     * @param axis Axis; the Axis that provides the data to setup the ValueAxis
     */
    private static void configureAxis(final ValueAxis valueAxis, final Axis axis)
    {
        valueAxis.setLabel("\u2192 " + axis.getName());
        valueAxis.setRange(axis.getMinimumValue().getInUnit(), axis.getMaximumValue().getInUnit());
    }

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed, or to change axes).
     */
    public final void reGraph()
    {
        NumberAxis numberAxis = new NumberAxis();
        configureAxis(numberAxis, this.xAxis);
        this.chartPanel.getXYPlot().setDomainAxis(numberAxis);
        this.chartPanel.getPlot().axisChanged(new AxisChangeEvent(numberAxis));
        numberAxis = new NumberAxis();
        configureAxis(numberAxis, this.yAxis);
        this.chartPanel.getXYPlot().setRangeAxis(numberAxis);
        this.chartPanel.getPlot().axisChanged(new AxisChangeEvent(numberAxis));
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

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return 1;
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
        return this.samples.size();
    }

    /**
     * Retrieve a value from the recorded samples.
     * @param item Integer; the rank number of the sample
     * @param axis Axis; the axis that determines which quantity to retrieve
     * @return Double; the requested value, or Double.NaN if the sample does not (yet) exist
     */
    private Double getSample(final int item, final Axis axis)
    {
        if (item >= this.samples.size())
        {
            return Double.NaN;
        }
        double result = this.samples.get(item).getValue(axis);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getXValue(final int series, final int item)
    {
        return getSample(item, this.xAxis);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getYValue(final int series, final int item)
    {
        return getSample(item, this.yAxis);
    }

    /** {@inheritDoc} */
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        final String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        final String[] fields = command.split("[/]");
        if (fields.length == 2)
        {
            for (String field : fields)
            {
                if (field.equalsIgnoreCase(this.densityAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.densityAxis;
                    }
                    else
                    {
                        this.xAxis = this.densityAxis;
                    }
                }
                else if (field.equalsIgnoreCase(this.flowAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.flowAxis;
                    }
                    else
                    {
                        this.xAxis = this.flowAxis;
                    }
                }
                else if (field.equalsIgnoreCase(this.speedAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.speedAxis;
                    }
                    else
                    {
                        this.xAxis = this.speedAxis;
                    }
                }
                else
                {
                    throw new Error("Cannot find axis name: " + field);
                }
            }
            reGraph();
        }
        else
        {
            throw new Error("Unknown ActionEvent");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "FundamentalDiagramLane [caption=" + this.caption + ", aggregationTime=" + this.aggregationTime
                + ", samples.size=" + this.samples.size() + ", lane=" + this.lane + ", flow=" + this.flow + "]";
    }

    /**
     * Storage for one sample of data collected with mean speed [m/s] and number of vehicles per km. Flow per second can be
     * calculated from these two numbers; currently the flow is provided (but never used).
     * <p>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Sample implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20140000L;

        /** Mean speed observed during this sample [m/s]. */
        private final double meanSpeed;

        /** Density [veh/m]. */
        private final double density;

        /** Flow [veh/s]. */
        private final double flow;

        /**
         * @param meanSpeed mean speed observed during this sample [m/s]
         * @param density density [veh/m]
         * @param flow [veh/s]
         */
        public Sample(final double meanSpeed, final double density, final double flow)
        {
            super();
            this.meanSpeed = meanSpeed;
            this.density = density;
            this.flow = flow;
        }

        /**
         * Retrieve a value stored in this Sample.
         * @param axis Axis; the axis along which the data is requested
         * @return double; the retrieved value
         */
        public double getValue(final Axis axis)
        {
            if (axis == getDensityAxis())
            {
                return 1000.0 * this.density; // [veh/km]
            }
            else if (axis == getFlowAxis())
            {
                return 3600.0 * this.meanSpeed * this.density; // [veh/h]
            }
            else if (axis == getSpeedAxis())
            {
                return 3.6 * this.meanSpeed; // [km / h]
            }
            else
            {
                throw new Error("Sample.getValue: Can not identify axis");
            }
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "Sample [meanSpeed=" + this.meanSpeed + ", density=" + this.density + ", flow=" + this.flow + "]";
        }
    }

    /** */
    private class FlowSensor extends AbstractSensor
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param lane the lane for which to build the flowSensor
         * @throws NetworkException when the position on the lane is out of bounds
         */
        public FlowSensor(final Lane lane) throws NetworkException
        {
            super("FLOW", lane, lane.getLength().divideBy(2.0), RelativePosition.FRONT, null);
        }

        /** {@inheritDoc} */
        @Override
        public void triggerResponse(final LaneBasedGTU gtu)
        {
            FundamentalDiagramLane.this.flow += 1;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "FlowSensor []";
        }

        /** {@inheritDoc} */
        @Override
        public FlowSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
                final boolean animation) throws NetworkException
        {
            Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
            return new FlowSensor((Lane) newCSE);
        }

    }

}
