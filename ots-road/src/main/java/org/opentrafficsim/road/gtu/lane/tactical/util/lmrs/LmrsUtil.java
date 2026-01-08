package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.Iterator;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
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
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /**
     * Do not instantiate.
     */
    private LmrsUtil()
    {
        //
    }

    /**
     * Determines a simple representation of an operational plan.
     * @param gtu gtu
     * @param carFollowingModel car-following model
     * @param lmrsData LMRS data
     * @param perception perception
     * @param mandatoryIncentives set of mandatory lane change incentives
     * @param voluntaryIncentives set of voluntary lane change incentives
     * @return simple operational plan
     * @throws GtuException gtu exception
     * @throws NetworkException network exception
     * @throws ParameterException parameter exception
     * @throws OperationalPlanException operational plan exception
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static SimpleOperationalPlan determinePlan(final LaneBasedGtu gtu, final CarFollowingModel carFollowingModel,
            final LmrsData lmrsData, final LanePerception perception, final Iterable<MandatoryIncentive> mandatoryIncentives,
            final Iterable<VoluntaryIncentive> voluntaryIncentives) throws GtuException, NetworkException, ParameterException
    {

        // obtain objects to get info
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        SpeedLimitProspect slp = infra.getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        Parameters params = gtu.getParameters();
        EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);
        Speed speed = ego.getSpeed();
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);

        // regular car-following
        Acceleration a;
        if (lmrsData.isHumanLongitudinalControl())
        {
            lmrsData.getTailgating().tailgate(perception, params);
            if (!leaders.isEmpty() && lmrsData.isNewLeader(leaders.first()))
            {
                initHeadwayRelaxation(params, leaders.first());
            }
            a = gtu.getCarFollowingAcceleration();
        }
        else
        {
            a = Acceleration.POS_MAXVALUE;
        }

        // determine lane change desire based on incentives
        Desire desire = getLaneChangeDesire(params, perception, carFollowingModel, mandatoryIncentives, voluntaryIncentives,
                lmrsData.getDesireMap());

        // lane change decision
        LateralDirectionality initiatedOrContinuedLaneChange;
        TurnIndicatorIntent turnIndicatorStatus = TurnIndicatorIntent.NONE;
        double dFree = params.getParameter(DFREE);
        initiatedOrContinuedLaneChange = LateralDirectionality.NONE;
        turnIndicatorStatus = TurnIndicatorIntent.NONE;
        if (desire.leftIsLargerOrEqual() && desire.left() >= dFree)
        {
            if (acceptLaneChange(perception, params, sli, carFollowingModel, desire.left(), speed, a,
                    LateralDirectionality.LEFT, lmrsData.getGapAcceptance()))
            {
                // change left
                initiatedOrContinuedLaneChange = LateralDirectionality.LEFT;
                turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                params.setParameter(DLC, desire.left());
                setDesiredHeadway(params, desire.left());
                leaders = neighbors.getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next
                    // step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a,
                        carFollowingModel.followingAcceleration(params, speed, sli, neighbors.getLeaders(RelativeLane.LEFT)));
            }
        }
        else if (!desire.leftIsLargerOrEqual() && desire.right() >= dFree)
        {
            if (acceptLaneChange(perception, params, sli, carFollowingModel, desire.right(), speed, a,
                    LateralDirectionality.RIGHT, lmrsData.getGapAcceptance()))
            {
                // change right
                initiatedOrContinuedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                params.setParameter(DLC, desire.right());
                setDesiredHeadway(params, desire.right());
                leaders = neighbors.getLeaders(RelativeLane.RIGHT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a,
                        carFollowingModel.followingAcceleration(params, speed, sli, neighbors.getLeaders(RelativeLane.RIGHT)));
            }
        }

        if (initiatedOrContinuedLaneChange.isLeft())
        {
            // Let surrounding GTUs respond fully to our movement
            params.setParameter(DLEFT, 1.0);
            params.setParameter(DRIGHT, 0.0);
        }
        else if (initiatedOrContinuedLaneChange.isRight())
        {
            // Let surrounding GTUs respond fully to our movement
            params.setParameter(DLEFT, 0.0);
            params.setParameter(DRIGHT, 1.0);
        }
        else
        {
            params.setParameter(DLEFT, desire.left());
            params.setParameter(DRIGHT, desire.right());

            // take action if we cannot change lane
            Acceleration aSync;

            // synchronize
            double dSync = params.getParameter(DSYNC);
            lmrsData.setSynchronizationState(Synchronizable.State.NONE);
            if (desire.leftIsLargerOrEqual() && desire.left() >= dSync)
            {
                Synchronizable.State state;
                if (desire.left() >= params.getParameter(DCOOP))
                {
                    // switch on left indicator
                    turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                    state = Synchronizable.State.INDICATING;
                }
                else
                {
                    state = Synchronizable.State.SYNCHRONIZING;
                }
                aSync = lmrsData.getSynchronization().synchronize(perception, params, sli, carFollowingModel, desire.left(),
                        LateralDirectionality.LEFT, lmrsData, initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync, lmrsData, state);
            }
            else if (!desire.leftIsLargerOrEqual() && desire.right() >= dSync)
            {
                Synchronizable.State state;
                if (desire.right() >= params.getParameter(DCOOP))
                {
                    // switch on right indicator
                    turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                    state = Synchronizable.State.INDICATING;
                }
                else
                {
                    state = Synchronizable.State.SYNCHRONIZING;
                }
                aSync = lmrsData.getSynchronization().synchronize(perception, params, sli, carFollowingModel, desire.right(),
                        LateralDirectionality.RIGHT, lmrsData, initiatedOrContinuedLaneChange);
                a = applyAcceleration(a, aSync, lmrsData, state);
            }

            // cooperate
            aSync = lmrsData.getCooperation().cooperate(perception, params, sli, carFollowingModel, LateralDirectionality.LEFT,
                    desire);
            a = applyAcceleration(a, aSync, lmrsData, Synchronizable.State.COOPERATING);
            aSync = lmrsData.getCooperation().cooperate(perception, params, sli, carFollowingModel, LateralDirectionality.RIGHT,
                    desire);
            a = applyAcceleration(a, aSync, lmrsData, Synchronizable.State.COOPERATING);

            // relaxation
            exponentialHeadwayRelaxation(params);
        }

        lmrsData.finalizeStep();

        SimpleOperationalPlan simplePlan =
                new SimpleOperationalPlan(a, params.getParameter(DT), initiatedOrContinuedLaneChange);
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
        Double dlc = leader.getBehavior().getParameters().getParameterOrNull(DLC);
        if (dlc != null)
        {
            setDesiredHeadway(params, dlc);
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
        params.setParameter(T,
                Duration.interpolate(params.getParameter(T), params.getParameter(TMAX), ratio <= 1.0 ? ratio : 1.0));
    }

    /**
     * Determines lane change desire for the given GtU. Mandatory desire is deduced as the maximum of a set of mandatory
     * incentives, while voluntary desires are added. Depending on the level of mandatory lane change desire, voluntary desire
     * may be included partially. If both are positive or negative, voluntary desire is fully included. Otherwise, voluntary
     * desire is less considered within the range dSync &lt; |mandatory| &lt; dCoop. The absolute value is used as large
     * negative mandatory desire may also dominate voluntary desire.
     * @param parameters parameters
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param mandatoryIncentives mandatory incentives
     * @param voluntaryIncentives voluntary incentives
     * @param desireMap map where calculated desires are stored in
     * @return lane change desire for gtu
     * @throws ParameterException if a parameter is not defined
     * @throws GtuException if there is no mandatory incentive, the model requires at least one
     */
    public static Desire getLaneChangeDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Iterable<MandatoryIncentive> mandatoryIncentives,
            final Iterable<VoluntaryIncentive> voluntaryIncentives, final Map<Class<? extends Incentive>, Desire> desireMap)
            throws ParameterException, GtuException
    {
        if (perception.getGtu().getLaneChangeDirection().isLeft())
        {
            return new Desire(1.0, 0.0);
        }
        else if (perception.getGtu().getLaneChangeDirection().isRight())
        {
            return new Desire(0.0, 1.0);
        }

        double dSync = parameters.getParameter(DSYNC);
        double dCoop = parameters.getParameter(DCOOP);

        // Mandatory desire
        double dLeftMandatory = 0.0;
        double dRightMandatory = 0.0;
        Desire mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        for (MandatoryIncentive incentive : mandatoryIncentives)
        {
            Desire d = incentive.determineDesire(parameters, perception, carFollowingModel, mandatoryDesire);
            desireMap.put(incentive.getClass(), d);
            dLeftMandatory = Math.abs(d.left()) > Math.abs(dLeftMandatory) ? d.left() : dLeftMandatory;
            dRightMandatory = Math.abs(d.right()) > Math.abs(dRightMandatory) ? d.right() : dRightMandatory;
            mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        }

        // Voluntary desire
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        Desire voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        for (VoluntaryIncentive incentive : voluntaryIncentives)
        {
            Desire d = incentive.determineDesire(parameters, perception, carFollowingModel, mandatoryDesire, voluntaryDesire);
            desireMap.put(incentive.getClass(), d);
            dLeftVoluntary += d.left();
            dRightVoluntary += d.right();
            voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        }

        // Total desire
        double thetaA = parameters.getParameter(LAMBDA_V);
        double leftThetaV = 0;
        double dLeftMandatoryAbs = Math.abs(dLeftMandatory);
        double dRightMandatoryAbs = Math.abs(dRightMandatory);
        if (dLeftMandatoryAbs <= dSync || dLeftMandatory * dLeftVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            leftThetaV = 1;
        }
        else if (dSync < dLeftMandatoryAbs && dLeftMandatoryAbs < dCoop && dLeftMandatory * dLeftVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            leftThetaV = (dCoop - dLeftMandatoryAbs) / (dCoop - dSync);
        }
        double rightThetaV = 0;
        if (dRightMandatoryAbs <= dSync || dRightMandatory * dRightVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            rightThetaV = 1;
        }
        else if (dSync < dRightMandatoryAbs && dRightMandatoryAbs < dCoop && dRightMandatory * dRightVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            rightThetaV = (dCoop - dRightMandatoryAbs) / (dCoop - dSync);
        }
        return new Desire(dLeftMandatory + thetaA * leftThetaV * dLeftVoluntary,
                dRightMandatory + thetaA * rightThetaV * dRightVoluntary);

    }

    /**
     * Determine whether a lane change is acceptable (gap, lane markings, etc.).
     * @param perception perception
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param ownAcceleration current car-following acceleration
     * @param lat lateral direction for synchronization
     * @param gapAcceptance gap-acceptance model
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    static boolean acceptLaneChange(final LanePerception perception, final Parameters params, final SpeedLimitInfo sli,
            final CarFollowingModel cfm, final double desire, final Speed ownSpeed, final Acceleration ownAcceleration,
            final LateralDirectionality lat, final GapAcceptance gapAcceptance)
            throws ParameterException, OperationalPlanException
    {
        // beyond start distance
        LaneBasedGtu gtu = Try.assign(() -> perception.getGtu(), "Cannot obtain GTU.");
        if (!gtu.laneChangeAllowed())
        {
            return false;
        }

        // legal?
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        if (infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, lat).si <= 0.0)
        {
            return false;
        }

        // safe regarding neighbors?
        if (!gapAcceptance.acceptGap(perception, params, sli, cfm, desire, ownSpeed, ownAcceleration, lat))
        {
            return false;
        }

        // intersection causes for deceleration
        IntersectionPerception intersection = perception.getPerceptionCategoryOrNull(IntersectionPerception.class);
        if (intersection != null)
        {
            NeighborsPerception neighbors = perception.getPerceptionCategoryOrNull(NeighborsPerception.class);
            RelativeLane lane = new RelativeLane(lat, 1);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(lane);

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
            EgoPerception<?, ?> ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
            PerceptionCollectable<PerceivedConflict, Conflict> conflicts = intersection.getConflicts(lane);
            try
            {
                Acceleration a = ConflictUtil.approachConflicts(params, conflicts, leaders, cfm, ego.getLength(),
                        ego.getWidth(), ownSpeed, ownAcceleration, sli, new ConflictPlans(), perception.getGtu(), lane);
                if (a.lt(params.getParameter(ParameterTypes.BCRIT).neg()))
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
            conflicts = intersection.getConflicts(RelativeLane.CURRENT);
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
                            && conflict.getDistance().neg().lt(gtu.getLength()))
                    {
                        return false; // partially before the split; adjacent lane might be ambiguous
                    }
                }
            }

            // traffic lights
            Iterable<PerceivedTrafficLight> trafficLights = intersection.getTrafficLights(lane);
            for (PerceivedTrafficLight trafficLight : trafficLights)
            {
                if (trafficLight.getTrafficLightColor().isRedOrYellow())
                {
                    Acceleration a = TrafficLightUtil.respondToTrafficLight(params, trafficLight, cfm, ownSpeed, sli);
                    if (a.lt(params.getParameter(ParameterTypes.BCRIT).neg()))
                    {
                        return false;
                    }
                }
            }
        }

        // cut-in vehicles from 2nd lane
        RelativeLane lane = new RelativeLane(lat, 2);
        Acceleration b = params.getParameter(ParameterTypes.B).neg();
        for (PerceivedGtu leader : perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane))
        {
            if (leader.getManeuver().isChangingLane(lat.flip())
                    && CarFollowingUtil.followSingleLeader(cfm, params, ownSpeed, sli, leader).lt(b))
            {
                return false;
            }
        }

        return true;

    }

    /**
     * Returns a quickly determined acceleration to consider on an adjacent lane, following from conflicts and traffic lights.
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @param intersection intersection perception
     * @return a quickly determined acceleration to consider on an adjacent lane, following from conflicts and traffic lights
     * @throws ParameterException if a parameter is not defined
     */
    private static Acceleration quickIntersectionScan(final Parameters params, final SpeedLimitInfo sli,
            final CarFollowingModel cfm, final Speed ownSpeed, final LateralDirectionality lat,
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
                    a = Acceleration.min(a, CarFollowingUtil.followSingleLeader(cfm, params, ownSpeed, sli,
                            conflicts.next().getDistance(), Speed.ZERO));
                }
                Iterator<PerceivedTrafficLight> trafficLights = intersection.getTrafficLights(lane).iterator();
                if (trafficLights.hasNext())
                {
                    PerceivedTrafficLight trafficLight = trafficLights.next();
                    if (trafficLight.getTrafficLightColor().isRedOrYellow())
                    {
                        a = Acceleration.min(a, CarFollowingUtil.followSingleLeader(cfm, params, ownSpeed, sli,
                                trafficLight.getDistance(), Speed.ZERO));
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
     * @throws ParameterException if T, TMIN or TMAX is not in the parameters
     */
    static void setDesiredHeadway(final Parameters params, final double desire) throws ParameterException
    {
        double limitedDesire = desire < 0 ? 0 : desire > 1 ? 1 : desire;
        double tDes = limitedDesire * params.getParameter(TMIN).si + (1 - limitedDesire) * params.getParameter(TMAX).si;
        double t = params.getParameter(T).si;
        params.setParameterResettable(T, Duration.ofSI(tDes < t ? tDes : t));
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
     * @param distance distance from follower to leader
     * @param followerSpeed speed of follower
     * @param leaderSpeed speed of leader
     * @param desire level of lane change desire
     * @param params parameters
     * @param sli speed limit info
     * @param cfm car-following model
     * @return acceleration from car-following
     * @throws ParameterException if a parameter is not defined
     */
    public static Acceleration singleAcceleration(final Length distance, final Speed followerSpeed, final Speed leaderSpeed,
            final double desire, final Parameters params, final SpeedLimitInfo sli, final CarFollowingModel cfm)
            throws ParameterException
    {
        // set T
        setDesiredHeadway(params, desire);
        // calculate acceleration
        Acceleration a = CarFollowingUtil.followSingleLeader(cfm, params, followerSpeed, sli, distance, leaderSpeed);
        // reset T
        resetDesiredHeadway(params);
        return a;
    }

}
