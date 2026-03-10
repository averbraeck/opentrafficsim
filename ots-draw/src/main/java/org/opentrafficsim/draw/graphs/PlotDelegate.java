package org.opentrafficsim.draw.graphs;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.LocalEventProducer;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.draw.graphs.AbstractPlot.PaintState;

/**
 * Plot delegate. This class functions as a template for a data source that is shared among different plots. To keep logic local
 * the delegate is intended as an internal state of a plot. Any changes to settings should occur through the plots, and not
 * directly on the delegate. Typical usage is:
 * <ul>
 * <li>Each plot receives the {@link PlotDelegate} in its constructor and requests from it an initial update interval, delay,
 * and {@link PlotScheduler}.</li>
 * <li>UI events should not directly call the delegate, but only plot objects. When a setting is changed through UI, the plot
 * should be invoked, and the plot should call a method on the delegate to set the relevant setting.</li>
 * <li>When a setting is changed on the delegate that invalidates the whole time span, {@link #invalidateTimeSpan} should be
 * invoked by the method of the delegate that changes the setting.</li>
 * <li>The delegate method should also fire an event defined to indicate the setting change to all UI components that reflect
 * its value.</li>
 * <li>UI elements should listen for the event by calling a method on the plot that adds the listener to the delegate through
 * {@link #addListener}.</li>
 * <li>When {@link AbstractPlot#calculatePaintState} is called on a plot that uses a delegate, it can simply call
 * {@link #calculatePaintStateSafe} on the delegate.</li>
 * <li>If a setting is changed for which the update interval should change, the delegate should invoke
 * {@link AbstractPlot#offerUpdateInterval} on all plots using {@link #getPlots()}.</li>
 * <li>To know whether all of the time span needs to be calculated the calculation method can use
 * {@link #getAndResetInvalidTimeSpan}.</li>
 * <li>To know whether (expensive) calculations can be abandoned as a setting was changed that invalidated the whole time span,
 * the calculation method can use {@link #isInvalidTimeSpan}.</li>
 * <li>The delegate should calculate all state(s) and offer them to the relevant plots using {@link #getPlots()} and
 * {@link AbstractPlot#offerPaintState}.</li>
 * </ul>
 * Notes on synchronization:
 * <ul>
 * <li>Implementations need to synchronize parts that read and write settings, as different threads may access them.</li>
 * <li>Synchronization should be otherwise minimized to prevent a slow UI or delayed calculations. For example when setting the
 * {@code smooth} setting:
 *
 * <pre>
 * public void setSmooth(final boolean smooth)
 * {
 *     synchronized (this)
 *     {
 *         this.smooth = smooth;
 *         invalidateTimeSpan();
 *     }
 *     fireEvent(SMOOTH, smooth);
 * }
 * </pre>
 *
 * </li>
 * <li>Calculation of the paint state should not be class-level synchronized; that would make the UI have to wait on
 * calculations. Method {@link #calculatePaintStateSafe} makes sure a separate lock prevents parallel calculations.</li>
 * <li>Calculations are based on settings. These settings need to be gathered at class-level synchronization, which then needs
 * to be released for the actual calculations. This should occur in {@link #calculatePaintStateUnsafe}. For example:
 *
 * <pre>
 * public void calculatePaintStateUnsafe(final Duration time)
 * {
 *     boolean smooth0;
 *     synchronized (this) // obtain settings safely
 *     {
 *         smooth0 = this.smooth;
 *     }
 *
 *     // do calculations ...
 *
 *     for (FundamentalDiagram plot : getPlots())
 *     {
 *         plot.offerPaintState(paintState);
 *     }
 * }
 * </pre>
 *
 * </li>
 * </ul>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <S> paint state for the plot(s)
 * @param <P> plot type
 */
public abstract class PlotDelegate<S extends PaintState, P extends AbstractPlot<S>> extends LocalEventProducer
{

    /** Initial update interval. */
    private final Duration initialUpdateInterval;

    /** Delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories. */
    private final Duration delay;

    /** Plot scheduler. */
    private PlotScheduler plotScheduler;

    /** Plots. */
    private final Set<P> plots = new LinkedHashSet<>();

    /** Whether the whole time span is invalid. */
    private volatile boolean invalidTimeSpan = true;

    /** Lock to prevent simultaneous calculations. */
    private final Object calculationLock = new Object();

    /**
     * Constructor.
     * @param initialUpdateInterval initial update interval
     * @param delay delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     * @param plotScheduler plot scheduler
     */
    public PlotDelegate(final Duration initialUpdateInterval, final Duration delay, final PlotScheduler plotScheduler)
    {
        this.initialUpdateInterval = initialUpdateInterval;
        this.delay = delay;
        this.plotScheduler = plotScheduler;
    }

    /**
     * Returns the update interval for a plot using this delegate.
     * @return update interval
     */
    public Duration getInitialUpdateInterval()
    {
        return this.initialUpdateInterval;
    }

    /**
     * Returns the delay for a plot using this delegate.
     * @return delay
     */
    public Duration getDelay()
    {
        return this.delay;
    }

    /**
     * Returns the plot scheduler for the first plot that requests one. This plot will be in charge of the updates. All other
     * plots will receive a plot scheduler that will ignore the scheduling of update events.
     * @return plot scheduler
     */
    public PlotScheduler getPlotScheduler()
    {
        PlotScheduler out = this.plotScheduler;
        this.plotScheduler = new PlotScheduler()
        {
            @Override
            public Duration getTime()
            {
                return out.getTime();
            }
        };
        return out;
    }

    /**
     * Add plot. Used to notify plots when data has changed.
     * @param plot plot
     */
    public void addPlot(final P plot)
    {
        this.plots.add(plot);
    }

    /**
     * Clears all connected plots.
     */
    public void clearPlots()
    {
        this.plots.clear();
    }

    /**
     * Returns the plots.
     * @return plots
     */
    public ImmutableSet<P> getPlots()
    {
        return new ImmutableLinkedHashSet<>(this.plots);
    }

    /**
     * Invalidates the whole time span.
     */
    public synchronized void invalidateTimeSpan()
    {
        this.invalidTimeSpan = true;
    }

    /**
     * Returns whether the time span is invalid. This can indicate that calculations can be stopped as some setting was changed
     * that invalidated the time span.
     * @return whether the time span is invalid
     */
    public boolean isInvalidTimeSpan()
    {
        return this.invalidTimeSpan;
    }

    /**
     * Returns whether the whole time span is invalid, and resets this information.
     * @return whether the whole time span is invalid
     */
    public synchronized boolean getAndResetInvalidTimeSpan()
    {
        boolean out = this.invalidTimeSpan;
        this.invalidTimeSpan = false;
        return out;
    }

    /**
     * Invokes {@link #calculatePaintStateUnsafe} in a thread-safe manner. This method should be invoked by plots that use a
     * delegate when the plot is asked to calculate the paint state.
     * @param time current time
     */
    public void calculatePaintStateSafe(final Duration time)
    {
        // worker thread from one plot may call this while another is still calculating
        synchronized (this.calculationLock)
        {
            calculatePaintStateUnsafe(time);
        }
    }

    /**
     * Calculates paint state and offers it to the coupled plots. This method should only be invoked by
     * {@link #calculatePaintStateSafe} which makes sure that setting changes from different plots (with different working
     * threads) do not cause parallel calculations on the same delegate. This makes sure that internal data gathering can occur
     * consistently.
     * @param time current time
     */
    protected abstract void calculatePaintStateUnsafe(Duration time);

}
