package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.event.TimedEvent;

/**
 * OTS publishes events about the lanes to IMB, e.g. to know about the number of vehicles on a particular lane.<br>
 * At the start of the OTS simulation, or when a Lane is added later, a NEW message is sent to IMB to identify the lane, the
 * coordinates of its center line, and the start and end nodes of the link. The CHANGE message is posted whenever a vehicle
 * enters or leaves a link. When a Link is removed from the network, a DELETE event is posted. The Link NEW messages are posted
 * after the Network NEW, Node NEW and Link NEW messages are posted.
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
 * <td>laneId</td>
 * <td>String</td>
 * <td>id of the Lane, unique within the Link</td>
 * </tr>
 * <tr>
 * <td>laneNumber</td>
 * <td>int</td>
 * <td>identification of the lane number within the Link.</td>
 * </tr>
 * <tr>
 * <td>centerLine.numberOfPoints</td>
 * <td>int</td>
 * <td>number of points for the center line of the Lane. The number of doubles that follow is 3 times this number</td>
 * </tr>
 * <tr>
 * <td>centerLine.x1</td>
 * <td>double</td>
 * <td>x-coordinate of the first point of the center line</td>
 * </tr>
 * <tr>
 * <td>centerLine.y1</td>
 * <td>double</td>
 * <td>y-coordinate of the first point of the center line</td>
 * </tr>
 * <tr>
 * <td>centerLine.z1</td>
 * <td>double</td>
 * <td>z-coordinate of the first point of the center line</td>
 * </tr>
 * <tr>
 * <td>...</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>centerLine.xn</td>
 * <td>double</td>
 * <td>x-coordinate of the last point of the center line</td>
 * </tr>
 * <tr>
 * <td>centerLine.yn</td>
 * <td>double</td>
 * <td>y-coordinate of the last point of the center line</td>
 * </tr>
 * <tr>
 * <td>centerLine.zn</td>
 * <td>double</td>
 * <td>z-coordinate of the last point of the center line</td>
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
 * <td>Id of the Network where the Lane resides</td>
 * </tr>
 * <tr>
 * <td>linkId</td>
 * <td>String</td>
 * <td>id of the Link to which the lane belongs</td>
 * </tr>
 * <tr>
 * <td>laneId</td>
 * <td>String</td>
 * <td>id of the Lane, unique within the Link</td>
 * </tr>
 * <tr>
 * <td>isVehicleAdded</td>
 * <td>boolean</td>
 * <td>true if vehicle added, false if vehicle removed</td>
 * </tr>
 * <tr>
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the gtu that was added or removed from the Lane</td>
 * </tr>
 * <tr>
 * <td>countAfterEvent</td>
 * <td>int</td>
 * <td>the number of vehicles on the Lane after the event</td>
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
 * <td>id of the Link</td>
 * </tr>
 * <tr>
 * <td>laneId</td>
 * <td>String</td>
 * <td>id of the Lane that is removed from the Network</td>
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
public class LaneGTUTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160918L;

    /** the OTS network on which Links / Lanes are registered. */
    private final OTSNetwork network;

    /**
     * Construct a new LaneGTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which Links for Lanes are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public LaneGTUTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("Lane_GTU", connector, simulator);
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
                System.err.println("LaneGTUTransceiver.notify(LINK_ADD) - Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;

            csl.addListener(this, CrossSectionLink.LANE_ADD_EVENT);
            csl.addListener(this, CrossSectionLink.LANE_REMOVE_EVENT);

            // For already existing lanes, post ourselves a LANE_ADD_EVENT
            for (Lane lane : csl.getLanes())
            {
                try
                {
                    this.notify(new Event(CrossSectionLink.LANE_ADD_EVENT, csl, new Object[] {link.getNetwork().getId(),
                            link.getId(), lane.getId(), lane, csl.getLanes().indexOf(lane)}));
                }
                catch (RemoteException exception)
                {
                    System.err.println("LaneGTUTransceiver.notify(LINK_ADD) - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(CrossSectionLink.LANE_ADD_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            Lane lane = (Lane) content[3];

            // post the Lane_GTU message to register the lane on the IMB bus
            try
            {
                getConnector().postIMBMessage("Lane_GTU", IMBEventType.NEW, transformNew(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LaneGTUTransceiver.notify(LANE_ADD) - IMBException: " + exception.getMessage());
                return;
            }

            lane.addListener(this, Lane.GTU_ADD_EVENT);
            lane.addListener(this, Lane.GTU_REMOVE_EVENT);

            // Post ourselves a GTU_ADD_EVENT for every GTU currently on the lane
            int gtuCount = lane.getGtuList().size();
            for (LaneBasedGTU gtu : lane.getGtuList())
            {
                try
                {
                    this.notify(new TimedEvent<Time>(Lane.GTU_ADD_EVENT, lane, new Object[] {gtu.getId(), gtu, gtuCount},
                            getSimulator().getSimulatorTime()));
                }
                catch (RemoteException exception)
                {
                    System.err.println("LaneGTUTransceiver.notify(LANE_ADD) - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            Link link = this.network.getLink((String) event.getContent());
            if (!(link instanceof CrossSectionLink))
            {
                System.err.println("LaneGTUTransceiver.notify(LINK_REMOVE) - Don't know how to handle a non-CrossSectionLink");
                return;
            }
            CrossSectionLink csl = (CrossSectionLink) link;

            csl.removeListener(this, CrossSectionLink.LANE_ADD_EVENT);
            csl.removeListener(this, CrossSectionLink.LANE_REMOVE_EVENT);

            // For already existing lanes, post ourselves a LANE_REMOVE_EVENT
            for (Lane lane : csl.getLanes())
            {
                try
                {
                    this.notify(new Event(CrossSectionLink.LANE_REMOVE_EVENT, csl,
                            new Object[] {link.getNetwork().getId(), link.getId(), lane.getId()}));
                }
                catch (RemoteException exception)
                {
                    System.err.println("LaneGTUTransceiver.notify(LINK_REMOVE) - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(CrossSectionLink.LANE_REMOVE_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            String laneId = (String) content[2];
            CrossSectionLink csl = (CrossSectionLink) event.getSource();
            Lane lane = (Lane) csl.getCrossSectionElement(laneId);

            lane.removeListener(this, Lane.GTU_ADD_EVENT);
            lane.removeListener(this, Lane.GTU_REMOVE_EVENT);

            // post the Node message to de-register the lane from the IMB bus
            try
            {
                getConnector().postIMBMessage("Lane_GTU", IMBEventType.DELETE, transformDelete(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LaneGTUTransceiver.notify(LANE_REMOVE) - IMBException: " + exception.getMessage());
                return;
            }
        }

        else if (type.equals(Lane.GTU_ADD_EVENT) || type.equals(Lane.GTU_REMOVE_EVENT))
        {
            try
            {
                getConnector().postIMBMessage("Lane_GTU", IMBEventType.CHANGE, transformChange(event));
            }
            catch (IMBException exception)
            {
                System.err.println("LaneGTUTransceiver.notify CHANGE - IMBException: " + exception.getMessage());
                return;
            }
        }

        else
        {
            System.err.println("LaneGTUTransceiver.notify - Unhandled event: " + event);
        }
    }

    /**
     * Transform the addition of a Lane to the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a NEW message.
     * @return the NEW payload
     */
    public Object[] transformNew(final EventInterface event)
    {
        if (CrossSectionLink.LANE_ADD_EVENT.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            Lane lane = (Lane) content[3];
            int laneNumber = (Integer) content[4];
            double timestamp = getSimulator().getSimulatorTime().si;
            List<Object> resultList = new ArrayList<>();
            resultList.add(timestamp);
            resultList.add(this.network.getId());
            resultList.add(lane.getParentLink().getId());
            resultList.add(lane.getId());
            resultList.add(laneNumber);
            resultList.add(lane.getCenterLine().size());
            for (int i = 0; i < lane.getCenterLine().size(); i++)
            {
                try
                {
                    OTSPoint3D p = lane.getCenterLine().get(i);
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
        System.err.println("LaneGTUTransceiver.transformNew: Don't know how to transform event " + event);
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
        Lane lane = (Lane) event.getSource();
        double timestamp = getSimulator().getSimulatorTime().si;
        if (Lane.GTU_ADD_EVENT.equals(event.getType()))
        {
            return new Object[] {timestamp, this.network.getId(), lane.getParentLink().getId(), lane.getId(), true, gtuId,
                    countAfterEvent};
        }
        else if (Lane.GTU_REMOVE_EVENT.equals(event.getType()))
        {
            return new Object[] {timestamp, this.network.getId(), lane.getParentLink().getId(), lane.getId(), false, gtuId,
                    countAfterEvent};
        }
        System.err.println("LaneGTUTransceiver.transformChange: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the removal of a Lane from the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a DELETE message.
     * @return the DELETE payload
     */
    public Object[] transformDelete(final EventInterface event)
    {
        if (CrossSectionLink.LANE_REMOVE_EVENT.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            String networkId = (String) content[0];
            String linkId = (String) content[1];
            String laneId = (String) content[2];
            double timestamp = getSimulator().getSimulatorTime().si;
            return new Object[] {timestamp, networkId, linkId, laneId};
        }
        System.err.println("LaneGTUTransceiver.transformDelete: Don't know how to transform event " + event);
        return new Object[] {};
    }

}
