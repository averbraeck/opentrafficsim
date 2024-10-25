package org.opentrafficsim.draw.egtf;

/**
 * Listener that allows another thread to monitor, report on, and wait for the filtering result.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class EgtfParallelListener implements EgtfListener
{

    /** Filter results after the EGTF is done. */
    private Filter filter;

    /** Wait lock. */
    private Object lock = new Object();

    /** Progress at the last event. */
    private double progress = 0.0;

    /** Whether the user interrupted the EGTF. */
    private boolean interrupted = false;

    /**
     * Package-private constructor.
     */
    EgtfParallelListener()
    {
        //
    }

    /**
     * Waits until progress has been reached, or timeout was exceeded.
     * @param untilProgress progress to wait until
     * @param timeout time out in milliseconds
     * @return value equal to or above untilProgress, or lower if the timeout was exceeded
     * @throws InterruptedException when the calling thread is interrupted
     */
    public double wait(final double untilProgress, final long timeout) throws InterruptedException
    {
        long t1 = System.currentTimeMillis() + timeout;
        long tOut = timeout;
        double uProgress = Math.min(1.0, untilProgress);
        while (this.progress < uProgress && tOut > 0)
        {
            synchronized (this.lock)
            {
                this.lock.wait(tOut);
            }
            tOut = t1 - System.currentTimeMillis();
        }
        return this.progress;
    }

    /**
     * Set the filter results, this is done by the EGTF.
     * @param filter filter results
     */
    public void setFilter(final Filter filter)
    {
        this.filter = filter;
        this.progress = 1.0;
        synchronized (this.lock)
        {
            this.lock.notifyAll();
        }
    }

    /**
     * Get the filter results after the EGTF is done.
     * @return filter results
     */
    public Filter getFilter()
    {
        if (this.filter == null)
        {
            throw new IllegalStateException("Trying to obtain the filter results before filtering is done.");
        }
        return this.filter;
    }

    @Override
    public void notifyProgress(final EgtfEvent event)
    {
        if (this.interrupted)
        {
            event.interrupt();
            this.progress = 1.0;
        }
        else
        {
            // set the current progress, but do not allow a value of 1.0 until the filter result is actually set
            this.progress = Math.min(event.getProgress(), 1.0 - 1e-9);
        }
        synchronized (this.lock)
        {
            this.lock.notifyAll();
        }
    }

    /**
     * Interrupts the EGTF.
     */
    public void interrupt()
    {
        this.interrupted = true;
    }

    @Override
    public String toString()
    {
        return "EgtfParallelListener [filter=" + this.filter + ", progress=" + this.progress + "]";
    }

}
