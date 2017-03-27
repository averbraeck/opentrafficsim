package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeSpeed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Determines lane change desire for speed. The anticipation speed in the current and adjacent lanes are compared. The larger
 * the difference, the larger the lane change desire. For negative differences, negative desire results. Anticipation speed
 * involves the the most critical vehicle considered to be in a lane. Vehicles are more critical if their speed is lower, and if
 * they are closer. The set of vehicles considered to be on a lane includes drivers on adjacent lanes of the considered lane,
 * with a lane change desire towards the considered lane above a certain certain threshold. If such vehicles have low speeds
 * (i.e. vehicle accelerating to merge), this may result in a courtesy lane change, or in not changing lane out of courtesy from
 * the 2nd lane of the mainline. Vehicle on the current lane of the driver, are not considered on adjacent lanes. This would
 * maintain a large speed difference between the lanes where all drivers do not change lane as they consider leading vehicles to
 * be on the adjacent lane, lowering the anticipation speed on the adjacent lane. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.<br>
 * <br>
 * <b>Note:</b> This incentive includes speed, and a form of courtesy. It should therefore not be combined with incentives
 * solely for speed, or solely for courtesy.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeedWithCourtesy implements VoluntaryIncentive
{

    /** Anticipation speed difference at full lane change desired. */
    public static final ParameterTypeSpeed VGAIN =
            new ParameterTypeSpeed("vGain", "Anticipation speed difference at " + "full lane change desired.",
                    new Speed(69.6, SpeedUnit.KM_PER_HOUR), AbstractParameterType.Check.POSITIVE);

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Desire mandatoryDesire,
            final Desire voluntaryDesire) throws ParameterException, OperationalPlanException
    {

        // zero if no lane change is possible
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getLegalLaneChangePossibility(RelativeLane.CURRENT,
                LateralDirectionality.LEFT).si == 0
                && perception.getPerceptionCategory(InfrastructurePerception.class)
                        .getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si == 0)
        {
            return new Desire(0, 0);
        }

        // gather some info
        Speed vCur = anticipationSpeed(RelativeLane.CURRENT, behavioralCharacteristics, perception, carFollowingModel);
        Speed vGain = behavioralCharacteristics.getParameter(VGAIN);

        // calculate aGain (default 1; lower as acceleration is higher than 0)
        Dimensionless aGain;
        /*
         * Instead of instantaneous car-following acceleration, use current acceleration; only then is the acceleration factor
         * consistent with possible lane change incentives pertaining to speed (which used to be only vehicles in the original
         * LMRS, but can be any number of reason here. E.g. traffic lights, conflicts, etc.)
         */
        Acceleration aCur = perception.getPerceptionCategory(EgoPerception.class).getAcceleration();
        // SortedSet<AbstractHeadwayGTU> leaders =
        // perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
        // SpeedLimitInfo speedLimitInfo = perception.getPerceptionCategory(InfrastructurePerception.class)
        // .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        // Acceleration aCur = CarFollowingUtil.followLeaders(carFollowingModel, behavioralCharacteristics,
        // perception.getPerceptionCategory(EgoPerception.class).getSpeed(), speedLimitInfo, leaders);
        if (aCur.si > 0)
        {
            Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
            aGain = a.minus(aCur).divideBy(a);
        }
        else
        {
            aGain = new Dimensionless(1, DimensionlessUnit.SI);
        }

        // left desire
        Dimensionless dLeft;
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getCrossSection().contains(RelativeLane.LEFT))
        {
            Speed vLeft = anticipationSpeed(RelativeLane.LEFT, behavioralCharacteristics, perception, carFollowingModel);
            dLeft = aGain.multiplyBy(vLeft.minus(vCur)).divideBy(vGain);
        }
        else
        {
            dLeft = Dimensionless.ZERO;
        }

        // right desire
        Dimensionless dRight;
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getCrossSection().contains(RelativeLane.RIGHT))
        {
            Speed vRight = anticipationSpeed(RelativeLane.RIGHT, behavioralCharacteristics, perception, carFollowingModel);
            dRight = aGain.multiplyBy(vRight.minus(vCur)).divideBy(vGain);
        }
        else
        {
            dRight = Dimensionless.ZERO;
        }

        // return desire
        return new Desire(dLeft, dRight);
    }

    /**
     * Determine the anticipation speed on the given lane. This depends on leading vehicles, leading vehicles in adjacent lanes
     * with their indicator to this lane, and conflicts.
     * @param lane lane to anticipate the speed on
     * @param bc behavioral characteristics
     * @param perception perception
     * @param cfm car-following model, used for the desired speed
     * @return anticipation speed on lane
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private Speed anticipationSpeed(final RelativeLane lane, final BehavioralCharacteristics bc,
            final LanePerception perception, final CarFollowingModel cfm) throws ParameterException, OperationalPlanException
    {

        SpeedLimitInfo sli = perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(lane)
                .getSpeedLimitInfo(Length.ZERO);
        Speed anticipationSpeed = cfm.desiredSpeed(bc, sli);
        Speed desiredSpeed = new Speed(anticipationSpeed);
        Length x0 = bc.getParameter(ParameterTypes.LOOKAHEAD);

        // leaders with right indicators on left lane of considered lane
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getCrossSection().contains(lane.getLeft()))
        {
            for (HeadwayGTU headwayGTU : perception.getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(lane.getLeft()))
            {
                // leaders on the current lane with indicator to an adjacent lane are not considered
                if (headwayGTU.isRightTurnIndicatorOn() && !lane.getLeft().equals(RelativeLane.CURRENT))
                {
                    anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayGTU);
                }
            }
        }

        // leaders with left indicators on right lane of considered lane
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getCrossSection().contains(lane.getRight()))
        {
            for (HeadwayGTU headwayGTU : perception.getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(lane.getRight()))
            {
                // leaders on the current lane with indicator to an adjacent lane are not considered
                if (headwayGTU.isLeftTurnIndicatorOn() && !lane.getRight().equals(RelativeLane.CURRENT))
                {
                    anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayGTU);
                }
            }
        }

        // leaders in the considered lane
        for (HeadwayGTU headwayGTU : perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane))
        {
            anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayGTU);
        }

        // conflicts in the considered lane
        if (perception.contains(DirectIntersectionPerception.class))
        {
            for (HeadwayConflict headwayConflict : perception.getPerceptionCategory(IntersectionPerception.class)
                    .getConflicts(lane))
            {
                Length vehicleLength = perception.getPerceptionCategory(EgoPerception.class).getLength();
                Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
                SortedSet<HeadwayGTU> leaders =
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane);
                if (!headwayConflict.getConflictType().isCrossing())
                {
                    // consider first downstream vehicle on split or merge (ignore others)
                    SortedSet<HeadwayGTU> conflictVehicles = headwayConflict.getDownstreamConflictingGTUs();
                    if (!conflictVehicles.isEmpty() && conflictVehicles.first().isParallel())
                    {
                        HeadwayGTU conflictingGtu = conflictVehicles.first();
                        Length distance = headwayConflict.getDistance().plus(headwayConflict.getLength())
                                .plus(conflictingGtu.getOverlapRear());
                        HeadwayGTU leadingGtu;
                        try
                        {
                            leadingGtu = new HeadwayGTUSimple(conflictingGtu.getId(), conflictingGtu.getGtuType(), distance,
                                    conflictingGtu.getLength());
                        }
                        catch (GTUException exception)
                        {
                            throw new OperationalPlanException("Could not create HeadwayGTUSimple for GTU on split.",
                                    exception);
                        }
                        anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, leadingGtu);
                    }
                }
                switch (headwayConflict.getConflictPriority())
                {
                    case SPLIT:
                    {
                        // nothing
                        break;
                    }
                    case PRIORITY:
                    {
                        if (ConflictUtil.stopForPriorityConflict(headwayConflict, leaders, speed, vehicleLength, bc,
                                new ConflictPlans()))
                        {
                            anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayConflict);
                        }
                        break;
                    }
                    case GIVE_WAY:
                    {
                        Acceleration acceleration = perception.getPerceptionCategory(EgoPerception.class).getAcceleration();
                        if (ConflictUtil.stopForGiveWayConflict(headwayConflict, leaders, speed, acceleration, vehicleLength,
                                bc, sli, cfm))
                        {
                            anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayConflict);
                        }
                        break;
                    }
                    case STOP:
                    {
                        Acceleration acceleration = perception.getPerceptionCategory(EgoPerception.class).getAcceleration();
                        if (ConflictUtil.stopForStopConflict(headwayConflict, leaders, speed, acceleration, vehicleLength, bc,
                                sli, cfm))
                        {
                            anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayConflict);
                        }
                        break;
                    }
                    case ALL_STOP:
                    {
                        if (ConflictUtil.stopForAllStopConflict(headwayConflict, new ConflictPlans()))
                        {
                            anticipationSpeed = anticipateSingle(anticipationSpeed, desiredSpeed, x0, headwayConflict);
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException(
                                "Conflict priority " + headwayConflict.getConflictPriority() + " is unknown.");
                }
            }
        }

        return anticipationSpeed;
    }

    /**
     * Anticipate a single leader by possibly lowering the anticipation speed.
     * @param anticipationSpeed anticipation speed
     * @param desiredSpeed desired speed on anticipated lane
     * @param x0 look-ahead distance
     * @param headway leader/object to anticipate
     * @return possibly lowered anticipation speed
     */
    private Speed anticipateSingle(final Speed anticipationSpeed, final Speed desiredSpeed, final Length x0,
            final Headway headway)
    {
        Speed speed = headway.getSpeed() == null ? Speed.ZERO : headway.getSpeed();
        if (speed.gt(anticipationSpeed) || headway.getDistance().gt(x0))
        {
            return anticipationSpeed;
        }
        Speed vSingle = Speed.interpolate(speed, desiredSpeed, headway.getDistance().si / x0.si);
        return anticipationSpeed.lt(vSingle) ? anticipationSpeed : vSingle;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSpeedWithCourtesy";
    }

}
