package org.opentrafficsim.imb.transceiver.urbanstrategy;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * IMB transceiver for the network. <br>
 * OTS publishes events about the Network to IMB to be able to identify the network. At the start of the OTS simulation, or when
 * a (sub) network is added later, a NEW message is sent to IMB to identify the network's id. No CHANGE or DELETE messages are
 * posted.
 * <p>
 * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
 * <h2>NEW</h2>
 * <table summary="" style="width:800px;">
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
 * <td>Name of the network as used in e.g., Link messages</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2> Not sent
 * </p>
 * <p>
 * <h2>DELETE</h2> Not sent
 * </p>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 16, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class NetworkTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160918L;

    /**
     * Construct a new NetworkTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to retrieve the timestamp
     * @param network OTSNetwork; the OTS network
     * @throws IMBException when the message cannot be sent
     * @throws NullPointerException in case one of the arguments is null.
     */
    public NetworkTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("Network", connector, simulator);
        Throw.whenNull(network, "Network cannot be null");
        connector.postIMBMessage("Network", IMBEventType.NEW, new Object[] {simulator.getSimulatorTime().si, network.getId()});
    }

}
