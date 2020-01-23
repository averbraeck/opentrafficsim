package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.event.TimedEvent;

/**
 * OTS publishes events about the links to IMB, e.g. to know about the number of vehicles on a link.<br>
 * At the start of the OTS simulation, or when a Link is added later, a NEW message is sent to IMB to identify the link, the
 * coordinates of its design line, and the start and end nodes of the link. The CHANGE message is posted whenever a vehicle
 * enters or leaves a link. When a Link is removed from the network, a DELETE event is posted. The Link NEW messages are posted
 * after the Network NEW and Node NEW messages are posted.
 * <p>
 * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
 * <h2>NEW</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * <tr>
 * <td>linkId</td>
 * <td>String</td>
 * <td>id of the Link; unique within the Network</td>
 * </tr>
 * <tr>
 * <td>startNodeId</td>
 * <td>String</td>
 * <td>id of the start node, provided in a Node NEW message</td>
 * </tr>
 * <tr>
 * <td>endNodeId</td>
 * <td>String</td>
 * <td>id of the end node, provided in a Node NEW message</td>
 * </tr>
 * <tr>
 * <td>designLine.numberOfPoints</td>
 * <td>int</td>
 * <td>number of points for the design line of the Link. The number of doubles that follow is 3 times this number</td>
 * </tr>
 * <tr>
 * <td>designLine.x1</td>
 * <td>double</td>
 * <td>x-coordinate of the first point of the design line</td>
 * </tr>
 * <tr>
 * <td>designLine.y1</td>
 * <td>double</td>
 * <td>y-coordinate of the first point of the design line</td>
 * </tr>
 * <tr>
 * <td>designLine.z1</td>
 * <td>double</td>
 * <td>z-coordinate of the first point of the design line</td>
 * </tr>
 * <tr>
 * <td>...</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>designLine.xn</td>
 * <td>double</td>
 * <td>x-coordinate of the last point of the design line</td>
 * </tr>
 * <tr>
 * <td>designLine.yn</td>
 * <td>double</td>
 * <td>y-coordinate of the last point of the design line</td>
 * </tr>
 * <tr>
 * <td>designLine.zn</td>
 * <td>double</td>
 * <td>z-coordinate of the last point of the design line</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * <tr>
 * <td>linkId</td>
 * <td>String</td>
 * <td>id of the Link</td>
 * </tr>
 * <tr>
 * <td>isVehicleAdded</td>
 * <td>boolean</td>
 * <td>true if vehicle added, false if vehicle removed</td>
 * </tr>
 * <tr>
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the gtu that was added or removed from the Link</td>
 * </tr>
 * <tr>
 * <td>countAfterEvent</td>
 * <td>int</td>
 * <td>the number of vehicles on the link after the event</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>DELETE</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * <tr>
 * <td>linkId</td>
 * <td>String</td>
 * <td>id of the Link that is removed from the Network</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinkGTUTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160913L;

    /** the OTS network on which Links are registered. */
    private final OTSNetwork network;

    /**
     * Construct a new LinkGTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which Links are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public LinkGTUTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("Link_GTU", connector, simulator);
        this.network = network;

        // listen on network changes and register the listener to all the Links
        addListeners();
    }

    /**
     * Ensure that we get notified about newly created and destroyed Links instrument all currently existing Links.
     * @throws IMBException in case notification of existing Lanes fails
     */
    private void addListeners() throws IMBException
    {
        // Subscribe to all future link creation and removal events.
        this.network.addListener(this, Network.LINK_ADD_EVENT);
        this.network.addListener(this, Network.LINK_REMOVE_EVENT);

        // For already existing links, post ourselves a LINK_ADD_EVENT
        for (Link link : this.network.getLinkMap().values())
        {
            try
            {
                this.notify(new TimedEvent<Time>(Network.LINK_ADD_EVENT, this.network, link.getId(),
                        getSimulator().getSimulatorTime()));
            }
            catch (RemoteException exception)
            {
                throw new IMBException(exception);
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
                System.err.println("LinkGTUTransceiver.notify NEW - Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;

            // post the Link_GTU message to register the link on the IMB bus
            try
            {
                getConnector().postIMBMessage("Link_GTU", IMBEventType.NEW, transformNew(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LinkGTUTransceiver.notify NEW - IMBException: " + exception.getMessage());
                return;
            }

            csl.addListener(this, Link.GTU_ADD_EVENT);
            csl.addListener(this, Link.GTU_REMOVE_EVENT);

            // Post ourselves a GTU_ADD_EVENT for every GTU currently on the link
            int gtuCount = link.getGTUs().size();
            for (GTU gtu : link.getGTUs())
            {
                try
                {
                    this.notify(new TimedEvent<Time>(Link.GTU_ADD_EVENT, link, new Object[] {gtu.getId(), gtu, gtuCount},
                            getSimulator().getSimulatorTime()));
                }
                catch (RemoteException exception)
                {
                    System.err.println("LinkGTUTransceiver.notify NEW - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            Link link = this.network.getLink((String) event.getContent());
            if (!(link instanceof CrossSectionLink))
            {
                System.err.println("LinkGTUTransceiver.notify DELETE - Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;
            csl.removeListener(this, Link.GTU_ADD_EVENT);
            csl.removeListener(this, Link.GTU_REMOVE_EVENT);

            // post the Node message to de-register the link from the IMB bus
            try
            {
                getConnector().postIMBMessage("Link_GTU", IMBEventType.DELETE, transformDelete(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LinkGTUTransceiver.notify DELETE - IMBException: " + exception.getMessage());
                return;
            }
        }

        else if (type.equals(Link.GTU_ADD_EVENT) || type.equals(Link.GTU_REMOVE_EVENT))
        {
            try
            {
                getConnector().postIMBMessage("Link_GTU", IMBEventType.CHANGE, transformChange(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LinkGTUTransceiver.notify CHANGE - IMBException: " + exception.getMessage());
                return;
            }
        }

        else
        {
            System.err.println("LinkGTUTransceiver.notify - Unhandled event: " + event);
        }
    }

    /**
     * Transform the addition of a link to the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a NEW message.
     * @return the NEW payload
     */
    public Object[] transformNew(final EventInterface event)
    {
        if (Network.LINK_ADD_EVENT.equals(event.getType()))
        {
            String linkId = (String) event.getContent();
            Link link = this.network.getLink(linkId);
            double timestamp = getSimulator().getSimulatorTime().si;
            List<Object> resultList = new ArrayList<>();
            resultList.add(timestamp);
            resultList.add(this.network.getId());
            resultList.add(linkId);
            resultList.add(link.getStartNode().getId());
            resultList.add(link.getEndNode().getId());
            resultList.add(link.getDesignLine().size());
            for (int i = 0; i < link.getDesignLine().size(); i++)
            {
                try
                {
                    OTSPoint3D p = link.getDesignLine().get(i);
                    resultList.add(p.x);
                    resultList.add(p.y);
                    resultList.add(p.z);
                }
                catch (OTSGeometryException exception)
                {
                    exception.printStackTrace();
                    resultList.add(0.0d);
                    resultList.add(0.0d);
                    resultList.add(0.0d);
                }
            }
            return resultList.toArray();
        }
        System.err.println("LinkGTUTransceiver.transformNew: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the GTU added or removed event content to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a CHANGE message.
     * @return the CHANGE payload
     */
    public Object[] transformChange(final EventInterface event)
    {
        Object[] gtuInfo = (Object[]) event.getContent();
        String gtuId = (String) gtuInfo[0];
        int countAfterEvent = (Integer) gtuInfo[2];
        Link link = (Link) event.getSource();
        double timestamp = getSimulator().getSimulatorTime().si;
        if (Link.GTU_ADD_EVENT.equals(event.getType()))
        {
            return new Object[] {timestamp, link.getNetwork().getId(), link.getId(), true, gtuId, countAfterEvent};
        }
        else if (Link.GTU_REMOVE_EVENT.equals(event.getType()))
        {
            return new Object[] {timestamp, link.getNetwork().getId(), link.getId(), false, gtuId, countAfterEvent};
        }
        System.err.println("LinkGTUTransceiver.transformChange: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the removal of a link to the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a DELETE message.
     * @return the DELETE payload
     */
    public Object[] transformDelete(final EventInterface event)
    {
        if (Network.LINK_REMOVE_EVENT.equals(event.getType()))
        {
            String linkId = (String) event.getContent();
            double timestamp = getSimulator().getSimulatorTime().si;
            return new Object[] {timestamp, this.network.getId(), linkId};
        }
        System.err.println("LinkGTUTransceiver.transformDelete: Don't know how to transform event " + event);
        return new Object[] {};
    }

}
