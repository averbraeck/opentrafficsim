package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Duration;
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
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.draw.graphs.AbstractPlot.PaintState;

/**
 * Super class of all plots. This schedules regular updates, creates menus and deals with listeners. There are a number of
 * methods for sub-classes to implement.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <S> paint state implementation
 */
public abstract class AbstractPlot<S extends PaintState> implements Identifiable, Dataset
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
    public static final Duration DEFAULT_INITIAL_UPPER_TIME_BOUND = Duration.ofSI(300.0);

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

    /** Delay so critical future events have occurred, e.g. GTU's next move to extend trajectories. */
    private final Duration delay;

    /** Update interval. */
    private Duration updateInterval;

    /** New update interval to use. */
    private volatile Duration suggestedUpdateInterval;

    /** Queue for the worker thread. */
    private BlockingQueue<Duration> workerQueue = new LinkedBlockingQueue<>();

    /** Current paint state. */
    private volatile S paintState;

    /** Thread safe offered paint state. */
    private final AtomicReference<S> pendingPaintState = new AtomicReference<>();

    /** Makes sure the paint state is only set once. */
    private final AtomicBoolean adoptionPosted = new AtomicBoolean(false);

    /**
     * Constructor.
     * @param scheduler scheduler
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
        this.paintState = emptyPaintState();
        scheduleUpdateEvent(); // start redraw chain

        // worker thread
        Thread invokingThread = Thread.currentThread();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!invokingThread.isInterrupted())
                {
                    try
                    {
                        Duration time = AbstractPlot.this.workerQueue.take();
                        if (time != null && AbstractPlot.this.workerQueue.isEmpty()) // only take last update request
                        {
                            calculatePaintState(time);
                        }
                    }
                    catch (InterruptedException exception)
                    {
                        Logger.ots().error(exception, "Worker thread for plot {} stopped.", AbstractPlot.this.caption);
                        break;
                    }
                }
            }
        }, AbstractPlot.this.caption);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Returns an empty paint state. This is used at plot initialization.
     * @return empty paint state.
     */
    protected abstract S emptyPaintState();

    /**
     * Sets the chart and adds menus and listeners.
     * @param chart chart
     */
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
     * Returns the chart as a byte array representing a PNG image.
     * @param width width
     * @param height height
     * @param fontSize font size (16 is the original on screen size)
     * @return the chart as a byte array representing a PNG image
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

    @Override
    public final DatasetGroup getGroup()
    {
        return null; // not used
    }

    @Override
    public final void setGroup(final DatasetGroup group)
    {
        // not used
    }

    /**
     * Overridable; activates auto bounds on domain axis from user input. This class does not force the use of {@link XYPlot}s,
     * but the auto bounds command comes from the {@code ChartPanel} that shows this plot. In case the used plot is a
     * {@link XYPlot}, this method is then invoked. Sub classes with auto domain bounds that work with an {@link XYPlot} should
     * implement this. The method is not abstract as the use of {@code XYPlot} is not obligated.
     * @param plot plot
     */
    public void setAutoBoundDomain(final XYPlot plot)
    {
        throw new UnsupportedOperationException("Plot is a XYPlot but does not implement setAutoBoundDomain");
    }

    /**
     * Overridable; activates auto bounds on range axis from user input. This class does not force the use of {@link XYPlot}s,
     * but the auto bounds command comes from the {@code ChartPanel} that shows this plot. In case the used plot is a
     * {@link XYPlot}, this method is then invoked. Sub classes with auto range bounds that work with an {@link XYPlot} should
     * implement this. The method is not abstract as the use of {@code XYPlot} is not obligated.
     * @param plot plot
     */
    public void setAutoBoundRange(final XYPlot plot)
    {
        throw new UnsupportedOperationException("Plot is a XYPlot but does not implement setAutoBoundRange");
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
     * Returns the chart.
     * @return chart
     */
    public JFreeChart getChart()
    {
        return this.chart;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Retrieve the caption.
     * @return the caption of the plot
     */
    public String getCaption()
    {
        return this.caption;
    }

    // ===== Listeners =====

    @Override
    public void addChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.add(listener);
    }

    @Override
    public void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listeners.remove(listener);
    }

    /**
     * Notify all change listeners.
     */
    public void notifyPlotChange()
    {
        // take a snapshot to avoid concurrent modification during iteration
        final List<DatasetChangeListener> snapshot;
        synchronized (this)
        {
            snapshot = new ArrayList<>(this.listeners);
        }

        Runnable r = () ->
        {
            DatasetChangeEvent event = new DatasetChangeEvent(this, this);
            for (DatasetChangeListener dcl : snapshot)
            {
                dcl.datasetChanged(event);
            }
        };

        // invoke only on Swing EDT
        if (SwingUtilities.isEventDispatchThread())
        {
            r.run();
        }
        else
        {
            SwingUtilities.invokeLater(r);
        }
    }

    // ===== Paint state =====

    /**
     * Requests a calculation of the paint state. May be invoked by sub-classes whenever a setting was changed that needs a
     * recalculation.
     */
    protected void invalidate()
    {
        this.workerQueue.offer(this.scheduler.getTime());
    }

    /**
     * Calculates the paint state object and offers it through {@link #offerPaintState}, or delegates this work to a delegate.
     * This method is invoked by the worker thread and can thus perform heavy calculations outside of the Swing EDT.
     * Intermediate paint states during long calculations may also be offered. It is up to the implementation to either
     * calculate a complete paint state, or cumulatively built on the results from previous calls. It is also up to the
     * implementation to know when the whole time span needs to be recalculated due to property changes.
     * @param time time until which data in the paint state should be calculated
     */
    protected abstract void calculatePaintState(Duration time);

    /**
     * Offer new paint state. This method can be invoked by any thread, and will make sure the actual setting of the paint state
     * will occur on the Swing EDT. This assures that no paint state is changed as Swing is painting (i.e. as the plot is asked
     * for data to paint). Listeners are notified on the Swing EDT as soon as the paint state has been set.
     * @param paintState paint state
     */
    @SuppressWarnings("hiddenfield")
    public void offerPaintState(final S paintState)
    {
        Logger.ots().trace("Offering paint state on plot: {}", this.caption);
        this.pendingPaintState.set(paintState);
        if (this.adoptionPosted.compareAndSet(false, true))
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                setPaintState();
            }
            else
            {
                SwingUtilities.invokeLater(() -> setPaintState());
            }
        }
    }

    /**
     * Sets the paint state in a thread safe manner and notifies the listeners. This method is always invoked on the Swing EDT.
     * This method may be overridden to use a newly set paint state (after calling {@code super.setPaintState()}) to set
     * internal properties. For example, setting the block size of an internal block renderer based on the granularity of the
     * data.
     */
    protected void setPaintState()
    {
        Logger.ots().trace("Setting paint state on plot: {}", this.caption);
        try
        {
            S s = this.pendingPaintState.getAndSet(null);
            if (s != null)
            {
                // single point where the visible paint state changes
                this.paintState = s;
                // notify on Swing EDT; painting will occur after this completes
                Logger.ots().trace("Notifying plot changed: {}", this.caption);
                notifyPlotChange();
            }
        }
        finally
        {
            this.adoptionPosted.set(false);
        }
    }

    /**
     * Returns the current paint state that should be used to return paint data (i.e. x-values, etc.)
     * @return current paint state
     */
    protected S getPaintState()
    {
        return this.paintState;
    }

    /**
     * Returns up to what time data is available for painting.
     * @return up to what time data is available for painting
     */
    public Duration getAvailableTime()
    {
        return this.paintState.getAvailableTime();
    }

    // ===== Update chain =====

    /**
     * Suggests a new update interval. This does not affect any time granularity, but only when update events occur. This method
     * will ask the {@link PlotScheduler} to schedule an update now. The next update will set the update interval and schedule
     * the next regular update aligning with the new interval. If an update is also desired right now {@link #invalidate} needs
     * to be invoked. This method is typically called on the Swing EDT and will request the scheduler asynchronously. This
     * method does not block.
     * @param interval update interval
     */
    public void offerUpdateInterval(final Duration interval)
    {
        this.suggestedUpdateInterval = interval;
        // run asynchronous because we do not want the Swing EDT to wait for the simulation thread semaphore
        CompletableFuture.runAsync(() ->
        {
            this.scheduler.scheduleUpdateNow(this); // divert to scheduling thread
        });
    }

    /*
     * Implementation note: A specific problem is prevented by using offerUpdateInterval() to schedule an update, and update()
     * to then take up the new update interval. When the interval is changed, the next time to schedule an update needs to be
     * determined. If this is done by offerUpdateInterval() by taking the current simulation time and adding a delta within the
     * Swing EDT thread, the scheduler thread may progress time beyond the resulting update time before the event is actually
     * scheduled. Only the scheduler thread should be in control of time and update event scheduling. Furthermore the scheduling
     * is parallelized to allow the Swing EDT to not wait for a potentially very busy simulator thread.
     */

    /**
     * Requests the worker thread to perform calculations up to the current time and (in the Swing EDT thread) update the plots.
     * The worker thread will take on this request once any current calculations are done. If multiple updates are requested
     * before the worker thread is done, only the update with latest time is executed. This method should only be invoked by the
     * thread that governs time (typically by the {@link PlotScheduler}). Otherwise events may be erroneously scheduled in the
     * past.
     * <p>
     * After a new update interval was suggested through {@link #offerUpdateInterval} this method does not do the above, but
     * instead only schedules the next update aligning with the new interval.
     */
    public void update()
    {
        if (this.updateInterval != null && this.suggestedUpdateInterval != null
                && !this.updateInterval.equals(this.suggestedUpdateInterval))
        {
            // take up new update interval and reset 'updates' to fall in alignment
            this.updateInterval = this.suggestedUpdateInterval;
            this.suggestedUpdateInterval = null;
        }
        else
        {
            invalidate();
        }
        scheduleUpdateEvent();
    }

    /**
     * Schedules the next update event.
     */
    private void scheduleUpdateEvent()
    {
        double t = this.scheduler.getTime().si;
        int n = (int) (t / this.updateInterval.si) + 1; // robust to accidental duplicate/out-of-tempo updates
        // events are scheduled slightly later, so all influencing movements have occurred
        double tNext = this.updateInterval.si * n + this.delay.si;
        if (tNext <= t)
        {
            tNext += this.updateInterval.si;
        }
        this.scheduler.scheduleUpdate(Duration.ofSI(tNext), this);
    }

    /**
     * Interface for paint state objects.
     */
    interface PaintState
    {

        /**
         * Returns up to what time data is available for painting.
         * @return up to what time data is available for painting
         */
        Duration getAvailableTime();

    }

}
