package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedAltruistic;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedEgoistic;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedLaneMovementStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Lane-based tactical planner that implements car following behavior and rule-based lane change. This tactical planner
 * retrieves the car following model from the strategical planner and will generate an operational plan for the GTU.
 * <p>
 * A lane change occurs when:
 * <ol>
 * <li>The route indicates that the current lane does not lead to the destination; main choices are the time when the GTU
 * switches to the "right" lane, and what should happen when the split gets closer and the lane change has failed. Observations
 * indicate that vehicles if necessary stop in their current lane until they can go to the desired lane. A lane drop is
 * automatically part of this implementation, because the lane with a lane drop will not lead to the GTU's destination.</li>
 * <li>The desired speed of the vehicle is a particular delta-speed higher than its predecessor, the headway to the predecessor
 * in the current lane has exceeded a certain value, it is allowed to change to the target lane, the target lane lies on the
 * GTU's route, and the gap in the target lane is acceptable (including the evaluation of the perceived speed of a following GTU
 * in the target lane).</li>
 * <li>The current lane is not the optimum lane given the traffic rules (for example, to keep right), the headway to the
 * predecessor on the target lane is greater than a certain value, the speed of the predecessor on the target lane is greater
 * than or equal to our speed, the target lane is on the route, it is allowed to switch to the target lane, and the gap at the
 * target lane is acceptable (including the perceived speed of any vehicle in front or behind on the target lane).</li>
 * </ol>
 * <p>
 * This lane-based tactical planner makes decisions based on headway (GTU following model). It can ask the strategic planner for
 * assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedGTUFollowingDirectedChangeTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20160129L;

    /** Acceleration parameter type. */
    protected static final ParameterTypeAcceleration A = ParameterTypes.A;

    /** Desired headway parameter type. */
    protected static final ParameterTypeDuration T = ParameterTypes.T;

    /** Speed limit adherance factor parameter type. */
    protected static final ParameterTypeDouble FSPEED = ParameterTypes.FSPEED;

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Earliest next lane change time (unless we HAVE to change lanes). */
    private Time earliestNextLaneChangeTime = Time.ZERO;

    /** Time a GTU should stay in its current lane after a lane change. */
    private Duration durationInLaneAfterLaneChange = new Duration(15.0, DurationUnit.SECOND);

    /** Lane we changed to at instantaneous lane change. */
    private Lane laneAfterLaneChange = null;

    /** Position on the reference lane. */
    private Length posAfterLaneChange = null;

    /** When a failure in planning occurs, should we destroy the GTU to avoid halting of the model? */
    private boolean destroyGtuOnFailure = false;

    /**
     * Instantiated a tactical planner with just GTU following behavior and no lane changes.
     * @param carFollowingModel GTUFollowingModelOld; Car-following model.
     * @param gtu LaneBasedGTU; GTU
     */
    public LaneBasedGTUFollowingDirectedChangeTacticalPlanner(final GTUFollowingModelOld carFollowingModel,
            final LaneBasedGTU gtu)
    {
        super(carFollowingModel, gtu, new CategoricalLanePerception(gtu));
        getPerception().addPerceptionCategory(new DirectDefaultSimplePerception(getPerception()));
        setNoLaneChange(new Duration(0.25, DurationUnit.SECOND));
    }

    /**
     * Returns the car-following model.
     * @return The car-following model.
     */
    public final GTUFollowingModelOld getCarFollowingModelOld()
    {
        return (GTUFollowingModelOld) super.getCarFollowingModel();
    }

    /**
     * Indicate that no lane change should happen for the indicated duration.
     * @param noLaneChangeDuration Duration; the duration for which no lane change should happen.
     */
    public final void setNoLaneChange(final Duration noLaneChangeDuration)
    {
        Throw.when(noLaneChangeDuration.lt0(), RuntimeException.class, "noLaneChangeDuration should be >= 0");
        this.earliestNextLaneChangeTime = getGtu().getSimulator().getSimulatorAbsTime().plus(noLaneChangeDuration);
    }

    /**
     * Headway for synchronization.
     */
    private Headway syncHeadway;

    /**
     * Headway for cooperation.
     */
    private Headway coopHeadway;

    /**
     * Time when (potential) dead-lock was first recognized.
     */
    private Time deadLock = null;

    /**
     * Time after which situation is labeled a dead-lock.
     */
    private final Duration deadLockThreshold = new Duration(5.0, DurationUnit.SI);

    /**
     * Headways that are causing the dead-lock.
     */
    private Collection<Headway> blockingHeadways = new LinkedHashSet<>();

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:methodlength")
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, NetworkException, GTUException, ParameterException
    {
        try
        {

            // ask Perception for the local situation
            LaneBasedGTU laneBasedGTU = getGtu();
            DefaultSimplePerception simplePerception = getPerception().getPerceptionCategory(DefaultSimplePerception.class);
            Parameters parameters = laneBasedGTU.getParameters();
            // This is the only interaction between the car-following model and the parameters
            getCarFollowingModelOld().setA(parameters.getParameter(A));
            getCarFollowingModelOld().setT(parameters.getParameter(T));
            getCarFollowingModelOld().setFspeed(parameters.getParameter(FSPEED));

            // start with the turn indicator off -- this can change during the method
            laneBasedGTU.setTurnIndicatorStatus(TurnIndicatorStatus.NONE);

            // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
            if (laneBasedGTU.getMaximumSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                return new OperationalPlan(getGtu(), locationAtStartTime, startTime, new Duration(1.0, DurationUnit.SECOND));
            }

            // perceive the forward headway, accessible lanes and speed limit.
            simplePerception.updateForwardHeadwayGTU();
            simplePerception.updateForwardHeadwayObject();
            simplePerception.updateAccessibleAdjacentLanesLeft();
            simplePerception.updateAccessibleAdjacentLanesRight();
            simplePerception.updateSpeedLimit();

            // find out where we are going
            Length forwardHeadway = parameters.getParameter(LOOKAHEAD);
            LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, forwardHeadway);
            NextSplitInfo nextSplitInfo = determineNextSplit(laneBasedGTU, forwardHeadway);
            Set<Lane> correctLanes = laneBasedGTU.positions(laneBasedGTU.getReference()).keySet();
            correctLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());

            // Step 1: Do we want to change lanes because of the current lane not leading to our destination?
            this.syncHeadway = null;
            if (lanePathInfo.getPath().getLength().lt(forwardHeadway) && correctLanes.isEmpty())
            {
                LateralDirectionality direction = determineLeftRight(laneBasedGTU, nextSplitInfo);
                if (direction != null)
                {
                    getGtu().setTurnIndicatorStatus(direction.isLeft() ? TurnIndicatorStatus.LEFT : TurnIndicatorStatus.RIGHT);
                    if (canChange(laneBasedGTU, getPerception(), lanePathInfo, direction))
                    {
                        DirectedPoint newLocation = changeLane(laneBasedGTU, direction);
                        lanePathInfo = buildLanePathInfo(laneBasedGTU, forwardHeadway, this.laneAfterLaneChange,
                                this.posAfterLaneChange, laneBasedGTU.getDirection(this.laneAfterLaneChange));
                        return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                    }
                    else
                    {
                        simplePerception.updateNeighboringHeadways(direction);
                        Length minDistance = new Length(Double.MAX_VALUE, LengthUnit.SI);
                        for (Headway headway : simplePerception.getNeighboringHeadways(direction))
                        {
                            if ((headway.isAhead() || headway.isParallel()) && (headway instanceof AbstractHeadwayGTU))
                            {
                                if (headway.isParallel() || headway.getDistance().lt(minDistance))
                                {
                                    this.syncHeadway = headway;
                                    if (!headway.isParallel())
                                    {
                                        minDistance = headway.getDistance();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (this.syncHeadway != null && this.syncHeadway.isParallel() && getGtu().getSpeed().si < 10)
            {
                // do not sync at low speeds when being parallel
                this.syncHeadway = null;
            }

            // Cooperation
            this.coopHeadway = null;
            for (LateralDirectionality direction : new LateralDirectionality[] {LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT})
            {
                simplePerception.updateNeighboringHeadways(direction);
                for (Headway headway : simplePerception.getNeighboringHeadways(direction))
                {
                    // other vehicle ahead, its a vehicle, its the nearest, and its indicator is on
                    if (headway.isAhead() && (headway instanceof AbstractHeadwayGTU)
                            && (this.coopHeadway == null || headway.getDistance().lt(this.coopHeadway.getDistance()))
                            && (direction.isLeft() ? ((AbstractHeadwayGTU) headway).isRightTurnIndicatorOn()
                                    : ((AbstractHeadwayGTU) headway).isLeftTurnIndicatorOn()))
                    {
                        this.coopHeadway = headway;
                    }
                }
            }

            // Condition, if we have just changed lane, let's not change immediately again.
            if (getGtu().getSimulator().getSimulatorAbsTime().lt(this.earliestNextLaneChangeTime))
            {
                return currentLanePlan(laneBasedGTU, startTime, locationAtStartTime, lanePathInfo);
            }

            // Step 2. Do we want to change lanes to the left because of predecessor speed on the current lane?
            // And does the lane left of us bring us to our destination as well?
            Set<Lane> leftLanes = simplePerception.getAccessibleAdjacentLanesLeft().get(lanePathInfo.getReferenceLane());
            if (nextSplitInfo.isSplit())
            {
                leftLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());
            }
            if (!leftLanes.isEmpty()) // && laneBasedGTU.getSpeed().si > 4.0) // only if we are driving...
            {
                simplePerception.updateBackwardHeadway();
                simplePerception.updateParallelHeadwaysLeft();
                simplePerception.updateNeighboringHeadwaysLeft();
                if (simplePerception.getParallelHeadwaysLeft().isEmpty())
                {
                    Collection<Headway> sameLaneTraffic = new LinkedHashSet<>();
                    // TODO should it be getObjectType().isGtu() or !getObjectType().isDistanceOnly() ?
                    // XXX Object & GTU
                    if (simplePerception.getForwardHeadwayGTU() != null
                            && simplePerception.getForwardHeadwayGTU().getObjectType().isGtu())
                    {
                        sameLaneTraffic.add(simplePerception.getForwardHeadwayGTU());
                    }
                    if (simplePerception.getBackwardHeadway() != null
                            && simplePerception.getBackwardHeadway().getObjectType().isGtu())
                    {
                        sameLaneTraffic.add(simplePerception.getBackwardHeadway());
                    }
                    DirectedLaneChangeModel dlcm = new DirectedAltruistic(getPerception());
                    DirectedLaneMovementStep dlms = dlcm.computeLaneChangeAndAcceleration(laneBasedGTU,
                            LateralDirectionality.LEFT, sameLaneTraffic, simplePerception.getNeighboringHeadwaysLeft(),
                            parameters.getParameter(LOOKAHEAD), simplePerception.getSpeedLimit(),
                            // changes 1.0 to 0.0, no bias to the left: changed 0.5 to 0.1 (threshold from MOBIL model)
                            Acceleration.ZERO, new Acceleration(0.5, AccelerationUnit.SI),
                            new Duration(0.5, DurationUnit.SECOND));
                    if (dlms.getLaneChange() != null)
                    {
                        getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.LEFT);
                        if (canChange(laneBasedGTU, getPerception(), lanePathInfo, LateralDirectionality.LEFT))
                        {
                            DirectedPoint newLocation = changeLane(laneBasedGTU, LateralDirectionality.LEFT);
                            lanePathInfo = buildLanePathInfo(laneBasedGTU, forwardHeadway, this.laneAfterLaneChange,
                                    this.posAfterLaneChange, laneBasedGTU.getDirection(this.laneAfterLaneChange));
                            return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                        }
                    }
                }
            }

            // Step 3. Do we want to change lanes to the right because of TODO traffic rules?
            Set<Lane> rightLanes = simplePerception.getAccessibleAdjacentLanesRight().get(lanePathInfo.getReferenceLane());
            if (nextSplitInfo.isSplit())
            {
                rightLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());
            }
            if (!rightLanes.isEmpty()) // && laneBasedGTU.getSpeed().si > 4.0) // only if we are driving...
            {
                simplePerception.updateBackwardHeadway();
                simplePerception.updateParallelHeadwaysRight();
                simplePerception.updateNeighboringHeadwaysRight();
                if (simplePerception.getParallelHeadwaysRight().isEmpty())
                {
                    Collection<Headway> sameLaneTraffic = new LinkedHashSet<>();
                    // TODO should it be getObjectType().isGtu() or !getObjectType().isDistanceOnly() ?
                    // XXX GTU & Object
                    if (simplePerception.getForwardHeadwayGTU() != null
                            && simplePerception.getForwardHeadwayGTU().getObjectType().isGtu())
                    {
                        sameLaneTraffic.add(simplePerception.getForwardHeadwayGTU());
                    }
                    if (simplePerception.getBackwardHeadway() != null
                            && simplePerception.getBackwardHeadway().getObjectType().isGtu())
                    {
                        sameLaneTraffic.add(simplePerception.getBackwardHeadway());
                    }
                    DirectedLaneChangeModel dlcm = new DirectedAltruistic(getPerception());
                    DirectedLaneMovementStep dlms = dlcm.computeLaneChangeAndAcceleration(laneBasedGTU,
                            LateralDirectionality.RIGHT, sameLaneTraffic, simplePerception.getNeighboringHeadwaysRight(),
                            parameters.getParameter(LOOKAHEAD), simplePerception.getSpeedLimit(),
                            // 1.0 = bias?
                            Acceleration.ZERO, new Acceleration(0.1, AccelerationUnit.SI),
                            new Duration(0.5, DurationUnit.SECOND));
                    if (dlms.getLaneChange() != null)
                    {
                        getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.RIGHT);
                        if (canChange(laneBasedGTU, getPerception(), lanePathInfo, LateralDirectionality.RIGHT))
                        {
                            DirectedPoint newLocation = changeLane(laneBasedGTU, LateralDirectionality.RIGHT);
                            lanePathInfo = buildLanePathInfo(laneBasedGTU, forwardHeadway, this.laneAfterLaneChange,
                                    this.posAfterLaneChange, laneBasedGTU.getDirection(this.laneAfterLaneChange));
                            return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                        }
                    }
                }
            }

            if (this.deadLock != null
                    && getGtu().getSimulator().getSimulatorAbsTime().minus(this.deadLock).ge(this.deadLockThreshold)
                    && isDestroyGtuOnFailure())
            {
                System.err.println("Deleting gtu " + getGtu().getId() + " to prevent dead-lock.");
                try
                {
                    getGtu().getSimulator().scheduleEventRel(new Duration(0.001, DurationUnit.SI), this, getGtu(), "destroy",
                            new Object[0]);
                }
                catch (SimRuntimeException exception)
                {
                    throw new RuntimeException(exception);
                }
            }

            return currentLanePlan(laneBasedGTU, startTime, locationAtStartTime, lanePathInfo);
        }
        catch (GTUException | NetworkException | OperationalPlanException exception)
        {
            if (isDestroyGtuOnFailure())
            {
                System.err.println("LaneBasedGTUFollowingChange0TacticalPlanner.generateOperationalPlan() failed for "
                        + getGtu() + " because of " + exception.getMessage() + " -- GTU destroyed");
                getGtu().destroy();
                return new OperationalPlan(getGtu(), locationAtStartTime, startTime, new Duration(1.0, DurationUnit.SECOND));
            }
            throw exception;
        }
    }

    /**
     * Make a plan for the current lane.
     * @param laneBasedGTU LaneBasedGTU; the gtu to generate the plan for
     * @param startTime Time; the time from which the new operational plan has to be operational
     * @param locationAtStartTime DirectedPoint; the location of the GTU at the start time of the new plan
     * @param lanePathInfo LanePathInfo; the lane path for the current lane.
     * @return An operation plan for staying in the current lane.
     * @throws OperationalPlanException when there is a problem planning a path in the network
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     * @throws ParameterException in case LOOKAHEAD parameter cannot be found
     * @throws NetworkException in case the headways to GTUs or objects cannot be calculated
     */
    private OperationalPlan currentLanePlan(final LaneBasedGTU laneBasedGTU, final Time startTime,
            final DirectedPoint locationAtStartTime, final LanePathInfo lanePathInfo)
            throws OperationalPlanException, GTUException, ParameterException, NetworkException
    {
        DefaultSimplePerception simplePerception = getPerception().getPerceptionCategory(DefaultSimplePerception.class);

        // No lane change. Continue on current lane.
        AccelerationStep accelerationStep = mostLimitingAccelerationStep(lanePathInfo, simplePerception.getForwardHeadwayGTU(),
                simplePerception.getForwardHeadwayObject());

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6 && laneBasedGTU.getSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(laneBasedGTU, locationAtStartTime, startTime, accelerationStep.getDuration());
        }

        // build a list of lanes forward, with a maximum headway.
        List<Segment> operationalPlanSegmentList = new ArrayList<>();
        if (accelerationStep.getAcceleration().si == 0.0)
        {
            Segment segment = new OperationalPlan.SpeedSegment(accelerationStep.getDuration());
            operationalPlanSegmentList.add(segment);
        }
        else
        {
            Segment segment =
                    new OperationalPlan.AccelerationSegment(accelerationStep.getDuration(), accelerationStep.getAcceleration());
            operationalPlanSegmentList.add(segment);
        }
        OperationalPlan op = new OperationalPlan(laneBasedGTU, lanePathInfo.getPath(), startTime, laneBasedGTU.getSpeed(),
                operationalPlanSegmentList);
        return op;
    }

    /**
     * We are not on a lane that leads to our destination. Determine whether the lateral direction to go is left or right.
     * @param laneBasedGTU LaneBasedGTU; the gtu
     * @param nextSplitInfo NextSplitInfo; the information about the next split
     * @return the lateral direction to go, or null if this cannot be determined
     */
    private LateralDirectionality determineLeftRight(final LaneBasedGTU laneBasedGTU, final NextSplitInfo nextSplitInfo)
    {
        // are the lanes in nextSplitInfo.getCorrectCurrentLanes() left or right of the current lane(s) of the GTU?
        try
        {
            Set<Lane> lanes = laneBasedGTU.positions(laneBasedGTU.getReference()).keySet();
            for (Lane correctLane : nextSplitInfo.getCorrectCurrentLanes())
            {
                for (Lane currentLane : lanes)
                {
                    if (correctLane.getParentLink().equals(currentLane.getParentLink()))
                    {
                        double deltaOffset =
                                correctLane.getDesignLineOffsetAtBegin().si - currentLane.getDesignLineOffsetAtBegin().si;
                        if (laneBasedGTU.getDirection(currentLane).equals(GTUDirectionality.DIR_PLUS))
                        {
                            return deltaOffset > 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
                        }
                        else
                        {
                            return deltaOffset < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
                        }
                    }
                }
            }
        }
        catch (GTUException exception)
        {
            System.err.println(
                    "Exception in LaneBasedGTUFollowingChange0TacticalPlanner.determineLeftRight: " + exception.getMessage());
        }
        // perhaps known from split info (if need to change away from all lanes on current link)
        return nextSplitInfo.getRequiredDirection();
    }

    /**
     * See if a lane change in the given direction if possible.
     * @param gtu LaneBasedGTU; the GTU that has to make the lane change
     * @param perception LanePerception; the perception, where forward headway, accessible lanes and speed limit have been
     *            assessed
     * @param lanePathInfo LanePathInfo; the information for the path on the current lane
     * @param direction LateralDirectionality; the lateral direction, either LEFT or RIGHT
     * @return whether a lane change is possible.
     * @throws NetworkException when there is a network inconsistency in updating the perception
     * @throws GTUException when there is an issue retrieving GTU information for the perception update
     * @throws ParameterException when there is a parameter problem.
     * @throws OperationalPlanException in case a perception category is not present
     */
    private boolean canChange(final LaneBasedGTU gtu, final LanePerception perception, final LanePathInfo lanePathInfo,
            final LateralDirectionality direction)
            throws GTUException, NetworkException, ParameterException, OperationalPlanException
    {

        // TODO remove this hack
        if (!((AbstractLaneBasedGTU) gtu).isSafeToChange())
        {
            return false;
        }

        // rear should be able to change
        Map<Lane, Length> positions = getGtu().positions(getGtu().getRear());
        for (Lane lane : positions.keySet())
        {
            Length pos = positions.get(lane);
            if (pos.si > 0.0 && pos.si < lane.getLength().si && lane
                    .accessibleAdjacentLanesLegal(direction, getGtu().getGTUType(), getGtu().getDirection(lane)).isEmpty())
            {
                return false;
            }
        }

        Collection<Headway> otherLaneTraffic;
        DefaultSimplePerception simplePerception = getPerception().getPerceptionCategory(DefaultSimplePerception.class);
        simplePerception.updateForwardHeadwayGTU();
        simplePerception.updateForwardHeadwayObject();
        simplePerception.updateBackwardHeadway();
        if (direction.isLeft())
        {
            simplePerception.updateParallelHeadwaysLeft();
            this.blockingHeadways = simplePerception.getParallelHeadwaysLeft();
            simplePerception.updateNeighboringHeadwaysLeft();
            otherLaneTraffic = simplePerception.getNeighboringHeadwaysLeft();
        }
        else if (direction.isRight())
        {
            simplePerception.updateParallelHeadwaysRight();
            this.blockingHeadways = simplePerception.getParallelHeadwaysRight();
            simplePerception.updateNeighboringHeadwaysRight();
            otherLaneTraffic = simplePerception.getNeighboringHeadwaysRight();
        }
        else
        {
            throw new GTUException("Lateral direction is neither LEFT nor RIGHT during a lane change");
        }
        if (!simplePerception.getParallelHeadways(direction).isEmpty())
        {
            return false;
        }

        Collection<Headway> sameLaneTraffic = new LinkedHashSet<>();
        // TODO should it be getObjectType().isGtu() or !getObjectType().isDistanceOnly() ?
        // XXX Object & GTU
        if (simplePerception.getForwardHeadwayGTU() != null && simplePerception.getForwardHeadwayGTU().getObjectType().isGtu())
        {
            sameLaneTraffic.add(simplePerception.getForwardHeadwayGTU());
        }
        if (simplePerception.getBackwardHeadway() != null && simplePerception.getBackwardHeadway().getObjectType().isGtu())
        {
            sameLaneTraffic.add(simplePerception.getBackwardHeadway());
        }

        // TODO make type of plan (Egoistic, Altruistic) parameter of the class
        DirectedLaneChangeModel dlcm = new DirectedEgoistic(getPerception());
        // TODO make the elasticities 2.0 and 0.1 parameters of the class
        DirectedLaneMovementStep dlms = dlcm.computeLaneChangeAndAcceleration(gtu, direction, sameLaneTraffic, otherLaneTraffic,
                gtu.getParameters().getParameter(LOOKAHEAD), simplePerception.getSpeedLimit(),
                new Acceleration(2.0, AccelerationUnit.SI), new Acceleration(0.1, AccelerationUnit.SI),
                new Duration(0.5, DurationUnit.SECOND));
        if (dlms.getLaneChange() == null)
        {
            return false;
        }

        return true;
    }

    /**
     * Change lanes instantaneously.
     * @param gtu LaneBasedGTU; the gtu
     * @param direction LateralDirectionality; the direction
     * @return the new location of the GTU after the lane change
     * @throws GTUException in case the enter lane fails
     */
    private DirectedPoint changeLane(final LaneBasedGTU gtu, final LateralDirectionality direction) throws GTUException
    {
        gtu.changeLaneInstantaneously(direction);

        // stay at a certain number of seconds in the current lane (unless we HAVE to change lanes)
        this.earliestNextLaneChangeTime = gtu.getSimulator().getSimulatorAbsTime().plus(this.durationInLaneAfterLaneChange);

        // make sure out turn indicator is on!
        gtu.setTurnIndicatorStatus(direction.isLeft() ? TurnIndicatorStatus.LEFT : TurnIndicatorStatus.RIGHT);

        this.laneAfterLaneChange = gtu.getReferencePosition().getLane();
        this.posAfterLaneChange = gtu.getReferencePosition().getPosition();
        return gtu.getLocation();
    }

    /**
     * Calculate which Headway in front of us is leading to the most limiting acceleration step (i.e. to the lowest or most
     * negative acceleration). There could, e.g. be a GTU in front of us, a speed sign in front of us, and a traffic light in
     * front of the GTU and speed sign. This method will return the acceleration based on the headway that limits us most.<br>
     * The method can e.g., be called with:
     * <code>mostLimitingHeadway(simplePerception.getForwardHeadwayGTU(), simplePerception.getForwardHeadwayObject());</code>
     * @param lanePathInfo LanePathInfo; the lane path info that was calculated for this GTU.
     * @param headways Headway...; zero or more headways specifying possible limitations on our acceleration.
     * @return the acceleration based on the most limiting headway.
     * @throws OperationalPlanException in case the PerceptionCategory cannot be found
     * @throws ParameterException in case LOOKAHEAD parameter cannot be found
     * @throws GTUException in case the AccelerationStep cannot be calculated
     * @throws NetworkException in case the headways to GTUs or objects cannot be calculated
     */
    private AccelerationStep mostLimitingAccelerationStep(final LanePathInfo lanePathInfo, final Headway... headways)
            throws OperationalPlanException, ParameterException, GTUException, NetworkException
    {
        DefaultSimplePerception simplePerception = getPerception().getPerceptionCategory(DefaultSimplePerception.class);
        simplePerception.updateForwardHeadwayGTU();
        simplePerception.updateForwardHeadwayObject();
        boolean sinkAtEnd = false;
        for (SingleSensor sensor : (lanePathInfo.getLanes().get(lanePathInfo.getLanes().size() - 1).getSensors()))
        {
            if (sensor instanceof SinkSensor)
            {
                sinkAtEnd = true;
            }
        }
        boolean stopForEndOrSplit = !sinkAtEnd;
        Parameters params = getGtu().getParameters();
        Length maxDistance = sinkAtEnd ? new Length(Double.MAX_VALUE, LengthUnit.SI)
                : Length.min(getGtu().getParameters().getParameter(LOOKAHEAD),
                        lanePathInfo.getPath().getLength().minus(getGtu().getLength().times(2.0)));
        // params.setParameter(B, params.getParameter(B0));
        AccelerationStep mostLimitingAccelerationStep = getCarFollowingModelOld().computeAccelerationStepWithNoLeader(getGtu(),
                maxDistance, simplePerception.getSpeedLimit());
        // bc.resetParameter(B);
        Acceleration minB = params.getParameter(B).neg();
        Acceleration numericallySafeB =
                Acceleration.max(minB, getGtu().getSpeed().divide(mostLimitingAccelerationStep.getDuration()).neg());
        if ((this.syncHeadway != null || this.coopHeadway != null) && mostLimitingAccelerationStep.getAcceleration().gt(minB))
        {
            AccelerationStep sync;
            if (this.syncHeadway == null)
            {
                sync = null;
            }
            else if (this.syncHeadway.isParallel())
            {
                sync = new AccelerationStep(numericallySafeB, mostLimitingAccelerationStep.getValidUntil(),
                        mostLimitingAccelerationStep.getDuration());
            }
            else
            {
                sync = getCarFollowingModelOld().computeAccelerationStep(getGtu(), this.syncHeadway.getSpeed(),
                        this.syncHeadway.getDistance(), maxDistance, simplePerception.getSpeedLimit());
            }
            AccelerationStep coop;
            if (this.coopHeadway == null)
            {
                coop = null;
            }
            else
            {
                coop = getCarFollowingModelOld().computeAccelerationStep(getGtu(), this.coopHeadway.getSpeed(),
                        this.coopHeadway.getDistance(), maxDistance, simplePerception.getSpeedLimit());
            }
            AccelerationStep adjust;
            if (sync == null)
            {
                adjust = coop;
            }
            else if (coop == null)
            {
                adjust = sync;
            }
            else
            {
                adjust = sync.getAcceleration().lt(coop.getAcceleration()) ? sync : coop;
            }
            if (adjust.getAcceleration().lt(minB))
            {
                mostLimitingAccelerationStep = new AccelerationStep(numericallySafeB,
                        mostLimitingAccelerationStep.getValidUntil(), mostLimitingAccelerationStep.getDuration());
            }
            else
            {
                mostLimitingAccelerationStep = adjust;
            }
        }

        for (Headway headway : headways)
        {
            if (headway != null && headway.getDistance().lt(maxDistance))
            {
                AccelerationStep accelerationStep = getCarFollowingModelOld().computeAccelerationStep(getGtu(),
                        headway.getSpeed(), headway.getDistance(), maxDistance, simplePerception.getSpeedLimit());
                if (accelerationStep.getAcceleration().lt(mostLimitingAccelerationStep.getAcceleration()))
                {
                    stopForEndOrSplit = false;
                    mostLimitingAccelerationStep = accelerationStep;
                }
            }
        }

        // recognize dead-lock
        if (!this.blockingHeadways.isEmpty() && stopForEndOrSplit)
        {
            Speed maxSpeed = getGtu().getSpeed();
            for (Headway headway : this.blockingHeadways)
            {
                maxSpeed = Speed.max(maxSpeed, headway.getSpeed());
            }
            if (maxSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                if (this.deadLock == null)
                {
                    this.deadLock = getGtu().getSimulator().getSimulatorAbsTime();
                }
            }
            else
            {
                this.deadLock = null;
            }
        }
        else
        {
            this.deadLock = null;
        }

        return mostLimitingAccelerationStep;

    }

    /**
     * @return destroyGtuOnFailure, indicating when a failure in planning occurs, whether we should destroy the GTU to avoid
     *         halting of the model
     */
    public final boolean isDestroyGtuOnFailure()
    {
        return this.destroyGtuOnFailure;
    }

    /**
     * When a failure in planning occurs, should we destroy the GTU to avoid halting of the model?
     * @param destroyGtuOnFailure boolean; set destroyGtuOnFailure to true or false
     */
    public final void setDestroyGtuOnFailure(final boolean destroyGtuOnFailure)
    {
        this.destroyGtuOnFailure = destroyGtuOnFailure;
    }

    /**
     * Get the duration to stay in a Lane after a lane change.
     * @return Duration; durationInLaneAfterLaneChange
     */
    protected final Duration getDurationInLaneAfterLaneChange()
    {
        return this.durationInLaneAfterLaneChange;
    }

    /**
     * Set the duration to stay in a Lane after a lane change.
     * @param durationInLaneAfterLaneChange Duration; set duration to stay in a Lane after a lane change
     * @throws GTUException when durationInLaneAfterLaneChange less than zero
     */
    protected final void setDurationInLaneAfterLaneChange(final Duration durationInLaneAfterLaneChange) throws GTUException
    {
        Throw.when(durationInLaneAfterLaneChange.lt0(), GTUException.class, "durationInLaneAfterLaneChange should be >= 0");
        this.durationInLaneAfterLaneChange = durationInLaneAfterLaneChange;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGTUFollowingChange0TacticalPlanner [earliestNexLaneChangeTime=" + this.earliestNextLaneChangeTime
                + ", referenceLane=" + this.laneAfterLaneChange + ", referencePos=" + this.posAfterLaneChange
                + ", destroyGtuOnFailure=" + this.destroyGtuOnFailure + "]";
    }

}
