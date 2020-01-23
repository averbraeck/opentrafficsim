package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param id String; the id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSSimulatorInterface; the simulator to schedule plan changes on
     * @param network OTSNetwork; the network in which this GTU is (initially) registered
     * @throws GTUException when the construction of the original waiting path fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLinkBasedGTU(final String id, final GTUType gtuType, final OTSSimulatorInterface simulator,
            final OTSNetwork network) throws GTUException
    {
        super(id, gtuType, simulator, network);
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
     * @param network OTSNetwork; change the network this GTU is registered in
     */
    public final void setNetwork(final OTSNetwork network)
    {
        this.network = network;
    }

    /*
     * Return the link on which the given position of the GTU currently is. Returns <b>null</b> if the GTU is not on a link with
     * this position.
     * @param referencePosition the position type (FRONT, BACK, etc.) for which we want to know the link
     * @return Link; the link on which the position of the GTU currently is
     */
    // TODO public abstract Link getLink(final RelativePosition.TYPE referencePosition);

    /**
     * Return the link on which the REFERENCE position of the GTU currently is on. Returns <b>null</b> if the GTU is not on a
     * link with its reference position.
     * @return Link; the link on which the REFERENCE position of the GTU currently is on
     */
    public abstract Link getLink();
}
