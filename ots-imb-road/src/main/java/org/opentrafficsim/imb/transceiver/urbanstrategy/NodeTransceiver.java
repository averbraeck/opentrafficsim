package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventTypeInterface;
import org.djutils.event.TimedEvent;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * OTS publishes events about the Nodes to IMB to be able to identify the nodes in the network.<br>
 * At the start of the OTS simulation, or when a Node is added later, a NEW message is sent to IMB for each node to identify the
 * node id. No CHANGE messages are posted. When a Node is removed from the network, a DELETE event is posted. The Node NEW
 * messages are posted after the Network NEW message is posted.
 * <p>
 * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
 * <H2>NEW</H2>
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
 * <td>Id of the Network where the Node resides</td>
 * </tr>
 * <tr>
 * <td>nodeId</td>
 * <td>String</td>
 * <td>id of the Node; unique within the Network</td>
 * </tr>
 * <tr>
 * <td>coordinate.x</td>
 * <td>double</td>
 * <td>x-coordinate of the Node</td>
 * </tr>
 * <tr>
 * <td>coordinate.y</td>
 * <td>double</td>
 * <td>y-coordinate of the Node</td>
 * </tr>
 * <tr>
 * <td>coordinate.z</td>
 * <td>double</td>
 * <td>z-coordinate of the Node</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2> Not sent
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
 * <td>Id of the Network where the Node resides</td>
 * </tr>
 * <tr>
 * <td>nodeId</td>
 * <td>String</td>
 * <td>id of the Node that is removed from the Network</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class NodeTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160918L;

    /** the OTS network on which Nodes are registered. */
    private final OTSNetwork network;

    /**
     * Construct a new NodeTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which Nodes are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public NodeTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("Node", connector, simulator);
        this.network = network;

        // listen on network changes and register the listener to all the Links
        addListeners();
    }

    /**
     * Ensure that we get notified about newly created and destroyed Nodes, and instrument all currently existing Nodes.
     * @throws IMBException in case notification of existing Nodes fails
     */
    private void addListeners() throws IMBException
    {
        // Subscribe to all future link creation and removal events.
        this.network.addListener(this, Network.NODE_ADD_EVENT);
        this.network.addListener(this, Network.NODE_REMOVE_EVENT);

        // For already existing links, post ourselves a LINK_ADD_EVENT
        for (Node node : this.network.getNodeMap().values())
        {
            try
            {
                this.notify(new TimedEvent<Time>(Network.NODE_ADD_EVENT, this.network, node.getId(),
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
        EventTypeInterface type = event.getType();
        if (type.equals(Network.NODE_ADD_EVENT))
        {
            Node node = this.network.getNode((String) event.getContent());
            try
            {
                getConnector().postIMBMessage("Node", IMBEventType.NEW, new Object[] {getSimulator().getSimulatorTime().si,
                        this.network.getId(), node.getId(), node.getPoint().x, node.getPoint().y, node.getPoint().z});
            }
            catch (IMBException exception)
            {
                exception.printStackTrace();
            }
        }
        else if (type.equals(Network.NODE_REMOVE_EVENT))
        {
            Node node = this.network.getNode((String) event.getContent());
            try
            {
                getConnector().postIMBMessage("Node", IMBEventType.DELETE,
                        new Object[] {getSimulator().getSimulatorTime().si, this.network.getId(), node.getId()});
            }
            catch (IMBException exception)
            {
                exception.printStackTrace();
            }
        }
        else
        {
            System.err.println("NodeTransceiver.notify: Unhandled event: " + event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "NodeTransceiver";
    }
}
