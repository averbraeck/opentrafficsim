package org.opentrafficsim.road.network.lane.object.sensor;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.gtu.RelativePosition;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventType;

/**
 * An occupancy sensor is a lane-based object that can be triggered by a relative position of the GTU (e.g., front, back) when
 * that relative position passes over the sensor location on the lane. XXX
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface NonDirectionalOccupancySensor extends Serializable, Identifiable
{
    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionTypeEntry();

    /** @return the relative position type of the vehicle (e.g., FRONT, BACK) that triggers the sensor. */
    RelativePosition.TYPE getPositionTypeExit();

    /**
     * Return the A position of this NonDirectionalOccupancySensor.
     * @return Length; the lane and position on the lane where GTU entry is detected
     */
    Length getLanePositionA();

    /**
     * Return the B position of this NonDirectionalOccupancySensor.
     * @return Length; the lane and position on the lane where GTU exit is detected
     */
    Length getLanePositionB();

    /** @return The id of the sensor. */
    @Override
    String getId();

    /** @return The simulator. */
    DEVSSimulatorInterface.TimeDoubleUnit getSimulator();

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the entry of a NonDirectionalOccupancySensor. <br>
     * Payload: Object[] {String sensorId, NonDirectionalOccupancySensor sensor, LaneBasedGTU gtu, RelativePosition.TYPE
     * relativePosition}
     */
    EventType NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_ENTRY_EVENT =
            new EventType("NONDIRECTIONALOCCUPANCYSENSOR.TRIGGER.ENTRY");

    /**
     * The <b>timed</b> event type for pub/sub indicating the triggering of the exit of an NonDirectionalOccupancySensor. <br>
     * Payload: Object[] {String sensorId, NonDirectionalOccupancySensor sensor, LaneBasedGTU gtu, RelativePosition.TYPE
     * relativePosition}
     */
    EventType NON_DIRECTIONAL_OCCUPANCY_SENSOR_TRIGGER_EXIT_EVENT = new EventType("NONDIRECTIONALOCCUPANCYSENSOR.TRIGGER.EXIT");

    // TODO enforce clone method

}
