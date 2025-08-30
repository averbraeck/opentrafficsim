package org.opentrafficsim.core.dsol;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.perception.HistoryManager;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DevsSimulatorInterface;
import nl.tudelft.simulation.naming.context.ContextInterface;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 11 mei 2015 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface OtsSimulatorInterface extends DevsSimulatorInterface<Duration>, Contextualized
{
    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param model the simulation to execute
     * @param replication the replication with the run control parameters
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     */
    void initialize(OtsModelInterface model, OtsReplication replication) throws SimRuntimeException;

    /**
     * Initialize a simulation engine without animation; the easy way. PauseOnError is set to true;
     * @param startTime the start time of the simulation
     * @param warmupPeriod the warm up period of the simulation (use Duration.ZERO if you don't know what this is)
     * @param runLength the duration of the simulation
     * @param model the simulation to execute
     * @param historyManager history manager
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     */
    void initialize(Time startTime, Duration warmupPeriod, Duration runLength, OtsModelInterface model,
            HistoryManager historyManager) throws SimRuntimeException, NamingException;

    /**
     * Initialize a simulation engine without animation and prescribed replication number; the easy way. PauseOnError is set to
     * true;
     * @param startTime the start time of the simulation
     * @param warmupPeriod the warm up period of the simulation (use Duration.ZERO if you don't know what this is)
     * @param runLength the duration of the simulation
     * @param model the simulation to execute
     * @param historyManager history manager
     * @param replicationNr the replication number
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when context for the animation cannot be created
     */
    void initialize(Time startTime, Duration warmupPeriod, Duration runLength, OtsModelInterface model,
            HistoryManager historyManager, int replicationNr) throws SimRuntimeException, NamingException;

    /**
     * Return the absolute simulator time rather than the relative one since the start of the simulation.
     * @return the absolute simulator time rather than the relative one since the start of the simulation
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
     * @return the absolute start time of the replication
     */
    default Time getStartTimeAbs()
    {
        return getReplication().getStartTimeAbs();
    }

    /**
     * Runs the simulator up to a certain time; any events at that time, or the solving of the differential equation at that
     * timestep, will not yet be executed.
     * @param stopTime the absolute time till when we want to run the simulation, coded as a SimTime object
     * @throws SimRuntimeException whenever starting fails. Possible occasions include starting a started simulator
     */
    default void runUpTo(final Time stopTime) throws SimRuntimeException
    {
        runUpTo(stopTime.minus(getStartTimeAbs()));
    }

    /**
     * Runs the simulator up to a certain time; all events at that time, or the solving of the differential equation at that
     * timestep, will be executed.
     * @param stopTime the absolute time till when we want to run the simulation, coded as a SimTime object
     * @throws SimRuntimeException whenever starting fails. Possible occasions include starting a started simulator
     */
    default void runUpToAndIncluding(final Time stopTime) throws SimRuntimeException
    {
        runUpToAndIncluding(stopTime.minus(getStartTimeAbs()));
    }

    @Override
    OtsReplication getReplication();

    @Override
    default ContextInterface getContext()
    {
        return getReplication().getContext();
    }

}
