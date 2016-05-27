package org.opentrafficsim.core.gtu;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.network.OTSNetwork;

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

    /** The network in which this GTU is (initially) registered. */
    private OTSNetwork network;

    /**
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator the simulator to schedule plan changes on
     * @param strategicalPlanner the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs
     *            to go. It operates by instantiating tactical planners to do the work.
     * @param perception the perception unit that takes care of observing the environment of the GTU
     * @param initialLocation the initial location (and direction) of the GTU
     * @param initialSpeed the initial speed of the GTU
     * @param network the network in which this GTU is (initially) registered
     * @throws SimRuntimeException when scheduling after the first move fails
     * @throws GTUException when the construction of the original waiting path fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLinkBasedGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
        final StrategicalPlanner strategicalPlanner, final Perception perception, final DirectedPoint initialLocation,
        final Speed initialSpeed, final OTSNetwork network) throws SimRuntimeException, GTUException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLocation, initialSpeed, network);
        this.network = network;
    }

    /**
     * @return the network in which the GTU is registered
     */
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * @param network change the network this GTU is registered in
     */
    public final void setNetwork(final OTSNetwork network)
    {
        this.network = network;
    }

}
