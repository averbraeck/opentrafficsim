package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.OvertakingManeuver.OvertakingManeuver;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.Cruise.BasicHuman.RightHandRuleManeuver.RightHandRuleManeuver;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.HumanDrivenVehicle;

/**
 * Represents the cruise driving task for freeway driving. This task is always active as long as the vehicle is on the freeway.
 * It considers overtaking and right-hand rule maneuvers based on the current traffic situation.
 */
public class BasicHuman extends DrivingTask
{

    private final Speed overtakingSpeedGainThreshold = new Speed(5.0, SpeedUnit.KM_PER_HOUR); // Speed gain threshold for
                                                                                              // overtaking

    private final Speed maxSpeedGain = new Speed(20.0, SpeedUnit.KM_PER_HOUR); // Maximum speed gain for overtaking

    private final Length overtakingLeftLaneDistanceThreshold = new Length(200.0, LengthUnit.METER); // Distance threshold for
                                                                                                    // left lane overtaking

    private final Duration righthandFreeDrivingTimeThreshold = new Duration(12.0, DurationUnit.SECOND); // Time threshold for
                                                                                                    // right-hand rule

    private Speed possibleSpeedGain; // Speed gain in left lane

    public BasicHuman(final HumanDrivenVehicle AbstractMirovaVehicle) throws OperationalPlanException
    {
        super(AbstractMirovaVehicle);
        this.possibleSpeedGain = Speed.NaN; // Initialize possible speed gain

    }

    /**
     * Returns boolean if driving task should be further considered in decision over next action. Always on as long as vehicle
     * is on freeway (returns true, because we do not consider other scenarios at the moment).
     */
    @Override
    public boolean getActivation()
    {
        return true;
    }

    /**
     * Placeholder for utility calculation.
     */
    @Override
    public double calculateUtility()
    {
        // Not implemented
        return 0.0;
    }

    /**
     * Decides the appropriate maneuver advice for cruising. Considers overtaking and right-hand rule maneuvers.
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    @Override
    public ManeuverPattern decideManeuverPattern() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {

        if (conditionDesireToOvertake() && conditionLanechangeSafety(LateralDirectionality.LEFT, 1.0))
        {
            // Check if overtaking is desired and safe
            return new OvertakingManeuver(this);
        }

        else if (conditionRightHandFreeDrivingTime() && conditionLanechangeSafety(LateralDirectionality.RIGHT, 0.75))
        {
            return new RightHandRuleManeuver(this);
        }

        return null;
    }

    /**
     * Checks whether the conditions for an overtaking maneuver are fulfilled.
     * <p>
     * Conditions:
     * <ul>
     * <li>There is a vehicle ahead within perception range.</li>
     * <li>The speed advantage is sufficiently large.</li>
     * <li>The vehicle is not already on the leftmost lane.</li>
     * </ul>
     * @return true if overtaking is reasonable and possible, otherwise false
     * @throws ParameterException
     */
    public boolean conditionDesireToOvertake() throws ParameterException
    {

        if (getTargetLaneRemainingDistance(LateralDirectionality.LEFT).si >= this.overtakingLeftLaneDistanceThreshold.si
                && getPossibleSpeedGain(RelativeLane.LEFT).si > this.overtakingSpeedGainThreshold.si)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks if a left lane change is safe.
     * * @param laneChangeDirection The direction of the lane change (LEFT).
     * @param reducedAcceptedDecelerationFactor Factor to reduce the accepted deceleration for safety checks. Values below 1.0 indicate higher safety and less aggression
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    public boolean conditionLanechangeSafety(final LateralDirectionality laneChangeDirection,
            final double reducedAcceptedDecelerationFactor)
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        if (getAbstractMirovaVehicle().getLaneChangeFollowerDeceleration(laneChangeDirection).ge(getAbstractMirovaVehicle()
                .getGtu().getParameters().getParameter(ParameterTypes.B).times(reducedAcceptedDecelerationFactor)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks if the right-hand rule applies for a lane change to the right.
     * @return true if the right-hand rule condition is met, otherwise false
     * @throws ParameterException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws OperationalPlanException
     */
    private boolean conditionRightHandFreeDrivingTime() throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        Duration freeDrivingTime = getAbstractMirovaVehicle().getFreeDrivingTime(LateralDirectionality.RIGHT);

        if (freeDrivingTime.gt(this.righthandFreeDrivingTimeThreshold))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * Returns the possible speed gain in the target lane.
     * @param targetLane
     * @return the possible speed gain in the target lane
     * @throws ParameterException
     */
    public Speed getPossibleSpeedGain(final RelativeLane targetLane) throws ParameterException
    {
        Speed currentLaneSpeed = getTrafficPerception().getSpeed(RelativeLane.CURRENT);
        Speed targetLaneSpeed = getTrafficPerception().getSpeed(targetLane);
        return targetLaneSpeed.minus(currentLaneSpeed);
    }

    public Speed getOvertakingSpeedGainThreshold()
    {
        return this.overtakingSpeedGainThreshold;
    }

    public Length getOvertakingLeftLaneDistanceThreshold()
    {
        return this.overtakingLeftLaneDistanceThreshold;
    }

    public Length getTargetLaneRemainingDistance(final LateralDirectionality laneChangeDirection)
    {
        return getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, laneChangeDirection);
    }

    public Speed getMaxSpeedGain()
    {
        return this.maxSpeedGain;
    }
}
