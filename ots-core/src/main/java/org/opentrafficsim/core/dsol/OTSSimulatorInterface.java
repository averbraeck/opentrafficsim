package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface OTSSimulatorInterface extends DEVSSimulatorInterface<Duration>, Contextualized
{
    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param model OTSModelInterface; the simulation to execute
     * @param replication OTSReplication; the replication with the run control parameters
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     */
    void initialize(OTSModelInterface model, OTSReplication replication) throws SimRuntimeException;

    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    void initialize(Time startTime, Duration warmupPeriod, Duration runLength, OTSModelInterface model)
            throws SimRuntimeException, NamingException;

    /**
     * Initialize a simulation engine without animation and prescribed replication number; the easy way. PauseOnError is set to
     * true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @param replicationNr int; the replication number
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when context for the animation cannot be created
     */
    void initialize(Time startTime, Duration warmupPeriod, Duration runLength, OTSModelInterface model, int replicationNr)
            throws SimRuntimeException, NamingException;

    /**
     * Construct and schedule a SimEvent using a Time to specify the execution time.
     * @param executionTime Time; the time at which the event must happen
     * @param priority short; should be between <cite>SimEventInterface.MAX_PRIORITY</cite> and
     *            <cite>SimEventInterface.MIN_PRIORITY</cite>; most normal events should use
     *            <cite>SimEventInterface.NORMAL_PRIORITY</cite>
     * @param source Object; the object that creates/schedules the event
     * @param target Object; the object that must execute the event
     * @param method String; the name of the method of <code>target</code> that must execute the event
     * @param args Object[]; the arguments of the <code>method</code> that must execute the event
     * @return SimEvent&lt;Duration&gt;; the event that was scheduled (the caller should save this if a need to cancel the event
     *         may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    default SimEvent<Duration> scheduleEventAbsTime(final Time executionTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<Duration> simEvent =
                new SimEvent<>(executionTime.minus(getStartTimeAbs()), priority, source, target, method, args);
        scheduleEvent(simEvent);
        return simEvent;
    }

    /**
     * Construct and schedule a SimEvent using a Time to specify the execution time.
     * @param executionTime Time; the time at which the event must happen
     * @param source Object; the object that creates/schedules the event
     * @param target Object; the object that must execute the event
     * @param method String; the name of the method of <code>target</code> that must execute the event
     * @param args Object[]; the arguments of the <code>method</code> that must execute the event
     * @return SimEvent&lt;Duration&gt;; the event that was scheduled (the caller should save this if a need to cancel the event
     *         may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    default SimEvent<Duration> scheduleEventAbsTime(final Time executionTime, final Object source, final Object target,
            final String method, final Object[] args) throws SimRuntimeException
    {
        return scheduleEventAbsTime(executionTime, SimEventInterface.NORMAL_PRIORITY, source, target, method, args);
    }

    /**
     * Return the absolute simulator time rather than the relative one since the start of the simulation.
     * @return Time; the absolute simulator time rather than the relative one since the start of the simulation
     */
    default Time getSimulatorAbsTime()
    {
        if (getSimulatorTime() == null || Double.isNaN(getSimulatorTime().si))
        {
            return getReplication() == null ? Time.ZERO : getStartTimeAbs();
        }
        return getStartTimeAbs().plus(getSimulatorTime());
    }

    /**
     * Return the absolute start time of the replication.
     * @return Time; the absolute start time of the replication
     */
    default Time getStartTimeAbs()
    {
        return getReplication().getStartTimeAbs();
    }

    /**
     * Runs the simulator up to a certain time; any events at that time, or the solving of the differential equation at that
     * timestep, will not yet be executed.
     * @param stopTime Time; the absolute time till when we want to run the simulation, coded as a SimTime object
     * @throws SimRuntimeException whenever starting fails. Possible occasions include starting a started simulator
     */
    default void runUpTo(final Time stopTime) throws SimRuntimeException
    {
        runUpTo(stopTime.minus(getStartTimeAbs()));
    }

    /**
     * Runs the simulator up to a certain time; all events at that time, or the solving of the differential equation at that
     * timestep, will be executed.
     * @param stopTime Time; the absolute time till when we want to run the simulation, coded as a SimTime object
     * @throws SimRuntimeException whenever starting fails. Possible occasions include starting a started simulator
     */
    default void runUpToAndIncluding(final Time stopTime) throws SimRuntimeException
    {
        runUpToAndIncluding(stopTime.minus(getStartTimeAbs()));
    }

    /** {@inheritDoc} */
    @Override
    OTSReplication getReplication();

    /** {@inheritDoc} */
    @Override
    default ContextInterface getContext()
    {
        return getReplication().getContext();
    }

}
