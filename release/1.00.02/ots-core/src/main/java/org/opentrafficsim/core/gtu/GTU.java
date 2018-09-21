package org.opentrafficsim.core.gtu;

import java.awt.Color;
import java.io.Serializable;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventProducerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.immutablecollections.ImmutableSet;

/**
 * Generalized Travel Unit. <br>
 * A GTU is an object (person, car, ship) that can travel over the infrastructure. It has a (directed) location, dimensions, and
 * some properties that all GTUs share. The GTU is not bound to any infrastructure and can travel freely in the world. <br>
 * For its movement, a GTU uses an OperationalPlan, which indicates a shape in the world with a speed profile that the GTU will
 * use to move. The OperationalPlan can be updated or replaced, for which a tactical planner is responsible. A tactical plan can
 * for instance be for a car to change two lanes to the left during the next 200 m to be able to make a left turn in 200 m. The
 * operational plans are then the implementation of segments of the movement (time, location, speed, acceleration) that the car
 * will make to drive on the road and (safely) make the lane changes. On the highest level, a StrategicPlan puts boundary
 * conditions on the tactical plans. The strategic plan contains for instance the destination we want to reach and possibly some
 * constraints on solutions that the tactical plans have to comply with.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTU extends Locatable, Serializable, EventProducerInterface, Identifiable
{
    /** @return the id of the GTU */
    @Override
    String getId();

    /** @return the maximum length of the GTU (parallel with driving direction). */
    Length getLength();

    /** @return the maximum width of the GTU (perpendicular to driving direction). */
    Length getWidth();

    /** @return the maximum speed of the GTU, in the direction of movement. */
    Speed getMaximumSpeed();

    /** @return the maximum acceleration of the GTU, in the linear direction. */
    Acceleration getMaximumAcceleration();

    /** @return the maximum deceleration of the GTU, in the linear direction, stored as a negative number. */
    Acceleration getMaximumDeceleration();

    /** @return the type of GTU, e.g. TruckType, CarType, BusType. */
    GTUType getGTUType();

    /** @return the simulator of the GTU. */
    DEVSSimulatorInterface.TimeDoubleUnit getSimulator();

    /** @return the reference position of the GTU, by definition (0, 0, 0). */
    RelativePosition getReference();

    /** @return the front position of the GTU, relative to its reference point. */
    RelativePosition getFront();

    /** @return the rear position of the GTU, relative to its reference point. */
    RelativePosition getRear();

    /** @return the center position of the GTU, relative to its reference point. */
    RelativePosition getCenter();

    /** @return the contour points of the GTU. */
    ImmutableSet<RelativePosition> getContourPoints();

    /** @return the positions for this GTU, but not the contour points. */
    ImmutableMap<RelativePosition.TYPE, RelativePosition> getRelativePositions();

    /** @return the current speed of the GTU, along the direction of movement. */
    Speed getSpeed();

    /**
     * @param time Time; time at which to obtain the speed
     * @return the current speed of the GTU, along the direction of movement.
     */
    Speed getSpeed(Time time);

    /** @return the current acceleration of the GTU, along the direction of movement. */
    Acceleration getAcceleration();

    /**
     * @param time Time; time at which to obtain the acceleration
     * @return the current acceleration of the GTU, along the direction of movement.
     */
    Acceleration getAcceleration(Time time);

    /**
     * @return Length; the current odometer value.
     */
    Length getOdometer();

    /**
     * @param time Time; time to obtain the odometer at
     * @return Length; the odometer value at given time.
     */
    Length getOdometer(Time time);

    /** @return Parameters. */
    Parameters getParameters();

    /** @param parameters Parameters; parameters */
    void setParameters(Parameters parameters);

    /**
     * @return StrategicalPlanner; the planner responsible for the overall 'mission' of the GTU, usually indicating where it
     *         needs to go. It operates by instantiating tactical planners to do the work.
     */
    StrategicalPlanner getStrategicalPlanner();

    /**
     * @param time Time; time to obtain the strategical planner at
     * @return StrategicalPlanner; the planner responsible for the overall 'mission' of the GTU, usually indicating where it
     *         needs to go. It operates by instantiating tactical planners to do the work.
     */
    StrategicalPlanner getStrategicalPlanner(Time time);

    /** @return TacticalPlanner; the current tactical planner that can generate an operational plan */
    default TacticalPlanner<?, ?> getTacticalPlanner()
    {
        return getStrategicalPlanner().getTacticalPlanner();
    }

    /**
     * @param time Time; time to obtain the tactical planner at
     * @return TacticalPlanner; the tactical planner that can generate an operational plan at the given time
     */
    default TacticalPlanner<?, ?> getTacticalPlanner(Time time)
    {
        return getStrategicalPlanner(time).getTacticalPlanner(time);
    }

    /** @return the current operational plan for the GTU */
    OperationalPlan getOperationalPlan();

    /**
     * @param time Time; time to obtain the operational plan at
     * @return the operational plan for the GTU at the given time.
     */
    OperationalPlan getOperationalPlan(Time time);

    /** Destroy the GTU from the simulation and animation. */
    void destroy();

    /**
     * Returns whether the GTU is destroyed.
     * @return whether the GTU is destroyed
     */
    boolean isDestroyed();

    /**
     * Return the base color of the GTU (not the state-based color).
     * @return Color; the base color of the GTU (not the state-based color)
     */
    Color getBaseColor();

    /**
     * Adds the provided GTU to this GTU, meaning it moves with this GTU.
     * @param gtu GTU; gtu to enter this GTU
     * @throws GTUException if the gtu already has a parent
     */
    void addGtu(GTU gtu) throws GTUException;

    /**
     * Removes the provided GTU from this GTU, meaning it no longer moves with this GTU.
     * @param gtu GTU; gtu to exit this GTU
     */
    void removeGtu(GTU gtu);

    /**
     * Set the parent GTU.
     * @param gtu GTU; parent GTU, may be {@code null}
     * @throws GTUException if the gtu already has a parent
     */
    void setParent(GTU gtu) throws GTUException;

    /**
     * Returns the parent GTU, or {@code null} if this GTU has no parent.
     * @return GTU; parent GTU, or {@code null} if this GTU has no parent
     */
    GTU getParent();

    /**
     * Returns the children GTU's.
     * @return Set&lt;GTU&gt;; children GTU's
     */
    Set<GTU> getChildren();

    /**
     * The event type for pub/sub indicating the initialization of a new GTU. <br>
     * Payload: [String id, DirectedPoint initialPosition, Length length, Length width, Color gtuBaseColor]
     */
    EventType INIT_EVENT = new EventType("GTU.INIT");

    /**
     * The event type for pub/sub indicating a move. <br>
     * Payload: [String id, DirectedPoint position, Speed speed, Acceleration acceleration, Length odometer]
     */
    EventType MOVE_EVENT = new EventType("GTU.MOVE");

    /**
     * The event type for pub/sub indicating destruction of the GTU. <br>
     * Payload: [String id, DirectedPoint lastPosition, Length odometer]
     */
    EventType DESTROY_EVENT = new EventType("GTU.DESTROY");

}
