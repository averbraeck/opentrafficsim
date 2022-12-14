package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventProducer;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.geometry.OtsShape;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

/**
 * Implements the basic functionalities of any GTU: the ability to move on 3D-space according to a plan.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractGtu extends EventProducer implements Gtu
{
    /** */
    private static final long serialVersionUID = 20140822L;

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
    protected final Historical<OperationalPlan> operationalPlan;

    /** The next move event as scheduled on the simulator, can be used for interrupting the current move. */
    private SimEvent<Duration> nextMoveEvent;

    /** The model in which this GTU is registered. */
    private PerceivableContext perceivableContext;

    /** Is this GTU destroyed? */
    private boolean destroyed = false;

    /** aligned or not. */
    // TODO: should be indicated with a Parameter
    public static boolean ALIGNED = true;

    /** aligned schedule count. */
    // TODO: can be removed after testing period
    public static int ALIGN_COUNT = 0;

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

    /** shape of the Gtu contour. */
    private OtsShape shape = null;

    /**
     * @param id String; the id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSSimulatorInterface; the simulator to schedule plan changes on
     * @param perceivableContext PerceivableContext; the perceivable context in which this GTU will be registered
     * @throws GtuException when the preconditions of the constructor are not met
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGtu(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
            final PerceivableContext perceivableContext) throws GtuException
    {
        Throw.when(id == null, GtuException.class, "id is null");
        Throw.when(gtuType == null, GtuException.class, "gtuType is null");
        Throw.when(perceivableContext == null, GtuException.class, "perceivableContext is null for GTU with id %s", id);
        Throw.when(perceivableContext.containsGtuId(id), GtuException.class,
                "GTU with id %s already registered in perceivableContext %s", id, perceivableContext.getId());
        Throw.when(simulator == null, GtuException.class, "simulator is null for GTU with id %s", id);

        HistoryManager historyManager = simulator.getReplication().getHistoryManager(simulator);
        this.id = id;
        this.uniqueNumber = ++staticUNIQUENUMBER;
        this.gtuType = gtuType;
        this.simulator = simulator;
        this.odometer = new HistoricalValue<>(historyManager, Length.ZERO);
        this.perceivableContext = perceivableContext;
        this.perceivableContext.addGTU(this);
        this.strategicalPlanner = new HistoricalValue<>(historyManager);
        this.tacticalPlanner = new HistoricalValue<>(historyManager, null);
        this.operationalPlan = new HistoricalValue<>(historyManager, null);
    }

    /**
     * @param idGenerator IdGenerator; the generator that will produce a unique id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSSimulatorInterface; the simulator to schedule plan changes on
     * @param perceivableContext PerceivableContext; the perceivable context in which this GTU will be registered
     * @throws GtuException when the preconditions of the constructor are not met
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGtu(final IdGenerator idGenerator, final GtuType gtuType, final OtsSimulatorInterface simulator,
            final PerceivableContext perceivableContext) throws GtuException
    {
        this(generateId(idGenerator), gtuType, simulator, perceivableContext);
    }

    /**
     * Initialize the GTU at a location and speed, and give it a mission to fulfill through the strategical planner.
     * @param strategicalPlanner StrategicalPlanner; the strategical planner responsible for the overall 'mission' of the GTU,
     *            usually indicating where it needs to go. It operates by instantiating tactical planners to do the work.
     * @param initialLocation DirectedPoint; the initial location (and direction) of the GTU
     * @param initialSpeed Speed; the initial speed of the GTU
     * @throws SimRuntimeException when scheduling after the first move fails
     * @throws GtuException when the preconditions of the parameters are not met or when the construction of the original
     *             waiting path fails
     */
    @SuppressWarnings({"checkstyle:hiddenfield", "hiding", "checkstyle:designforextension"})
    public void init(final StrategicalPlanner strategicalPlanner, final DirectedPoint initialLocation, final Speed initialSpeed)
            throws SimRuntimeException, GtuException
    {
        Throw.when(strategicalPlanner == null, GtuException.class, "strategicalPlanner is null for GTU with id %s", this.id);
        Throw.whenNull(initialLocation, "Initial location of GTU cannot be null");
        Throw.when(Double.isNaN(initialLocation.x) || Double.isNaN(initialLocation.y) || Double.isNaN(initialLocation.z),
                GtuException.class, "initialLocation %s invalid for GTU with id %s", initialLocation, this.id);
        Throw.when(initialSpeed == null, GtuException.class, "initialSpeed is null for GTU with id %s", this.id);
        Throw.when(!getId().equals(strategicalPlanner.getGtu().getId()), GtuException.class,
                "GTU %s is initialized with a strategical planner for GTU %s", getId(), strategicalPlanner.getGtu().getId());

        this.strategicalPlanner.set(strategicalPlanner);
        this.tacticalPlanner.set(strategicalPlanner.getTacticalPlanner());
        Time now = this.simulator.getSimulatorAbsTime();

        DirectedPoint location = getLocation();
        fireTimedEvent(Gtu.INIT_EVENT, new Object[] {getId(), new OtsPoint3D(location).doubleVector(PositionUnit.METER),
                new Direction(location.getZ(), DirectionUnit.EAST_RADIAN), getLength(), getWidth()}, now);

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
     * Generate an id, but check first that we have a valid IdGenerator.
     * @param idGenerator IdGenerator; the generator that will produce a unique id of the GTU
     * @return a (hopefully unique) Id of the GTU
     * @throws GtuException when the idGenerator is null
     */
    private static String generateId(final IdGenerator idGenerator) throws GtuException
    {
        Throw.when(idGenerator == null, GtuException.class, "AbstractGTU.<init>: idGenerator is null");
        return idGenerator.nextId();
    }

    /**
     * Destructor. Don't forget to call with super.destroy() from any override to avoid memory leaks in the network.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        DirectedPoint location = getLocation();
        fireTimedEvent(Gtu.DESTROY_EVENT,
                new Object[] {getId(), new OtsPoint3D(location).doubleVector(PositionUnit.METER),
                        new Direction(location.getZ(), DirectionUnit.EAST_RADIAN), getOdometer()},
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
     * @param fromLocation DirectedPoint; the last known location (initial location, or end location of the previous operational
     *            plan)
     * @return boolean; whether an exception occurred
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws OperationalPlanException when there is a problem creating a good path for the GTU
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., a dead end where it is not expected
     * @throws ParameterException in there is a parameter problem
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected boolean move(final DirectedPoint fromLocation)
            throws SimRuntimeException, OperationalPlanException, GtuException, NetworkException, ParameterException
    {
        try
        {
            Time now = this.simulator.getSimulatorAbsTime();

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
            // TODO discuss when a new tactical planner may be needed
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

            // TODO allow alignment at different intervals, also different between GTU's within a single simulation
            if (ALIGNED && newOperationalPlan.getTotalDuration().si == 0.5)
            {
                // schedule the next move at exactly 0.5 seconds on the clock
                // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
                double tNext = Math.floor(2.0 * now.si + 1.0) / 2.0;
                DirectedPoint p = (tNext - now.si < 0.5) ? newOperationalPlan.getEndLocation()
                        : newOperationalPlan.getLocation(new Duration(tNext - now.si, DurationUnit.SI));
                this.nextMoveEvent =
                        new SimEvent<Duration>(new Duration(tNext - getSimulator().getStartTimeAbs().si, DurationUnit.SI), this,
                                this, "move", new Object[] {p});
                ALIGN_COUNT++;
            }
            else
            {
                // schedule the next move at the end of the current operational plan
                // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
                this.nextMoveEvent =
                        new SimEvent<>(now.plus(newOperationalPlan.getTotalDuration()).minus(getSimulator().getStartTimeAbs()),
                                this, this, "move", new Object[] {newOperationalPlan.getEndLocation()});
            }
            this.simulator.scheduleEvent(this.nextMoveEvent);
            fireTimedEvent(Gtu.MOVE_EVENT,
                    new Object[] {getId(), new OtsPoint3D(fromLocation).doubleVector(PositionUnit.METER),
                            new Direction(fromLocation.getZ(), DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
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
     * @throws OperationalPlanException when there was a problem retrieving the location from the running plan
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws OperationalPlanException when there is a problem creating a good path for the GTU
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., unreachability of a certain point
     * @throws ParameterException when there is a problem with a parameter
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void interruptMove()
            throws SimRuntimeException, OperationalPlanException, GtuException, NetworkException, ParameterException
    {
        this.simulator.cancelEvent(this.nextMoveEvent);
        move(this.operationalPlan.get().getLocation(this.simulator.getSimulatorTime()));
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public GtuType getType()
    {
        return this.gtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getReference()
    {
        return RelativePosition.REFERENCE_POSITION;
    }

    /** {@inheritDoc} */
    @Override
    public final OtsSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final Parameters getParameters()
    {
        return this.parameters;
    }

    /** {@inheritDoc} */
    @Override
    public final void setParameters(final Parameters parameters)
    {
        this.parameters = parameters;
    }

    /** {@inheritDoc} */
    @Override
    public StrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner.get();
    }

    /** {@inheritDoc} */
    @Override
    public StrategicalPlanner getStrategicalPlanner(final Time time)
    {
        return this.strategicalPlanner.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan getOperationalPlan()
    {
        return this.operationalPlan.get();
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan getOperationalPlan(final Time time)
    {
        return this.operationalPlan.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOdometer()
    {
        return getOdometer(this.simulator.getSimulatorAbsTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOdometer(final Time time)
    {
        synchronized (this)
        {
            if (getOperationalPlan(time) == null)
            {
                return this.odometer.get(time);
            }
            try
            {
                return this.odometer.get(time).plus(getOperationalPlan(time).getTraveledDistance(time));
            }
            catch (OperationalPlanException ope)
            {
                return this.odometer.get(time);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        synchronized (this)
        {
            return getSpeed(this.simulator.getSimulatorAbsTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed(final Time time)
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

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        synchronized (this)
        {
            return getAcceleration(this.simulator.getSimulatorAbsTime());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration(final Time time)
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
     * @return maximumAcceleration
     */
    @Override
    public final Acceleration getMaximumAcceleration()
    {
        return this.maximumAcceleration;
    }

    /**
     * @param maximumAcceleration Acceleration; set maximumAcceleration
     */
    public final void setMaximumAcceleration(final Acceleration maximumAcceleration)
    {
        if (maximumAcceleration.le(Acceleration.ZERO))
        {
            throw new RuntimeException("Maximum acceleration of GTU " + this.id + " set to value <= 0");
        }
        this.maximumAcceleration = maximumAcceleration;
    }

    /**
     * @return maximumDeceleration
     */
    @Override
    public final Acceleration getMaximumDeceleration()
    {
        return this.maximumDeceleration;
    }

    /**
     * Set the maximum deceleration.
     * @param maximumDeceleration Acceleration; set maximumDeceleration, must be a negative number
     */
    public final void setMaximumDeceleration(final Acceleration maximumDeceleration)
    {
        if (maximumDeceleration.ge(Acceleration.ZERO))
        {
            throw new RuntimeException("Cannot set maximum deceleration of GTU " + this.id + " to " + maximumDeceleration
                    + " (value must be negative)");
        }
        this.maximumDeceleration = maximumDeceleration;
    }

    /** Cache location time. */
    private Time cacheLocationTime = new Time(Double.NaN, TimeUnit.DEFAULT);

    /** Cached location at that time. */
    private DirectedPoint cacheLocation = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        synchronized (this)
        {
            if (this.operationalPlan.get() == null)
            {
                this.simulator.getLogger().always()
                        .error("No operational plan for GTU " + this.id + " at t=" + this.getSimulator().getSimulatorTime());
                return new DirectedPoint(0, 0, 0); // Do not cache it
            }
            try
            {
                // cache
                Time locationTime = this.simulator.getSimulatorAbsTime();
                if (null == this.cacheLocationTime || this.cacheLocationTime.si != locationTime.si)
                {
                    this.cacheLocationTime = null;
                    this.cacheLocation = this.operationalPlan.get().getLocation(locationTime);
                    this.cacheLocationTime = locationTime;
                }
                return this.cacheLocation;
            }
            catch (OperationalPlanException exception)
            {
                return new DirectedPoint(0, 0, 0);
            }
        }
    }

    /**
     * Return the shape of a dynamic object at time 'time'. Note that the getShape() method without a time returns the Minkowski
     * sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all locations of a GTU
     * for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @param time Time; the time for which we want the shape
     * @return OtsShape; the shape of the object at time 'time'
     */
    @Override
    public OtsShape getShape(final Time time)
    {
        try
        {
            if (this.shape == null)
            {
                double w = getWidth().si;
                double l = getLength().si;
                this.shape = new OtsShape(new OtsPoint3D(-0.5 * l, -0.5 * w, 0.0), new OtsPoint3D(-0.5 * l, 0.5 * w, 0.0),
                        new OtsPoint3D(0.5 * l, 0.5 * w, 0.0), new OtsPoint3D(0.5 * l, -0.5 * w, 0.0));
            }
            OtsShape s = transformShape(this.shape, this.operationalPlan.get(time).getLocation(time));
            System.out.println("gtu " + getId() + ", shape(t)=" + s);
            return s;
        }
        catch (OtsGeometryException | OperationalPlanException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Return the shape of the GTU for the validity time of the operational plan. Note that this method without a time returns
     * the Minkowski sum of all shapes of the spatial object for a validity time window, e.g., a contour that describes all
     * locations of a GTU for the next time step, i.e., the contour of the GTU belonging to the next operational plan.
     * @return OtsShape; the shape of the object over the validity of the operational plan
     */
    @Override
    public OtsShape getShape()
    {
        try
        {
            // TODO: the actual contour of the GTU has to be moved over the path
            OtsLine3D path = this.operationalPlan.get().getPath();
            // part of the Gtu length has to be added before the start and after the end of the path.
            // we assume the reference point is within the contour of the Gtu.
            double rear = Math.max(0.0, getReference().getDx().si - getRear().getDx().si);
            double front = path.getLengthSI() + Math.max(0.0, getFront().getDx().si - getReference().getDx().si);
            DirectedPoint p0 = path.getLocationExtendedSI(-rear);
            DirectedPoint pn = path.getLocationExtendedSI(front);
            List<OtsPoint3D> pList = new ArrayList<>(Arrays.asList(path.getPoints()));
            pList.add(0, new OtsPoint3D(p0));
            pList.add(new OtsPoint3D(pn));
            OtsLine3D extendedPath = new OtsLine3D(pList);
            List<OtsPoint3D> swath = new ArrayList<>();
            swath.addAll(Arrays.asList(extendedPath.offsetLine(getWidth().si / 2.0).getPoints()));
            swath.addAll(Arrays.asList(extendedPath.offsetLine(-getWidth().si / 2.0).reverse().getPoints()));
            OtsShape s = new OtsShape(swath);
            // System.out.println("gtu " + getId() + ", w=" + getWidth() + ", path="
            //         + this.operationalPlan.get().getPath().toString() + ", shape=" + s);
            return s;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isDestroyed()
    {
        return this.destroyed;
    }

    /** {@inheritDoc} */
    @Override
    public PerceivableContext getPerceivableContext()
    {
        return this.perceivableContext;
    }

    /** {@inheritDoc} */
    @Override
    public void addGtu(final Gtu gtu) throws GtuException
    {
        this.children.add(gtu);
        gtu.setParent(this);
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void setParent(final Gtu gtu) throws GtuException
    {
        Throw.when(gtu != null && this.parent != null, GtuException.class, "GTU %s already has a parent.", this);
        this.parent = gtu;
    }

    /** {@inheritDoc} */
    @Override
    public Gtu getParent()
    {
        return this.parent;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Gtu> getChildren()
    {
        return new LinkedHashSet<>(this.children); // safe copy
    }

    /**
     * @return errorHandler.
     */
    protected GtuErrorHandler getErrorHandler()
    {
        return this.errorHandler;
    }

    /** {@inheritDoc} */
    @Override
    public void setErrorHandler(final GtuErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    /** {@inheritDoc} */
    @Override
    public final Serializable getSourceId()
    {
        return this; // TODO: see where the actual pointer to the GTU is needed
    }

    /**
     * Note that destroying the next move event of the GTU can be dangerous!
     * @return nextMoveEvent the next move event of the GTU, e.g. to cancel it from outside.
     */
    public final SimEvent<Duration> getNextMoveEvent()
    {
        return this.nextMoveEvent;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("designforextension")
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + this.uniqueNumber;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"designforextension", "needbraces"})
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractGtu other = (AbstractGtu) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.uniqueNumber != other.uniqueNumber)
            return false;
        return true;
    }

}
