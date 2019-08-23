package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.event.EventType;

/**
 * Super class of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * delegate methods for sub classes to implement.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractPlot implements Identifiable, Dataset
{

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a graph. Not used internally.<br>
     * Payload: String graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_ADD_EVENT = new EventType("GRAPH.ADD");

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a graph. Not used internally.<br>
     * Payload: String Graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_REMOVE_EVENT = new EventType("GRAPH.REMOVE");

    /** Initial upper bound for the time scale. */
    public static final Time DEFAULT_INITIAL_UPPER_TIME_BOUND = Time.createSI(300.0);

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Unique ID of the chart. */
    private final String id = UUID.randomUUID().toString();

    /** Caption. */
    private final String caption;

    /** The chart, so we can export it. */
    private JFreeChart chart;

    /** List of parties interested in changes of this plot. */
    private Set<DatasetChangeListener> listeners = new LinkedHashSet<>();

    /** Delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories. */
    private final Duration delay;

    /** Time of last data update. */
    private Time updateTime;

    /** Number of updates. */
    private int updates = 0;

    /** Update interval. */
    private Duration updateInterval;

    /** Event of next update. */
    private SimEventInterface<SimTimeDoubleUnit> updateEvent;

    /**
     * Constructor.
     * @param simulator OTSSimulatorInterface; simulator
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param delay Duration; amount of time that chart runs behind simulation to prevent gaps in the charted data
     */
    public AbstractPlot(final OTSSimulatorInterface simulator, final String caption, final Duration updateInterval,
            final Duration delay)
    {
        this.simulator = simulator;
        this.caption = caption;
        this.updateInterval = updateInterval;
        this.delay = delay;
        update(); // start redraw chain
    }

    /**
     * Sets the chart and adds menus and listeners.
     * @param chart JFreeChart; chart
     */
    @SuppressWarnings("methodlength")
    protected void setChart(final JFreeChart chart)
    {
        this.chart = chart;

        // make title somewhat smaller
        chart.setTitle(new TextTitle(chart.getTitle().getText(), new Font("SansSerif", java.awt.Font.BOLD, 16)));

        // default colors and zoom behavior
        chart.getPlot().setBackgroundPaint(Color.LIGHT_GRAY);
        chart.setBackgroundPaint(Color.WHITE);
        if (chart.getPlot() instanceof XYPlot)
        {
            chart.getXYPlot().setDomainGridlinePaint(Color.WHITE);
            chart.getXYPlot().setRangeGridlinePaint(Color.WHITE);
        }

    }

    /**
     * Returns the chart as a byte array representing a png image.
     * @param width int; width
     * @param height int; height
     * @param fontSize double; font size (16 is the original on screen size)
     * @return byte[]; the chart as a byte array representing a png image
     * @throws IOException on IO exception
     */
    public byte[] encodeAsPng(final int width, final int height, final double fontSize) throws IOException
    {
        // to double the font size, we halve the base dimensions
        // JFreeChart will the assign more area (relatively) to the fixed actual font size
        double baseWidth = width / (fontSize / 16);
        double baseHeight = height / (fontSize / 16);
        // this code is from ChartUtils.writeScaledChartAsPNG
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        // to compensate for the base dimensions which are not w x h, we scale the drawing
        AffineTransform saved = g2.getTransform();
        g2.transform(AffineTransform.getScaleInstance(width / baseWidth, height / baseHeight));
        getChart().draw(g2, new Rectangle2D.Double(0, 0, baseWidth, baseHeight), null, null);
        g2.setTransform(saved);
        g2.dispose();
        return ChartUtils.encodeAsPNG(image);
    }

    /** {@inheritDoc} */
    @Override
    public final DatasetGroup getGroup()
    {
        return null; // not used
    }

    /** {@inheritDoc} */
    @Override
    public final void setGroup(final DatasetGroup group)
    {
        // not used
    }

    /**
     * Overridable; activates auto bounds on domain axis from user input. This class does not force the use of {@code XYPlot}s,
     * but the auto bounds command comes from the {@code ChartPanel} that this class creates. In case the used plot is a
     * {@code XYPlot}, this method is then invoked. Sub classes with auto domain bounds that work with an {@code XYPlot} should
     * implement this. The method is not abstract as the use of {@code XYPlot} is not obligated.
     * @param plot XYPlot; plot
     */
    protected void setAutoBoundDomain(final XYPlot plot)
    {
        //
    }

    /**
     * Overridable; activates auto bounds on range axis from user input. This class does not force the use of {@code XYPlot}s,
     * but the auto bounds command comes from the {@code ChartPanel} that this class creates. In case the used plot is a
     * {@code XYPlot}, this method is then invoked. Sub classes with auto range bounds that work with an {@code XYPlot} should
     * implement this. The method is not abstract as the use of {@code XYPlot} is not obligated.
     * @param plot XYPlot; plot
     */
    protected void setAutoBoundRange(final XYPlot plot)
    {
        //
    }

    /**
     * Return the graph type for transceiver.
     * @return GraphType; the graph type.
     */
    public abstract GraphType getGraphType();

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue double; domain value (x-axis)
     * @param rangeValue double; range value (y-axis)
     * @return String; status label when the mouse is over the given location
     */
    protected abstract String getStatusLabel(double domainValue, double rangeValue);

    /**
     * Increase the simulated time span.
     * @param time Time; time to increase to
     */
    protected abstract void increaseTime(Time time);

    /**
     * Notify all change listeners.
     */
    public final void notifyPlotChange()
    {
        DatasetChangeEvent event = new DatasetChangeEvent(this, this);
        for (DatasetChangeListener dcl : this.listeners)
        {
            dcl.datasetChanged(event);
        }
    }

    /**
     * Returns the chart.
     * @return JFreeChart; chart
     */
    protected final JFreeChart getChart()
    {
        return this.chart;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final void addChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.add(listener);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Retrieve the simulator.
     * @return OTSSimulatorInterface; the simulator
     */
    public OTSSimulatorInterface getSimulator()
    {
        return simulator;
    }

    /**
     * Sets a new update interval.
     * @param interval Duration; update interval
     */
    protected final void setUpdateInterval(final Duration interval)
    {
        if (this.updateEvent != null)
        {
            this.simulator.cancelEvent(this.updateEvent);
        }
        this.updates = (int) (this.simulator.getSimulatorTime().si / interval.si);
        this.updateInterval = interval;
        this.updateTime = Time.createSI(this.updates * this.updateInterval.si);
        scheduleNextUpdateEvent();
    }

    /**
     * Returns time until which data should be plotted.
     * @return Time; time until which data should be plotted
     */
    protected final Time getUpdateTime()
    {
        return this.updateTime;
    }

    /**
     * Redraws the plot and schedules the next update.
     */
    protected void update()
    {
        this.updateTime = this.simulator.getSimulatorTime();
        increaseTime(this.updateTime.minus(this.delay));
        notifyPlotChange();
        scheduleNextUpdateEvent();
    }

    /**
     * Schedules the next update event.
     */
    private void scheduleNextUpdateEvent()
    {
        try
        {
            this.updates++;
            // events are scheduled slightly later, so all influencing movements have occurred
            this.updateEvent = this.simulator.scheduleEventAbs(
                    Time.createSI(this.updateInterval.si * this.updates + this.delay.si), this, this, "update", null);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Unexpected exception while updating plot.", exception);
        }
    }

    /**
     * Retrieve the caption.
     * @return String; the caption of the plot
     */
    public String getCaption()
    {
        return caption;
    }

}
