package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.Iterator;
import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContext;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AbstractIncentivesTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @throws GtuException gtu exception
     * @throws NetworkException network exception
     * @throws ParameterException parameter exception
     * @throws OperationalPlanException operational plan exception
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static SimpleOperationalPlan determinePlan(final TacticalContextEgo context, final LmrsData lmrsData,
            final AbstractIncentivesTacticalPlanner incentives) throws GtuException, NetworkException, ParameterException
    {
        // obtain objects to get info
        NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);

        // regular car-following
        Acceleration a;
        if (lmrsData.isHumanLongitudinalControl())
        {
            lmrsData.getTailgating().tailgate(context);
            if (!leaders.isEmpty() && lmrsData.isNewLeader(leaders.first()))
            {
                initHeadwayRelaxation(context.getParameters(), leaders.first());
            }
            a = context.getGtu().getCarFollowingAcceleration();
        }
        else
        {
            a = Acceleration.POS_MAXVALUE;
        }

        // determine lane change desire based on incentives
        Desire desire = getLaneChangeDesire(context, incentives);

        // lane change decision
        LateralDirectionality initiatedOrContinuedLaneChange;
        TurnIndicatorIntent turnIndicatorStatus = TurnIndicatorIntent.NONE;
        double dFree = context.getParameters().getParameter(DFREE);
        initiatedOrContinuedLaneChange = LateralDirectionality.NONE;
        turnIndicatorStatus = TurnIndicatorIntent.NONE;
        if (desire.leftIsLargerOrEqual() && desire.left() >= dFree)
        {
            if (acceptLaneChange(context, desire.left(), LateralDirectionality.LEFT, lmrsData.getGapAcceptance()))
            {
                // change left
                initiatedOrContinuedLaneChange = LateralDirectionality.LEFT;
                turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                context.getParameters().setClaimedParameter(DLC, desire.left(), PARAMETER_KEY);
                setDesiredHeadway(context.getParameters(), desire.left(), false);
                leaders = neighbors.getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next
                    // step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a, context.getCarFollowingModel().followingAcceleration(context.getParameters(),
                        context.getSpeed(), context.getSpeedLimitInfo(), neighbors.getLeaders(RelativeLane.LEFT)));
            }
        }
        else if (!desire.leftIsLargerOrEqual() && desire.right() >= dFree)
        {
            if (acceptLaneChange(context, desire.right(), LateralDirectionality.RIGHT, lmrsData.getGapAcceptance()))
            {
                // change right
                initiatedOrContinuedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                context.getParameters().setClaimedParameter(DLC, desire.right(), PARAMETER_KEY);
                setDesiredHeadway(context.getParameters(), desire.right(), false);
                leaders = neighbors.getLeaders(RelativeLane.RIGHT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a, context.getCarFollowingModel().followingAcceleration(context.getParameters(),
                        context.getSpeed(), context.getSpeedLimitInfo(), neighbors.getLeaders(RelativeLane.RIGHT)));
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
                Synchronizable.State state;
                if (desire.left() >= context.getParameters().getParameter(DCOOP))
                {
                    // switch on left indicator
                    turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                    state = Synchronizable.State.INDICATING;
                }
                else
                {
                    state = Synchronizable.State.SYNCHRONIZING;
                }
                aSync = lmrsData.getSynchronization().synchronize(context, desire.left(), LateralDirectionality.LEFT, lmrsData,
                        initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync, lmrsData, state);
            }
            else if (!desire.leftIsLargerOrEqual() && desire.right() >= dSync)
            {
                Synchronizable.State state;
                if (desire.right() >= context.getParameters().getParameter(DCOOP))
                {
                    // switch on right indicator
                    turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                    state = Synchronizable.State.INDICATING;
                }
                else
                {
                    state = Synchronizable.State.SYNCHRONIZING;
                }
                aSync = lmrsData.getSynchronization().synchronize(context, desire.right(), LateralDirectionality.RIGHT,
                        lmrsData, initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync, lmrsData, state);
            }

            // cooperate
            aSync = lmrsData.getCooperation().cooperate(context, LateralDirectionality.LEFT, desire);
            a = applyAcceleration(a, aSync, lmrsData, Synchronizable.State.COOPERATING);
            aSync = lmrsData.getCooperation().cooperate(context, LateralDirectionality.RIGHT, desire);
            a = applyAcceleration(a, aSync, lmrsData, Synchronizable.State.COOPERATING);

            // relaxation
            exponentialHeadwayRelaxation(context.getParameters());
        }

        lmrsData.finalizeStep();

        SimpleOperationalPlan simplePlan =
                new SimpleOperationalPlan(a, context.getParameters().getParameter(DT), initiatedOrContinuedLaneChange);
        if (turnIndicatorStatus.isLeft())
        {
            simplePlan.setIndicatorIntentLeft();
        }
        else if (turnIndicatorStatus.isRight())
        {
            simplePlan.setIndicatorIntentRight();
        }
        return simplePlan;

    }

    /**
     * Minimizes the acceleration and sets the synchronization state if applicable.
     * @param a previous acceleration
     * @param aNew new acceleration
     * @param lmrsData lmrs data
     * @param state Synchronizable.State; synchronization state
     * @return minimized acceleration
     */
    private static Acceleration applyAcceleration(final Acceleration a, final Acceleration aNew, final LmrsData lmrsData,
            final Synchronizable.State state)
    {
        if (a.si < aNew.si)
        {
            return a;
        }
        lmrsData.setSynchronizationState(state);
        return aNew;
    }

    /**
     * Sets the headway as a response to a new leader.
     * @param params parameters
     * @param leader leader
     * @throws ParameterException if DLC is not present
     */
    private static void initHeadwayRelaxation(final Parameters params, final PerceivedGtu leader) throws ParameterException
    {
        Optional<Double> dlc = leader.getBehavior().getParameters().getOptionalParameter(DLC);
        if (dlc.isPresent())
        {
            setDesiredHeadway(params, dlc.get(), false);
        }
        // else could not be perceived
    }

    /**
     * Updates the desired headway following an exponential shape approximated with fixed time step <code>DT</code>.
     * @param params parameters
     * @throws ParameterException in case of a parameter exception
     */
    private static void exponentialHeadwayRelaxation(final Parameters params) throws ParameterException
    {
        double ratio = params.getParameter(DT).si / params.getParameter(TAU).si;
        params.setClaimedParameter(T,
                Duration.interpolate(params.getParameter(T), params.getParameter(TMAX), ratio <= 1.0 ? ratio : 1.0), T_KEY);
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
     * @throws GtuException if there is no mandatory incentive, the model requires at least one
     */
    public static Desire getLaneChangeDesire(final TacticalContextEgo context,
            final AbstractIncentivesTacticalPlanner incentives) throws ParameterException, GtuException
    {
        if (context.getPerception().getGtu().getLaneChangeDirection().isLeft())
        {
            return new Desire(1.0, 0.0);
        }
        else if (context.getPerception().getGtu().getLaneChangeDirection().isRight())
        {
            return new Desire(0.0, 1.0);
        }

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
        // beyond start distance
        if (!context.getGtu().laneChangeAllowed())
        {
            return false;
        }

        // legal?
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        if (infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).si <= 0.0)
        {
            return false;
        }

        // safe regarding neighbors?
        double consideredDesire = context.getGtu().getLaneChangeDirection().equals(lat) ? 1.0 : desire;
        if (!gapAcceptance.acceptGap(context, consideredDesire, lat))
        {
            return false;
        }

        // intersection causes for deceleration
        Optional<IntersectionPerception> intersection =
                context.getPerception().getPerceptionCategoryOptional(IntersectionPerception.class);
        if (intersection.isPresent())
        {
            RelativeLane lane = new RelativeLane(lat, 1);

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
            try
            {
                Acceleration a = ConflictUtil.approachConflicts(context, new ConflictPlans(), lane, Length.ZERO, false);
                if (a.lt(context.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
                {
                    return false;
                }
                // gap-acceptance on merge conflicts
                // TODO: this approach is a hack
                for (PerceivedConflict conflict : conflicts)
                {
                    if (conflict.isMerge() && conflict.getDistance().si < 10.0)
                    {
                        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> down = conflict.getDownstreamConflictingGTUs();
                        if (!down.isEmpty() && down.first().getKinematics().getOverlap().isParallel())
                        {
                            return false; // GTU on conflict
                        }
                        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> up = conflict.getUpstreamConflictingGTUs();
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
                    if (a.lt(context.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
                    {
                        return false;
                    }
                }
            }
        }

        // cut-in vehicles from 2nd lane
        RelativeLane lane = new RelativeLane(lat, 2);
        Acceleration b = context.getParameters().getParameter(ParameterTypes.B).neg();
        for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.class).getLeaders(lane))
        {
            if (leader.getManeuver().isChangingLane(lat.flip()) && CarFollowingUtil.followSingleLeader(context, leader).lt(b))
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

    /**
     * Sets value for T depending on level of lane change desire.
     * @param params parameters
     * @param desire lane change desire
     * @param resettable whether the T value will be reset later (ignoring key), or regular claimed setting (with key)
     * @throws ParameterException if T, TMIN or TMAX is not in the parameters
     */
    static void setDesiredHeadway(final Parameters params, final double desire, final boolean resettable)
            throws ParameterException
    {
        double limitedDesire = desire < 0 ? 0 : desire > 1 ? 1 : desire;
        double tDes = limitedDesire * params.getParameter(TMIN).si + (1 - limitedDesire) * params.getParameter(TMAX).si;
        double tSi = params.getParameter(T).si;
        Duration t = Duration.ofSI(tDes < tSi ? tDes : tSi);
        if (resettable)
        {
            params.setParameterResettable(T, t);
        }
        else
        {
            params.setClaimedParameter(T, t, T_KEY);
        }
    }

    /**
     * Resets value for T depending on level of lane change desire.
     * @param params parameters
     * @throws ParameterException if T is not in the parameters
     */
    static void resetDesiredHeadway(final Parameters params) throws ParameterException
    {
        params.resetParameter(T);
    }

    /**
     * Determine acceleration from car-following with desire-adjusted headway.
     * @param context tactical information such as parameters and car-following model
     * @param distance distance from follower to leader
     * @param leaderSpeed speed of leader
     * @param desire level of lane change desire
     * @return acceleration from car-following
     * @throws ParameterException if a parameter is not defined
     */
    public static Acceleration singleAcceleration(final TacticalContext context, final Length distance, final Speed leaderSpeed,
            final double desire) throws ParameterException
    {
        // set T
        setDesiredHeadway(context.getParameters(), desire, true);
        // calculate acceleration
        Acceleration a = CarFollowingUtil.followSingleLeader(context, distance, leaderSpeed);
        // reset T
        resetDesiredHeadway(context.getParameters());
        return a;
    }

}
