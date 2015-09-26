package org.opentrafficsim.core.dsol;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Time;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSRealTimeClock extends
    DEVSRealTimeClock<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> implements
    OTSDEVSSimulatorInterface, OTSAnimatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Create a new OTSRealTimeClock.
     */
    public OTSDEVSRealTimeClock()
    {
        super();
    }

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
    public final void scheduleEventRel(final Time.Rel relativeDelay, final short priority, final Object source,
        final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventRel(relativeDelay, priority, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventRel(final Time.Rel relativeDelay, final Object source, final Object target,
        final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventRel(relativeDelay, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Time.Abs absoluteTime, final Object source, final Object target,
        final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventAbs(absoluteTime, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleEventAbs(final Time.Abs absoluteTime, final short priority, final Object source,
        final Object target, final String method, final Object[] args) throws SimRuntimeException
    {
        super.scheduleEventAbs(absoluteTime, priority, source, target, method, args);
    }

    /** {@inheritDoc} */
    @Override
    public final void runUpTo(final Time.Abs when) throws SimRuntimeException
    {
        super.runUpTo(when);
    }

    /** {@inheritDoc} */
    @Override
    protected final Time.Rel relativeMillis(final double factor)
    {
        return new Time.Rel(factor, TimeUnit.MILLISECOND);
    }
}
