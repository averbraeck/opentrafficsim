package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.unit.DimensionlessUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire for speed. The anticipation speed in the current and adjacent lanes are compared. The larger
 * the difference, the larger the lane change desire. For negative differences, negative desire results. Anticipation speed
 * involves the most critical vehicle considered to be in a lane. Vehicles are more critical if their speed is lower, and if
 * they are closer. The set of vehicles considered to be on a lane includes drivers on adjacent lanes of the considered lane,
 * with a lane change desire towards the considered lane above a certain threshold. If such vehicles have low speeds (i.e.
 * vehicle accelerating to merge), this may result in a courtesy lane change, or in not changing lane out of courtesy from the
 * 2nd lane of the mainline. Vehicles on the current lane of the driver, are not considered on adjacent lanes. This would
 * maintain a large speed difference between the lanes where all drivers do not change lane as they consider leading vehicles to
 * be on the adjacent lane, lowering the anticipation speed on the adjacent lane. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.<br>
 * <br>
 * <b>Note:</b> This incentive includes speed, and a form of courtesy. It should therefore not be combined with incentives
 * solely for speed, or solely for courtesy.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeedWithCourtesy implements VoluntaryIncentive
{

    /** Acceleration parameter type. */
    protected static final ParameterTypeAcceleration A = ParameterTypes.A;

    /** Anticipation speed difference parameter type. */
    protected static final ParameterTypeSpeed VGAIN = LmrsParameters.VGAIN;

    /** Speed threshold below which traffic is considered congested. */
    protected static final ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {

        // zero if no lane change is possible
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        TrafficPerception traffic = perception.getPerceptionCategory(TrafficPerception.class);
        EgoPerception ego = perception.getPerceptionCategory(EgoPerception.class);
        double leftDist = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si;
        double rightDist = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si;

        // gather some info
        Speed vCur = traffic.getSpeed(RelativeLane.CURRENT);
        Speed vGain = parameters.getParameter(VGAIN);

        // calculate aGain (default 1; lower as acceleration is higher than 0)
        Dimensionless aGain;
        /*
         * Instead of instantaneous car-following acceleration, use current acceleration; only then is the acceleration factor
         * consistent with possible lane change incentives pertaining to speed (which used to be only vehicles in the original
         * LMRS, but can be any number of reasons here. E.g. traffic lights, conflicts, etc.)
         */
        Acceleration aCur = ego.getAcceleration();

        /*
         * The idea to let aCur simply be the current acceleration is wrong; aCur -should- only describe the car-following
         * relation, as this describes a sense of sticking to a lane as the leader is getting away.
         */
        aCur = Try.assign(() -> perception.getGtu().getCarFollowingAcceleration(), "Could not obtain the GTU.");
        if (aCur.si > 0)
        {
            Acceleration a = parameters.getParameter(A);
            aGain = a.minus(aCur).divide(a);
        }
        else
        {
            aGain = new Dimensionless(1, DimensionlessUnit.SI);
        }

        // left desire
        double dLeft;
        if (leftDist > 0.0 && infra.getCrossSection().contains(RelativeLane.LEFT))
        {
            Speed vLeft = traffic.getSpeed(RelativeLane.LEFT);
            dLeft = aGain.si * (vLeft.si - vCur.si) / vGain.si;
        }
        else
        {
            dLeft = 0.0;
        }

        // right desire
        double dRight;
        if (rightDist > 0.0 && infra.getCrossSection().contains(RelativeLane.RIGHT))
        {
            Speed vRight = traffic.getSpeed(RelativeLane.RIGHT);
            if (vCur.si >= parameters.getParameter(VCONG).si)
            {
                dRight = aGain.si * Math.min(vRight.si - vCur.si, 0) / vGain.si;
            }
            else
            {
                dRight = aGain.si * (vRight.si - vCur.si) / vGain.si;
            }
        }
        else
        {
            dRight = 0.0;
        }

        // return desire
        return new Desire(dLeft, dRight);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSpeedWithCourtesy";
    }

}
