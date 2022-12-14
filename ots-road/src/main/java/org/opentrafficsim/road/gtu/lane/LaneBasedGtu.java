package org.opentrafficsim.road.gtu.lane;

import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.event.EventType;
import org.djutils.event.TimedEventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * This interface defines a lane based GTU.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface LaneBasedGtu extends Gtu
{
    /** @return the road network to which the LaneBasedGtu belongs */
    RoadNetwork getNetwork();

    /** {@inheritDoc} */
    @Override
    LaneBasedStrategicalPlanner getStrategicalPlanner();

    /** {@inheritDoc} */
    @Override
    LaneBasedStrategicalPlanner getStrategicalPlanner(Time time);

    /** {@inheritDoc} */
    @Override
    default LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return getStrategicalPlanner().getTacticalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    default LaneBasedTacticalPlanner getTacticalPlanner(final Time time)
    {
        return getStrategicalPlanner(time).getTacticalPlanner(time);
    }

    /**
     * Return the location without a RemoteException. {@inheritDoc}
     */
    @Override
    DirectedPoint getLocation();

    /**
     * Change lanes instantaneously.
     * @param laneChangeDirection LateralDirectionality; the direction to change to
     * @throws GtuException in case lane change fails
     */
    void changeLaneInstantaneously(LateralDirectionality laneChangeDirection) throws GtuException;

    /**
     * Register on lanes in target lane.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     * @throws GtuException exception
     */
    void initLaneChange(LateralDirectionality laneChangeDirection) throws GtuException;

    /**
     * Sets event to finalize lane change.
     * @param event SimEventInterface&lt;SimTimeDoubleUnit&gt;; event
     */
    void setFinalizeLaneChangeEvent(SimEventInterface<Duration> event);

    /**
     * Returns the next lane for a given lane to stay on the route.
     * @param lane Lane; the lane for which we want to know the next Lane
     * @return Lane; next lane, {@code null} if none
     */
    Lane getNextLaneForRoute(Lane lane);

    /**
     * Returns a set of {@code Lane}s that can be followed considering the route.
     * @param lane Lane; the lane for which we want to know the next Lane
     * @return set of {@code Lane}s that can be followed considering the route
     */
    Set<Lane> getNextLanesForRoute(Lane lane);

    /**
     * Get projected length on the lane.
     * @param lane Lane; lane to project the vehicle on
     * @return Length; the length on the lane, which is different from the actual length during deviative tactical plans
     * @throws GtuException when the vehicle is not on the given lane
     */
    default Length getProjectedLength(final Lane lane) throws GtuException
    {
        Length front = position(lane, getFront());
        Length rear = position(lane, getRear());
        return front.minus(rear);
    }

    /**
     * Sets whether the GTU perform lane changes instantaneously or not.
     * @param instantaneous boolean; whether the GTU perform lane changes instantaneously or not
     */
    void setInstantaneousLaneChange(boolean instantaneous);

    /**
     * Returns whether the GTU perform lane changes instantaneously or not.
     * @return boolean; whether the GTU perform lane changes instantaneously or not
     */
    boolean isInstantaneousLaneChange();

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered. <br>
     * <b>Note:</b> If a GTU is registered in multiple parallel lanes, the lateralLaneChangeModel is used to determine the
     * center line of the vehicle at this point in time. Otherwise, the average of the center positions of the lines will be
     * taken.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition) throws GtuException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition, Time when) throws GtuException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane at the current
     * simulation time. <br>
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GtuException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition) throws GtuException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane.
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GtuException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition, Time when) throws GtuException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.<br>
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition) throws GtuException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GtuException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, Time when) throws GtuException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the fractional relative position on the lane at the given time.
     * @throws GtuException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition, Time when) throws GtuException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.<br>
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the fractional relative position on the lane at the given time.
     * @throws GtuException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition) throws GtuException;

    /**
     * Return the current Lane, position and directionality of the GTU.
     * @return DirectedLanePosition; the current Lane, position and directionality of the GTU
     * @throws GtuException in case the reference position of the GTU cannot be found on the lanes in its current path
     */
    LanePosition getReferencePosition() throws GtuException;

    /**
     * Add an event to the list of lane triggers scheduled for this GTU.
     * @param lane Lane; the lane on which the event occurs
     * @param event SimEventInterface&lt;SimTimeDoubleUnit&gt;; SimeEvent&lt;SimTimeDoubleUnit&gt; the event
     */
    void addTrigger(Lane lane, SimEventInterface<Duration> event);

    /**
     * Set distance over which the GTU should not change lane after being created.
     * @param distance Length; distance over which the GTU should not change lane after being created
     */
    void setNoLaneChangeDistance(Length distance);

    /**
     * Returns whether a lane change is allowed.
     * @return whether a lane change is allowed
     */
    boolean laneChangeAllowed();

    /**
     * This method returns the current desired speed of the GTU. This value is required often, so implementations can cache it.
     * @return Speed; current desired speed
     */
    Speed getDesiredSpeed();

    /**
     * This method returns the current car-following acceleration of the GTU. This value is required often, so implementations
     * can cache it.
     * @return Acceleration; current car-following acceleration
     */
    Acceleration getCarFollowingAcceleration();

    /**
     * Returns the vehicle model.
     * @return VehicleModel; vehicle model
     */
    default VehicleModel getVehicleModel()
    {
        return VehicleModel.MINMAX;
    }

    /**
     * The default implementation returns {@code true} if the deceleration is larger than a speed-dependent threshold given
     * by:<br>
     * <br>
     * c0 * g(v) + c1 + c3*v^2<br>
     * <br>
     * where c0 = 0.2, c1 = 0.15 and c3 = 0.00025 (with c2 = 0 implicit) are empirically derived averages, and g(v) is 0 below
     * 25 km/h or 1 otherwise, representing that the engine is disengaged at low speeds.
     * @return boolean; whether the braking lights are on
     */
    default boolean isBrakingLightsOn()
    {
        return isBrakingLightsOn(getSimulator().getSimulatorAbsTime());
    }

    /**
     * The default implementation returns {@code true} if the deceleration is larger than a speed-dependent threshold given
     * by:<br>
     * <br>
     * c0 * g(v) + c1 + c3*v^2<br>
     * <br>
     * where c0 = 0.2, c1 = 0.15 and c3 = 0.00025 (with c2 = 0 implicit) are empirically derived averages, and g(v) is 0 below
     * 25 km/h or 1 otherwise, representing that the engine is disengaged at low speeds.
     * @param when Time; time
     * @return boolean; whether the braking lights are on
     */
    default boolean isBrakingLightsOn(final Time when)
    {
        double v = getSpeed(when).si;
        double a = getAcceleration(when).si;
        return a < (v < 6.944 ? 0.0 : -0.2) - 0.15 - 0.00025 * v * v;
    }

    /**
     * Returns the lateral position of the GTU relative to the lane center line. Negative values are towards the right.
     * @param lane Lane; lane to consider (most important regarding left/right, not upstream downstream)
     * @return Length; lateral position of the GTU relative to the lane center line
     * @throws GtuException when the vehicle is not on the given lane.
     */
    Length getLateralPosition(Lane lane) throws GtuException;

    /** @return the status of the turn indicator */
    TurnIndicatorStatus getTurnIndicatorStatus();

    /**
     * @param time Time; time to obtain the turn indicator status at
     * @return the status of the turn indicator at the given time
     */
    TurnIndicatorStatus getTurnIndicatorStatus(Time time);

    /**
     * Set the status of the turn indicator.
     * @param turnIndicatorStatus TurnIndicatorStatus; the new status of the turn indicator.
     * @throws GtuException when GtuType does not have a turn indicator
     */
    void setTurnIndicatorStatus(TurnIndicatorStatus turnIndicatorStatus) throws GtuException;

    /**
     * The lane-based event type for pub/sub indicating the initialization of a new GTU. <br>
     * Payload: [String gtuId, PositionVector initialPosition, Direction initialDirection, Length length, Length width, String
     * linkId, String laneId, Length positionOnReferenceLane, GTUDirectionality direction, GtuType gtuType]
     */
    TimedEventType LANEBASED_INIT_EVENT = new TimedEventType("LANEBASEDGTU.INIT", new MetaData("Lane based GTU created",
            "Lane based GTU created",
            new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("initial position", "initial position", PositionVector.class),
                    new ObjectDescriptor("initial direction", "initial direction", Direction.class),
                    new ObjectDescriptor("Length", "Length", Length.class),
                    new ObjectDescriptor("Width", "Width", Length.class),
                    new ObjectDescriptor("Link id", "Link id", String.class),
                    new ObjectDescriptor("Lane id", "Lane id", String.class),
                    new ObjectDescriptor("Longitudinal position on lane", "Longitudinal position on lane", Length.class),
                    new ObjectDescriptor("GTU type name", "GTU type name", String.class)}));

    /**
     * The lane-based event type for pub/sub indicating a move. <br>
     * Payload: [String gtuId, PositionVector currentPosition, Direction currentDirection, Speed speed, Acceleration
     * acceleration, TurnIndicatorStatus turnIndicatorStatus, Length odometer, Link id of referenceLane, Lane id of
     * referenceLane, Length positionOnReferenceLane, GTUDirectionality direction]
     */
    TimedEventType LANEBASED_MOVE_EVENT = new TimedEventType("LANEBASEDGTU.MOVE", new MetaData("Lane based GTU moved",
            "Lane based GTU moved",
            new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Position", "Position", PositionVector.class),
                    new ObjectDescriptor("Direction", "Direction", Direction.class),
                    new ObjectDescriptor("Speed", "Speed", Speed.class),
                    new ObjectDescriptor("Acceleration", "Acceleration", Acceleration.class),
                    new ObjectDescriptor("TurnIndicatorStatus", "Turn indicator status", String.class),
                    new ObjectDescriptor("Odometer", "Odometer value", Length.class),
                    new ObjectDescriptor("Link id", "Link id", String.class),
                    new ObjectDescriptor("Lane id", "Lane id", String.class),
                    new ObjectDescriptor("Longitudinal position on lane", "Longitudinal position on lane", Length.class)}));

    /**
     * The lane-based event type for pub/sub indicating destruction of the GTU. <br>
     * Payload: [String gtuId, PositionVector finalPosition, Direction finalDirection, Length finalOdometer, Link referenceLink,
     * Lane referenceLane, Length positionOnReferenceLane, GTUDirectionality direction]
     */
    TimedEventType LANEBASED_DESTROY_EVENT = new TimedEventType("LANEBASEDGTU.DESTROY", new MetaData("Lane based GTU destroyed",
            "Lane based GTU destroyed",
            new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Position", "Position", PositionVector.class),
                    new ObjectDescriptor("Direction", "Direction", Direction.class),
                    new ObjectDescriptor("Odometer", "Odometer value", Length.class),
                    new ObjectDescriptor("Link id", "Link id", String.class),
                    new ObjectDescriptor("Lane id", "Lane id", String.class),
                    new ObjectDescriptor("Longitudinal position on lane", "Longitudinal position on lane", Length.class)}));

    // TODO: the next 2 events are never fired...
    /**
     * The event type for pub/sub indicating that the GTU entered a new lane (with the FRONT position if driving forward; REAR
     * if driving backward). <br>
     * Payload: [String gtuId, String link id, String lane id]
     */
    EventType LANE_ENTER_EVENT = new EventType("LANE.ENTER",
            new MetaData("Lane based GTU entered lane", "Front of lane based GTU entered lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class)}));

    /**
     * The event type for pub/sub indicating that the GTU exited a lane (with the REAR position if driving forward; FRONT if
     * driving backward). <br>
     * Payload: [String gtuId, String link id, String lane id]
     */
    EventType LANE_EXIT_EVENT = new EventType("LANE.EXIT",
            new MetaData("Lane based GTU exited lane", "Rear of lane based GTU exited lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class)}));

    /**
     * The event type for pub/sub indicating that the GTU change lane. <br>
     * Payload: [String gtuId, LateralDirectionality direction, DirectedLanePosition from]
     */
    TimedEventType LANE_CHANGE_EVENT = new TimedEventType("LANE.CHANGE",
            new MetaData("Lane based GTU changes lane", "Lane based GTU changes lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Lateral direction of lane change", "Lateral direction of lane change",
                                    String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id of vacated lane", "Lane id of vacated lane", String.class),
                            new ObjectDescriptor("Position along vacated lane", "Position along vacated lane", Length.class)}));

}
