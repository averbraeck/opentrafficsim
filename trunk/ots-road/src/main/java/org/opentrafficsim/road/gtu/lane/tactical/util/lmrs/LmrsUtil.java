package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TurnIndicatorIntent;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class LmrsUtil implements LmrsParameters
{

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
     * @param startTime start time
     * @param carFollowingModel car-following model
     * @param laneChange lane change status
     * @param lmrsData LMRS data
     * @param perception perception
     * @param mandatoryIncentives set of mandatory lane change incentives
     * @param voluntaryIncentives set of voluntary lane change incentives
     * @param desireMap map where calculated desires are stored in
     * @return simple operational plan
     * @throws GTUException gtu exception
     * @throws NetworkException network exception
     * @throws ParameterException parameter exception
     * @throws OperationalPlanException operational plan exception
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static SimpleOperationalPlan determinePlan(final LaneBasedGTU gtu, final Time startTime,
            final CarFollowingModel carFollowingModel, final LaneChange laneChange, final LmrsData lmrsData,
            final LanePerception perception, final LinkedHashSet<MandatoryIncentive> mandatoryIncentives,
            final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives,
            final Map<Class<? extends Incentive>, Desire> desireMap)
            throws GTUException, NetworkException, ParameterException, OperationalPlanException
    {

        // TODO this is a hack to prevent right lane changes of all vehicles on the left lane when placed in network at t=0
        if (startTime.si == 0.0)
        {
            //return new SimpleOperationalPlan(Acceleration.ZERO, LateralDirectionality.NONE);
        }

        // obtain objects to get info
        SpeedLimitProspect slp =
                perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtu.getBehavioralCharacteristics();

        // regular car-following
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        SortedSet<HeadwayGTU> leaders =
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
        if (!leaders.isEmpty() && lmrsData.isNewLeader(leaders.first()))
        {
            initHeadwayRelaxation(bc, leaders.first());
        }
        Acceleration a;
        CarFollowingModel regularFollowing = carFollowingModel;
        SortedSet<HeadwayGTU> followers =
                perception.getPerceptionCategory(NeighborsPerception.class).getFollowers(RelativeLane.CURRENT);
        if (!followers.isEmpty())
        {
            HeadwayGTU follower = followers.first();
            Speed desiredSpeedFollower = follower.getCarFollowingModel().desiredSpeed(follower.getBehavioralCharacteristics(),
                    follower.getSpeedLimitInfo());
            Speed desiredSpeed = carFollowingModel.desiredSpeed(bc, sli);
            if (desiredSpeed.lt(desiredSpeedFollower))
            {
                // wrap car-following model with adjusted desired speed
                CarFollowingModel carFollowingModelWrapped = new CarFollowingModelWrapper(carFollowingModel,
                        Speed.interpolate(desiredSpeed, desiredSpeedFollower, bc.getParameter(HIERARCHY)));
                a = CarFollowingUtil.followLeaders(carFollowingModelWrapped, bc, speed, sli, leaders);
                // remember this wrapper for when following the second leader during a lane change
                regularFollowing = carFollowingModelWrapped;
            }
            else
            {
                a = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli, leaders);
            }
        }
        else
        {
            a = CarFollowingUtil.followLeaders(carFollowingModel, bc, speed, sli, leaders);
        }

        // during a lane change, both leaders are followed
        LateralDirectionality initiatedLaneChange;
        TurnIndicatorIntent turnIndicatorStatus = TurnIndicatorIntent.NONE;
        if (laneChange.isChangingLane())
        {
            RelativeLane secondLane = laneChange.getSecondLane(gtu);
            initiatedLaneChange = LateralDirectionality.NONE;
            SortedSet<HeadwayGTU> secondLeaders =
                    perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(secondLane);
            Acceleration aSecond = CarFollowingUtil.followLeaders(regularFollowing, bc, speed, sli, secondLeaders);
            if (!secondLeaders.isEmpty() && lmrsData.isNewLeader(secondLeaders.first()))
            {
                initHeadwayRelaxation(bc, secondLeaders.first());
            }
            a = Acceleration.min(a, aSecond);
        }
        else
        {

            // determine lane change desire based on incentives
            Desire desire =
                    getLaneChangeDesire(bc, perception, carFollowingModel, mandatoryIncentives, voluntaryIncentives, desireMap);

            // gap acceptance
            boolean acceptLeft =
                    acceptGap(perception, bc, sli, carFollowingModel, desire.getLeft(), speed, LateralDirectionality.LEFT);
            boolean acceptRight =
                    acceptGap(perception, bc, sli, carFollowingModel, desire.getRight(), speed, LateralDirectionality.RIGHT);

            // lane change decision
            double dFree = bc.getParameter(DFREE);
            double dSync = bc.getParameter(DSYNC);
            double dCoop = bc.getParameter(DCOOP);
            // decide

            if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dFree && acceptLeft)
            {
                // change left
                initiatedLaneChange = LateralDirectionality.LEFT;
                turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                bc.setParameter(DLC, desire.getLeft());
                setDesiredHeadway(bc, desire.getLeft());
                leaders = perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a, CarFollowingUtil.followLeaders(regularFollowing, bc, speed, sli,
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.LEFT)));
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dFree && acceptRight)
            {
                // change right
                initiatedLaneChange = LateralDirectionality.RIGHT;
                turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                bc.setParameter(DLC, desire.getRight());
                setDesiredHeadway(bc, desire.getRight());
                leaders = perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.RIGHT);
                if (!leaders.isEmpty())
                {
                    // don't respond on its lane change desire, but remember it such that it isn't a new leader in the next step
                    lmrsData.isNewLeader(leaders.first());
                }
                a = Acceleration.min(a, CarFollowingUtil.followLeaders(regularFollowing, bc, speed, sli,
                        perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(RelativeLane.RIGHT)));
            }
            else
            {
                initiatedLaneChange = LateralDirectionality.NONE;
                turnIndicatorStatus = TurnIndicatorIntent.NONE;
            }
            laneChange.setLaneChangeDuration(gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LCDUR));

            // take action if we cannot change lane
            Acceleration aSync;
            if (initiatedLaneChange.equals(LateralDirectionality.NONE))
            {
                // synchronize
                if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dSync)
                {
                    aSync = lmrsData.getSynchronization().synchronize(perception, bc, sli, carFollowingModel, desire.getLeft(),
                            LateralDirectionality.LEFT, lmrsData);
                    a = Acceleration.min(a, aSync);
                }
                else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dSync)
                {
                    aSync = lmrsData.getSynchronization().synchronize(perception, bc, sli, carFollowingModel, desire.getRight(),
                            LateralDirectionality.RIGHT, lmrsData);
                    a = Acceleration.min(a, aSync);
                }
                // use indicators to indicate lane change need
                if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dCoop)
                {
                    // switch on left indicator
                    turnIndicatorStatus = TurnIndicatorIntent.LEFT;
                }
                else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dCoop)
                {
                    // switch on right indicator
                    turnIndicatorStatus = TurnIndicatorIntent.RIGHT;
                }
                bc.setParameter(DLEFT, desire.getLeft());
                bc.setParameter(DRIGHT, desire.getRight());
            }
            else
            {
                bc.setParameter(DLEFT, 0.0);
                bc.setParameter(DRIGHT, 0.0);
            }

            // cooperate
            aSync = lmrsData.getSynchronization().cooperate(perception, bc, sli, carFollowingModel, LateralDirectionality.LEFT,
                    desire);
            a = Acceleration.min(a, aSync);
            aSync = lmrsData.getSynchronization().cooperate(perception, bc, sli, carFollowingModel, LateralDirectionality.RIGHT,
                    desire);
            a = Acceleration.min(a, aSync);

            // relaxation
            exponentialHeadwayRelaxation(bc);

        }
        lmrsData.finalizeStep();

        SimpleOperationalPlan simplePlan = new SimpleOperationalPlan(a, initiatedLaneChange);
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
     * Sets the headway as a response to a new leader.
     * @param bc behavioral characteristics
     * @param leader leader
     * @throws ParameterException if DLC is not present
     */
    private static void initHeadwayRelaxation(final BehavioralCharacteristics bc, final HeadwayGTU leader)
            throws ParameterException
    {
        if (leader.getBehavioralCharacteristics().contains(DLC))
        {
            setDesiredHeadway(bc, leader.getBehavioralCharacteristics().getParameter(DLC));
        }
        // else could not be perceived
    }

    /**
     * Updates the desired headway following an exponential shape approximated with fixed time step <tt>DT</tt>.
     * @param bc behavioral characteristics
     * @throws ParameterException in case of a parameter exception
     */
    private static void exponentialHeadwayRelaxation(final BehavioralCharacteristics bc) throws ParameterException
    {
        double ratio = bc.getParameter(ParameterTypes.DT).si / bc.getParameter(ParameterTypes.TAU).si;
        bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.T),
                bc.getParameter(ParameterTypes.TMAX), ratio <= 1.0 ? ratio : 1.0));
    }

    /**
     * Determines lane change desire for the given RSU. Mandatory desire is deduced as the maximum of a set of mandatory
     * incentives, while voluntary desires are added. Depending on the level of mandatory lane change desire, voluntary desire
     * may be included partially. If both are positive or negative, voluntary desire is fully included. Otherwise, voluntary
     * desire is less considered within the range dSync &lt; |mandatory| &lt; dCoop. The absolute value is used as large
     * negative mandatory desire may also dominate voluntary desire.
     * @param behavioralCharacteristics behavioral characteristics
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param mandatoryIncentives mandatory incentives
     * @param voluntaryIncentives voluntary incentives
     * @param desireMap map where calculated desires are stored in
     * @return lane change desire for gtu
     * @throws ParameterException if a parameter is not defined
     * @throws GTUException if there is no mandatory incentive, the model requires at least one
     * @throws OperationalPlanException perception exception
     */
    private static Desire getLaneChangeDesire(final BehavioralCharacteristics behavioralCharacteristics,
            final LanePerception perception, final CarFollowingModel carFollowingModel,
            final LinkedHashSet<MandatoryIncentive> mandatoryIncentives,
            final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives,
            final Map<Class<? extends Incentive>, Desire> desireMap)
            throws ParameterException, GTUException, OperationalPlanException
    {

        double dSync = behavioralCharacteristics.getParameter(DSYNC);
        double dCoop = behavioralCharacteristics.getParameter(DCOOP);

        // Mandatory desire
        double dLeftMandatory = 0.0;
        double dRightMandatory = 0.0;
        Desire mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        for (MandatoryIncentive incentive : mandatoryIncentives)
        {
            Desire d = incentive.determineDesire(behavioralCharacteristics, perception, carFollowingModel, mandatoryDesire);
            desireMap.put(incentive.getClass(), d);
            dLeftMandatory = Math.abs(d.getLeft()) > Math.abs(dLeftMandatory) ? d.getLeft() : dLeftMandatory;
            dRightMandatory = Math.abs(d.getRight()) > Math.abs(dRightMandatory) ? d.getRight() : dRightMandatory;
            mandatoryDesire = new Desire(dLeftMandatory, dRightMandatory);
        }

        // Voluntary desire
        double dLeftVoluntary = 0;
        double dRightVoluntary = 0;
        Desire voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        for (VoluntaryIncentive incentive : voluntaryIncentives)
        {
            Desire d = incentive.determineDesire(behavioralCharacteristics, perception, carFollowingModel, mandatoryDesire,
                    voluntaryDesire);
            desireMap.put(incentive.getClass(), d);
            dLeftVoluntary += d.getLeft();
            dRightVoluntary += d.getRight();
            voluntaryDesire = new Desire(dLeftVoluntary, dRightVoluntary);
        }

        // Total desire
        double thetaLeft = 0;
        double dLeftMandatoryAbs = Math.abs(dLeftMandatory);
        double dRightMandatoryAbs = Math.abs(dRightMandatory);
        if (dLeftMandatoryAbs <= dSync || dLeftMandatory * dLeftVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            thetaLeft = 1;
        }
        else if (dSync < dLeftMandatoryAbs && dLeftMandatoryAbs < dCoop && dLeftMandatory * dLeftVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            thetaLeft = (dCoop - dLeftMandatoryAbs) / (dCoop - dSync);
        }
        double thetaRight = 0;
        if (dRightMandatoryAbs <= dSync || dRightMandatory * dRightVoluntary >= 0)
        {
            // low mandatory desire, or same sign
            thetaRight = 1;
        }
        else if (dSync < dRightMandatoryAbs && dRightMandatoryAbs < dCoop && dRightMandatory * dRightVoluntary < 0)
        {
            // linear from 1 at dSync to 0 at dCoop
            thetaRight = (dCoop - dRightMandatoryAbs) / (dCoop - dSync);
        }
        return new Desire(dLeftMandatory + thetaLeft * dLeftVoluntary, dRightMandatory + thetaRight * dRightVoluntary);

    }

    /**
     * Determine whether a gap is acceptable.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private static boolean acceptGap(final LanePerception perception, final BehavioralCharacteristics bc,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
            final LateralDirectionality lat) throws ParameterException, OperationalPlanException
    {
        
        // beyond start distance
        try
        {
            if (!perception.getGtu().laneChangeAllowed())
            {
                return false;
            }
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("Cannot obtain GTU.", exception);
        }

        // legal?
        if (perception.getPerceptionCategory(InfrastructurePerception.class).getLegalLaneChangePossibility(RelativeLane.CURRENT,
                lat).si <= 0.0)
        {
            return false;
        }

        // conflicts alongside?
        if (perception.contains(IntersectionPerception.class))
        {
            if ((lat.isLeft() && perception.getPerceptionCategory(IntersectionPerception.class).isAlongsideConflictLeft())
                    || (lat.isRight()
                            && perception.getPerceptionCategory(IntersectionPerception.class).isAlongsideConflictRight()))
            {
                return false;
            }
        }

        // safe regarding neighbors?
        return acceptGapNeighbors(perception, bc, sli, cfm, desire, ownSpeed, lat);
    }

    /**
     * Determine whether a gap is acceptable.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return whether a gap is acceptable
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    static boolean acceptGapNeighbors(final LanePerception perception, final BehavioralCharacteristics bc,
            final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
            final LateralDirectionality lat) throws ParameterException, OperationalPlanException
    {

        if (perception.getPerceptionCategory(NeighborsPerception.class).isGtuAlongside(lat))
        {
            // gtu alongside
            return false;
        }

        // TODO
        /*-
         * Followers and are accepted if the acceleration and speed is 0, a leader is accepted if the ego speed is 0. This is in
         * place as vehicles that provide courtesy, will decelerate for us and overshoot the stand-still distance. As a 
         * consequence, they will cease cooperation as they are too close. A pattern will arise where followers slow down to
         * (near) stand-still, and accelerate again, before we could ever accept the gap.
         * 
         * By accepting the gap in the moment that they reach stand-still, this vehicle can at least accept the gap at some 
         * point. All of this is only a problem if the own vehicle is standing still. Otherwise the stand-still distance is not
         * important and movement of our own will create an acceptable situation.
         * 
         * What needs to be done, is to find a better way to deal with the cooperation and gap-acceptance, such that this hack 
         * is not required.
         */
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration aFollow = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (

        HeadwayGTU follower : perception.getPerceptionCategory(NeighborsPerception.class).getFirstFollowers(lat))
        {
            if (follower.getSpeed().gt0() || follower.getAcceleration().gt0())
            {
                Acceleration a = singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire,
                        follower.getBehavioralCharacteristics(), follower.getSpeedLimitInfo(), follower.getCarFollowingModel());
                aFollow = Acceleration.min(aFollow, a);
            }
        }

        Acceleration aSelf = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        if (ownSpeed.gt0())
        {
            for (

            HeadwayGTU leader : perception.getPerceptionCategory(NeighborsPerception.class).getFirstLeaders(lat))
            {
                Acceleration a = singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                aSelf = Acceleration.min(aSelf, a);
            }
        }

        Acceleration threshold = b.multiplyBy(-desire);
        return aFollow.ge(threshold) && aSelf.ge(threshold);
    }

    /**
     * Sets value for T depending on level of lane change desire.
     * @param bc behavioral characteristics
     * @param desire lane change desire
     * @throws ParameterException if T, TMIN or TMAX is not in the behavioral characteristics
     */
    static void setDesiredHeadway(final BehavioralCharacteristics bc, final double desire) throws ParameterException
    {
        double limitedDesire = desire < 0 ? 0 : desire > 1 ? 1 : desire;
        double tDes = limitedDesire * bc.getParameter(ParameterTypes.TMIN).si
                + (1 - limitedDesire) * bc.getParameter(ParameterTypes.TMAX).si;
        double t = bc.getParameter(ParameterTypes.T).si;
        bc.setParameter(ParameterTypes.T, new Duration(tDes < t ? tDes : t, TimeUnit.SI));
    }

    /**
     * Resets value for T depending on level of lane change desire.
     * @param bc behavioral characteristics
     * @throws ParameterException if T is not in the behavioral characteristics
     */
    static void resetDesiredHeadway(final BehavioralCharacteristics bc) throws ParameterException
    {
        bc.resetParameter(ParameterTypes.T);
    }

    /**
     * Determine acceleration from car-following.
     * @param distance distance from follower to leader
     * @param followerSpeed speed of follower
     * @param leaderSpeed speed of leader
     * @param desire level of lane change desire
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @return acceleration from car-following
     * @throws ParameterException if a parameter is not defined
     */
    public static Acceleration singleAcceleration(final Length distance, final Speed followerSpeed, final Speed leaderSpeed,
            final double desire, final BehavioralCharacteristics bc, final SpeedLimitInfo sli, final CarFollowingModel cfm)
            throws ParameterException
    {
        // set T
        setDesiredHeadway(bc, desire);
        // calculate acceleration
        Acceleration a = CarFollowingUtil.followSingleLeader(cfm, bc, followerSpeed, sli, distance, leaderSpeed);
        // reset T
        resetDesiredHeadway(bc);
        return a;
    }

    /**
     * Wrapper for car-following model to adjust desired speed.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static final class CarFollowingModelWrapper implements CarFollowingModel
    {

        /** Wrapped car-following model. */
        private final CarFollowingModel carFollowingModel;

        /** Desired speed. */
        private final Speed desiredSpeed;

        /**
         * @param carFollowingModel car-following model
         * @param desiredSpeed desired speed
         */
        CarFollowingModelWrapper(final CarFollowingModel carFollowingModel, final Speed desiredSpeed)
        {
            this.carFollowingModel = carFollowingModel;
            this.desiredSpeed = desiredSpeed;
        }

        /** {@inheritDoc} */
        @Override
        public Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedLimitInfo speedInfo)
                throws ParameterException
        {
            return this.desiredSpeed;
        }

        /** {@inheritDoc} */
        @Override
        public Length desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
                throws ParameterException
        {
            return this.carFollowingModel.desiredHeadway(behavioralCharacteristics, speed);
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed,
                final SpeedLimitInfo speedLimitInfo, final SortedMap<Length, Speed> leaders) throws ParameterException
        {
            return this.carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaders);
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return this.carFollowingModel.getName();
        }

        /** {@inheritDoc} */
        @Override
        public String getLongName()
        {
            return this.carFollowingModel.getLongName();
        }

    }

}
