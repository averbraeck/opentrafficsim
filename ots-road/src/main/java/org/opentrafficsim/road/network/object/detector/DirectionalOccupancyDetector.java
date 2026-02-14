package org.opentrafficsim.road.network.object.detector;

import org.djutils.base.Identifiable;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.object.Detector;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * An occupancy detector is a lane-based object that can be triggered by a relative position of the GTU (e.g., front, back) when
 * that relative position passes over the detector location on the lane.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface DirectionalOccupancyDetector extends Identifiable
{
    /**
     * Returns the entry position.
     * @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     */
    RelativePosition.Type getPositionTypeEntry();

    /**
     * Returns the exit position.
     * @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the detector.
     */
    RelativePosition.Type getPositionTypeExit();

    /**
     * Return the entry position of this DirectionalOccupancyDetector.
     * @return the lane and position on the lane where GTU entry is detected
     */
    LaneBasedObject getLanePositionEntry();

    /**
     * Return the exit position of this DirectionalOccupancyDetector.
     * @return the lane and position on the lane where GTU exit is detected
     */
    LaneBasedObject getLanePositionExit();

    /**
     * Returns the id.
     * @return The id of the detector.
     */
    @Override
    String getId();

    /**
     * Returns the simulator.
     * @return The simulator.
     */
    OtsSimulatorInterface getSimulator();

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of an OccupancyDetector. <br>
     * Payload: Object[] {String detectorId, Detector detector, LaneBasedGtu gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_DETECTOR_TRIGGER_ENTRY_EVENT = new EventType("DIRECTIONALOCCUPANCYDETECTOR.TRIGGER.ENTRY",
            new MetaData("Occupancy detector trigger", "Occupancy detector is triggered",
                    new ObjectDescriptor("Detector id", "Id of the detector", String.class),
                    new ObjectDescriptor("Detector", "Detector itself", Detector.class),
                    new ObjectDescriptor("GTU", "Triggering GTU", LaneBasedGtu.class),
                    new ObjectDescriptor("Position", "Relative GTU position that triggered", RelativePosition.Type.class)));

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an OccupancyDetector. <br>
     * Payload: Object[] {String detectorId, Detector detector, LaneBasedGtu gtu, RelativePosition.TYPE relativePosition}
     */
    EventType DIRECTIONAL_OCCUPANCY_DETECTOR_TRIGGER_EXIT_EVENT = new EventType("DIRECTIONALOCCUPANCYDETECTOR.TRIGGER.EXIT",
            new MetaData("Occupancy detector trigger", "Occupancy detector is triggered",
                    new ObjectDescriptor("Detector id", "Id of the detector", String.class),
                    new ObjectDescriptor("Detector", "Detector itself", Detector.class),
                    new ObjectDescriptor("GTU", "Triggering GTU", LaneBasedGtu.class),
                    new ObjectDescriptor("Position", "Relative GTU position that triggered", RelativePosition.Type.class)));

    // TODO enforce clone method

}
