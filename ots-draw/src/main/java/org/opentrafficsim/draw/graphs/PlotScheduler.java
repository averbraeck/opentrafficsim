package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Interface between plots (subclasses of {@link AbstractPlot}) and some source that knows about time in the context, e.g. a
 * simulator, or a data loader which knows all time has past. For offline purposes, {@code PlotScheduler.OFFLINE} can be used.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PlotScheduler
{

    /**
     * Default offline scheduler. This will only provide an infinite time, causing {@link AbstractPlot#update} to be invoked on
     * all plots once.
     */
    PlotScheduler OFFLINE = new PlotScheduler()
    {
        @Override
        public Duration getTime()
        {
            return Duration.ofSI(Double.MAX_VALUE);
        }
    };

    /**
     * Returns the time.
     * @return time
     */
    Duration getTime();

    /**
     * Cancel event on plot.
     * <p>
     * The default implementation does nothing, allowing non-verbose anonymous extensions.
     * @param abstractPlot plot
     */
    default void cancelEvent(final AbstractPlot<?> abstractPlot)
    {
        //
    }

    /**
     * Schedule {@link AbstractPlot#update} call on {@code abstractPlot}.
     * <p>
     * The default implementation does nothing, allowing non-verbose anonymous extensions.
     * @param time time.
     * @param abstractPlot plot.
     */
    default void scheduleUpdate(final Duration time, final AbstractPlot<?> abstractPlot)
    {
        //
    }

    /**
     * Schedule {@link AbstractPlot#update} call on {@code abstractPlot} now.
     * <p>
     * The default implementation does nothing, allowing non-verbose anonymous extensions.
     * @param abstractPlot plot.
     */
    default void scheduleUpdateNow(final AbstractPlot<?> abstractPlot)
    {
        //
    }

}
