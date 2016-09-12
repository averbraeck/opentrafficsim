package org.opentrafficsim.core.dsol;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Typed extension of the DEVSSimulator without remote exceptions.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSSimulator extends DEVSSimulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        implements OTSDEVSSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** {@inheritDoc} */
    @Override
    public final void initialize(
            final Replication<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> initReplication,
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
    public final void scheduleEventRel(final Duration relativeDelay, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventRel(relativeDelay, priority, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventRel(final Duration relativeDelay, final Object source, final Object target,
            final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventRel(relativeDelay, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Time absoluteTime, final Object source, final Object target,
            final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventAbs(absoluteTime, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Time absoluteTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventAbs(absoluteTime, priority, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void runUpTo(final Time when) throws SimRuntimeException
    {
        super.runUpTo(when);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void start(final boolean fireStartEvent) throws SimRuntimeException
    {
        if (this.isRunning())
        {
            throw new SimRuntimeException("Cannot start a running simulator");
        }
        if (this.replication == null)
        {
            throw new SimRuntimeException("Cannot start a simulator" + " without replication details");
        }
        if (this.simulatorTime.ge(this.replication.getTreatment().getEndTime()))
        {
            throw new SimRuntimeException("Cannot start simulator : " + "simulatorTime = runLength");
        }
        synchronized (this.semaphore)
        {
            this.running = true;
            if (fireStartEvent)
            {
                this.fireEvent(START_EVENT);
            }
            this.fireTimedEvent(SimulatorInterface.TIME_CHANGED_EVENT, this.simulatorTime, this.simulatorTime);
            this.worker.interrupt();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSDEVSSimulator [time=" + getSimulatorTime().getTime() + "]";
    }

}
