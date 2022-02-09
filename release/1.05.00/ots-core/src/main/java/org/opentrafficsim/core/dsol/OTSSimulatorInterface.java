package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface OTSSimulatorInterface extends DEVSSimulatorInterface.TimeDoubleUnit
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
     * @return SimEvent&lt;SimTimeDoubleUnit&gt;; the event that was scheduled (the caller should save this if a need to cancel
     *         the event may arise later)
     * @throws SimRuntimeException when the <code>executionTime</code> is in the past
     */
    SimEvent<SimTimeDoubleUnit> scheduleEvent(Time executionTime, short priority, Object source, Object target, String method,
            Object[] args) throws SimRuntimeException;

    /** {@inheritDoc} */
    @Override
    OTSReplication getReplication();

}
