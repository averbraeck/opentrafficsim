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
 * $LastChangedDate: 2018-10-30 14:03:57 +0100 (Tue, 30 Oct 2018) $, @version $Revision: 4727 $, by $Author: pknoppers $,
 * initial version 12 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OTSSimulator extends DEVSSimulator<Duration> implements OTSSimulatorInterface, Serializable
{
    /** */
    private static final long serialVersionUID = 20150510L;

    /** Counter for replication. */
    private int lastReplication = 0;

    /**
     * Construct an OTSSimulator.
     * @param simulatorId the id of the simulator to use in remote communication
     */
    public OTSSimulator(final Serializable simulatorId)
    {
        super(simulatorId);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model) throws SimRuntimeException, NamingException
    {
        initialize(startTime, warmupPeriod, runLength, model, ++this.lastReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Time startTime, final Duration warmupPeriod, final Duration runLength,
            final OTSModelInterface model, final int replicationNr) throws SimRuntimeException, NamingException
    {
        setErrorStrategy(ErrorStrategy.WARN_AND_PAUSE);
        OTSReplication newReplication = new OTSReplication("rep" + replicationNr, startTime, warmupPeriod, runLength);
        super.initialize(model, newReplication);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final OTSModelInterface model, final OTSReplication replication) throws SimRuntimeException
    {
        super.initialize(model, replication);
    }

    /** {@inheritDoc} */
    @Override
    public OTSReplication getReplication()
    {
        return (OTSReplication) super.getReplication();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OTSSimulator [lastReplication=" + this.lastReplication + "]";
    }

}
