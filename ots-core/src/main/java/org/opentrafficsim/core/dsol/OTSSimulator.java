package org.opentrafficsim.core.dsol;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.simulators.Simulator;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Typed extension of the Simulator without remote exceptions.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class OTSSimulator extends
    Simulator<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> implements
    OTSSimulatorInterface
{
    /** */
    private static final long serialVersionUID = 20140909L;

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
}
