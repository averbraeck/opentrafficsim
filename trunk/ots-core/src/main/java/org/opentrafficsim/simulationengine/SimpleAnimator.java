package org.opentrafficsim.simulationengine;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 11 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimpleAnimator extends DEVSRealTimeClock.TimeDoubleUnit implements OTSSimulatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150511L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Create a simulation engine with animation; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    public SimpleAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException, PropertyException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        initialize(new OTSReplication("rep" + ++this.lastReplication, new SimTimeDoubleUnit(startTime), warmupPeriod, runLength,
                model), ReplicationMode.TERMINATING);
    }

    /**
     * Create a simulation engine with animation and prescribed replication number; the easy way. PauseOnError is set to true;
     * @param startTime Time; the start time of the simulation
     * @param warmupPeriod Duration; the warm up period of the simulation (use new Duration(0, SECOND) if you don't know what
     *            this is)
     * @param runLength Duration; the duration of the simulation
     * @param model OTSModelInterface; the simulation to execute
     * @param replication int; the replication number
     * @throws SimRuntimeException on ???
     * @throws NamingException when context for the animation cannot be created
     * @throws PropertyException when one of the user modified properties has the empty string as key
     */
    public SimpleAnimator(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replication) throws SimRuntimeException, NamingException, PropertyException
    {
        setPauseOnError(true);
        setAnimationDelay(20); // 50 Hz animation update
        initialize(new OTSReplication("rep" + replication, new SimTimeDoubleUnit(startTime), warmupPeriod, runLength, model),
                ReplicationMode.TERMINATING);
    }

    /** {@inheritDoc} */
    @Override
    public final SimEvent<SimTimeDoubleUnit> scheduleEvent(final Time executionTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        SimEvent<SimTimeDoubleUnit> result = new SimEvent<>(
                new SimTimeDoubleUnit(new Time(executionTime.getSI(), TimeUnit.BASE)), priority, source, target, method, args);
        scheduleEvent(result);
        return result;
    }
    
    /** {@inheritDoc} */
    @Override
    public final OTSReplication getReplication()
    {
        return (OTSReplication) super.getReplication();
    }

}
