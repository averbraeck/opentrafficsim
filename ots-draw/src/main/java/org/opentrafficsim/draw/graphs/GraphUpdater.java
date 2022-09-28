package org.opentrafficsim.draw.graphs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.djutils.logger.CategoryLogger;

/**
 * The GraphUpdater can be used to repeatedly offer a value that is automatically processed in order of offering in a parallel
 * Thread.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of value in queue
 */
public class GraphUpdater<T>
{

    /** Queue of update times for executing Thread. */
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

    /**
     * Constructs and starts a thread that performs each given task from a queue.
     * @param workerName String; name for the working thread
     * @param invokingThread Thread; invoking thread, the worker will stop when this thread is interrupted
     * @param updater Updater&lt;T&gt;; updater to perform with the queued value
     */
    public GraphUpdater(final String workerName, final Thread invokingThread, final Updater<T> updater)
    {
        new Thread(new Runnable()
        {
            /** {@inheritDoc} */
            @SuppressWarnings({"synthetic-access"})
            @Override
            public void run()
            {
                while (!invokingThread.isInterrupted())
                {
                    try
                    {
                        T t = GraphUpdater.this.queue.poll(5, TimeUnit.SECONDS);
                        if (t != null)
                        {
                            updater.update(t);
                        }
                    }
                    catch (InterruptedException exception)
                    {
                        CategoryLogger.always().error(exception, "Worker {} thread stopped.", workerName);
                        break;
                    }
                }
            }
        }, workerName).start();
    }

    /**
     * Offer a next value to the queue.
     * @param t T; next value to offer to the queue
     */
    public final void offer(final T t)
    {
        this.queue.offer(t);
    }

    /**
     * Functional interface for updates to perform.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <T> type of value in queue
     */
    @FunctionalInterface
    interface Updater<T>
    {
        /**
         * Perform an update.
         * @param t T; value to update by
         */
        void update(T t);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GraphUpdater [queue=" + this.queue + "]";
    }

}
