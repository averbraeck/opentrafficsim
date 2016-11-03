package org.opentrafficsim.road.network.lane.object.sensor;

import java.util.HashSet;
import java.util.Set;

import nl.tudelft.simulation.event.EventType;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This traffic light sensor reports whether it whether any GTUs are within its area. The area is a sub-section of a Lane. This
 * traffic sensor does <b>not</b> report the total number of GTUs within the area; only whether that number is zero or non-zero.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrafficLightSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161103L;

    /**
     * The sensor event type for pub/sub indicating that the detector becomes occupied. <br>
     * Payload: [String TrafficLightSensorId]
     */
    public static final EventType TRAFFIC_LIGHT_SENSOR_OCCUPIED_EVENT = new EventType("TRAFFIC_LIGHT_SENSOR.OCCUPIED");

    /**
     * The sensor event type for pub/sub indicating that the detector becomes unoccupied. <br>
     * Payload: [String TrafficLightSensorId]
     */
    public static final EventType TRAFFIC_LIGHT_SENSOR_CLEARED_EVENT = new EventType("TRAFFIC_LIGHT_SENSOR.CLEARED");

    /** The sensor that detects when the rear of a GTU leaves the sensor area. */
    private final DownSensor downSensor;

    /** GTUs detected by the upSensor, but not yet removed by the downSensor. */
    private final Set<LaneBasedGTU> currentGTUs = new HashSet<>();

    /**
     * @param id String; id of this sensor
     * @param lane Lane; the lane of this sensor
     * @param position Length; the position where the front of LaneBasedGTUs is detected by this sensor
     * @param length Length; the distance after position where the rear of LaneBasedGTUs is detected by this sensor
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @throws NetworkException when the network is inconsistent.
     */
    public TrafficLightSensor(final String id, final Lane lane, final Length position, final Length length,
            final OTSDEVSSimulatorInterface simulator) throws NetworkException
    {
        super(id, lane, position, RelativePosition.FRONT, simulator);
        this.downSensor = new DownSensor(id + ".DN", lane, position.plus(length), simulator, this);
        // TODO detect GTUs that enter or leave the sensor sideways or in the reverse direction
    }

    /**
     * Remove a GTU from the set.
     * @param gtu LaneBasedGTU; the GTU that must be removed
     */
    final void removeGTU(final LaneBasedGTU gtu)
    {
        if (this.currentGTUs.remove(gtu))
        {
            if (this.currentGTUs.size() == 0)
            {
                fireTimedEvent(TrafficLightSensor.TRAFFIC_LIGHT_SENSOR_CLEARED_EVENT, new Object[] { getId() }, getSimulator()
                        .getSimulatorTime());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final void triggerResponse(final LaneBasedGTU gtu)
    {
        if (this.currentGTUs.add(gtu))
        {
            if (this.currentGTUs.size() == 1)
            {
                fireTimedEvent(TrafficLightSensor.TRAFFIC_LIGHT_SENSOR_OCCUPIED_EVENT, new Object[] { getId() }, getSimulator()
                        .getSimulatorTime());

            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final AbstractSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        return new TrafficLightSensor(getId(), (Lane) newCSE, getLongitudinalPosition(), this.downSensor
                .getLongitudinalPosition().minus(this.getLongitudinalPosition()), (OTSDEVSSimulatorInterface) newSimulator);
    }

}

/**
 * Sub-sensor of a traffic light sensor.
 */
class DownSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20161027L;

    /** The traffic light sensor that this FlankSensor is a part of. */
    private final TrafficLightSensor parent;

    /**
     * Construct a new DownSensor.
     * @param id String; name of the sensor
     * @param lane Lane; lane on which the sensor is positioned
     * @param position Length; position from the start of the lane
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param parent TrafficLightSensor; the traffic light sensor that detects the up flanks and maintains the set of detected
     *            GTUs
     * @throws NetworkException if the network is inconsistent
     */
    DownSensor(final String id, final Lane lane, final Length position, final OTSDEVSSimulatorInterface simulator,
            final TrafficLightSensor parent) throws NetworkException
    {
        super(id, lane, position, RelativePosition.REAR, simulator);
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    protected void triggerResponse(final LaneBasedGTU gtu)
    {
        this.parent.removeGTU(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
            final boolean animation) throws NetworkException
    {
        return null; // Not used; the TrafficLight sensor takes care of cloning the DownSensor
    }
    
}
