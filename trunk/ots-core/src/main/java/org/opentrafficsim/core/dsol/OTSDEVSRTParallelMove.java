package org.opentrafficsim.core.dsol;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 2386 $, $LastChangedDate: 2016-10-16 14:55:54 +0200 (Sun, 16 Oct 2016) $, by $Author: averbraeck $,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSRTParallelMove extends DEVSRealTimeClock<Time, Duration, OTSSimTimeDouble>
        implements OTSDEVSSimulatorInterface, OTSAnimatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /** number of threads to use for move(). */
    private int moveThreads = 1;

    /** the thread pool for parallel execution. */
    private ExecutorService executor = null;

    /**
     * Create a new OTSRealTimeClock.
     * @param moveThreads The number of move threads to use
     */
    public OTSDEVSRTParallelMove(final int moveThreads)
    {
        super();
        setMoveThreads(moveThreads);
        setEventList(new SynchronizedRedBlackTree<>());
    }

    /**
     * Create a new OTSRealTimeClock.
     */
    public OTSDEVSRTParallelMove()
    {
        this(1);
    }

    /**
     * @param moveThreads set moveThreads
     */
    public final void setMoveThreads(final int moveThreads)
    {
        this.moveThreads = moveThreads;
    }

    /**
     * @return moveThreads
     */
    public final int getMoveThreads()
    {
        return this.moveThreads;
    }

    /** {@inheritDoc} */
    @Override
    public final void initialize(final Replication<Time, Duration, OTSSimTimeDouble> initReplication,
            final ReplicationMode replicationMode) throws SimRuntimeException
    {
        try
        {
            super.initialize(initReplication, replicationMode);
        }
        catch (RemoteException exception)
        {
            throw new SimRuntimeException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void runUpTo(final Time when) throws SimRuntimeException
    {
        super.runUpTo(when);
    }

    /** {@inheritDoc} */
    @Override
    protected final Duration relativeMillis(final double factor)
    {
        return new Duration(factor, DurationUnit.MILLISECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSDEVSRealTimeClock [time=" + getSimulatorTime().getTime() + "]";
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void run()
    {
        AnimationThread animationThread = new AnimationThread(this);
        animationThread.start();

        long clockTime0 = System.currentTimeMillis(); // _________ current zero for the wall clock
        OTSSimTimeDouble simTime0 = this.simulatorTime; // _______ current zero for the sim clock
        double factor = getSpeedFactor(); // _____________________ local copy of speed factor to detect change
        double msec1 = relativeMillis(1.0).doubleValue(); // _____ translation factor for 1 msec for sim clock
        Duration rSim = this.relativeMillis(getUpdateMsec() * factor); // sim clock change for 'updateMsec' wall clock

        while (this.isRunning() && !this.eventList.isEmpty()
                && this.simulatorTime.le(this.replication.getTreatment().getEndTime()))
        {
            // check if speedFactor has changed. If yes: re-baseline.
            if (factor != getSpeedFactor())
            {
                clockTime0 = System.currentTimeMillis();
                simTime0 = this.simulatorTime;
                factor = getSpeedFactor();
                rSim = this.relativeMillis(getUpdateMsec() * factor);
            }

            // check if we are behind; syncTime is the needed current time on the wall-clock
            double syncTime = (System.currentTimeMillis() - clockTime0) * msec1 * factor;
            // delta is the time we might be behind
            double simTime = this.simulatorTime.minus(simTime0).doubleValue();

            if (syncTime > simTime)
            {
                // we are behind
                if (!isCatchup())
                {
                    // if no catch-up: re-baseline.
                    clockTime0 = System.currentTimeMillis();
                    simTime0 = this.simulatorTime;
                }
                else
                {
                    // jump to the required wall-clock related time or to the time of the next event, whichever comes
                    // first
                    synchronized (super.semaphore)
                    {
                        Duration delta = relativeMillis((syncTime - simTime) / msec1);
                        OTSSimTimeDouble absSyncTime = this.simulatorTime.plus(delta);
                        OTSSimTimeDouble eventTime = this.eventList.first().getAbsoluteExecutionTime();
                        if (absSyncTime.lt(eventTime))
                        {
                            this.simulatorTime = absSyncTime;
                        }
                        else
                        {
                            this.simulatorTime = eventTime;
                        }
                    }
                }
            }

            // peek at the first event and determine the time difference relative to RT speed; that determines
            // how long we have to wait.
            SimEventInterface<OTSSimTimeDouble> event = this.eventList.first();
            double simTimeDiffMillis = (event.getAbsoluteExecutionTime().minus(simTime0)).doubleValue() / (msec1 * factor);

            /*
             * simTimeDiff gives the number of milliseconds between the last event and this event. if speed == 1, this is the
             * number of milliseconds we have to wait. if speed == 10, we have to wait 1/10 of that. If the speed == 0.1, we
             * have to wait 10 times that amount. We might also be behind.
             */
            if (simTimeDiffMillis >= (System.currentTimeMillis() - clockTime0))
            {
                while (simTimeDiffMillis > System.currentTimeMillis() - clockTime0)
                {
                    try
                    {
                        Thread.sleep(getUpdateMsec());

                        // check if speedFactor has changed. If yes: break out of this loop and execute event.
                        // this could cause a jump.
                        if (factor != getSpeedFactor())
                        {
                            simTimeDiffMillis = 0.0;
                        }

                    }
                    catch (InterruptedException ie)
                    {
                        // do nothing
                        ie = null;
                    }

                    // check if an event has been inserted. In a real-time situation this can be dome by other threads
                    if (!event.equals(this.eventList.first())) // event inserted by a thread...
                    {
                        event = this.eventList.first();
                        simTimeDiffMillis = (event.getAbsoluteExecutionTime().minus(simTime0)).doubleValue() / (msec1 * factor);
                    }
                    else
                    {
                        // make a small time step for the animation during wallclock waiting.
                        // but never beyond the next event time.
                        if (this.simulatorTime.plus(rSim).lt(event.getAbsoluteExecutionTime()))
                        {
                            synchronized (super.semaphore)
                            {
                                this.simulatorTime.add(rSim);
                            }
                        }
                    }
                }
            }

            this.simulatorTime = event.getAbsoluteExecutionTime();
            this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime.get());

            if (this.moveThreads <= 1)
            {
                synchronized (super.semaphore)
                {
                    // carry out all events scheduled on this simulation time, as long as we are still running.
                    while (this.isRunning() && !this.eventList.isEmpty()
                            && event.getAbsoluteExecutionTime().eq(this.simulatorTime))
                    {
                        event = this.eventList.removeFirst();
                        try
                        {
                            event.execute();
                        }
                        catch (Exception exception)
                        {
                            exception.printStackTrace();
                            if (this.isPauseOnError())
                            {
                                this.stop();
                            }
                        }
                        if (!this.eventList.isEmpty())
                        {
                            // peek at next event for while loop.
                            event = this.eventList.first();
                        }
                    }
                }
            }

            else

            {
                // parallel execution of the move method
                // first carry out all the non-move events and make a list of move events to be carried out in parallel
                List<SimEventInterface<OTSSimTimeDouble>> moveEvents = new ArrayList<>();
                synchronized (super.semaphore)
                {
                    while (this.isRunning() && !this.eventList.isEmpty()
                            && event.getAbsoluteExecutionTime().eq(this.simulatorTime))
                    {
                        event = this.eventList.removeFirst();
                        SimEvent<OTSSimTimeDouble> se = (SimEvent<OTSSimTimeDouble>) event;
                        if (se.getTarget() instanceof GTU && se.getMethod().equals("move"))
                        {
                            moveEvents.add(event);
                        }
                        else
                        {
                            try
                            {
                                event.execute();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                if (this.isPauseOnError())
                                {
                                    this.stop();
                                }
                            }
                        }
                        if (!this.eventList.isEmpty())
                        {
                            // peek at next event for while loop.
                            event = this.eventList.first();
                        }
                    }
                }

                // then carry out the move events, based on a constant state at that time.
                // first make sure that new events will be stored in a temporary event list...
                this.executor = Executors.newFixedThreadPool(1);
                for (int i = 0; i < moveEvents.size(); i++)
                {
                    SimEvent<OTSSimTimeDouble> se = (SimEvent<OTSSimTimeDouble>) moveEvents.get(i);
                    final SimEventInterface<OTSSimTimeDouble> moveEvent =
                            new SimEvent<>(this.simulatorTime, se.getSource(), se.getTarget(), "movePrep", se.getArgs());
                    this.executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                moveEvent.execute();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                if (OTSDEVSRTParallelMove.this.isPauseOnError())
                                {
                                    OTSDEVSRTParallelMove.this.stop();
                                }
                            }
                        }
                    });
                }
                this.executor.shutdown();
                try
                {
                    this.executor.awaitTermination(1L, java.util.concurrent.TimeUnit.HOURS);
                }
                catch (InterruptedException exception)
                {
                    //
                }

                this.executor = Executors.newFixedThreadPool(1);
                for (int i = 0; i < moveEvents.size(); i++)
                {
                    SimEvent<OTSSimTimeDouble> se = (SimEvent<OTSSimTimeDouble>) moveEvents.get(i);
                    final SimEventInterface<OTSSimTimeDouble> moveEvent =
                            new SimEvent<>(this.simulatorTime, se.getSource(), se.getTarget(), "moveGenerate", se.getArgs());
                    this.executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                moveEvent.execute();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                if (OTSDEVSRTParallelMove.this.isPauseOnError())
                                {
                                    OTSDEVSRTParallelMove.this.stop();
                                }
                            }
                        }
                    });
                }
                this.executor.shutdown();
                try
                {
                    this.executor.awaitTermination(1L, java.util.concurrent.TimeUnit.HOURS);
                }
                catch (InterruptedException exception)
                {
                    //
                }

                this.executor = Executors.newFixedThreadPool(1);
                for (int i = 0; i < moveEvents.size(); i++)
                {
                    SimEvent<OTSSimTimeDouble> se = (SimEvent<OTSSimTimeDouble>) moveEvents.get(i);
                    final SimEventInterface<OTSSimTimeDouble> moveEvent =
                            new SimEvent<>(this.simulatorTime, se.getSource(), se.getTarget(), "moveFinish", se.getArgs());
                    this.executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                moveEvent.execute();
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                if (OTSDEVSRTParallelMove.this.isPauseOnError())
                                {
                                    OTSDEVSRTParallelMove.this.stop();
                                }
                            }
                        }
                    });
                }
                this.executor.shutdown();
                try
                {
                    this.executor.awaitTermination(1L, java.util.concurrent.TimeUnit.HOURS);
                }
                catch (InterruptedException exception)
                {
                    //
                }

            }
        }
        this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime.get());

        updateAnimation();
        animationThread.stopAnimation();
    }

}
