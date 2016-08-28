package org.opentrafficsim.road.gtu.lane;

import java.util.Map;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.event.EventType;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * This interface defines a lane based GTU.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    LaneBasedTacticalPlanner getTacticalPlanner();

    /**
     * @return a safe copy of the lanes on which the GTU is registered.
     */
    Map<Lane, GTUDirectionality> getLanes();

    /**
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the GTU.
     * The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and rear positions
     * are relative towards this position.
     * @param lane the lane to add to the list of lanes on which the GTU is registered.
     * @param gtuDirection the direction of the GTU on the lane (which can be bidirectional). If the GTU has a positive speed,
     *            it is moving in this direction.
     * @param position the position on the lane.
     * @throws GTUException when positioning the GTU on the lane causes a problem
     */
    void enterLane(Lane lane, Length position, GTUDirectionality gtuDirection) throws GTUException;

    /**
     * Unregister the GTU from a lane.
     * @param lane the lane to remove from the list of lanes on which the GTU is registered.
     * @throws GTUException when leaveLane should not be called
     */
    void leaveLane(Lane lane) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered. <br>
     * <b>Note:</b> If a GTU is registered in multiple parallel lanes, the lateralLaneChangeModel is used to determine the
     * center line of the vehicle at this point in time. Otherwise, the average of the center positions of the lines will be
     * taken.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length> positions(RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane at the current
     * simulation time. <br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane.
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    Length position(Lane lane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.<br>
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws GTUException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the fractional relative position on the lane at the given time.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.<br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the fractional relative position on the lane at the given time.
     * @throws GTUException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition) throws GTUException;

    /**
     * Return the longitudinal position that this GTU would have if it were to change to another Lane with a / the current
     * CrossSectionLink.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GTUException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    Length projectedPosition(Lane projectionLane, RelativePosition relativePosition, Time when) throws GTUException;

    /**
     * Add an event to the list of lane triggers scheduled for this GTU.
     * @param lane Lane; the lane on which the event occurs
     * @param event SimeEvent&lt;OTSSimTimeDouble&gt; the event
     */
    void addTrigger(Lane lane, SimEvent<OTSSimTimeDouble> event);

    /**
     * The lane-based event type for pub/sub indicating a move. <br>
     * Payload: [String id, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
     * turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane]
     */
    EventType MOVE_EVENT = new EventType("LANEBASEDGTU.MOVE");

    /**
     * The lane-based event type for pub/sub indicating the initialization of a new GTU. <br>
     * Payload: [String id, DirectedPoint initialPosition, Length length, Length width, Lane referenceLane, Length
     * positionOnReferenceLane]
     */
    EventType INIT_EVENT = new EventType("LANEBASEDGTU.INIT");

    /**
     * The lane-based event type for pub/sub indicating destruction of the GTU. <br>
     * Payload: [String id, DirectedPoint lastPosition, Length odometer, Lane referenceLane, Length positionOnReferenceLane]
     */
    EventType DESTROY_EVENT = new EventType("LANEBASEDGTU.DESTROY");

}
