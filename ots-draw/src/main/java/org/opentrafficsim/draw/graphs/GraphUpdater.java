package org.opentrafficsim.draw.graphs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.djutils.logger.CategoryLogger;

/**
 * The GraphUpdater can be used to repeatedly offer a value that is automatically processed in order of offering in a parallel
 * Thread.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of value in queue
 */
public class GraphUpdater<T>
{

    /** Queue of update times for executing Thread. */
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    /** Default updater. */
    private final Updater<T> updater;

    /**
     * Constructs and starts a thread that performs each given task from a queue.
     * @param workerName name for the working thread
     * @param invokingThread invoking thread, the worker will stop when this thread is interrupted
     * @param updater updater to perform with the queued value
     */
    public GraphUpdater(final String workerName, final Thread invokingThread, final Updater<T> updater)
    {
        this.updater = updater;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!invokingThread.isInterrupted())
                {
                    try
                    {
                        Runnable runnable = GraphUpdater.this.queue.poll(5, TimeUnit.SECONDS);
                        if (runnable != null)
                        {
                            runnable.run();
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
     * Offer a next value to the queue for the default updater.
     * @param t next value to offer to the queue
     */
    public final void offer(final T t)
    {
        this.queue.offer(() -> this.updater.update(t));
    }

    /**
     * Add specific runnable to the queue.
     * @param runnable specific runnable for the queue
     */
    public final void offer(final Runnable runnable)
    {
        this.queue.offer(runnable);
    }

    /**
     * Functional interface for updates to perform.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> type of value in queue
     */
    @FunctionalInterface
    interface Updater<T>
    {
        /**
         * Perform an update.
         * @param t value to update by
         */
        void update(T t);
    }

    @Override
    public String toString()
    {
        return "GraphUpdater [queue=" + this.queue + "]";
    }

}
