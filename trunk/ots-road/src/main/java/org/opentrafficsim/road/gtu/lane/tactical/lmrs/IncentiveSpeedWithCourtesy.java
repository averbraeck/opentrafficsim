package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeedWithCourtesy implements VoluntaryIncentive
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Acceleration parameter type. */
    protected static final ParameterTypeAcceleration A = ParameterTypes.A;

    /** Anticipation speed difference parameter type. */
    protected static final ParameterTypeSpeed VGAIN = LmrsParameters.VGAIN;

    /** Anticipated speed by vehicles in the left lane. */
    private Map<RelativeLane, Double> antFromLeft = new HashMap<>();

    /** Anticipated speed by vehicles in the lane. */
    private Map<RelativeLane, Double> antInLane = new HashMap<>();

    /** Anticipated speed by vehicles in the right lane. */
    private Map<RelativeLane, Double> antFromRight = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {

        this.antFromLeft.clear();
        this.antInLane.clear();
        this.antFromRight.clear();

        // zero if no lane change is possible
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        EgoPerception ego = perception.getPerceptionCategory(EgoPerception.class);
        IntersectionPerception inter = perception.getPerceptionCategory(IntersectionPerception.class);
        double leftDist = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si;
        double rightDist = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si;

        // gather some info
        Speed vCur = anticipationSpeed(RelativeLane.CURRENT, parameters, carFollowingModel, neighbors, infra, ego, inter);
        Speed vGain = parameters.getParameter(VGAIN);

        // calculate aGain (default 1; lower as acceleration is higher than 0)
        Dimensionless aGain;
        /*
         * Instead of instantaneous car-following acceleration, use current acceleration; only then is the acceleration factor
         * consistent with possible lane change incentives pertaining to speed (which used to be only vehicles in the original
         * LMRS, but can be any number of reasons here. E.g. traffic lights, conflicts, etc.)
         */
        Acceleration aCur = perception.getPerceptionCategory(EgoPerception.class).getAcceleration();
        if (aCur.si > 0)
        {
            Acceleration a = parameters.getParameter(A);
            aGain = a.minus(aCur).divideBy(a);
        }
        else
        {
            aGain = new Dimensionless(1, DimensionlessUnit.SI);
        }

        // left desire
        Dimensionless dLeft;
        if (leftDist > 0.0 && infra.getCrossSection().contains(RelativeLane.LEFT))
        {
            Speed vLeft = anticipationSpeed(RelativeLane.LEFT, parameters, carFollowingModel, neighbors, infra, ego, inter);
            dLeft = aGain.multiplyBy(vLeft.minus(vCur)).divideBy(vGain);
        }
        else
        {
            dLeft = Dimensionless.ZERO;
        }

        // right desire
        Dimensionless dRight;
        if (rightDist > 0.0 && infra.getCrossSection().contains(RelativeLane.RIGHT))
        {
            Speed vRight = anticipationSpeed(RelativeLane.RIGHT, parameters, carFollowingModel, neighbors, infra, ego, inter);
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
     * Returns the anticipation speed in a lane. GTU's in adjacent lanes with their indicator towards the given lane are
     * included in the evaluation.
     * @param lane RelativeLane; lane to assess
     * @param params Parameters; parameters
     * @param cfm Parameters; car-following model
     * @param neighbors NeighborsPerception; neighbors perception
     * @param infra InfrastructurePerception; infrastructure perception
     * @param ego EgoPerception; ego perception
     * @param inter IntersectionPerception; intersection perception, may be {@code null}
     * @return Speed; anticipation speed in lane
     * @throws ParameterException on missing parameter
     */
    private Speed anticipationSpeed(final RelativeLane lane, final Parameters params, final CarFollowingModel cfm,
            final NeighborsPerception neighbors, final InfrastructurePerception infra, final EgoPerception ego,
            final IntersectionPerception inter) throws ParameterException
    {
        if (!this.antInLane.containsKey(lane))
        {
            anticipateSpeedFromLane(lane, params, cfm, neighbors, infra, ego, inter);
        }
        double v = this.antInLane.get(lane);
        if (infra.getCrossSection().contains(lane.getLeft()))
        {
            if (!this.antFromLeft.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getLeft(), params, cfm, neighbors, infra, ego, inter);
            }
            double fromLeft = this.antFromLeft.get(lane);
            v = v < fromLeft ? v : fromLeft;
        }
        if (infra.getCrossSection().contains(lane.getRight()))
        {
            if (!this.antFromRight.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getRight(), params, cfm, neighbors, infra, ego, inter);
            }
            double fromRight = this.antFromRight.get(lane);
            v = v < fromRight ? v : fromRight;
        }
        return Speed.createSI(v);
    }

    /**
     * Anticipate speed from the GTUs in one lane. This affects up to 3 lanes, all this information is stored.
     * @param lane RelativeLane; lane to assess
     * @param params Parameters; parameters
     * @param cfm Parameters; car-following model
     * @param neighbors NeighborsPerception; neighbors perception
     * @param infra InfrastructurePerception; infrastructure perception
     * @param ego EgoPerception; ego perception
     * @param inter IntersectionPerception; intersection perception, may be {@code null}
     * @throws ParameterException on missing parameter
     */
    private void anticipateSpeedFromLane(final RelativeLane lane, final Parameters params, final CarFollowingModel cfm,
            final NeighborsPerception neighbors, final InfrastructurePerception infra, final EgoPerception ego,
            final IntersectionPerception inter) throws ParameterException
    {
        SpeedLimitInfo sli = infra.getSpeedLimitProspect(lane).getSpeedLimitInfo(Length.ZERO);
        double desiredSpeed = cfm.desiredSpeed(params, sli).si;
        double x0 = params.getParameter(LOOKAHEAD).si;
        double vLeft = desiredSpeed;
        double vCur = desiredSpeed;
        double vRight = desiredSpeed;
        if (!lane.isCurrent() && lane.getNumLanes() < 2)
        {
            for (HeadwayGTU headwayGTU : neighbors.getLeaders(lane))
            {
                double single = anticipateSingle(desiredSpeed, x0, headwayGTU);
                vCur = vCur < single ? vCur : single;
                if (headwayGTU.isLeftTurnIndicatorOn())
                {
                    vLeft = vLeft < single ? vLeft : single;
                }
                else if (headwayGTU.isRightTurnIndicatorOn())
                {
                    vRight = vRight < single ? vRight : single;
                }
            }
        }
        else
        {
            // leaders on the current lane with indicator to an adjacent lane are not considered
            for (HeadwayGTU headwayGTU : neighbors.getLeaders(lane))
            {
                double single = anticipateSingle(desiredSpeed, x0, headwayGTU);
                vCur = vCur < single ? vCur : single;
            }
        }
        this.antFromLeft.put(lane.getRight(), vRight);
        this.antInLane.put(lane, vCur);
        this.antFromRight.put(lane.getLeft(), vLeft);
    }

    /**
     * Anticipate a single leader by possibly lowering the anticipation speed.
     * @param desiredSpeed desired speed on anticipated lane
     * @param x0 look-ahead distance
     * @param headway leader/object to anticipate
     * @return possibly lowered anticipation speed
     */
    private double anticipateSingle(final double desiredSpeed, final double x0, final Headway headway)
    {
        Speed speed = headway.getSpeed();
        double v = speed == null ? 0.0 : speed.si;
        if (v > desiredSpeed || headway.getDistance().si > x0)
        {
            return desiredSpeed;
        }
        double f = headway.getDistance().si / x0;
        return f * v + (1 - f) * desiredSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSpeedWithCourtesy";
    }

}
