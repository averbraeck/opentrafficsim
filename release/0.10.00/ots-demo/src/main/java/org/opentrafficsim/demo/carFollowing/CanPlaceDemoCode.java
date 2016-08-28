package org.opentrafficsim.demo.carFollowing;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Demo implementation of the canPlace method required by the LaneBasedGTUGenerator.RoomChecker interface.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 15, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CanPlaceDemoCode implements LaneBasedGTUGenerator.RoomChecker
{
    /** Maximum distance supplied to the minimumHeadway method of the GTUFollowingModel. */
    private static Length maxDistance = new Length(Double.MAX_VALUE, LengthUnit.SI);

    /** Precision requested of the minimumHeadway method of the GTUFollowingModel. */
    private static Length precision = new Length(0.1, LengthUnit.METER);

    /** {@inheritDoc} */
    @Override
    public Speed canPlace(Speed leaderSpeed, org.djunits.value.vdouble.scalar.Length headway,
            LaneBasedGTUCharacteristics laneBasedGTUCharacteristics) throws NetworkException
    {
        // This simple minded implementation returns null if the headway is less than the headway wanted for driving at
        // the current speed of the leader
        Lane lane = null;
        for (DirectedLanePosition dlp : laneBasedGTUCharacteristics.getInitialLongitudinalPositions())
        {
            if (dlp.getLane().getLaneType().isCompatible(laneBasedGTUCharacteristics.getGTUType()))
            {
                lane = dlp.getLane();
                break;
            }
        }
        if (null == lane)
        {
            throw new NetworkException("No " + laneBasedGTUCharacteristics.getGTUType()
                    + "-compatible lane in initial longitudinal positions");
        }
        // Use the speed limit of the first compatible lane in the initial longitudinal positions.
        Speed speedLimit = lane.getSpeedLimit(laneBasedGTUCharacteristics.getGTUType());
        Speed maximumSpeed = laneBasedGTUCharacteristics.getMaximumSpeed();
        GTUFollowingModelOld gfm = new IDMOld();
        if (headway.lt(//laneBasedGTUCharacteristics.getStrategicalPlanner().getBehavioralCharacteristics().getGTUFollowingModel()
            gfm.minimumHeadway(leaderSpeed, leaderSpeed, precision, maxDistance, speedLimit, maximumSpeed)))
        {
            return null;
        }
        return leaderSpeed;
    }

}
