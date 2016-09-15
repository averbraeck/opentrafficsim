package org.opentrafficsim.imb.simulators;

import java.rmi.RemoteException;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.event.TimedEvent;

import org.opentrafficsim.core.dsol.OTSDEVSRealTimeClock;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.simulators.SimulatorTransceiver.EmptyTransformer;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.imb.transceiver.Connector;
import org.opentrafficsim.imb.transceiver.OTSToIMBTransformer;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

/**
 * Report GTUs entering or leaving links.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTULinkTransceiver extends AbstractTransceiver
{

    /** */
    private static final long serialVersionUID = 20160913L;

    /** the OTS network on which Links are registered. */
    private final OTSNetwork network;

    /** the Empty transformer for IMB. */
    private final EmptyTransformer emptyTransformer = new EmptyTransformer();

    /** the GTU transformer for IMB. */
    // private final GTULinkTransformer transformer = new GTULinkTransformer();

    /**
     * Construct a new GTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator OTSDEVSSimulatorInterface; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which GTUs are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public GTULinkTransceiver(final Connector connector, final OTSDEVSSimulatorInterface simulator, final OTSNetwork network)
            throws IMBException
    {
        super("GTULINK", connector, simulator);
        this.network = network;

        // register the OTS to IMB updates for the simulator
        final OTSDEVSRealTimeClock animator = (OTSDEVSRealTimeClock) simulator;
        addOTSToIMBChannel(animator, Link.GTU_ADD_EVENT, "GTU_enters_link", new Object[] {}, this.emptyTransformer);
        addOTSToIMBChannel(animator, Link.GTU_REMOVE_EVENT, "GTU_exits_link", new Object[] {}, this.emptyTransformer);

        // listen on network changes and register the listener to all the Links
        addListeners();
    }

    /**
     * Ensure that we get notified about newly created and destroyed Links instrument all currently existing Links.
     */
    private void addListeners()
    {
        // Subscribe to all future link creation and removal events.
        this.network.addListener(this, Network.LINK_ADD_EVENT);
        this.network.addListener(this, Network.LINK_REMOVE_EVENT);

        // For already existing links, post ourselves a LINK_ADD_EVENT
        for (Link link : this.network.getLinkMap().values())
        {
            try
            {
                this.notify(new TimedEvent<OTSSimTimeDouble>(Network.LINK_ADD_EVENT, this.network, link.getId(), getSimulator()
                        .getSimulatorTime()));
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        EventType type = event.getType();
        if (type.equals(Network.LINK_ADD_EVENT))
        {
            Link link = this.network.getLink((String) event.getContent());
            if (!(link instanceof CrossSectionLink))
            {
                System.err.println("Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;
            csl.addListener(this, Link.GTU_ADD_EVENT);
            csl.addListener(this, Link.GTU_REMOVE_EVENT);
            // Post ourselves a GTU_ADD_EVENT for every GTU currently on the link
            int gtuCount = link.getGTUs().size();
            for (GTU gtu : link.getGTUs())
            {
                try
                {
                    this.notify(new TimedEvent<OTSSimTimeDouble>(Link.GTU_ADD_EVENT, link, new Object[] { gtu.getId(), gtu,
                            gtuCount }, getSimulator().getSimulatorTime()));
                }
                catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            // Is it a matter of politeness to tell a link that is being removed that we're no longer interested in it?
            Link link = this.network.getLink((String) event.getContent());
            if (!(link instanceof CrossSectionLink))
            {
                System.err.println("Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;
            csl.removeListener(this, Link.GTU_ADD_EVENT);
            csl.removeListener(this, Link.GTU_REMOVE_EVENT);
        }
        else if (type.equals(Link.GTU_ADD_EVENT) || type.equals(Link.GTU_REMOVE_EVENT))
        {
            super.notify(event);
        }
        else
        {
            System.err.println("Unhandled event: " + event);
        }
    }

    /**
     * Transform the GTU added or removed event content to a corresponding IMB message.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: pknoppers $,
     * initial version Sep 13, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    static class GTULinkTransformer implements OTSToIMBTransformer
    {
        /** {@inheritDoc} */
        @Override
        public Object[] transform(final EventInterface event)
        {
            Object[] gtuInfo = (Object[]) event.getContent();
            String gtuId = (String) gtuInfo[0];
            //LaneBasedGTU gtu = (LaneBasedGTU) gtuInfo[1];
            int count = (Integer) gtuInfo[2];
            Link link = (Link) event.getSource();
            if (Link.GTU_ADD_EVENT.equals(event.getType()))
            {
                return new Object[] {link.getId(), gtuId, count};
            }
            else if (Link.GTU_REMOVE_EVENT.equals(event.getType()))
            {
                return new Object[] {link.getId(), gtuId, count};
            }
            System.err.println("Don't know how to transform event " + event);
            return new Object[] {};
        }
    }

}
