package org.opentrafficsim.road.gtu.lane;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * This interface defines a lane based GTU.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTU extends GTU
{

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
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the GTU.
     * The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and rear positions
     * are relative towards this position.
     * @param lane Lane; the lane to add to the list of lanes on which the GTU is registered.
     * @param gtuDirection GTUDirectionality; the direction of the GTU on the lane (which can be bidirectional). If the GTU has
     *            a positive speed, it is moving in this direction.
     * @param position Length; the position on the lane.
     * @throws GTUException when positioning the GTU on the lane causes a problem
     */
    void enterLane(Lane lane, Length position, GTUDirectionality gtuDirection) throws GTUException;

    /**
     * Unregister the GTU from a lane.
     * @param lane Lane; the lane to remove from the list of lanes on which the GTU is registered.
     * @throws GTUException when leaveLane should not be called
     */
    void leaveLane(Lane lane) throws GTUException;

    /**
     * Change lanes instantaneously.
     * @param laneChangeDirection LateralDirectionality; the direction to change to
     * @throws GTUException in case lane change fails
     */
    void changeLaneInstantaneously(LateralDirectionality laneChangeDirection) throws GTUException;

    /**
     * Sets event to finalize lane change.
     * @param event SimEventInterface&lt;SimTimeDoubleUnit&gt;; event
     */
    void setFinalizeLaneChangeEvent(SimEventInterface<SimTimeDoubleUnit> event);

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered. <br>
     * <b>Note:</b> If a GTU is registered in multiple parallel lanes, the lateralLaneChangeModel is used to determine the
     * center line of the vehicle at this point in time. Otherwise, the average of the center positions of the lines will be
     * taken.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane at the current
     * simulation time. <br>
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane.
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.<br>
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @param when Time; the future time for which to calculate the positions.
     * @return the fractional relative position on the lane at the given time.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.<br>
     * @param lane Lane; the position on this lane will be returned.
     * @param relativePosition RelativePosition; the position on the vehicle relative to the reference point.
     * @return the fractional relative position on the lane at the given time.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal position that the indicated relative position of this GTU would have if it were to change to
     * another Lane with a / the current CrossSectionLink. This point may be before the begin or after the end of the link of
     * the projection lane of the GTU. This preserves the length of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GTUException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    Length translatedPosition(Lane projectionLane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position on the projection lane that has the same fractional position on one of the current lanes
     * of the indicated relative position. This preserves the fractional positions of all relative positions of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GTUException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    Length projectedPosition(Lane projectionLane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the current Lane, position and directionality of the GTU.
     * @return DirectedLanePosition; the current Lane, position and directionality of the GTU
     * @throws GTUException in case the reference position of the GTU cannot be found on the lanes in its current path
     */
    DirectedLanePosition getReferencePosition() throws GTUException;

    /**
     * Return the directionality of a lane on which the GTU is registered for its current operational plan.
     * @param lane Lane; the lane for which we want to know the direction
     * @return GTUDirectionality; the direction on the given lane
     * @throws GTUException in case the GTU is not registered on the Lane
     */
    GTUDirectionality getDirection(Lane lane) throws GTUException;

    /**
     * Add an event to the list of lane triggers scheduled for this GTU.
     * @param lane Lane; the lane on which the event occurs
     * @param event SimEventInterface&lt;SimTimeDoubleUnit&gt;; SimeEvent&lt;SimTimeDoubleUnit&gt; the event
     */
    void addTrigger(Lane lane, SimEventInterface<SimTimeDoubleUnit> event);

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
        return isBrakingLightsOn(getSimulator().getSimulatorTime());
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
        return a < (v < 6.944 ? 0.0 : -0.2) - 0.15 * v - 0.00025 * v * v;
    }

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
     * @throws GTUException when GTUType does not have a turn indicator
     */
    void setTurnIndicatorStatus(TurnIndicatorStatus turnIndicatorStatus) throws GTUException;

    /**
     * The lane-based event type for pub/sub indicating the initialization of a new GTU. <br>
     * Payload: [String gtuId, DirectedPoint initialPosition, Length length, Length width, Lane referenceLane, Length
     * positionOnReferenceLane, GTUDirectionality direction, GTUType gtuType]
     */
    EventType LANEBASED_INIT_EVENT = new EventType("LANEBASEDGTU.INIT");

    /**
     * The lane-based event type for pub/sub indicating a move. <br>
     * Payload: [String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
     * turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane, GTUDirectionality direction]
     */
    EventType LANEBASED_MOVE_EVENT = new EventType("LANEBASEDGTU.MOVE");

    /**
     * The lane-based event type for pub/sub indicating destruction of the GTU. <br>
     * Payload: [String gtuId, DirectedPoint lastPosition, Length odometer, Lane referenceLane, Length positionOnReferenceLane,
     * GTUDirectionality direction]
     */
    EventType LANEBASED_DESTROY_EVENT = new EventType("LANEBASEDGTU.DESTROY");

    /**
     * The event type for pub/sub indicating that the GTU entered a new link (with the FRONT position if driving forward; REAR
     * if driving backward). <br>
     * Payload: [String gtuId, Link link]
     */
    EventType LINK_ENTER_EVENT = new EventType("LINK.ENTER");

    /**
     * The event type for pub/sub indicating that the GTU exited a link (with the REAR position if driving forward; FRONT if
     * driving backward). <br>
     * Payload: [String gtuId, Link link]
     */
    EventType LINK_EXIT_EVENT = new EventType("LINK.EXIT");

    /**
     * The event type for pub/sub indicating that the GTU entered a new lane (with the FRONT position if driving forward; REAR
     * if driving backward). <br>
     * Payload: [String gtuId, Lane lane]
     */
    EventType LANE_ENTER_EVENT = new EventType("LANE.ENTER");

    /**
     * The event type for pub/sub indicating that the GTU exited a lane (with the REAR position if driving forward; FRONT if
     * driving backward). <br>
     * Payload: [String gtuId, Lane lane]
     */
    EventType LANE_EXIT_EVENT = new EventType("LANE.EXIT");

    /**
     * The event type for pub/sub indicating that the GTU change lane. <br>
     * Payload: [String gtuId, LateralDirectionality direction, DirectedLanePosition from]
     */
    EventType LANE_CHANGE_EVENT = new EventType("LANE.CHANGE");

}
