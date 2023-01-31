package org.opentrafficsim.road.network.lane.object.sensor;

import java.io.Serializable;

import org.djutils.event.EventType;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * An occupancy detector is a lane-based object that can be triggered by a relative position of the GTU (e.g., front, back) when
 * that relative position passes over the detector location on the lane.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface DirectionalOccupancyDetector extends Serializable, Identifiable
{
    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector. */
    RelativePosition.TYPE getPositionTypeEntry();

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector. */
    RelativePosition.TYPE getPositionTypeExit();

    /**
     * Return the entry position of this DirectionalOccupancyDetector.
     * @return LaneBasedObject; the lane and position on the lane where GTU entry is detected
     */
    LaneBasedObject getLanePositionEntry();

    /**
     * Return the exit position of this DirectionalOccupancyDetector.
     * @return LaneBasedObject; the lane and position on the lane where GTU exit is detected
     */
    LaneBasedObject getLanePositionExit();

    /** @return The id of the detector. */
    @Override
    String getId();

    /** @return The simulator. */
    OtsSimulatorInterface getSimulator();

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of an OccupancyDetector. <br>
     * Payload: Object[] {String detectorId, Detector detector, LaneBasedGtu gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_DETECTOR_TRIGGER_ENTRY_EVENT = new EventType("DIRECTIONALOCCUPANCYDETECTOR.TRIGGER.ENTRY");

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an OccupancyDetector. <br>
     * Payload: Object[] {String detectorId, Detector detector, LaneBasedGtu gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_DETECTOR_TRIGGER_EXIT_EVENT = new EventType("DIRECTIONALOCCUPANCYDETECTOR.TRIGGER.EXIT");

    // TODO enforce clone method

}
