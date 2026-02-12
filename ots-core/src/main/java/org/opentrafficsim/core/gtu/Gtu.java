package org.opentrafficsim.core.gtu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.base.Identifiable;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.HierarchicallyTyped;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.geometry.OffsetRectangleShape;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.base.geometry.PolygonShape;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.Type;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Implements the basic functionalities of any GTU: the ability to move on 3D-space according to a plan.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Gtu extends LocalEventProducer implements HierarchicallyTyped<GtuType, Gtu>, OtsShape, Identifiable
{
    /** The id of the GTU. */
    private final String id;

    /** unique number of the GTU. */
    private final int uniqueNumber;

    /** the unique number counter. */
    private static int staticUNIQUENUMBER = 0;

    /** The type of GTU, e.g. TruckType, CarType, BusType. */
    private final GtuType gtuType;

    /** The simulator to schedule activities on. */
    private final OtsSimulatorInterface simulator;

    /** Model parameters. */
    private Parameters parameters;

    /** The maximum acceleration. */
    private Acceleration maximumAcceleration;

    /** The maximum deceleration, stored as a negative number. */
    private Acceleration maximumDeceleration;

    /**
     * The odometer which measures how much distance have we covered between instantiation and the last completed operational
     * plan. In order to get a complete odometer reading, the progress of the current plan execution has to be added to this
     * value.
     */
    private Historical<Length> odometer;

    /** The strategical planner that can instantiate tactical planners to determine mid-term decisions. */
    private final Historical<StrategicalPlanner> strategicalPlanner;

    /** The tactical planner that can generate an operational plan. */
    private final Historical<TacticalPlanner<?, ?>> tacticalPlanner;

    /** The current operational plan, which provides a short-term movement over time. */
    private final Historical<OperationalPlan> operationalPlan;

    /** The next move event as scheduled on the simulator, can be used for interrupting the current move. */
    private SimEventInterface<Duration> nextMoveEvent;

    /** The model in which this GTU is registered. */
    private PerceivableContext perceivableContext;

    /** Is this GTU destroyed? */
    private boolean destroyed = false;

    /** Align step. */
    private double alignStep = Double.NaN;

    /** Cache location time. */
    private Duration cacheLocationTime = Duration.NaN;

    /** Cached location at that time. */
    private DirectedPoint2d cacheLocation = null;

    /** Cached speed time. */
    private double cachedSpeedTime = Double.NaN;

    /** Cached speed. */
    private Speed cachedSpeed = null;

    /** Cached acceleration time. */
    private double cachedAccelerationTime = Double.NaN;

    /** Cached acceleration. */
    private Acceleration cachedAcceleration = null;

    /** Parent GTU. */
    private Gtu parent = null;

    /** Children GTU's. */
    private Set<Gtu> children = new LinkedHashSet<>();

    /** Error handler. */
    private GtuErrorHandler errorHandler = GtuErrorHandler.THROW;

    /** Shape. */
    private final OtsShape shape;

    /** Relative positions to the reference point of type RelativePosition.REFERENCE. */
    private final Map<RelativePosition.Type, RelativePosition> relativePositions = new LinkedHashMap<>();

    /** The maximum length of the GTU (parallel with driving direction). */
    private final Length length;

    /** The maximum width of the GTU (perpendicular to driving direction). */
    private final Length width;

    /** The maximum speed of the GTU (in the driving direction). */
    private final Speed maximumSpeed;

    /** Tags of the GTU, these are used for specific use cases of any sort. */
    private final Map<String, String> tags = new LinkedHashMap<>();

    /**
     * Constructor using shape.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator the simulator to schedule plan changes on
     * @param perceivableContext the perceivable context in which this GTU will be registered
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param front front distance relative to the reference position
     * @param contour contour relative to reference position, may be {@code null}
     * @param maximumSpeed the maximum speed of the GTU (in the driving direction)
     * @throws GtuException when id already exists in the context
     * @throws NullPointerException when any input is null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private Gtu(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
            final PerceivableContext perceivableContext, final Length length, final Length width, final Length front,
            final Polygon2d contour, final Speed maximumSpeed) throws GtuException
    {
        Throw.whenNull(id, "id");
        Throw.whenNull(gtuType, "gtuType");
        Throw.whenNull(simulator, "simulator");
        Throw.whenNull(perceivableContext, "perceivableContext");
        Throw.when(perceivableContext.containsGtuId(id), GtuException.class,
                "GTU with id %s already registered in perceivableContext %s", id, perceivableContext.getId());
        Throw.whenNull(maximumSpeed, "maximumSpeed");
        this.maximumSpeed = maximumSpeed;

        HistoryManager historyManager = simulator.getReplication().getHistoryManager(simulator);
        this.id = id;
        this.uniqueNumber = ++staticUNIQUENUMBER;
        this.gtuType = gtuType;
        this.simulator = simulator;
        this.odometer = new HistoricalValue<>(historyManager, this, Length.ZERO);
        this.perceivableContext = perceivableContext;
        this.perceivableContext.addGTU(this);
        this.strategicalPlanner = new HistoricalValue<>(historyManager, this);
        this.tacticalPlanner = new HistoricalValue<>(historyManager, this, null);
        this.operationalPlan = new HistoricalValue<>(historyManager, this, null);

        this.length = length;
        this.width = width;
        if (contour == null)
        {
            this.shape =
                    new OffsetRectangleShape(front.si - this.length.si, front.si, -this.width.si / 2.0, this.width.si / 2.0)
                    {
                        @Override
                        public DirectedPoint2d getLocation()
                        {
                            return Gtu.this.getLocation();
                        }
                    };
        }
        else
        {
            this.shape = new PolygonShape(contour)
            {
                @Override
                public DirectedPoint2d getLocation()
                {
                    return Gtu.this.getLocation();
                }
            };
        }

        this.relativePositions.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);
        this.relativePositions.put(RelativePosition.FRONT,
                new RelativePosition(front, Length.ZERO, Length.ZERO, RelativePosition.FRONT));
        this.relativePositions.put(RelativePosition.REAR,
                new RelativePosition(front.minus(this.length), Length.ZERO, Length.ZERO, RelativePosition.REAR));
        Point2d midPoint = this.shape.getRelativeBounds().midPoint();
        this.relativePositions.put(RelativePosition.CENTER,
                new RelativePosition(Length.ofSI(midPoint.x), Length.ofSI(midPoint.y), Length.ZERO, RelativePosition.CENTER));
    }

    /**
     * Constructor using contour.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator the simulator to schedule plan changes on
     * @param perceivableContext the perceivable context in which this GTU will be registered
     * @param contour contour relative to reference position
     * @param maximumSpeed the maximum speed of the GTU (in the driving direction)
     * @throws GtuException when id already exists in the context
     * @throws NullPointerException when any input is null
     */
    public Gtu(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
            final PerceivableContext perceivableContext, final Polygon2d contour, final Speed maximumSpeed) throws GtuException
    {
        this(id, gtuType, simulator, perceivableContext, Length.ofSI(contour.getAbsoluteBounds().getDeltaX()),
                Length.ofSI(contour.getAbsoluteBounds().getDeltaY()), Length.ofSI(contour.getAbsoluteBounds().getMaxX()),
                contour, maximumSpeed);
    }

    /**
     * Constructor using length, width and front.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. NL.CAR or NL.TRUCK
     * @param simulator the simulator to schedule plan changes on
     * @param perceivableContext the perceivable context in which this GTU will be registered
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param front front distance relative to the reference position
     * @param maximumSpeed the maximum speed of the GTU (in the driving direction)
     * @throws GtuException when id already exists in the context
     * @throws NullPointerException when any input is null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Gtu(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
            final PerceivableContext perceivableContext, final Length length, final Length width, final Length front,
            final Speed maximumSpeed) throws GtuException
    {
        this(id, gtuType, simulator, perceivableContext, length, width, front, null, maximumSpeed);
    }

    /**
     * Initialize the GTU at a location and speed, and give it a mission to fulfill through the strategical planner.
     * @param strategicalPlanner the strategical planner responsible for the overall 'mission' of the GTU, usually indicating
     *            where it needs to go. It operates by instantiating tactical planners to do the work.
     * @param initialLocation the initial location (and direction) of the GTU
     * @param initialSpeed the initial speed of the GTU
     * @throws SimRuntimeException when scheduling after the first move fails
     * @throws GtuException when the preconditions of the parameters are not met or when the construction of the original
     *             waiting path fails
     */
    @SuppressWarnings({"checkstyle:hiddenfield", "checkstyle:designforextension"})
    public void init(final StrategicalPlanner strategicalPlanner, final DirectedPoint2d initialLocation,
            final Speed initialSpeed) throws SimRuntimeException, GtuException
    {
        Throw.whenNull(strategicalPlanner, "strategicalPlanner");
        Throw.whenNull(initialLocation, "Initial location of GTU cannot be null");
        Throw.when(Double.isNaN(initialLocation.x) || Double.isNaN(initialLocation.y), GtuException.class,
                "initialLocation %s invalid for GTU with id %s", initialLocation, this.id);
        Throw.whenNull(initialSpeed, "initialSpeed");
        Throw.when(!getId().equals(strategicalPlanner.getGtu().getId()), GtuException.class,
                "GTU %s is initialized with a strategical planner for GTU %s", getId(), strategicalPlanner.getGtu().getId());

        this.strategicalPlanner.set(strategicalPlanner);
        this.tacticalPlanner.set(strategicalPlanner.getTacticalPlanner());

        try
        {
            move(initialLocation);
        }
        catch (OperationalPlanException | NetworkException | ParameterException exception)
        {
            throw new GtuException("Failed to create OperationalPlan for GTU " + this.id, exception);
        }
    }

    /**
     * Get front.
     * @return the front position of the GTU, relative to its reference point.
     */
    public RelativePosition getFront()
    {
        return this.relativePositions.get(RelativePosition.FRONT);
    }

    /**
     * Get rear.
     * @return the rear position of the GTU, relative to its reference point.
     */
    public RelativePosition getRear()
    {
        return this.relativePositions.get(RelativePosition.REAR);
    }

    /**
     * Get center.
     * @return the center position of the GTU, relative to its reference point.
     */
    public RelativePosition getCenter()
    {
        return this.relativePositions.get(RelativePosition.CENTER);
    }

    /**
     * Get relative positions.
     * @return the positions for this GTU, but not the contour points.
     */
    public ImmutableMap<Type, RelativePosition> getRelativePositions()
    {
        return new ImmutableLinkedHashMap<>(this.relativePositions, Immutable.WRAP);
    }

    /**
     * Get length.
     * @return the maximum length of the GTU (parallel with driving direction).
     */
    public Length getLength()
    {
        return this.length;
    }

    /**
     * Get width.
     * @return the maximum width of the GTU (perpendicular to driving direction).
     */
    public Length getWidth()
    {
        return this.width;
    }

    /**
     * Get maximum speed.
     * @return the maximum speed of the GTU, in the direction of movement.
     */
    public Speed getMaximumSpeed()
    {
        return this.maximumSpeed;
    }

    @Override
    public Bounds2d getRelativeBounds()
    {
        return this.shape.getRelativeBounds();
    }

    /**
     * Destructor. Don't forget to call with super.destroy() from any override to avoid memory leaks in the network.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        DirectedPoint2d location = getLocation();
        fireTimedEvent(Gtu.DESTROY_EVENT,
                new Object[] {getId(), new PositionVector(new double[] {location.x, location.y}, PositionUnit.METER),
                        new Direction(location.getDirZ(), DirectionUnit.EAST_RADIAN), getOdometer()},
                this.simulator.getSimulatorTime());

        // cancel the next move
        if (this.nextMoveEvent != null)
        {
            this.simulator.cancelEvent(this.nextMoveEvent);
            this.nextMoveEvent = null;
        }

        this.perceivableContext.removeGTU(this);
        this.destroyed = true;
    }

    /**
     * Move from the current location according to an operational plan to a location that will bring us nearer to reaching the
     * location provided by the strategical planner. <br>
     * This method can be overridden to carry out specific behavior during the execution of the plan (e.g., scheduling of
     * triggers, entering or leaving lanes, etc.). Please bear in mind that the call to super.move() is essential, and that one
     * has to take care to handle the situation that the plan gets interrupted.
     * @param fromLocation the last known location (initial location, or end location of the previous operational plan)
     * @return whether an exception occurred
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., a dead end where it is not expected
     * @throws ParameterException in there is a parameter problem
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected boolean move(final DirectedPoint2d fromLocation)
            throws SimRuntimeException, GtuException, NetworkException, ParameterException
    {
        try
        {
            Duration now = this.simulator.getSimulatorTime();

            // Add the odometer distance from the currently running operational plan.
            // Because a plan can be interrupted, we explicitly calculate the covered distance till 'now'
            Length currentOdometer;
            if (this.operationalPlan.get() != null)
            {
                currentOdometer = this.odometer.get().plus(this.operationalPlan.get().getTraveledDistance(now));
            }
            else
            {
                currentOdometer = this.odometer.get();
            }

            // Do we have an operational plan?
            TacticalPlanner<?, ?> tactPlanner = this.tacticalPlanner.get();
            if (tactPlanner == null)
            {
                // Tell the strategical planner to provide a tactical planner
                tactPlanner = this.strategicalPlanner.get().getTacticalPlanner();
                this.tacticalPlanner.set(tactPlanner);
            }
            synchronized (this)
            {
                tactPlanner.getPerception().perceive();
            }
            OperationalPlan newOperationalPlan = tactPlanner.generateOperationalPlan(now, fromLocation);
            synchronized (this)
            {
                this.operationalPlan.set(newOperationalPlan);
                this.cachedSpeedTime = Double.NaN;
                this.cachedAccelerationTime = Double.NaN;
                this.odometer.set(currentOdometer);
            }

            if (!Double.isNaN(this.alignStep))
            {
                // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
                double tNext = Math.floor(now.si / this.alignStep + 1.0) * this.alignStep;
                DirectedPoint2d p = (tNext - now.si < this.alignStep) ? newOperationalPlan.getEndLocation()
                        : newOperationalPlan.getLocationFromStart(new Duration(tNext - now.si, DurationUnit.SI));
                this.nextMoveEvent = this.simulator.scheduleEventRel(Duration.ofSI(tNext),
                        () -> Try.execute(() -> move(p), "ParameterException in move"));
            }
            else
            {
                // schedule the next move at the end of the current operational plan
                // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
                this.nextMoveEvent = this.simulator.scheduleEventRel(newOperationalPlan.getTotalDuration(),
                        () -> Try.execute(() -> move(newOperationalPlan.getEndLocation()), "ParameterException in move"));
            }

            fireTimedEvent(Gtu.MOVE_EVENT,
                    new Object[] {getId(),
                            new PositionVector(new double[] {fromLocation.x, fromLocation.y}, PositionUnit.METER),
                            new Direction(fromLocation.getDirZ(), DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
                            getOdometer()},
                    this.simulator.getSimulatorTime());

            return false;
        }
        catch (Exception ex)
        {
            try
            {
                this.errorHandler.handle(this, ex);
            }
            catch (Exception exception)
            {
                throw new GtuException(exception);
            }
            return true;
        }
    }

    /**
     * Interrupt the move and ask for a new plan. This method can be overridden to carry out the bookkeeping needed when the
     * current plan gets interrupted.
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., unreachability of a certain point
     * @throws ParameterException when there is a problem with a parameter
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void interruptMove() throws SimRuntimeException, GtuException, NetworkException, ParameterException
    {
        this.simulator.cancelEvent(this.nextMoveEvent);
        move(this.operationalPlan.get().getLocation(this.simulator.getSimulatorTime()));
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Sets a tag, these are used for specific use cases of any sort.
     * @param tag name of the tag.
     * @param value value of the tag.
     */
    public void setTag(final String tag, final String value)
    {
        this.tags.put(tag, value);
    }

    /**
     * Returns the value for the given tag, these are used for specific use cases of any sort.
     * @param tag name of the tag.
     * @return value of the tag, empty if it is not given to the GTU.
     */
    public Optional<String> getTag(final String tag)
    {
        return Optional.ofNullable(this.tags.get(tag));
    }

    @Override
    public GtuType getType()
    {
        return this.gtuType;
    }

    /**
     * Get reference.
     * @return the reference position of the GTU, by definition (0, 0, 0).
     */
    public RelativePosition getReference()
    {
        return RelativePosition.REFERENCE_POSITION;
    }

    /**
     * Get simulator.
     * @return the simulator of the GTU.
     */
    public OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * Get parameters.
     * @return Parameters.
     */
    public Parameters getParameters()
    {
        return this.parameters;
    }

    /**
     * Set parameters. This method clears any existing parameter history and should normally only be invoked for initialization.
     * @param parameters parameters
     */
    public void setParameters(final Parameters parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Get strategical planner.
     * @return the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to go. It
     *         operates by instantiating tactical planners to do the work.
     */
    public StrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner.get();
    }

    /**
     * Get strategical planner at time.
     * @param time simulation time to obtain the strategical planner at
     * @return the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs to go. It
     *         operates by instantiating tactical planners to do the work.
     */
    public StrategicalPlanner getStrategicalPlanner(final Duration time)
    {
        return this.strategicalPlanner.get(time);
    }

    /**
     * Get tactical planner.
     * @return the current tactical planner that can generate an operational plan
     */
    public TacticalPlanner<?, ?> getTacticalPlanner()
    {
        return getStrategicalPlanner().getTacticalPlanner();
    }

    /**
     * Get tactical planner at time.
     * @param time simulation time to obtain the tactical planner at
     * @return the tactical planner that can generate an operational plan at the given time
     */
    public TacticalPlanner<?, ?> getTacticalPlanner(final Duration time)
    {
        return getStrategicalPlanner(time).getTacticalPlanner(time);
    }

    /**
     * Get operational plan.
     * @return the current operational plan for the GTU
     */
    public OperationalPlan getOperationalPlan()
    {
        return this.operationalPlan.get();
    }

    /**
     * Get operational plan at time.
     * @param time simulation time to obtain the operational plan at
     * @return the operational plan for the GTU at the given time.
     */
    public OperationalPlan getOperationalPlan(final Duration time)
    {
        return this.operationalPlan.get(time);
    }

    /**
     * Set the operational plan. This method is for sub classes.
     * @param operationalPlan operational plan.
     */
    protected void setOperationalPlan(final OperationalPlan operationalPlan)
    {
        this.operationalPlan.set(operationalPlan);
    }

    /**
     * Get odometer.
     * @return the current odometer value.
     */
    public Length getOdometer()
    {
        return getOdometer(this.simulator.getSimulatorTime());
    }

    /**
     * Get odometer at time.
     * @param time simulation time to obtain the odometer at
     * @return the odometer value at given time.
     */
    public Length getOdometer(final Duration time)
    {
        synchronized (this)
        {
            OperationalPlan historicalPlan = getOperationalPlan(time);
            if (historicalPlan == null || historicalPlan.getStartTime().gt(time) || historicalPlan.getEndTime().lt(time))
            {
                return this.odometer.get(time);
            }
            try
            {
                return this.odometer.get(time).plus(getOperationalPlan(time).getTraveledDistance(time));
            }
            catch (OperationalPlanException ope)
            {
                Logger.ots().warn("OperationalPlan could not give a traveled distance it the requested time.");
                return this.odometer.get(time);
            }
        }
    }

    /**
     * Get speed.
     * @return the current speed of the GTU, along the direction of movement.
     */
    public Speed getSpeed()
    {
        synchronized (this)
        {
            return getSpeed(this.simulator.getSimulatorTime());
        }
    }

    /**
     * Get speed at time.
     * @param time simulation time at which to obtain the speed
     * @return the current speed of the GTU, along the direction of movement.
     */
    public Speed getSpeed(final Duration time)
    {
        synchronized (this)
        {
            if (this.cachedSpeedTime != time.si)
            {
                // Invalidate everything
                this.cachedSpeedTime = Double.NaN;
                this.cachedSpeed = null;
                OperationalPlan plan = getOperationalPlan(time);
                if (plan == null)
                {
                    this.cachedSpeed = Speed.ZERO;
                }
                else if (time.si < plan.getStartTime().si)
                {
                    this.cachedSpeed = plan.getStartSpeed();
                }
                else if (time.si > plan.getEndTime().si)
                {
                    if (time.si - plan.getEndTime().si < 1e-6)
                    {
                        this.cachedSpeed = Try.assign(() -> plan.getSpeed(plan.getEndTime()),
                                "getSpeed() could not derive a valid speed for the current operationalPlan");
                    }
                    else
                    {
                        throw new IllegalStateException("Requesting speed value beyond plan.");
                    }
                }
                else
                {
                    this.cachedSpeed = Try.assign(() -> plan.getSpeed(time),
                            "getSpeed() could not derive a valid speed for the current operationalPlan");
                }
                this.cachedSpeedTime = time.si; // Do this last
            }
            return this.cachedSpeed;
        }
    }

    /**
     * Get acceleration.
     * @return the current acceleration of the GTU, along the direction of movement.
     */
    public Acceleration getAcceleration()
    {
        synchronized (this)
        {
            return getAcceleration(this.simulator.getSimulatorTime());
        }
    }

    /**
     * Get acceleration at time.
     * @param time simulation time at which to obtain the acceleration
     * @return the current acceleration of the GTU, along the direction of movement.
     */
    public Acceleration getAcceleration(final Duration time)
    {
        synchronized (this)
        {
            if (this.cachedAccelerationTime != time.si)
            {
                // Invalidate everything
                this.cachedAccelerationTime = Double.NaN;
                this.cachedAcceleration = null;
                OperationalPlan plan = getOperationalPlan(time);
                if (plan == null)
                {
                    this.cachedAcceleration = Acceleration.ZERO;
                }
                else if (time.si < plan.getStartTime().si)
                {
                    this.cachedAcceleration =
                            Try.assign(() -> plan.getAcceleration(plan.getStartTime()), "Exception obtaining acceleration.");
                }
                else if (time.si > plan.getEndTime().si)
                {
                    if (time.si - plan.getEndTime().si < 1e-6)
                    {
                        this.cachedAcceleration = Try.assign(() -> plan.getAcceleration(plan.getEndTime()),
                                "getAcceleration() could not derive a valid acceleration for the current operationalPlan");
                    }
                    else
                    {
                        throw new IllegalStateException("Requesting acceleration value beyond plan.");
                    }
                }
                else
                {
                    this.cachedAcceleration = Try.assign(() -> plan.getAcceleration(time),
                            "getAcceleration() could not derive a valid acceleration for the current operationalPlan");
                }
                this.cachedAccelerationTime = time.si;
            }
            return this.cachedAcceleration;
        }
    }

    /**
     * Get maximum acceleration.
     * @return maximumAcceleration
     */
    public Acceleration getMaximumAcceleration()
    {
        return this.maximumAcceleration;
    }

    /**
     * Set maximum deceleration.
     * @param maximumAcceleration set maximumAcceleration
     */
    public void setMaximumAcceleration(final Acceleration maximumAcceleration)
    {
        if (maximumAcceleration.le(Acceleration.ZERO))
        {
            throw new OtsRuntimeException("Maximum acceleration of GTU " + this.id + " set to value <= 0");
        }
        this.maximumAcceleration = maximumAcceleration;
    }

    /**
     * Get maximum deceleration.
     * @return maximumDeceleration
     */
    public Acceleration getMaximumDeceleration()
    {
        return this.maximumDeceleration;
    }

    /**
     * Set the maximum deceleration.
     * @param maximumDeceleration set maximumDeceleration, must be a negative number
     */
    public void setMaximumDeceleration(final Acceleration maximumDeceleration)
    {
        if (maximumDeceleration.ge(Acceleration.ZERO))
        {
            throw new OtsRuntimeException("Cannot set maximum deceleration of GTU " + this.id + " to " + maximumDeceleration
                    + " (value must be negative)");
        }
        this.maximumDeceleration = maximumDeceleration;
    }

    @Override
    public synchronized DirectedPoint2d getLocation()
    {
        Duration locationTime = this.simulator.getSimulatorTime();
        if (null == this.cacheLocationTime || this.cacheLocationTime.si != locationTime.si)
        {
            this.cacheLocation = getLocation(locationTime);
            this.cacheLocationTime = locationTime;
        }
        return this.cacheLocation;
    }

    /**
     * Returns the location of the GTU at the given time.
     * @param time simulation time
     * @return location of the GTU at the given time
     */
    public synchronized DirectedPoint2d getLocation(final Duration time)
    {
        try
        {
            return this.operationalPlan.get(time).getLocation(time);
        }
        catch (OperationalPlanException exception)
        {
            return new DirectedPoint2d(0, 0, 0);
        }
    }

    @Override
    public double signedDistance(final Point2d point)
    {
        return this.shape.signedDistance(point);
    }

    /**
     * Return the shape of a dynamic object at time 'time'. Note that the getContour() method without a time returns the
     * Minkowski sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all
     * locations of a GTU for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @param time simulation time for which we want the shape
     * @return the shape of the object at time 'time'
     */
    @Override
    public Polygon2d getAbsoluteContour(final Duration time)
    {
        try
        {
            return new Polygon2d(0.0, OtsShape.toAbsoluteTransform(this.operationalPlan.get(time).getLocation(time))
                    .transform(getRelativeContour().iterator()));
        }
        catch (OperationalPlanException exception)
        {
            throw new OtsRuntimeException(exception);
        }
    }

    /**
     * Return the shape of the GTU for the validity time of the operational plan. Note that this method without a time returns
     * the Minkowski sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all
     * locations of a GTU for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @return the shape of the object over the validity of the operational plan
     */
    @Override
    public Polygon2d getAbsoluteContour()
    {
        try
        {
            // TODO: the actual contour of the GTU has to be moved over the path
            OtsLine2d path = this.operationalPlan.get().getPath();
            // part of the Gtu length has to be added before the start and after the end of the path.
            // we assume the reference point is within the contour of the Gtu.
            double rear = Math.max(0.0, getReference().dx().si - getRear().dx().si);
            double front = path.getLength() + Math.max(0.0, getFront().dx().si - getReference().dx().si);
            Point2d p0 = path.getLocationExtendedSI(-rear);
            Point2d pn = path.getLocationExtendedSI(front);
            List<Point2d> pList = path.getPointList();
            pList.add(0, p0);
            pList.add(pn);
            OtsLine2d extendedPath = new OtsLine2d(pList);
            List<Point2d> swath = new ArrayList<>();
            swath.addAll(extendedPath.offsetLine(getWidth().si / 2.0).getPointList());
            swath.addAll(extendedPath.offsetLine(-getWidth().si / 2.0).reverse().getPointList());
            Polygon2d s = new Polygon2d(0.0, swath);
            return s;
        }
        catch (Exception e)
        {
            throw new OtsRuntimeException(e);
        }
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.shape.getRelativeContour();
    }

    /**
     * Returns whether the GTU is destroyed.
     * @return whether the GTU is destroyed
     */
    public boolean isDestroyed()
    {
        return this.destroyed;
    }

    /**
     * Return perceivable context.
     * @return the context to which the GTU belongs
     */
    public PerceivableContext getPerceivableContext()
    {
        return this.perceivableContext;
    }

    /**
     * Adds the provided GTU to this GTU, meaning it moves with this GTU.
     * @param gtu gtu to enter this GTU
     * @throws GtuException if the gtu already has a parent
     */
    public void addGtu(final Gtu gtu) throws GtuException
    {
        this.children.add(gtu);
        gtu.setParent(this);
    }

    /**
     * Removes the provided GTU from this GTU, meaning it no longer moves with this GTU.
     * @param gtu gtu to exit this GTU
     */
    public void removeGtu(final Gtu gtu)
    {
        this.children.remove(gtu);
        try
        {
            gtu.setParent(null);
        }
        catch (GtuException exception)
        {
            // cannot happen, setting null is always ok
        }
    }

    /**
     * Set the parent GTU.
     * @param gtu parent GTU, may be {@code null}
     * @throws GtuException if the gtu already has a parent
     */
    public void setParent(final Gtu gtu) throws GtuException
    {
        Throw.when(gtu != null && this.parent != null, GtuException.class, "GTU %s already has a parent.", this);
        this.parent = gtu;
    }

    /**
     * Returns the parent GTU, or {@code null} if this GTU has no parent.
     * @return parent GTU, empty if this GTU has no parent
     */
    public Optional<Gtu> getParent()
    {
        return Optional.ofNullable(this.parent);
    }

    /**
     * Returns the children GTU's.
     * @return children GTU's
     */
    public Set<Gtu> getChildren()
    {
        return new LinkedHashSet<>(this.children); // safe copy
    }

    /**
     * Get error handler.
     * @return errorHandler.
     */
    protected GtuErrorHandler getErrorHandler()
    {
        return this.errorHandler;
    }

    /**
     * Sets the error handler.
     * @param errorHandler error handler
     */
    public void setErrorHandler(final GtuErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    /**
     * Returns the align step.
     * @return align step, NaN if not present
     */
    public double getAlignStep()
    {
        return this.alignStep;
    }

    /**
     * Set align step, use NaN to not align.
     * @param alignStep align step
     */
    public void setAlignStep(final double alignStep)
    {
        this.alignStep = alignStep;
    }

    /**
     * Note that destroying the next move event of the GTU can be dangerous!
     * @return nextMoveEvent the next move event of the GTU, e.g. to cancel it from outside.
     */
    public SimEventInterface<Duration> getNextMoveEvent()
    {
        return this.nextMoveEvent;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.uniqueNumber);
    }

    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Gtu other = (Gtu) obj;
        return this.uniqueNumber == other.uniqueNumber;
    }

    /**
     * The event type for pub/sub indicating a move. <br>
     * Payload: [String id, DirectedPoint position, Speed speed, Acceleration acceleration, Length odometer]
     */
    public static final EventType MOVE_EVENT = new EventType("GTU.MOVE",
            new MetaData("GTU move", "GTU id, position, speed, acceleration, odometer",
                    new ObjectDescriptor[] {new ObjectDescriptor("Id", "GTU Id", String.class),
                            new ObjectDescriptor("position", "position", PositionVector.class),
                            new ObjectDescriptor("direction", "direction", Direction.class),
                            new ObjectDescriptor("speed", "speed", Speed.class),
                            new ObjectDescriptor("acceleration", "acceleration", Acceleration.class),
                            new ObjectDescriptor("Odometer", "Total distance travelled since incarnation", Length.class)}));

    /**
     * The event type for pub/sub indicating destruction of the GTU. <br>
     * Payload: [String id, DirectedPoint lastPosition, Length odometer]
     */
    public static final EventType DESTROY_EVENT = new EventType("GTU.DESTROY",
            new MetaData("GTU destroy", "GTU id, final position, final odometer",
                    new ObjectDescriptor[] {new ObjectDescriptor("Id", "GTU Id", String.class),
                            new ObjectDescriptor("position", "position", PositionVector.class),
                            new ObjectDescriptor("direction", "direction", Direction.class),
                            new ObjectDescriptor("Odometer", "Total distance travelled since incarnation", Length.class)}));

}
