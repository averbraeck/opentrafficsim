package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Common code for a family of lane change models like in M. Treiber and A. Kesting <i>Traffic Flow Dynamics</i>,
 * Springer-Verlag Berlin Heidelberg 2013, pp 239-244.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractDirectedLaneChangeModel implements DirectedLaneChangeModel
{
    /** Attempt to overcome rounding errors. */
    private static Acceleration extraThreshold = new Acceleration(0.000001, AccelerationUnit.SI);

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;
    
    /** the perception. */
    private final LanePerception perception;

    /**
     * Construct a DirectedLaneChangeModel.
     * @param perception the perception.
     */
    public AbstractDirectedLaneChangeModel(final LanePerception perception)
    {
        this.perception = perception;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedLaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
            final LateralDirectionality direction, final Collection<Headway> sameLaneGTUs,
            final Collection<Headway> otherLaneGTUs, final Length maxDistance, final Speed speedLimit,
            final Acceleration otherLaneRouteIncentive, final Acceleration laneChangeThreshold, final Duration laneChangeTime)
            throws GTUException, ParameterException, OperationalPlanException
    {
        Lane lane = gtu.getReferencePosition().getLane();
        Length longitudinalPosition = gtu.getReferencePosition().getPosition();
        Lane otherLane = getPerception().getPerceptionCategory(DefaultSimplePerception.class).bestAccessibleAdjacentLane(lane,
                direction, longitudinalPosition);
        GTUFollowingModelOld gtuFollowingModel = (GTUFollowingModelOld) gtu.getTacticalPlanner().getCarFollowingModel();
        if (null == gtuFollowingModel)
        {
            throw new GTUException(gtu + " has null GTUFollowingModel");
        }
        DualAccelerationStep thisLaneAccelerationSteps =
                gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, maxDistance, speedLimit, laneChangeTime);
        if (thisLaneAccelerationSteps.getLeaderAcceleration().getSI() < -9999)
        {
            System.out.println(gtu + " has a problem: straightAccelerationSteps.getLeaderAcceleration().getSI() < -9999");
        }
        Acceleration straightA = applyDriverPersonality(thisLaneAccelerationSteps).plus(laneChangeThreshold);
        DualAccelerationStep otherLaneAccelerationSteps = null == otherLane ? null
                : gtuFollowingModel.computeDualAccelerationStep(gtu, otherLaneGTUs, maxDistance, speedLimit, laneChangeTime);
        if (null != otherLaneAccelerationSteps && otherLaneAccelerationSteps.getFollowerAcceleration()
                .getSI() < -gtu.getParameters().getParameter(B).getSI())
        {
            otherLane = null; // do not change to the other lane
        }
        Acceleration otherLaneAcceleration = (null == otherLane) ? null : applyDriverPersonality(otherLaneAccelerationSteps);
        if (null == otherLaneAcceleration)
        {
            // No lane change possible; this is definitely the easy case
            return new DirectedLaneMovementStep(thisLaneAccelerationSteps.getLeaderAccelerationStep(), null);
        }
        // A merge to the other lane is possible
        if (DoubleScalar.plus(otherLaneAcceleration, otherLaneRouteIncentive).plus(extraThreshold).ge(straightA))
        {
            // Merge to the other lane
            return new DirectedLaneMovementStep(otherLaneAccelerationSteps.getLeaderAccelerationStep(), direction);
        }
        else
        {
            // Stay in current lane
            return new DirectedLaneMovementStep(thisLaneAccelerationSteps.getLeaderAccelerationStep(), null);
        }
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerationStep DualAccelerationStep; the DualAccelerationStep that contains the AccelerationStep that the
     *            reference GTU will make and the AccelerationStep that the (new) follower GTU will make
     * @return Acceleration; the acceleration that the personality of the driver uses (in a comparison to a similarly computed
     *         acceleration in the non-, or different-lane-changed state) to decide if a lane change should be performed
     */
    public abstract Acceleration applyDriverPersonality(DualAccelerationStep accelerationStep);

    /** {@inheritDoc} */
    @Override
    public final LanePerception getPerception()
    {
        return this.perception;
    }
}
