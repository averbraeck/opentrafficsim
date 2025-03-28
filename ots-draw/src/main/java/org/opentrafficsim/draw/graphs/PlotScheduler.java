package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Interface between plots (subclasses of {@code AbstractPlot}) and some source that knows about time in the context, e.g. a
 * simulator, or a data loader which knows all time has past. For offline purposes, {@code PlotScheduler.OFFLINE} can be used.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PlotScheduler
{

    /**
     * Default offline scheduler. This will only provide an infinite time, causing {@code increaseTime()} to be invoked on all
     * plots once.
     */
    PlotScheduler OFFLINE = new PlotScheduler()
    {
        @Override
        public Time getTime()
        {
            return Time.instantiateSI(Double.MAX_VALUE);
        }

        @Override
        public void cancelEvent(final AbstractPlot abstractPlot)
        {
            // no action required
        }

        @Override
        public void scheduleUpdate(final Time time, final AbstractPlot abstractPlot)
        {
            // no action required
        }
    };

    /**
     * Returns the time.
     * @return time.
     */
    Time getTime();

    /**
     * Cancel event on plot.
     * @param abstractPlot plot.
     */
    void cancelEvent(AbstractPlot abstractPlot);

    /**
     * Schedule {@code update()} call on abstractPlot
     * @param time time.
     * @param abstractPlot plot.
     */
    void scheduleUpdate(Time time, AbstractPlot abstractPlot);

}
