package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRS extends AbstractLMRS
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /** Lane change direction, null if none. */
    private LateralDirectionality laneChangeDirectionality = null;

    /** Total number of time steps for lane change. */
    private int totalLaneChangeSteps = 6;

    /** Current time step number during lane changes. */
    private int currentLaneChangeStep = 0;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     */
    public LMRS(final CarFollowingModel carFollowingModel)
    {
        super(carFollowingModel);
    }

    /** {@inheritDoc} */
    @Override
    public final LinkedHashSet<MandatoryIncentive> getDefaultMandatoryIncentives()
    {
        LinkedHashSet<MandatoryIncentive> set = new LinkedHashSet<>();
        set.add(new IncentiveRoute());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkedHashSet<VoluntaryIncentive> getDefaultVoluntaryIncentives()
    {
        LinkedHashSet<VoluntaryIncentive> set = new LinkedHashSet<>();
        set.add(new IncentiveSpeedWithCourtesy());
        set.add(new IncentiveKeep());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final GTU gtu, final Time startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, GTUException, NetworkException,
        ParameterException
    {

        // obtain objects to get info
        LaneBasedGTU gtuLane = (LaneBasedGTU) gtu;
        LanePerception perception = gtuLane.getPerception();
        perception.perceive();
        SpeedLimitProspect slp = perception.getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtuLane.getBehavioralCharacteristics();

        // regular car-following
        Speed speed = gtu.getSpeed(startTime);
        Acceleration a =
            CarFollowingUtil.followLeaders(getCarFollowingModel(), bc, speed, sli, perception
                .getLeaders(RelativeLane.CURRENT));

        // approaching speed limits
        Acceleration aTrans = considerSpeedLimitTransitions(bc, speed, slp, getCarFollowingModel());
        a = a.lt(aTrans) ? a : aTrans;

        // traffic lights
        for (HeadwayTrafficLight headwayTrafficLight : perception.getTrafficLights())
        {
            Acceleration aLight =
                TrafficLightUtil.respondToTrafficLight(bc, headwayTrafficLight, getCarFollowingModel(), speed, sli);
            a = a.lt(aLight) ? a : aLight;
        }

        // during a lane change, both leaders are followed
        if (this.laneChangeDirectionality != null)
        {
            RelativeLane tar = this.laneChangeDirectionality.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
            Acceleration aTar =
                CarFollowingUtil.followLeaders(getCarFollowingModel(), bc, gtu.getSpeed(startTime), sli, perception
                    .getLeaders(tar));
            a = a.lt(aTar) ? a : aTar;

            Length distance = bc.getParameter(ParameterTypes.LOOKAHEAD);
            List<Lane> lanes = buildLanePathInfo(gtuLane, distance).getLanes();
            Length firstLanePosition = gtuLane.position(lanes.get(0), RelativePosition.REFERENCE_POSITION);
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtuLane, lanes, firstLanePosition, startTime, gtu
                    .getSpeed(), a, bc.getParameter(DT));
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        // relaxation
        exponentialHeadwayRelaxation(bc);

        // determine lane change desire based on incentives
        Desire desire = getLaneChangeDesire(gtuLane);

        // gap acceptance
        boolean acceptLeft =
            acceptGap(perception, bc, sli, getCarFollowingModel(), desire.getLeft(), speed, LateralDirectionality.LEFT);
        boolean acceptRight =
            acceptGap(perception, bc, sli, getCarFollowingModel(), desire.getRight(), speed, LateralDirectionality.RIGHT);

        // lane change decision
        double dFree = bc.getParameter(DFREE);
        double dSync = bc.getParameter(DSYNC);
        double dCoop = bc.getParameter(DCOOP);
        // decide
        boolean changeLeft = false;
        boolean changeRight = false;
        if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dFree && acceptLeft)
        {
            // change left
            changeLeft = true;
            bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc
                .getParameter(ParameterTypes.TMIN), desire.getLeft()));
            // TODO headway of other driver...
        }
        else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dFree && acceptRight)
        {
            // change right
            changeRight = true;
            bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc
                .getParameter(ParameterTypes.TMIN), desire.getRight()));
            // TODO headway of other driver...
        }

        // take action if we cannot change lane
        Acceleration aSync;
        TurnIndicatorStatus turnIndicatorStatus =
            changeLeft ? TurnIndicatorStatus.LEFT : changeRight ? TurnIndicatorStatus.RIGHT : TurnIndicatorStatus.NONE;
        if (!changeLeft && !changeRight)
        {
            // synchronize
            if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dSync)
            {
                aSync =
                    synchronize(perception, bc, sli, getCarFollowingModel(), desire.getLeft(), speed,
                        LateralDirectionality.LEFT);
                a = a.lt(aSync) ? a : aSync;
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dSync)
            {
                aSync =
                    synchronize(perception, bc, sli, getCarFollowingModel(), desire.getRight(), speed,
                        LateralDirectionality.RIGHT);
                a = a.lt(aSync) ? a : aSync;
            }
            // use indicators to indicate lane change need
            if (desire.leftIsLargerOrEqual() && desire.getLeft() >= dCoop)
            {
                // switch on left indicator
                turnIndicatorStatus = TurnIndicatorStatus.LEFT;
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() >= dCoop)
            {
                // switch on right indicator
                turnIndicatorStatus = TurnIndicatorStatus.RIGHT;
            }
        }
        gtu.setTurnIndicatorStatus(turnIndicatorStatus);

        // cooperate
        aSync = cooperate(perception, bc, sli, getCarFollowingModel(), desire.getLeft(), speed, LateralDirectionality.LEFT);
        a = a.lt(aSync) ? a : aSync;
        aSync =
            cooperate(perception, bc, sli, getCarFollowingModel(), desire.getRight(), speed, LateralDirectionality.RIGHT);
        a = a.lt(aSync) ? a : aSync;

        // operational plan
        if (changeLeft)
        {
            this.laneChangeDirectionality = LateralDirectionality.LEFT;
        }
        else if (changeRight)
        {
            this.laneChangeDirectionality = LateralDirectionality.RIGHT;
        }

        Length distance = bc.getParameter(ParameterTypes.LOOKAHEAD);
        List<Lane> lanes = buildLanePathInfo(gtuLane, distance).getLanes();
        if (this.laneChangeDirectionality == null)
        {
            try 
            {
                Length firstLanePosition = gtuLane.position(lanes.get(0), RelativePosition.REFERENCE_POSITION);
                return LaneOperationalPlanBuilder.buildAccelerationPlan(gtuLane, lanes, firstLanePosition, startTime, gtu
                    .getSpeed(), a, bc.getParameter(DT));
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }
        List<Lane> toLanes = new ArrayList<>();
        for (Lane lane : lanes)
        {
            toLanes.add(lane.accessibleAdjacentLanes(this.laneChangeDirectionality, gtu.getGTUType()).iterator().next());
        }
        OperationalPlan plan;
        try 
        {
            plan = LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(gtuLane, lanes, toLanes, gtu.getLocation(),
                startTime, gtu.getSpeed(), a, bc.getParameter(DT), this.totalLaneChangeSteps, this.currentLaneChangeStep);
            this.currentLaneChangeStep++;
            if (this.currentLaneChangeStep >= this.totalLaneChangeSteps)
            {
                this.currentLaneChangeStep = 0;
                this.laneChangeDirectionality = null;
            }
            return plan;
        }
        catch (OTSGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }

        // TODO Build the operational plan using minimum acceleration and including a possible lane change using
        // TODO determine lane change duration, shorten if required
        // a
        // changeLeft/changeRight
        //return buildAccelerationPlanWithLaneChange(gtuLane, startTime, speed, bc.getParameter(DT), a, bc, perception);

    }

    /*-
    
    /** Lane path info ahead at start of lane change. /
    private LanePathInfo startLanePathInfo = null;
    
    /** Lane change progress. /
    private double laneChangeProgress;

    /** Start time of lane change. /
    private Time laneChangeStartTime;
    
    /**
     * Returns a plan for fixed acceleration over some duration. A lane change is implemented when performed.
     * @param gtu GTU
     * @param startTime start time
     * @param startSpeed start speed
     * @param duration duration
     * @param a acceleration
     * @param bc behavioral characteristics
     * @param perception perceived surroundings
     * @return build operational plan
     * @throws GTUException if the reference lane cannot be found
     * @throws NetworkException if lane path info cannot be build
     * @throws ParameterException if a parameter is not defined
     /
    private OperationalPlan buildAccelerationPlanWithLaneChange(final LaneBasedGTU gtu, final Time startTime,
        final Speed startSpeed, final Duration duration, final Acceleration a, final BehavioralCharacteristics bc,
        final PerceivedSurroundings perception) throws GTUException, NetworkException, ParameterException
    {
        List<Segment> segmentList = new ArrayList<>();
        Acceleration b = a.multiplyBy(-1.0);
        Length pathLength;
        if (startSpeed.lt(b.multiplyBy(duration)))
        {
            // will reach zero speed within duration
            Duration d = startSpeed.divideBy(b);
            segmentList.add(new AccelerationSegment(d, a)); // decelerate to zero
            segmentList.add(new SpeedSegment(duration.minus(d))); // stay at zero for the remainder of duration
            pathLength = new Length(1.01 * (startSpeed.si * d.si + .5 * a.si * d.si * d.si), LengthUnit.SI);
        }
        else
        {
            segmentList.add(new AccelerationSegment(duration, a));
            pathLength =
                new Length(1.01 * (startSpeed.si * duration.si + .5 * a.si * duration.si * duration.si), LengthUnit.SI);
        }
        try
        {

            OTSLine3D path;
            if (this.laneChangeDirectionality == null || this.laneChangeProgress >= 1)
            {

                // end lane change
                if (this.laneChangeProgress >= 1)
                {
                    // unregister lanes ahead of lane change start
                    // for (LaneDirection laneDirection : this.startLanePathInfo.getLaneDirectionList())
                    // {
                    // if (gtu.getLanes().keySet().contains(laneDirection.getLane()))
                    // {
                    // gtu.leaveLane(laneDirection.getLane());
                    // }
                    // }
                    this.laneChangeProgress = 0;
                    this.startLanePathInfo = null;
                    this.laneChangeDirectionality = null;
                    this.laneChangeStartTime = null;
                }

                LanePathInfo currentLanePathInfo = buildLanePathInfo(gtu, pathLength);
                path = currentLanePathInfo.getPath();
            }
            else
            {
                // TODO create path based on lane change

                // start lane change
                if (this.startLanePathInfo == null)
                {
                    Length forwardHeadway = bc.getParameter(ParameterTypes.LOOKAHEAD);
                    this.startLanePathInfo = buildLanePathInfo(gtu, forwardHeadway);
                    this.laneChangeStartTime = startTime;
                    Lane startLane = getReferenceLane(gtu);
                    Lane adjacentLane =
                        startLane.accessibleAdjacentLanes(this.laneChangeDirectionality, gtu.getGTUType()).iterator().next();
                    Length startPosition = gtu.position(startLane, gtu.getReference());
                    double fraction = startLane.fraction(startPosition);
                    // gtu.enterLane(adjacentLane, adjacentLane.getLength().multiplyBy(fraction),
                    // gtu.getLanes().get(startLane));
                }

                // get lane change speed
                Length remainingDistance =
                    perception.getPhysicalLaneChangePossibility(RelativeLane.CURRENT, this.laneChangeDirectionality);
                Duration normalRemainingTime = new Duration(3, TimeUnit.SECOND).multiplyBy(1 - this.laneChangeProgress);
                Duration availableRemainingTime = remainingDistance.divideBy(startSpeed);
                Duration remainingTime =
                    normalRemainingTime.lt(availableRemainingTime) ? normalRemainingTime : availableRemainingTime;

                // build path

                Lane startLane = getReferenceLane(gtu);
                Lane adjacentLane =
                    startLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtu.getGTUType()).iterator().next();
                Length startPosition = gtu.position(startLane, gtu.getReference());
                super.buildLanePathInfo(gtu, pathLength);

                path = null;

            }
            return new OperationalPlan(gtu, path, startTime, startSpeed, segmentList);
        }
        catch (OperationalPlanException ope)
        {
            // should not happen, the acquired path is sufficiently long
            throw new RuntimeException(ope);
        }
    }
     */

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
     */
    protected final boolean acceptGap(final LanePerception perception, final BehavioralCharacteristics bc,
        final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
        final LateralDirectionality lat) throws ParameterException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        if (perception.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0)
        {
            Acceleration aFollow = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (AbstractHeadwayGTU follower : perception.getFirstFollowers(lat))
            {
                // TODO bc, sli and cfm of follower
                Acceleration a =
                    singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire, bc, sli, cfm);
                if (a.lt(aFollow))
                {
                    aFollow = a;
                }
            }
            Acceleration aSelf = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
            for (AbstractHeadwayGTU leader : perception.getFirstLeaders(lat))
            {
                Acceleration a = singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                if (a.lt(aSelf))
                {
                    aSelf = a;
                }
            }
            return aSelf.getSI() >= -b.si * desire && aFollow.getSI() >= -b.si * desire;
        }
        return false;
    }

    /**
     * Determine acceleration for synchronization.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for synchronization
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     */
    private Acceleration synchronize(final LanePerception perception, final BehavioralCharacteristics bc,
        final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
        final LateralDirectionality lat) throws ParameterException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (AbstractHeadwayGTU leader : perception.getFirstLeaders(lat))
        {
            Acceleration aSingle =
                singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
            if (aSingle.lt(a))
            {
                a = aSingle;
            }
        }
        return a.si < -b.si ? b.multiplyBy(-1) : a;
    }

    /**
     * Determine acceleration for cooperation.
     * @param perception perception
     * @param bc behavioral characteristics
     * @param sli speed limit info
     * @param cfm car-following model
     * @param desire level of lane change desire
     * @param ownSpeed own speed
     * @param lat lateral direction for cooperation
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     */
    private Acceleration cooperate(final LanePerception perception, final BehavioralCharacteristics bc,
        final SpeedLimitInfo sli, final CarFollowingModel cfm, final double desire, final Speed ownSpeed,
        final LateralDirectionality lat) throws ParameterException
    {
        Acceleration b = bc.getParameter(ParameterTypes.B);
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (AbstractHeadwayGTU leader : perception.getFirstLeaders(lat))
        {
            if ((lat == LateralDirectionality.LEFT && leader.isRightTurnIndicatorOn())
                || (lat == LateralDirectionality.RIGHT && leader.isLeftTurnIndicatorOn()))
            {
                Acceleration aSingle =
                    singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire, bc, sli, cfm);
                if (aSingle.lt(a))
                {
                    a = aSingle;
                }
            }
        }
        return a.si < -b.si ? b.multiplyBy(-1) : a;
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
    private Acceleration singleAcceleration(final Length distance, final Speed followerSpeed, final Speed leaderSpeed,
        final double desire, final BehavioralCharacteristics bc, final SpeedLimitInfo sli, final CarFollowingModel cfm)
        throws ParameterException
    {
        // set T
        bc.setParameter(ParameterTypes.T, Duration.interpolate(bc.getParameter(ParameterTypes.TMAX), bc
            .getParameter(ParameterTypes.TMIN), desire));
        // calculate acceleration
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(distance, leaderSpeed);
        Acceleration a = cfm.followingAcceleration(bc, followerSpeed, sli, leaders);
        // reset T
        bc.resetParameter(ParameterTypes.T);
        return a;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        String mandatory;
        try
        {
            mandatory = "mandatoryIncentives=" + getMandatoryIncentives() + ", ";
        }
        catch (GTUException ope)
        {
            // thrown if no mandatory incentives
            mandatory = "mandatoryIncentives=[]";
        }
        String voluntary;
        if (!getVoluntaryIncentives().isEmpty())
        {
            voluntary = "voluntaryIncentives=" + getVoluntaryIncentives();
        }
        else
        {
            voluntary = "voluntaryIncentives=[]";
        }
        return "LMRS [" + mandatory + voluntary + "]";
    }

}
