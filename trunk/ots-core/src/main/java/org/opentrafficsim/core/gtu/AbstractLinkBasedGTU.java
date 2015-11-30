package org.opentrafficsim.core.gtu;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 13, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLinkBasedGTU extends AbstractGTU
{
    /** */
    private static final long serialVersionUID = 20151114L;

    /**
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator the simulator to schedule plan changes on
     * @param strategicalPlanner the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs
     *            to go. It operates by instantiating tactical planners to do the work.
     * @param perception the perception unit that takes care of observing the environment of the GTU
     * @param initialLocation the initial location (and direction) of the GTU
     * @throws SimRuntimeException when scheduling after the first move fails
     * @throws NetworkException when the odometer fails to update (will never happen)
     */
    public AbstractLinkBasedGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
        final StrategicalPlanner strategicalPlanner, final Perception perception, final DirectedPoint initialLocation)
        throws SimRuntimeException, NetworkException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLocation);
    }

}
