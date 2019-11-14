package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.awt.Color;
import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.imb.transceiver.OTSToIMBTransformer;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.TimedEvent;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * The GTUTransceiver publishes the following information on the IMB bus:
 * <ol>
 * <li>LaneBasedGTU.LANEBASED_INIT_EVENT as an IMB NEW event for a GTU entering the network. This also is done for GTUs that
 * have been registered at the start of the simulation.</li>
 * <li>LaneBasedGTU.LANEBASED_MOVE_EVENT as an IMB CHANGE event for a GTU that moves on the network.</li>
 * <li>LaneBasedGTU.LANEBASED_DESTROY_EVENT as an IMB DELETE event for a GTU that leaves the network.</li>
 * </ol>
 * GTUs are identified by their gtuId.
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
 * <table caption="" style="width:800px;">
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
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the gtu's reference point is</td>
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
 * <td>longitudinalPosition</td>
 * <td>double</td>
 * <td>gtu position on the center line of the lane, in meters</td>
 * </tr>
 * <tr>
 * <td>length</td>
 * <td>double</td>
 * <td>length of the gtu, in meters</td>
 * </tr>
 * <tr>
 * <td>width</td>
 * <td>double</td>
 * <td>width of the gtu, in meters</td>
 * </tr>
 * <tr>
 * <td>baseColor.R</td>
 * <td>byte</td>
 * <td>R-component of the gtu's base color</td>
 * </tr>
 * <tr>
 * <td>baseColor.G</td>
 * <td>byte</td>
 * <td>G-component of the gtu's base color</td>
 * </tr>
 * <tr>
 * <td>baseColor.B</td>
 * <td>byte</td>
 * <td>B-component of the gtu's base color</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2>
 * <table caption="" style="width:800px;">
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
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the vehicle that has a new position</td>
 * </tr>
 * <tr>
 * <td>position.x</td>
 * <td>double</td>
 * <td>x-coordinate of the gtu position at the timestamp</td>
 * </tr>
 * <tr>
 * <td>position.y</td>
 * <td>double</td>
 * <td>y-coordinate of the gtu position at the timestamp</td>
 * </tr>
 * <tr>
 * <td>position.z</td>
 * <td>double</td>
 * <td>z-coordinate of the gtu position at the timestamp</td>
 * </tr>
 * <tr>
 * <td>position.rotZ</td>
 * <td>double</td>
 * <td>angle in the x-y plane of the gtu</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the gtu's reference point is</td>
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
 * <td>longitudinalPosition</td>
 * <td>double</td>
 * <td>gtu position on the center line of the lane, in meters</td>
 * </tr>
 * <tr>
 * <td>speed</td>
 * <td>double</td>
 * <td>current speed of the gtu, in m/s</td>
 * </tr>
 * <tr>
 * <td>acceleration</td>
 * <td>double</td>
 * <td>current acceleration of the gtu, in m/s2</td>
 * </tr>
 * <tr>
 * <td>turnIndicatorStatus</td>
 * <td>String</td>
 * <td>one of {NONE, LEFT, RIGHT, HAZARD, NOTPRESENT}</td>
 * </tr>
 * <tr>
 * <td>brakingLights</td>
 * <td>boolean</td>
 * <td>braking lights on or off</td>
 * </tr>
 * <tr>
 * <td>odometer</td>
 * <td>double</td>
 * <td>odometer reading of the GTU, in meters</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>DELETE</h2>
 * <table caption="" style="width:800px;">
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
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the GTU that that is removed from the simulation</td>
 * </tr>
 * <tr>
 * <td>lastPosition.x</td>
 * <td>double</td>
 * <td>x-coordinate of the gtu position at removal</td>
 * </tr>
 * <tr>
 * <td>lastPosition.y</td>
 * <td>double</td>
 * <td>y-coordinate of the gtu position at removal</td>
 * </tr>
 * <tr>
 * <td>lastPosition.z</td>
 * <td>double</td>
 * <td>z-coordinate of the gtu position at removal</td>
 * </tr>
 * <tr>
 * <td>position.rotZ</td>
 * <td>double</td>
 * <td>angle in the x-y plane of the gtu</td>
 * </tr>
 * <tr>
 * <td>lastNetworkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * <tr>
 * <td>lastLinkId</td>
 * <td>String</td>
 * <td>id of the Link where the GTU was destroyed</td>
 * </tr>
 * <tr>
 * <td>lastLaneId</td>
 * <td>String</td>
 * <td>id of the Lane in the Link where the GTU was destroyed</td>
 * </tr>
 * <tr>
 * <td>lastLongitudinalPosition</td>
 * <td>double</td>
 * <td>gtu position on the center line of the lane, in meters</td>
 * </tr>
 * <tr>
 * <td>odometer</td>
 * <td>double</td>
 * <td>final odometer reading of the GTU, in meters</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160911L;

    /** the OTS network on which GTUs are registered. */
    private final OTSNetwork network;

    /**
     * Construct a new GTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which GTUs are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public GTUTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("GTU", connector, simulator);
        this.network = network;

        // listen on network changes and register the listeners to the GTUs
        addListeners();
    }

    /**
     * Ensure that we get notified about newly created and destroyed GTUs and for each already existing GTU generate a
     * GTU_ADD_EVENT.
     * @throws IMBException in case notification of existing GTU fails
     */
    private void addListeners() throws IMBException
    {
        this.network.addListener(this, Network.GTU_ADD_EVENT);
        this.network.addListener(this, Network.GTU_REMOVE_EVENT);

        // Also add all GTUs that were instantiated when the model was constructed, and re-send their INIT event...
        for (GTU gtu : this.network.getGTUs())
        {
            try
            {
                this.notify(new TimedEvent<Time>(Network.GTU_ADD_EVENT, this.network, gtu.getId(),
                        gtu.getSimulator().getSimulatorTime()));
                LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
                DirectedLanePosition dlp = laneBasedGTU.getReferencePosition();
                this.notify(new TimedEvent<Time>(
                        LaneBasedGTU.LANEBASED_INIT_EVENT, gtu, new Object[] {gtu.getId(), gtu.getLocation(), gtu.getLength(),
                                gtu.getWidth(), dlp.getLane(), dlp.getGtuDirection(), gtu.getGTUType()},
                        gtu.getSimulator().getSimulatorTime()));
            }
            catch (RemoteException | GTUException exception)
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
                gtu.addListener(this, LaneBasedGTU.LANEBASED_INIT_EVENT, true);
                gtu.addListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT, true);
                gtu.addListener(this, LaneBasedGTU.LANEBASED_DESTROY_EVENT, true);
            }

            else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
            {
                String gtuId = event.getContent().toString();
                GTU gtu = this.network.getGTU(gtuId);
                gtu.removeListener(this, LaneBasedGTU.LANEBASED_INIT_EVENT);
                gtu.removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
                gtu.removeListener(this, LaneBasedGTU.LANEBASED_DESTROY_EVENT);
            }

            else if (event.getType().equals(LaneBasedGTU.LANEBASED_INIT_EVENT))
            {
                // register the IMB channel for this GTU, and send NEW payload
                getConnector().postIMBMessage("GTU", Connector.IMBEventType.NEW, transformNew(event));
            }

            else if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
            {
                // send CHANGE payload
                // TODO -- does not work because GTU is registered only once
                // super.notify(event);
                getConnector().postIMBMessage("GTU", Connector.IMBEventType.CHANGE, transformChange(event));
            }

            else if (event.getType().equals(LaneBasedGTU.LANEBASED_DESTROY_EVENT))
            {
                // send DELETE payload
                getConnector().postIMBMessage("GTU", Connector.IMBEventType.DELETE, transformDelete(event));
            }
        }
        catch (IMBException exception)
        {
            // TODO implement proper error handling
            exception.printStackTrace();
        }
    }

    /**
     * Transform the addition of a GTU to the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a NEW message.
     * @return the NEW payload
     */
    private Object[] transformNew(final EventInterface event)
    {
        if (LaneBasedGTU.LANEBASED_INIT_EVENT.equals(event.getType()))
        {
            // content contains: [String gtuId, DirectedPoint initialPosition, Length length, Length width, Color
            // gtuBaseColor, Lane referenceLane, Length positionOnReferenceLane, GTUDirectionality direction, GTUType gtuType]
            Object[] content = (Object[]) event.getContent();
            double timestamp = getSimulator().getSimulatorTime().si;
            String gtuId = content[0].toString();
            DirectedPoint location = (DirectedPoint) content[1];
            Lane lane = (Lane) content[5];
            double longitudinalPosition = ((Length) content[6]).si;
            double length = ((Length) content[2]).si;
            double width = ((Length) content[3]).si;
            Color color = (Color) content[4];
            GTUDirectionality direction = (GTUDirectionality) content[7];
            String gtuType = ((GTUType) content[8]).getId();
            return new Object[] {timestamp, gtuId, location.x, location.y, location.z, location.getRotZ(),
                    lane.getParentLink().getNetwork().getId(), lane.getParentLink().getId(), lane.getId(), longitudinalPosition,
                    length, width, (byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), gtuType};
        }
        System.err.println("LaneGTUTransceiver.transformNew: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the deletion of a GTU from the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a DELETE message.
     * @return the DELETE payload
     */
    private Object[] transformDelete(final EventInterface event)
    {
        if (LaneBasedGTU.LANEBASED_DESTROY_EVENT.equals(event.getType()))
        {
            // content contains: [String gtuId, DirectedPoint lastPosition, Length odometer, Lane referenceLane,
            // Length positionOnReferenceLane]
            Object[] content = (Object[]) event.getContent();
            double timestamp = getSimulator().getSimulatorTime().si;
            String gtuId = content[0].toString();
            DirectedPoint location = (DirectedPoint) content[1];
            double odometer = ((Length) content[2]).si;
            Lane lane = (Lane) content[3];
            double longitudinalPosition = ((Length) content[4]).si;
            return new Object[] {timestamp, gtuId, location.x, location.y, location.z, location.getRotZ(),
                    lane.getParentLink().getNetwork().getId(), lane.getParentLink().getId(), lane.getId(), longitudinalPosition,
                    odometer};
        }
        System.err.println("LaneGTUTransceiver.transformNew: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the move of a GTU in the network to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a CHANGE message.
     * @return the CHANGE payload
     */
    private Object[] transformChange(final EventInterface event)
    {
        // moveInfo contains: {String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration,
        // TurnIndicatorStatus turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane}
        Object[] moveInfo = (Object[]) event.getContent();
        String gtuId = moveInfo[0].toString();
        DirectedPoint location = (DirectedPoint) moveInfo[1];
        LaneBasedGTU gtu = (LaneBasedGTU) event.getSource();
        Lane lane = (Lane) moveInfo[6];
        double longitudinalPosition = ((Length) moveInfo[7]).si;
        double speed = ((Speed) moveInfo[2]).si;
        double acceleration = ((Acceleration) moveInfo[3]).si;
        double timestamp = gtu.getSimulator().getSimulatorTime().si;
        String turnIndicatorStatus = ((TurnIndicatorStatus) moveInfo[4]).toString();
        double odometer = ((Length) moveInfo[5]).si;
        boolean brakingLights = acceleration < 0.0; // TODO proper function for isBraking()
        return new Object[] {timestamp, gtuId, location.x, location.y, location.z, location.getRotZ(),
                lane.getParentLink().getNetwork().getId(), lane.getParentLink().getId(), lane.getId(), longitudinalPosition,
                speed, acceleration, turnIndicatorStatus, brakingLights, odometer};
    }

    /**
     * Transform the LaneBasedGTU.LANEBASED_MOVE_EVENT content to a corresponding IMB message.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    static class GTUTransformerChange implements OTSToIMBTransformer
    {
        /** {@inheritDoc} */
        @Override
        public Object[] transform(final EventInterface event)
        {
            // moveInfo contains: {String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration,
            // TurnIndicatorStatus turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane}
            Object[] moveInfo = (Object[]) event.getContent();
            String gtuId = moveInfo[0].toString();
            DirectedPoint location = (DirectedPoint) moveInfo[1];
            LaneBasedGTU gtu = (LaneBasedGTU) event.getSource();
            Lane lane = (Lane) moveInfo[6];
            double longitudinalPosition = ((Length) moveInfo[7]).si;
            double speed = ((Speed) moveInfo[2]).si;
            double acceleration = ((Acceleration) moveInfo[3]).si;
            double timestamp = gtu.getSimulator().getSimulatorTime().si;
            String turnIndicatorStatus = ((TurnIndicatorStatus) moveInfo[4]).toString();
            double odometer = ((Length) moveInfo[5]).si;
            boolean brakingLights = acceleration < 0.0; // TODO proper function for isBraking()
            return new Object[] {timestamp, gtuId, location.x, location.y, location.z, location.getRotZ(),
                    lane.getParentLink().getNetwork().getId(), lane.getParentLink().getId(), lane.getId(), longitudinalPosition,
                    speed, acceleration, turnIndicatorStatus, brakingLights, odometer};
        }
    }
}
