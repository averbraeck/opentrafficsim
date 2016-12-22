package org.opentrafficsim.core.dsol;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.DEVSAnimator;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class OTSDEVSAnimator extends DEVSAnimator<Time, Duration, OTSSimTimeDouble>
        implements OTSDEVSSimulatorInterface, OTSAnimatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

    /**
     * Create a new OTSDEVSAnimator..
     */
    public OTSDEVSAnimator()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public final void initialize(final Replication<Time, Duration, OTSSimTimeDouble> initReplication,
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
    public final void runUpTo(final Time when) throws SimRuntimeException
    {
        super.runUpTo(when);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OTSDEVSAnimator [time=" + getSimulatorTime().getTime() + "]";
    }

}
