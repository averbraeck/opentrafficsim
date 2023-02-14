package org.opentrafficsim.core.dsol;

import java.io.Serializable;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.ErrorStrategy;

/**
 * Construct a DSOL DEVSSimulator the easy way.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OtsSimulator extends DEVSSimulator<Duration> implements OtsSimulatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150510L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OtsSimulator.
     * @param simulatorId the id of the simulator to use in remote communication
     */
    public OtsSimulator(final Serializable simulatorId)
    {
        super(simulatorId);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model) throws SimRuntimeException, NamingException
    {
        initialize(startTime, warmupPeriod, runLength, model, ++this.lastReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OtsModelInterface model, final int replicationNr) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        OtsReplication newReplication = new OtsReplication("rep" + replicationNr, startTime, warmupPeriod, runLength);
        super.initialize(model, newReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final OtsModelInterface model, final OtsReplication replication) throws SimRuntimeException
    {
        super.initialize(model, replication);
    }

    /** {@inheritDoc} */
    @Override
    public OtsReplication getReplication()
    {
        return (OtsReplication) super.getReplication();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OtsSimulator [lastReplication=" + this.lastReplication + "]";
    }

}
