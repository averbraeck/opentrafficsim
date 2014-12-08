package org.opentrafficsim.simulationengine;

import java.rmi.RemoteException;

import javax.naming.Context;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * Dummy implementation. The only working method is getSimulatorTime which returns 0s all the time. Every other method
 * throws and Error. <br />
 * This class was created in order to be able to run various tests without having to set up a real simulator.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 14 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FakeSimulator implements OTSDEVSSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 20141117L;

    /** {@inheritDoc} */
    @Override
    public final boolean cancelEvent(final SimEventInterface<OTSSimTimeDouble> event) throws RemoteException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final EventListInterface<OTSSimTimeDouble> getEventList() throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEvent(final SimEventInterface<OTSSimTimeDouble> event) throws RemoteException,
            SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventRel(final Rel<TimeUnit> relativeDelay, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventRel(final Rel<TimeUnit> relativeDelay, final Object source, final Object target,
            final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final OTSSimTimeDouble absoluteTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Abs<TimeUnit> absoluteTime, final Object source, final Object target,
            final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Abs<TimeUnit> absoluteTime, final short priority, final Object source,
            final Object target, final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final OTSSimTimeDouble absoluteTime, final Object source, final Object target,
            final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventNow(final short priority, final Object source, final Object target,
            final String method, final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventNow(final Object source, final Object target, final String method,
            final Object[] args) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void setEventList(final EventListInterface<OTSSimTimeDouble> eventList) throws RemoteException,
            SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final OTSSimTimeDouble getSimulatorTime() throws RemoteException
    {
        return new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND));
    }

    /** {@inheritDoc} */
    @Override
    public final Replication<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getReplication() throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void initialize(final Replication<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> replication,
            final ReplicationMode replicationMode) throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isRunning() throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void start() throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void step() throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final void stop() throws RemoteException, SimRuntimeException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final Context getContext() throws NamingException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean addListener(final EventListenerInterface arg0, final EventType arg1) throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean addListener(final EventListenerInterface arg0, final EventType arg1, final boolean arg2)
            throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean addListener(final EventListenerInterface arg0, final EventType arg1, final short arg2)
            throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean addListener(final EventListenerInterface arg0, final EventType arg1, final short arg2,
            final boolean arg3) throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean removeListener(final EventListenerInterface arg0, final EventType arg1) throws RemoteException
    {
        throw new Error("Not supported in the fake simulator");
    }

}
