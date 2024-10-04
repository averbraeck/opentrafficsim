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
import org.djutils.base.Identifiable;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 * Super class of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * delegate methods for sub classes to implement.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractPlot implements Identifiable, Dataset
{

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a graph. Not used internally.<br>
     * Payload: String graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_ADD_EVENT = new EventType("GRAPH.ADD",
            new MetaData("Graph add", "Graph added", new ObjectDescriptor("Graph id", "Id of the graph", String.class)));

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a graph. Not used internally.<br>
     * Payload: String Graph caption (not an array, just a String)
     */
    public static final EventType GRAPH_REMOVE_EVENT = new EventType("GRAPH.REMOVE",
            new MetaData("Graph remove", "Graph removed", new ObjectDescriptor("Graph id", "Id of the graph", String.class)));

    /** Initial upper bound for the time scale. */
    public static final Time DEFAULT_INITIAL_UPPER_TIME_BOUND = Time.instantiateSI(300.0);

    /** Scheduler. */
    private final PlotScheduler scheduler;

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

    /**
     * Constructor.
     * @param scheduler scheduler.
     * @param caption caption
     * @param updateInterval regular update interval (simulation time)
     * @param delay amount of time that chart runs behind simulation to prevent gaps in the charted data
     */
    public AbstractPlot(final PlotScheduler scheduler, final String caption, final Duration updateInterval,
            final Duration delay)
    {
        this.scheduler = scheduler;
        this.caption = caption;
        this.updateInterval = updateInterval;
        this.delay = delay;
        this.updates = (int) (scheduler.getTime().si / updateInterval.si); // when creating plot during simulation
        update(); // start redraw chain
    }

    /**
     * Sets the chart and adds menus and listeners.
     * @param chart chart
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
     * @param width width
     * @param height height
     * @param fontSize font size (16 is the original on screen size)
     * @return the chart as a byte array representing a png image
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
     * @param plot plot
     */
    public void setAutoBoundDomain(final XYPlot plot)
    {
        //
    }

    /**
     * Overridable; activates auto bounds on range axis from user input. This class does not force the use of {@code XYPlot}s,
     * but the auto bounds command comes from the {@code ChartPanel} that this class creates. In case the used plot is a
     * {@code XYPlot}, this method is then invoked. Sub classes with auto range bounds that work with an {@code XYPlot} should
     * implement this. The method is not abstract as the use of {@code XYPlot} is not obligated.
     * @param plot plot
     */
    public void setAutoBoundRange(final XYPlot plot)
    {
        //
    }

    /**
     * Return the graph type for transceiver.
     * @return the graph type.
     */
    public abstract GraphType getGraphType();

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue domain value (x-axis)
     * @param rangeValue range value (y-axis)
     * @return status label when the mouse is over the given location
     */
    public abstract String getStatusLabel(double domainValue, double rangeValue);

    /**
     * Increase the simulated time span.
     * @param time time to increase to
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
     * @return chart
     */
    public final JFreeChart getChart()
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
     * Sets a new update interval.
     * @param interval update interval
     */
    public final void setUpdateInterval(final Duration interval)
    {
        this.scheduler.cancelEvent(this);
        this.updates = (int) (this.scheduler.getTime().si / interval.si);
        this.updateInterval = interval;
        this.updateTime = Time.instantiateSI(this.updates * this.updateInterval.si);
        scheduleNextUpdateEvent();
    }

    /**
     * Returns time until which data should be plotted.
     * @return time until which data should be plotted
     */
    public final Time getUpdateTime()
    {
        return this.updateTime;
    }

    /**
     * Redraws the plot and schedules the next update.
     */
    protected void update()
    {
        // TODO: next event may be scheduled in the past if the scheduler is running fast during these few calls
        this.updateTime = this.scheduler.getTime();
        increaseTime(this.updateTime.minus(this.delay));
        notifyPlotChange();
        scheduleNextUpdateEvent();
    }

    /**
     * Schedules the next update event.
     */
    private void scheduleNextUpdateEvent()
    {
        this.updates++;
        // events are scheduled slightly later, so all influencing movements have occurred
        this.scheduler.scheduleUpdate(Time.instantiateSI(this.updateInterval.si * this.updates + this.delay.si), this);
    }

    /**
     * Retrieve the caption.
     * @return the caption of the plot
     */
    public String getCaption()
    {
        return this.caption;
    }

}
