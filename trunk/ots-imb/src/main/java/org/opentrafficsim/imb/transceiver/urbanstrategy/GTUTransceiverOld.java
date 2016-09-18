package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.rmi.RemoteException;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.imb.transceiver.OTSToIMBTransformer;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.TimedEvent;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * The GTUTransceiver publishes the following information on the IMB bus:
 * <ol>
 * <li>GTU.INIT_EVENT as an IMB NEW event for a GTU entering the network. This also is done for GTUs that have been registered
 * at the start of the simulation.</li>
 * <li>GTU.MOVE_EVENT as an IMB CHANGE event for a GTU that moves on the network.</li>
 * <li>GTU.DESTROY_EVENT as an IMB DELETE event for a GTU that leaves the network.</li>
 * </ol>
 * GTUs are identified by their gtuId.
 * </p>
 * <p>
 * OTS publishes events about GTUs (vehicles) to IMB, e.g. to calculate statistics or emissions from the vehicles or to display
 * them in the US animation.
 * </p>
 * <p>
 * When a GTU is created, a NEW message is sent to IMB to identify the GTU and its initial characteristics, including the lane
 * on which it resides with its reference point, and the position on the lane. The CHANGE message is posted whenever a vehicle
 * initiates a new OperationalPlan, which coincides with a MOVE Event in OTS. When a GTU is removed from the network, a DELETE
 * event is posted. The GTU NEW messages are posted after the Network NEW, Node NEW, Link NEW, and Lane NEW messages are posted
 * to ensure the Lane on which the GTU resides is known.
 * </p>
 * <p>
 * The longitudinal position of a GTU on a lane is the (projected) position on the center line of the lane, in meters. The
 * zero-point is chosen at the start of the center line of the lane that has been provided in the Lane_GTU NEW message.
 * </p>
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
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the GTU that has been added to the simulation</td>
 * </tr>
 * <tr>
 * <td>position.x</td>
 * <td>double</td>
 * <td>x-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.y</td>
 * <td>double</td>
 * <td>y-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.z</td>
 * <td>double</td>
 * <td>z-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.rotZ</td>
 * <td>double</td>
 * <td>angle in the x-y plane of the gtu</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2>
 * <table summary="" style="width:800px;">
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the GTU that has been added to the simulation</td>
 * </tr>
 * <tr>
 * <td>position.x</td>
 * <td>double</td>
 * <td>x-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.y</td>
 * <td>double</td>
 * <td>y-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.z</td>
 * <td>double</td>
 * <td>z-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.rotZ</td>
 * <td>double</td>
 * <td>angle in the x-y plane of the gtu</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>DELETE</h2>
 * <table summary="" style="width:800px;">
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the GTU that has been added to the simulation</td>
 * </tr>
 * <tr>
 * <td>position.x</td>
 * <td>double</td>
 * <td>x-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.y</td>
 * <td>double</td>
 * <td>y-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.z</td>
 * <td>double</td>
 * <td>z-coordinate of the gtu position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>position.rotZ</td>
 * <td>double</td>
 * <td>angle in the x-y plane of the gtu</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTransceiverOld extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160911L;

    /** the OTS network on which GTUs are registered. */
    private final OTSNetwork network;

    /** the GTU transformer for IMB. */
    private final GTUTransformer transformer = new GTUTransformer();

    /**
     * Construct a new GTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator OTSDEVSSimulatorInterface; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which GTUs are registered
     * @throws NullPointerException in case one of the arguments is null.
     */
    public GTUTransceiverOld(final Connector connector, final OTSDEVSSimulatorInterface simulator, final OTSNetwork network)
    {
        super("GTU", connector, simulator);
        this.network = network;

        // listen on network changes and register the listeners to the GTUs
        addListeners();
    }

    /**
     * Ensure that we get notified about newly created and destroyed GTUs and for each already existing GTU generate a
     * GTU_ADD_EVENT.
     */
    private void addListeners()
    {
        this.network.addListener(this, Network.GTU_ADD_EVENT);
        this.network.addListener(this, Network.GTU_REMOVE_EVENT);

        // Also add all GTUs that were instantiated when the model was constructed, and re-send their INIT event...
        for (GTU gtu : this.network.getGTUs())
        {
            try
            {
                this.notify(new TimedEvent<OTSSimTimeDouble>(Network.GTU_ADD_EVENT, this.network, gtu.getId(),
                        gtu.getSimulator().getSimulatorTime()));
                this.notify(new TimedEvent<OTSSimTimeDouble>(GTU.INIT_EVENT, gtu,
                        new Object[] { gtu.getId(), gtu.getLocation(), gtu.getLength(), gtu.getWidth() },
                        gtu.getSimulator().getSimulatorTime()));
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
        try
        {
            // do not call super.notify(event); we will do that only when needed.

            if (event.getType().equals(Network.GTU_ADD_EVENT))
            {
                String gtuId = event.getContent().toString();
                GTU gtu = this.network.getGTU(gtuId);
                gtu.addListener(this, GTU.INIT_EVENT, true);
                gtu.addListener(this, GTU.MOVE_EVENT, true);
                gtu.addListener(this, GTU.DESTROY_EVENT, true);
            }

            else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
            {
                String gtuId = event.getContent().toString();
                GTU gtu = this.network.getGTU(gtuId);
                gtu.removeListener(this, GTU.INIT_EVENT);
                gtu.removeListener(this, GTU.MOVE_EVENT);
                gtu.removeListener(this, GTU.DESTROY_EVENT);
            }

            else if (event.getType().equals(GTU.INIT_EVENT))
            {
                // register the IMB channel for this GTU, and send initial payload, which is the same as the MOVE payload
                GTU gtu = (GTU) event.getSource();
                addOTSToIMBChannel(gtu, GTU.MOVE_EVENT, "GTU", this.transformer.transform(event), this.transformer);
            }

            else if (event.getType().equals(GTU.MOVE_EVENT))
            {
                // handled by the AbstractTransceiver
                super.notify(event);
            }

            else if (event.getType().equals(GTU.DESTROY_EVENT))
            {
                // register the IMB channel for this GTU, and send initial payload, which is the same as the MOVE payload
                GTU gtu = (GTU) event.getSource();
                removeOTSToIMBChannel(gtu, GTU.MOVE_EVENT, this.transformer.transform(event));
            }
        }
        catch (IMBException exception)
        {
            // TODO implement proper error handling
            exception.printStackTrace();
        }
    }

    /**
     * Transform the GTU.MOVE_EVENT content to a corresponding IMB message.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    static class GTUTransformer implements OTSToIMBTransformer
    {
        /** {@inheritDoc} */
        @Override
        public Object[] transform(final EventInterface event)
        {
            Object[] moveInfo = (Object[]) event.getContent();
            DirectedPoint location = (DirectedPoint) moveInfo[1];
            return new Object[] { moveInfo[0].toString(), location.x, location.y, location.z, location.getRotZ() };
        }
    }
}
