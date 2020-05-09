package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Construct a DSOL DEVSRealTimeClock the easy way.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2018-10-30 14:03:57 +0100 (Tue, 30 Oct 2018) $, @version $Revision: 4727 $, by $Author: pknoppers $,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSLoggingAnimator extends OTSAnimator
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSAnimator.
     * @param path path for logging
     * @param simulatorId the id of the simulator to use in remote communication
     */
    public OTSLoggingAnimator(final String path, final Serializable simulatorId)
    {
        super(simulatorId);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication =
                OTSReplication.create("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @param streams Map&lt;String, StreamInterface&gt;; streams
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final Map<String, StreamInterface> streams)
            throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication =
                OTSReplication.create("rep" + ++this.lastReplication, startTime, warmupPeriod, runLength, model);
        newReplication.getStreams().putAll(streams);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replicationnr) throws SimRuntimeException, NamingException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        OTSReplication newReplication = OTSReplication.create("rep" + replicationnr, startTime, warmupPeriod, runLength, model);
        super.initialize(newReplication, ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OTSLoggingAnimator [lastReplication=" + this.lastReplication + "]";
    }

    /** the current animation thread; null if none. */
    private AnimationThread animationThread = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:methodlength"})
    public void run()
    {
        synchronized (this)
        {
            if (this.isAnimation())
            {
                this.animationThread = new AnimationThread(this);
                this.animationThread.start();
            }
        }

        /* Baseline point for the wallclock time. */
        long wallTime0 = System.currentTimeMillis();

        /* Baseline point for the simulator time. */
        SimTimeDoubleUnit simTime0 = this.simulatorTime.copy();

        /* Speed factor is simulation seconds per 1 wallclock second. */
        double currentSpeedFactor = this.getSpeedFactor();

        /* wall clock milliseconds per 1 simulation clock millisecond. */
        double msec1 = simulatorTimeForWallClockMillis(1.0).doubleValue();

        while (this.isStartingOrRunning() && !this.eventList.isEmpty()
                && this.simulatorTime.le(this.replication.getTreatment().getEndSimTime()))
        {
            // check if speedFactor has changed. If yes: re-baseline.
            if (currentSpeedFactor != this.getSpeedFactor())
            {
                wallTime0 = System.currentTimeMillis();
                simTime0.set(this.simulatorTime.get());
                currentSpeedFactor = this.getSpeedFactor();
            }

            // check if we are behind; wantedSimTime is the needed current time on the wall-clock
            double wantedSimTime = (System.currentTimeMillis() - wallTime0) * msec1 * currentSpeedFactor;
            double simTimeSinceBaseline = this.simulatorTime.diff(simTime0).doubleValue();

            if (simTimeSinceBaseline < wantedSimTime)
            {
                // we are behind
                if (!this.isCatchup())
                {
                    // if no catch-up: re-baseline.
                    wallTime0 = System.currentTimeMillis();
                    simTime0.set(this.simulatorTime.get());
                }
                else
                {
                    // jump to the required wall-clock related time or to the time of the next event, whichever comes
                    // first
                    synchronized (super.semaphore)
                    {
                        Duration delta = simulatorTimeForWallClockMillis((wantedSimTime - simTimeSinceBaseline) / msec1);
                        SimTimeDoubleUnit absSyncTime = this.simulatorTime.plus(delta);
                        SimTimeDoubleUnit eventTime = this.eventList.first().getAbsoluteExecutionTime();
                        if (absSyncTime.lt(eventTime))
                        {
                            this.simulatorTime.set(absSyncTime.get());
                        }
                        else
                        {
                            this.simulatorTime.set(eventTime.get());
                        }
                    }
                }
            }

            // peek at the first event and determine the time difference relative to RT speed; that determines
            // how long we have to wait.
            SimEventInterface<SimTimeDoubleUnit> nextEvent = this.eventList.first();
            double wallMillisNextEventSinceBaseline =
                    (nextEvent.getAbsoluteExecutionTime().diff(simTime0)).doubleValue() / (msec1 * currentSpeedFactor);

            // wallMillisNextEventSinceBaseline gives the number of milliseconds on the wall clock since baselining for the
            // expected execution time of the next event on the event list .
            if (wallMillisNextEventSinceBaseline >= (System.currentTimeMillis() - wallTime0))
            {
                while (wallMillisNextEventSinceBaseline > System.currentTimeMillis() - wallTime0)
                {
                    try
                    {
                        Thread.sleep(this.getUpdateMsec());
                    }
                    catch (InterruptedException ie)
                    {
                        // do nothing
                        ie = null;
                        Thread.interrupted(); // clear the flag
                    }

                    // did we stop running between events?
                    if (!isStartingOrRunning())
                    {
                        wallMillisNextEventSinceBaseline = 0.0; // jump out of the while loop for sleeping
                        break;
                    }

                    // check if speedFactor has changed. If yes: rebaseline. Try to avoid a jump.
                    if (currentSpeedFactor != this.getSpeedFactor())
                    {
                        // rebaseline
                        wallTime0 = System.currentTimeMillis();
                        simTime0.set(this.simulatorTime.get());
                        currentSpeedFactor = this.getSpeedFactor();
                        wallMillisNextEventSinceBaseline = (nextEvent.getAbsoluteExecutionTime().diff(simTime0)).doubleValue()
                                / (msec1 * currentSpeedFactor);
                    }

                    // check if an event has been inserted. In a real-time situation this can be done by other threads
                    if (!nextEvent.equals(this.eventList.first())) // event inserted by a thread...
                    {
                        nextEvent = this.eventList.first();
                        wallMillisNextEventSinceBaseline = (nextEvent.getAbsoluteExecutionTime().diff(simTime0)).doubleValue()
                                / (msec1 * currentSpeedFactor);
                    }

                    // make a small time step for the animation during wallclock waiting, but never beyond the next event
                    // time. Changed 2019-04-30: this is now recalculated based on latest system time after the 'sleep'.
                    synchronized (super.semaphore)
                    {
                        Time nextEventSimTime = nextEvent.getAbsoluteExecutionTime().get();
                        Duration deltaToWall0inSimTime =
                                simulatorTimeForWallClockMillis((System.currentTimeMillis() - wallTime0) * currentSpeedFactor);
                        Time currentWallSimTime = simTime0.plus(deltaToWall0inSimTime).get();
                        if (nextEventSimTime.compareTo(currentWallSimTime) < 0)
                        {
                            if (nextEventSimTime.compareTo(this.simulatorTime.get()) > 0) // don't go back in time
                            {
                                this.simulatorTime.set(nextEventSimTime);
                            }
                            wallMillisNextEventSinceBaseline = 0.0; // force breakout of the loop
                        }
                        else
                        {
                            if (currentWallSimTime.compareTo(this.simulatorTime.get()) > 0) // don't go back in time
                            {
                                this.simulatorTime.set(currentWallSimTime);
                            }
                        }
                    }
                }
            }

            // only execute an event if we are still running...
            if (isStartingOrRunning())
            {
                synchronized (super.semaphore)
                {
                    if (nextEvent.getAbsoluteExecutionTime().ne(this.simulatorTime))
                    {
                        fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, nextEvent.getAbsoluteExecutionTime(),
                                nextEvent.getAbsoluteExecutionTime().get());
                    }
                    this.simulatorTime.set(nextEvent.getAbsoluteExecutionTime().get());

                    // carry out all events scheduled on this simulation time, as long as we are still running.
                    while (this.isStartingOrRunning() && !this.eventList.isEmpty()
                            && nextEvent.getAbsoluteExecutionTime().eq(this.simulatorTime))
                    {
                        nextEvent = this.eventList.removeFirst();
                        try
                        {
                            System.out.println(nextEvent.getAbsoluteExecutionTime() + " " + nextEvent.toString());
                            nextEvent.execute();
                        }
                        catch (Exception exception)
                        {
                            getLogger().always().error(exception);
                            if (this.isPauseOnError())
                            {
                                try
                                {
                                    this.stop();
                                }
                                catch (SimRuntimeException stopException)
                                {
                                    getLogger().always().error(stopException);
                                }
                            }
                        }
                        if (!this.eventList.isEmpty())
                        {
                            // peek at next event for while loop.
                            nextEvent = this.eventList.first();
                        }
                    }
                }
            }
        }
        fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime.get());

        synchronized (this)
        {
            if (this.isAnimation() && this.animationThread != null)
            {
                updateAnimation();
                this.animationThread.stopAnimation();
            }
        }
    }

}
