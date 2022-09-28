package org.opentrafficsim.road.network.lane.object.sensor;

import java.io.Serializable;

import org.djutils.event.EventType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * An occupancy sensor is a lane-based object that can be triggered by a relative position of the GTU (e.g., front, back) when
 * that relative position passes over the sensor location on the lane.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface DirectionalOccupancySensor extends Serializable, Identifiable
{
    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionTypeEntry();

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionTypeExit();

    /**
     * Return the entry position of this DirectionalOccupancySensor.
     * @return LaneBasedObject; the lane and position on the lane where GTU entry is detected
     */
    LaneBasedObject getLanePositionEntry();

    /**
     * Return the exit position of this DirectionalOccupancySensor.
     * @return LaneBasedObject; the lane and position on the lane where GTU exit is detected
     */
    LaneBasedObject getLanePositionExit();

    /** @return The id of the sensor. */
    @Override
    String getId();

    /** @return The simulator. */
    OTSSimulatorInterface getSimulator();

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of an OccupancySensor. <br>
     * Payload: Object[] {String sensorId, Sensor sensor, LaneBasedGTU gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT = new EventType("DIRECTIONALOCCUPANCYSENSOR.TRIGGER.ENTRY");

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an OccupancySensor. <br>
     * Payload: Object[] {String sensorId, Sensor sensor, LaneBasedGTU gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT = new EventType("DIRECTIONALOCCUPANCYSENSOR.TRIGGER.EXIT");

    // TODO enforce clone method

}
