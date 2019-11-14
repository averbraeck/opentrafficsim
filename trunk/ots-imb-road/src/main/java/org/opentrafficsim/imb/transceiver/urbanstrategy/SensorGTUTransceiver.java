package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.rmi.RemoteException;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.RelativePosition;
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
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.Event;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.event.TimedEvent;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * OTS publishes events about sensor triggers to IMB, e.g. to show that a vehicle has triggered a sensor for a traffic
 * light.<br>
 * At the start of the OTS simulation, or when a sensor is added later, a NEW message is sent to IMB to identify the sensor, the
 * lane on which it resides, and the position on the lane. The CHANGE message is posted whenever a vehicle triggers the sensor.
 * When a Sensor is removed from the network, a DELETE event is posted. The Sensor NEW messages are posted after the Network
 * NEW, Node NEW, Link NEW, and Lane NEW messages are posted.
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
 * <td>sensorId</td>
 * <td>String</td>
 * <td>id of the Sensor, unique within the Lane</td>
 * </tr>
 * <tr>
 * <td>longitudinalPosition</td>
 * <td>double</td>
 * <td>position on the center line of the lane, in meters</td>
 * </tr>
 * <tr>
 * <td>length</td>
 * <td>double</td>
 * <td>length of the sensor, in meters</td>
 * </tr>
 * <tr>
 * <td>sensorPosition.x</td>
 * <td>double</td>
 * <td>x-coordinate of the sensor position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>sensorPosition.y</td>
 * <td>double</td>
 * <td>y-coordinate of the sensor position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>sensorPosition.z</td>
 * <td>double</td>
 * <td>z-coordinate of the sensor position in (gis) coordinates</td>
 * </tr>
 * <tr>
 * <td>triggerPosition</td>
 * <td>String</td>
 * <td>Relative position of the vehicle that triggers this sensor. One of {FRONT, REAR, REFERENCE, CONTOUR, CENTER, DRIVER}</td>
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
 * <td>sensorId</td>
 * <td>String</td>
 * <td>id of the Sensor, unique within the Lane</td>
 * </tr>
 * <tr>
 * <td>gtuId</td>
 * <td>String</td>
 * <td>id of the vehicle that triggers the sensor</td>
 * </tr>
 * <tr>
 * <td>speed</td>
 * <td>double</td>
 * <td>speed of the vehicle that triggers the sensor, in m/s</td>
 * </tr>
 * <tr>
 * <td>triggerPosition</td>
 * <td>String</td>
 * <td>Relative position of the vehicle that triggers this sensor. One of {FRONT, REAR, REFERENCE, CONTOUR, CENTER, DRIVER}</td>
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
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * <tr>
 * <td>linkId</td>
 * <td>String</td>
 * <td>id of the Link in the Network</td>
 * </tr>
 * <tr>
 * <td>laneId</td>
 * <td>String</td>
 * <td>id of the Lane in the Link</td>
 * </tr>
 * <tr>
 * <td>sensorId</td>
 * <td>String</td>
 * <td>id of the Sensor that is removed from the Lane</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SensorGTUTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160918L;

    /** the OTS network on which Links / Lanes are registered. */
    private final OTSNetwork network;

    /**
     * Construct a new SensorGTUTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @param network OTSNetwork; the OTS network on which Links for Lanes are registered
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public SensorGTUTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator,
            final OTSNetwork network) throws IMBException
    {
        super("Sensor_GTU", connector, simulator);
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
                System.err.println("SensorGTUTransceiver.notify(LINK_ADD) - Don't know how to handle a non-CrossSectionLink");
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
                    System.err.println("SensorGTUTransceiver.notify(LINK_ADD) - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(CrossSectionLink.LANE_ADD_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            Lane lane = (Lane) content[3];

            lane.addListener(this, Lane.SENSOR_ADD_EVENT);
            lane.addListener(this, Lane.SENSOR_REMOVE_EVENT);

            // Post ourselves a SENSOR_ADD_EVENT for every Sensor currently on the lane
            for (SingleSensor sensor : lane.getSensors())
            {
                try
                {
                    this.notify(new TimedEvent<Time>(Lane.SENSOR_ADD_EVENT, lane, new Object[] {sensor.getId(), sensor},
                            getSimulator().getSimulatorTime()));
                }
                catch (RemoteException exception)
                {
                    System.err.println("SensorGTUTransceiver.notify(LANE_ADD) - RemoteException: " + exception.getMessage());
                    return;
                }
            }
        }

        else if (type.equals(Lane.SENSOR_ADD_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            SingleSensor sensor = (SingleSensor) content[1];
            sensor.addListener(this, SingleSensor.SENSOR_TRIGGER_EVENT);

            try
            {
                getConnector().postIMBMessage("Sensor_GTU", IMBEventType.NEW, transformNew(event));
            }
            catch (IMBException exception)
            {
                System.err.println("SensorGTUTransceiver.notify(SENSOR_ADD) - IMBException: " + exception.getMessage());
                return;
            }
        }

        else if (type.equals(SingleSensor.SENSOR_TRIGGER_EVENT))
        {
            try
            {
                getConnector().postIMBMessage("Sensor_GTU", IMBEventType.CHANGE, transformChange(event));
            }
            catch (IMBException exception)
            {
                System.err.println("SensorGTUTransceiver.notify(SENSOR_TRIGGER) - IMBException: " + exception.getMessage());
                return;
            }
        }

        else if (type.equals(Network.LINK_REMOVE_EVENT))
        {
            Link link = this.network.getLink((String) event.getContent());
            if (!(link instanceof CrossSectionLink))
            {
                System.err
                        .println("SensorGTUTransceiver.notify(LINK_REMOVE) - Don't know how to handle a non-CrossSectionLink");
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
                    System.err.println("SensorGTUTransceiver.notify(LINK_REMOVE) - RemoteException: " + exception.getMessage());
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

            lane.removeListener(this, Lane.SENSOR_ADD_EVENT);
            lane.removeListener(this, Lane.SENSOR_REMOVE_EVENT);

            // Post ourselves a SENSOR_REMOVE_EVENT for every Sensor currently on the lane
            for (SingleSensor sensor : lane.getSensors())
            {
                try
                {
                    this.notify(new TimedEvent<Time>(Lane.SENSOR_REMOVE_EVENT, lane, new Object[] {sensor.getId(), sensor},
                            getSimulator().getSimulatorTime()));
                }
                catch (RemoteException exception)
                {
                    System.err.println("SensorGTUTransceiver.notify(LANE_REMOVE) - RemoteException: " + exception.getMessage());
                    return;
                }
            }

            // post the Node message to de-register the lane from the IMB bus
            try
            {
                getConnector().postIMBMessage("Sensor_GTU", IMBEventType.DELETE, transformDelete(event));
            }
            catch (IMBException exception)
            {
                System.err.println("SensorGTUTransceiver.notify(LANE_REMOVE) - IMBException: " + exception.getMessage());
                return;
            }
        }

        else if (type.equals(Lane.SENSOR_REMOVE_EVENT))
        {
            Object[] content = (Object[]) event.getContent();
            SingleSensor sensor = (SingleSensor) content[1];
            sensor.removeListener(this, SingleSensor.SENSOR_TRIGGER_EVENT);

            try
            {
                getConnector().postIMBMessage("Sensor_GTU", IMBEventType.DELETE, transformDelete(event));
            }
            catch (IMBException exception)
            {
                System.err.println("SensorGTUTransceiver.notify(SENSOR_REMOVE) - IMBException: " + exception.getMessage());
                return;
            }
        }

        else
        {
            System.err.println("SensorGTUTransceiver.notify - Unhandled event: " + event);
        }
    }

    /**
     * Transform the addition of a Sensor to a Lane to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a NEW message.
     * @return the NEW payload
     */
    private Object[] transformNew(final EventInterface event)
    {
        if (Lane.SENSOR_ADD_EVENT.equals(event.getType()))
        {
            // Object[] {String sensorId, Sensor sensor}
            Object[] content = (Object[]) event.getContent();
            String sensorId = (String) content[0];
            SingleSensor sensor = (SingleSensor) content[1];
            Lane lane = sensor.getLane();
            double longitudinalPosition = sensor.getLongitudinalPosition().si;
            double length = 0.0; // sensor has zero length right now
            DirectedPoint pos = sensor.getLocation();
            String triggerPosition = sensor.getPositionType().toString();
            double timestamp = getSimulator().getSimulatorTime().si;
            return new Object[] {timestamp, this.network.getId(), lane.getParentLink().getId(), lane.getId(), sensorId,
                    longitudinalPosition, length, pos.x, pos.y, pos.z, triggerPosition};
        }
        System.err.println("SensorGTUTransceiver.transformNew: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the Sensor Triggered event content to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a CHANGE message.
     * @return the CHANGE payload
     */
    private Object[] transformChange(final EventInterface event)
    {
        if (SingleSensor.SENSOR_TRIGGER_EVENT.equals(event.getType()))
        {
            // Object[] {String sensorId, Sensor sensor, LaneBasedGTU gtu, RelativePosition.TYPE relativePosition}
            Object[] content = (Object[]) event.getContent();
            String sensorId = (String) content[0];
            SingleSensor sensor = (SingleSensor) content[1];
            Lane lane = sensor.getLane();
            LaneBasedGTU gtu = (LaneBasedGTU) content[2];
            String gtuId = gtu.getId();
            double gtuSpeed = gtu.getSpeed().si;
            String triggerPosition = ((RelativePosition.TYPE) content[3]).toString();
            double timestamp = getSimulator().getSimulatorTime().si;
            return new Object[] {timestamp, this.network.getId(), lane.getParentLink().getId(), lane.getId(), sensorId, gtuId,
                    gtuSpeed, triggerPosition};
        }
        System.err.println("SensorGTUTransceiver.transformChange: Don't know how to transform event " + event);
        return new Object[] {};
    }

    /**
     * Transform the removal of a Sensor from a Lane to a corresponding IMB message.
     * @param event EventInterface; the event to transform to a DELETE message.
     * @return the DELETE payload
     */
    private Object[] transformDelete(final EventInterface event)
    {
        if (Lane.SENSOR_REMOVE_EVENT.equals(event.getType()))
        {
            // Object[] {String sensorId, Sensor sensor}
            Object[] content = (Object[]) event.getContent();
            String sensorId = (String) content[0];
            SingleSensor sensor = (SingleSensor) content[1];
            Lane lane = sensor.getLane();
            double timestamp = getSimulator().getSimulatorTime().si;
            return new Object[] {timestamp, this.network.getId(), lane.getParentLink().getId(), lane.getId(), sensorId};
        }
        System.err.println("SensorGTUTransceiver.transformDelete: Don't know how to transform event " + event);
        return new Object[] {};
    }

}
