package org.opentrafficsim.core.gtu;

import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractLinkBasedGTU extends AbstractGtu
{
    /** */
    private static final long serialVersionUID = 20151114L;

    /** The network in which this GTU is (initially) registered. */
    private OTSNetwork network;

    /**
     * @param id String; the id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param network OTSNetwork; the network in which this GTU is (initially) registered
     * @throws GtuException when the construction of the original waiting path fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLinkBasedGTU(final String id, final GtuType gtuType, final OTSNetwork network) throws GtuException
    {
        super(id, gtuType, network.getSimulator(), network);
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
