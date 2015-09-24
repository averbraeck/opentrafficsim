package org.opentrafficsim.simulationengine;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.dsol.OTSDEVSRealTimeClock;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleAnimator extends OTSDEVSRealTimeClock implements SimpleSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Create a simulation engine with animation; the easy way. PauseOnError is set to true;
     * @param startTime DoubleScalar.Abs&lt;TimeUnit&gt;; the start time of the simulation
     * @param warmupPeriod DoubleScalar.Rel&lt;TimeUnit&gt;; the warm up period of the simulation (use new
     *            DoubleScalar.Rel&lt;TimeUnit&gt;(0, SECOND) if you don't know what this is)
     * @param runLength DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     */
    public SimpleAnimator(final Time.Abs startTime, final Time.Rel warmupPeriod, final Time.Rel runLength,
        final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        initialize(new OTSReplication("rep" + ++this.lastReplication, new OTSSimTimeDouble(startTime), warmupPeriod,
            runLength, model), ReplicationMode.TERMINATING);
    }

    /**
     * {@inheritDoc}
     */
    public final SimEvent<OTSSimTimeDouble> scheduleEvent(final Time.Abs executionTime, final short priority,
        final Object source, final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<OTSSimTimeDouble> result =
            new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(new Time.Abs(executionTime.getSI(), SECOND)), priority,
                source, target, method, args);
        scheduleEvent(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void run()
    {
        setAnimationDelay(20); // ________________________________ 50 Hz animation update
        AnimationThread animationThread = new AnimationThread(this);
        animationThread.start();

        long clockTime0 = System.currentTimeMillis(); // _________ current zero for the wall clock
        OTSSimTimeDouble simTime0 = this.simulatorTime; // _______ current zero for the sim clock
        double factor = getSpeedFactor(); // _____________________ local copy of speed factor to detect change
        double msec1 = relativeMillis(1.0).doubleValue(); // _____ translation factor for 1 msec for sim clock
        Time.Rel r1 = this.relativeMillis(factor); // sim clock change for 1 msec wall clock

        while (this.isRunning() && !this.eventList.isEmpty()
            && this.simulatorTime.le(this.replication.getTreatment().getEndTime()))
        {
            // check if speedFactor has changed. If yes: re-baseline.
            if (factor != getSpeedFactor())
            {
                clockTime0 = System.currentTimeMillis();
                simTime0 = this.simulatorTime;
                factor = getSpeedFactor();
                r1 = this.relativeMillis(factor);
            }

            // peek at the first event and determine the time difference relative to RT speed.
            SimEventInterface<OTSSimTimeDouble> event = this.eventList.first();
            double simTimeDiffMillis = (event.getAbsoluteExecutionTime().minus(simTime0)).doubleValue() / (msec1 * factor);

            /*
             * simTimeDiff gives the number of milliseconds between the last event and this event. if speed == 1, this is the
             * number of milliseconds we have to wait. if speed == 10, we have to wait 1/10 of that. If the speed == 0.1, we
             * have to wait 10 times that amount. We might also be behind.
             */
            if (simTimeDiffMillis < (System.currentTimeMillis() - clockTime0))
            {
                // we are behind.
                if (!isCatchup())
                {
                    // if no catch-up: re-baseline.
                    clockTime0 = System.currentTimeMillis();
                    simTime0 = this.simulatorTime;
                }
                else
                {
                    // if catch-up: indicate we were behind.
                    this.fireTimedEvent(BACKLOG_EVENT, this.simulatorTime, null);
                }
            }
            else
            {
                while (simTimeDiffMillis > System.currentTimeMillis() - clockTime0)
                {
                    try
                    {
                        Thread.sleep(1);

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

                    // make a small time step for the animation during wallclock waiting.
                    // but never beyond the next event time.
                    if (this.simulatorTime.plus(r1).lt(event.getAbsoluteExecutionTime()))
                    {
                        this.simulatorTime.add(r1);
                    }
                }
            }

            synchronized (super.semaphore)
            {
                this.simulatorTime = event.getAbsoluteExecutionTime();
                this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);

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
                        System.err.println(event.toString());
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
        this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);
        updateAnimation();
        animationThread.stopAnimation();
    }

    /** */
    @SuppressWarnings("checkstyle:designforextension")
    public void runOld()
    {
        AnimationThread animationThread = new AnimationThread(this);
        animationThread.start();

        long clockTime0 = System.currentTimeMillis(); // _________ current zero for the wall clock
        OTSSimTimeDouble simTime0 = this.simulatorTime; // _______ current zero for the sim clock
        double factor = getSpeedFactor(); // _____________________ local copy of speed factor to detect change
        double msec1 = relativeMillis(1.0).doubleValue(); // _____ translation factor for 1 msec for sim clock
        Time.Rel r10 = this.relativeMillis(10.0 * factor); // sim clock change for 10 msec wall clock

        while (this.isRunning() && !this.eventList.isEmpty()
            && this.simulatorTime.le(this.replication.getTreatment().getEndTime()))
        {
            // check if speedFactor has changed. If yes: re-baseline.
            if (factor != getSpeedFactor())
            {
                clockTime0 = System.currentTimeMillis();
                simTime0 = this.simulatorTime;
                factor = getSpeedFactor();
                r10 = this.relativeMillis(10.0 * factor);
            }

            // peek at the first event and determine the time difference relative to RT speed.
            SimEventInterface<OTSSimTimeDouble> event = this.eventList.first();
            double simTimeDiffMillis = (event.getAbsoluteExecutionTime().minus(simTime0)).doubleValue() / (msec1 * factor);

            /*
             * simTimeDiff gives the number of milliseconds between the last event and this event. if speed == 1, this is the
             * number of milliseconds we have to wait. if speed == 10, we have to wait 1/10 of that. If the speed == 0.1, we
             * have to wait 10 times that amount. We might also be behind.
             */
            if (simTimeDiffMillis < (System.currentTimeMillis() - clockTime0))
            {
                // we are behind.
                if (!isCatchup())
                {
                    // if no catch-up: re-baseline.
                    clockTime0 = System.currentTimeMillis();
                    simTime0 = this.simulatorTime;
                }
                else
                {
                    // if catch-up: indicate we were behind.
                    this.fireTimedEvent(BACKLOG_EVENT, this.simulatorTime, null);
                }
            }
            else
            {
                while (simTimeDiffMillis > System.currentTimeMillis() - clockTime0)
                {
                    try
                    {
                        Thread.sleep(10);

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

                    // make a small time step for the animation during wallclock waiting.
                    // but never beyond the next event time.
                    if (this.simulatorTime.plus(r10).lt(event.getAbsoluteExecutionTime()))
                    {
                        this.simulatorTime.add(r10);
                        this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);
                        updateAnimation();
                    }
                }
            }

            synchronized (super.semaphore)
            {
                this.simulatorTime = event.getAbsoluteExecutionTime();
                this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);

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
                        System.err.println(event.toString());
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
        this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);
        updateAnimation();
        animationThread.stopAnimation();
    }
}
