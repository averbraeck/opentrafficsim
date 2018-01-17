package org.opentrafficsim.demo.carFollowing;

import java.util.Set;
import java.util.SortedSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.Placement;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Demo implementation of the canPlace method required by the LaneBasedGTUGenerator.RoomChecker interface.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 15, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @deprecated contains inconsistencies and old model classes
 */
@Deprecated
public class CanPlaceDemoCode implements LaneBasedGTUGenerator.RoomChecker
{
    /** Maximum distance supplied to the minimumHeadway method of the GTUFollowingModel. */
    private static Length maxDistance = new Length(Double.MAX_VALUE, LengthUnit.SI);

    /** Precision requested of the minimumHeadway method of the GTUFollowingModel. */
    private static Length precision = new Length(0.1, LengthUnit.METER);

    /** {@inheritDoc} */
    @Override
    public final Placement canPlace(final SortedSet<HeadwayGTU> leaders, final LaneBasedGTUCharacteristics characteristics,
            final Duration since, final Set<DirectedLanePosition> initialPosition) throws NetworkException
    {
        if (leaders.isEmpty())
        {
            Speed speedLimit = initialPosition.iterator().next().getLane().getSpeedLimit(characteristics.getGTUType());
            return new Placement(Speed.min(characteristics.getMaximumSpeed(), speedLimit), initialPosition);
        }
        // This simple minded implementation returns null if the headway is less than the headway wanted for driving at
        // the current speed of the leader
        Lane lane = null;
        for (DirectedLanePosition dlp : initialPosition)
        {
            if (dlp.getLane().getLaneType().isCompatible(characteristics.getGTUType(), dlp.getGtuDirection()))
            {
                lane = dlp.getLane();
                break;
            }
        }
        if (null == lane)
        {
            throw new NetworkException(
                    "No " + characteristics.getGTUType() + "-compatible lane in initial longitudinal positions");
        }
        // Use the speed limit of the first compatible lane in the initial longitudinal positions.
        Speed speedLimit = lane.getSpeedLimit(characteristics.getGTUType());
        Speed maximumSpeed = characteristics.getMaximumSpeed();
        GTUFollowingModelOld gfm = new IDMOld();
        HeadwayGTU leader = leaders.first();
        if (leader.getDistance()
                .lt(gfm.minimumHeadway(leader.getSpeed(), leader.getSpeed(), precision, maxDistance, speedLimit, maximumSpeed)))
        {
            return Placement.NO;
        }
        return new Placement(leader.getSpeed(), initialPosition);
    }

}
