package org.opentrafficsim.road.gtu.tactical.util.lmrs;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.tactical.TacticalContext;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.lmrs.AbstractIncentivesTacticalPlanner;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.gtu.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Relaxation.RelaxationTrigger;
import org.opentrafficsim.road.network.conflict.Conflict;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public final class LmrsUtil implements LmrsParameters
{

    /** Fixed model time step. */
    public static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** Minimum car-following headway. */
    public static final ParameterTypeDuration TMIN = ParameterTypes.TMIN;

    /** Current car-following headway. */
    public static final ParameterTypeDuration T = ParameterTypes.T;

    /** Maximum car-following headway. */
    public static final ParameterTypeDuration TMAX = ParameterTypes.TMAX;

    /** Headway relaxation time. */
    public static final ParameterTypeDuration TAU = ParameterTypes.TAU;

    /** Maximum critical deceleration, e.g. stop/go at traffic light. */
    public static final ParameterTypeAcceleration BCRIT = ParameterTypes.BCRIT;

    /** Parameter key. */
    private static final Object PARAMETER_KEY = new Object()
    {
        @Override
        public String toString()
        {
            return "LmrsUtil.PARAMETER_KEY";
        }
    };

    /** Parameter key for T. This value might e.g. be set by relaxation, its initialization, and tailgating. */
    public static final Object T_KEY = new Object()
    {
        @Override
        public String toString()
        {
            return "LmrsUtil.T_KEY";
        }
    };

    /**
     * Do not instantiate.
     */
    private LmrsUtil()
    {
        //
    }

    /**
     * Determines a simple representation of an operational plan.
     * @param context tactical information such as parameters and car-following model
     * @param lmrsData LMRS data
     * @param incentives planner with set of incentives
     * @return simple operational plan
     * @throws NetworkException network exception
     * @throws ParameterException parameter exception
     * @throws OperationalPlanException operational plan exception
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static SimpleOperationalPlan determinePlan(final TacticalContextEgo context, final LmrsData lmrsData,
            final AbstractIncentivesTacticalPlanner incentives)
            throws NetworkException, ParameterException, OperationalPlanException
    {
        lmrsData.initStep();

        // obtain objects to get info
        NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.TYPE);
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);

        // regular car-following
        Acceleration a;
        if (lmrsData.isHumanLongitudinalControl())
        {
            lmrsData.getTailgating().tailgate(context);
            if (!leaders.isEmpty() && lmrsData.isNewLeader(leaders.first()))
            {
                context.getCarFollowingModel().getRelaxation().initRelaxation(context.getParameters(), context.getSpeed(),
                        leaders.first(), RelaxationTrigger.NEW_LEADER);
            }
            a = context.getCarFollowingAcceleration();
        }
        else
        {
            a = Acceleration.POS_MAXVALUE;
        }
        lmrsData.setDesiredSpeed(context.getDesiredSpeed());

        // determine lane change desire based on incentives
        Desire desire = getLaneChangeDesire(context, incentives);

        // lane change decision
        LateralDirectionality initiatedOrContinuedLaneChange;
        TurnIndicatorStatus turnIndicatorStatus = null;
        double dFree = context.getParameters().getParameter(DFREE);
        initiatedOrContinuedLaneChange = LateralDirectionality.NONE;
        if (desire.leftIsLargerOrEqual() && desire.left() >= dFree)
        {
            if (acceptLaneChange(context, desire.left(), LateralDirectionality.LEFT, lmrsData.getGapAcceptance()))
            {
                // change left
                initiatedOrContinuedLaneChange = LateralDirectionality.LEFT;
                turnIndicatorStatus = TurnIndicatorStatus.LEFT;
                context.getParameters().setClaimedParameter(DLC, desire.left(), PARAMETER_KEY);
                leaders = neighbors.getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    context.getCarFollowingModel().getRelaxation().initRelaxation(context.getParameters(), context.getSpeed(),
                            leaders.first(), RelaxationTrigger.EGO_LANE_CHANGE);
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a,
                        context.getCarFollowingModel().followingAcceleration(context.getParameters(), context.getSpeed(),
                                context.getSpeedLimits(), context.getMaximumSpeed(), neighbors.getLeaders(RelativeLane.LEFT)));
            }
        }
        else if (!desire.leftIsLargerOrEqual() && desire.right() >= dFree)
        {
            if (acceptLaneChange(context, desire.right(), LateralDirectionality.RIGHT, lmrsData.getGapAcceptance()))
            {
                // change right
                initiatedOrContinuedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
                context.getParameters().setClaimedParameter(DLC, desire.right(), PARAMETER_KEY);
                leaders = neighbors.getLeaders(RelativeLane.RIGHT);
                if (!leaders.isEmpty())
                {
                    context.getCarFollowingModel().getRelaxation().initRelaxation(context.getParameters(), context.getSpeed(),
                            leaders.first(), RelaxationTrigger.EGO_LANE_CHANGE);
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a,
                        context.getCarFollowingModel().followingAcceleration(context.getParameters(), context.getSpeed(),
                                context.getSpeedLimits(), context.getMaximumSpeed(), neighbors.getLeaders(RelativeLane.RIGHT)));
            }
        }

        context.getParameters().setClaimedParameter(DLEFT, desire.left(), PARAMETER_KEY);
        context.getParameters().setClaimedParameter(DRIGHT, desire.right(), PARAMETER_KEY);
        if (initiatedOrContinuedLaneChange.isNone())
        {
            // take action if we cannot change lane
            Acceleration aSync;

            // synchronize
            double dSync = context.getParameters().getParameter(DSYNC);
            lmrsData.setSynchronizationState(Synchronizable.State.NONE);
            if (desire.leftIsLargerOrEqual() && desire.left() >= dSync)
            {
                if (desire.left() >= context.getParameters().getParameter(DCOOP))
                {
                    // switch on left indicator
                    turnIndicatorStatus = TurnIndicatorStatus.LEFT;
                    lmrsData.setSynchronizationState(Synchronizable.State.INDICATING);
                }
                else
                {
                    lmrsData.setSynchronizationState(Synchronizable.State.SYNCHRONIZING);
                }
                aSync = lmrsData.getSynchronization().synchronize(context, desire.left(), LateralDirectionality.LEFT,
                        initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync);
            }
            else if (!desire.leftIsLargerOrEqual() && desire.right() >= dSync)
            {
                if (desire.right() >= context.getParameters().getParameter(DCOOP))
                {
                    // switch on right indicator
                    turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
                    lmrsData.setSynchronizationState(Synchronizable.State.INDICATING);
                }
                else
                {
                    lmrsData.setSynchronizationState(Synchronizable.State.SYNCHRONIZING);
                }
                aSync = lmrsData.getSynchronization().synchronize(context, desire.right(), LateralDirectionality.RIGHT,
                        initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync);
            }

            // cooperate
            aSync = lmrsData.getCooperation().cooperate(context, LateralDirectionality.LEFT, lmrsData, desire);
            a = applyAcceleration(a, aSync);
            aSync = lmrsData.getCooperation().cooperate(context, LateralDirectionality.RIGHT, lmrsData, desire);
            a = applyAcceleration(a, aSync);

            // relaxation
            context.getCarFollowingModel().getRelaxation().relax(context.getParameters());
        }

        SimpleOperationalPlan simplePlan = new SimpleOperationalPlan(a, context.getParameters().getParameter(ParameterTypes.DT),
                initiatedOrContinuedLaneChange);

        // set turn indicator
        if (turnIndicatorStatus != null)
        {
            context.addIntent(turnIndicatorStatus, Length.ZERO);
        }
        else
        {
            // lingering indicator during lane change
            TimeStampedObject<TurnIndicatorStatus> lingeringLaneChangeIndicator = lmrsData.getInitiatedLaneChange();
            if (lingeringLaneChangeIndicator != null)
            {
                if (lingeringLaneChangeIndicator.timestamp().si > context.getTime().si
                        - context.getParameters().getParameter(ParameterTypes.LCDUR).si)
                {
                    context.addIntent(lingeringLaneChangeIndicator.object(), Length.ZERO);
                }
                else
                {
                    lmrsData.setInitiatedLaneChange(null);
                }
            }
        }
        if (simplePlan.isLaneChange())
        {
            turnIndicatorStatus =
                    simplePlan.getLaneChangeDirection().isLeft() ? TurnIndicatorStatus.LEFT : TurnIndicatorStatus.RIGHT;
            lmrsData.setInitiatedLaneChange(new TimeStampedObject<TurnIndicatorStatus>(turnIndicatorStatus, context.getTime()));
        }
        return simplePlan;

    }

    /**
     * Minimizes the acceleration and sets the synchronization state if applicable.
     * @param a previous acceleration
     * @param aNew new acceleration
     * @return minimized acceleration
     */
    private static Acceleration applyAcceleration(final Acceleration a, final Acceleration aNew)
    {
        if (a.si < aNew.si)
        {
            return a;
        }
        return aNew;
    }

    /**
     * Determines lane change desire for the given GtU. Mandatory desire is deduced as the maximum of a set of mandatory
     * incentives, while voluntary desires are added. Depending on the level of mandatory lane change desire, voluntary desire
     * may be included partially. If both are positive or negative, voluntary desire is fully included. Otherwise, voluntary
     * desire is less considered within the range dSync &lt; |mandatory| &lt; dCoop. The absolute value is used as large
     * negative mandatory desire may also dominate voluntary desire.
     * @param context tactical information such as parameters and car-following model
     * @param incentives planner with set of incentives
     * @return lane change desire for gtu
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException if there is no mandatory incentive, the model requires at least one
     */
    public static Desire getLaneChangeDesire(final TacticalContextEgo context,
            final AbstractIncentivesTacticalPlanner incentives) throws ParameterException, OperationalPlanException
    {
        double dSync = context.getParameters().getParameter(DSYNC);
        double dCoop = context.getParameters().getParameter(DCOOP);

        Desire mandatoryDesire = incentives.getMandatoryDesire(context);
        Desire voluntaryDesire = incentives.getVoluntaryDesire(context);
        double thetaA = context.getParameters().getParameter(LAMBDA_V);
        double leftThetaV = getThetaV(mandatoryDesire.left(), voluntaryDesire.left(), dSync, dCoop);
        double rightThetaV = getThetaV(mandatoryDesire.right(), voluntaryDesire.right(), dSync, dCoop);
        return new Desire(mandatoryDesire.left() + thetaA * leftThetaV * voluntaryDesire.left(),
                mandatoryDesire.right() + thetaA * rightThetaV * voluntaryDesire.right());
    }

    /**
     * Obtains theta, which is the level by which voluntary incentives are considered, given the prevalence of mandatory desire.
     * @param mandatoryDesire mandatory desire
     * @param voluntaryDesire voluntary desire
     * @param dSync synchronization threshold
     * @param dCoop cooperation threshold
     * @return theta
     */
    private static double getThetaV(final double mandatoryDesire, final double voluntaryDesire, final double dSync,
            final double dCoop)
    {
        double leftThetaV = 0;
        double dLeftMandatoryAbs = Math.abs(mandatoryDesire);

        if (dLeftMandatoryAbs <= dSync || mandatoryDesire * voluntaryDesire >= 0)
        {
            // low mandatory desire, or same sign
            leftThetaV = 1;
        }
        else if (dSync < dLeftMandatoryAbs && dLeftMandatoryAbs < dCoop && mandatoryDesire * voluntaryDesire < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            leftThetaV = (dCoop - dLeftMandatoryAbs) / (dCoop - dSync);
        }
        return leftThetaV;
    }

    /**
     * Determine whether a lane change is acceptable (gap, lane markings, etc.).
     * @param context tactical information such as parameters and car-following model
     * @param desire lane change desire
     * @param lat lateral direction for synchronization
     * @param gapAcceptance gap-acceptance model
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    static boolean acceptLaneChange(final TacticalContextEgo context, final double desire, final LateralDirectionality lat,
            final GapAcceptance gapAcceptance) throws ParameterException, OperationalPlanException
    {
        // legal?
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.TYPE);
        if (infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).si <= 0.0)
        {
            return false;
        }

        // safe regarding neighbors?
        double consideredDesire = context.getLaneChangeDirection().equals(lat) ? 1.0 : desire;
        if (!gapAcceptance.acceptGap(context, consideredDesire, lat))
        {
            return false;
        }

        // intersection causes for deceleration
        Optional<IntersectionPerception> intersection =
                context.getPerception().getPerceptionCategoryOptional(IntersectionPerception.TYPE);
        if (intersection.isPresent())
        {
            RelativeLane lane = RelativeLane.of(lat, 1);

            // // conflicts alongside?
            // if ((lat.isLeft() && intersection.isAlongsideConflictLeft())
            // || (lat.isRight() && intersection.isAlongsideConflictRight()))
            // {
            // return false;
            // }
            // if (quickIntersectionScan(params, sli, cfm, ownSpeed, lat, intersection).lt(params.getParameter(BCRIT).neg()))
            // {
            // return false;
            // }

            // conflicts
            PerceptionCollectable<PerceivedConflict, Conflict> conflicts = intersection.get().getConflicts(lane);
            double bCrit = -context.getParameters().getParameter(ParameterTypes.BCRIT).si;
            try
            {
                Acceleration a = ConflictUtil.approachConflicts(context, new ConflictPlans(), lane, Length.ZERO, false);
                if (a.si < bCrit)
                {
                    return false;
                }
                // gap-acceptance on merge conflicts
                // TODO: this approach is a hack
                for (PerceivedConflict conflict : conflicts)
                {
                    if (conflict.isMerge() && conflict.getDistance().si < 10.0)
                    {
                        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> down = conflict.getDownstreamConflictingGtus();
                        if (!down.isEmpty() && down.first().getKinematics().getOverlap().isParallel())
                        {
                            return false; // GTU on conflict
                        }
                        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> up = conflict.getUpstreamConflictingGtus();
                        if (!up.isEmpty() && up.first().getKinematics().getOverlap().isParallel())
                        {
                            return false; // GTU on conflict
                        }
                    }
                }
            }
            catch (GtuException exception)
            {
                throw new OperationalPlanException(exception);
            }
            conflicts = intersection.get().getConflicts(RelativeLane.CURRENT);
            for (PerceivedConflict conflict : conflicts)
            {
                if (conflict.getLane().getLink().equals(conflict.getConflictingLink()))
                {
                    if (conflict.isMerge() && conflict.getDistance().le0()
                            && conflict.getDistance().neg().gt(conflict.getLength()))
                    {
                        return false; // partially past the merge; adjacent lane might be ambiguous
                    }
                    else if (conflict.isSplit() && conflict.getDistance().le0()
                            && conflict.getDistance().neg().lt(context.getLength()))
                    {
                        return false; // partially before the split; adjacent lane might be ambiguous
                    }
                }
            }

            // traffic lights
            Iterable<PerceivedTrafficLight> trafficLights = intersection.get().getTrafficLights(lane);
            for (PerceivedTrafficLight trafficLight : trafficLights)
            {
                if (trafficLight.getTrafficLightColor().isRedOrYellow())
                {
                    Acceleration a = TrafficLightUtil.respondToTrafficLight(context, trafficLight);
                    if (a.si < bCrit)
                    {
                        return false;
                    }
                }
            }
        }

        // cut-in vehicles from 2nd lane
        RelativeLane lane = RelativeLane.of(lat, 2);
        double b = -context.getParameters().getParameter(ParameterTypes.B).si;
        for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.TYPE).getLeaders(lane))
        {
            if (leader.getManeuver().isChangingLane(lat.flip()) && CarFollowingUtil.followSingleLeader(context, leader).si < b)
            {
                return false;
            }
        }

        return true;

    }

    /**
     * Returns a quickly determined acceleration to consider on an adjacent lane, following from conflicts and traffic lights.
     * @param context tactical information such as parameters and car-following model
     * @param lat lateral direction for synchronization
     * @param intersection intersection perception
     * @return a quickly determined acceleration to consider on an adjacent lane, following from conflicts and traffic lights
     * @throws ParameterException if a parameter is not defined
     */
    /*-
    private static Acceleration quickIntersectionScan(final TacticalContextEgo context, final LateralDirectionality lat,
            final IntersectionPerception intersection) throws ParameterException
    {
        Acceleration a = Acceleration.POSITIVE_INFINITY;
        if (intersection != null)
        {
            RelativeLane lane = lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT;
            Iterable<PerceivedConflict> iterable = intersection.getConflicts(lane);
            if (iterable != null)
            {
                Iterator<PerceivedConflict> conflicts = iterable.iterator();
                if (conflicts.hasNext())
                {
                    a = Acceleration.min(a,
                            CarFollowingUtil.followSingleLeader(context, conflicts.next().getDistance(), Speed.ZERO));
                }
                Iterator<PerceivedTrafficLight> trafficLights = intersection.getTrafficLights(lane).iterator();
                if (trafficLights.hasNext())
                {
                    PerceivedTrafficLight trafficLight = trafficLights.next();
                    if (trafficLight.getTrafficLightColor().isRedOrYellow())
                    {
                        a = Acceleration.min(a,
                                CarFollowingUtil.followSingleLeader(context, trafficLight.getDistance(), Speed.ZERO));
                    }
                }
            }
        }
        return a;
    }
    */

    /**
     * Determine acceleration from car-following with relaxation. This applies only desired headway relaxation (i.e. on
     * {@code T}) and excludes relaxation on speed difference (i.e. {@code KAPPA}). The reason for this is that acceleration to
     * synchronize should be slightly exaggerated compared to acceleration that would apply relaxation on both {@code T} and
     * {@code KAPPA}. This is because speed difference is required to adjust position, which can lead to the gap that this
     * function is accelerating towards being rejected. A never reached moving target results.
     * @param context tactical context
     * @param distance distance from follower to leader
     * @param leaderSpeed speed of leader
     * @param relaxation level of relaxation
     * @return acceleration from relaxed car-following
     * @throws ParameterException in case of a missing parameter
     */
    public static Acceleration synchronizationAcceleration(final TacticalContext context, final Length distance,
            final Speed leaderSpeed, final double relaxation) throws ParameterException
    {
        Relaxation rel = context.getCarFollowingModel().getRelaxation();
        rel.setRelaxation(context.getParameters(), relaxation);
        Acceleration a = CarFollowingUtil.followSingleLeader(context, distance, leaderSpeed);
        rel.resetRelaxation(context.getParameters());
        return a;
    }

}
