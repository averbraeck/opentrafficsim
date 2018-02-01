package org.opentrafficsim.core.gtu;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
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
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Implements the basic functionalities of any GTU: the ability to move on 3D-space according to a plan.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractGTU extends EventProducer implements GTU
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
    private final GTUType gtuType;

    /** The simulator to schedule activities on. */
    private final OTSDEVSSimulatorInterface simulator;

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
    private SimEvent<OTSSimTimeDouble> nextMoveEvent;

    /** The model in which this GTU is registered. */
    private PerceivableContext perceivableContext;

    /** Turn indicator status. */
    private final Historical<TurnIndicatorStatus> turnIndicatorStatus;

    /** Is this GTU destroyed? */
    private boolean destroyed = false;

    /** The cached base color. */
    private Color baseColor = null;

    /** aligned or not. */
    // TODO: should be indicated with a Parameter
    public static boolean ALIGNED = true;

    /** aligned schedule count. */
    // TODO: can be removed after testing period
    public static int ALIGN_COUNT = 0;

    /** Cache. */
    private final Map<CacheKey<?>, TimeStampedObject<?>> cache = new HashMap<>();

    /** Cached speed time. */
    private double cachedSpeedTime = Double.NaN;

    /** Cached speed. */
    private Speed cachedSpeed = null;

    /** Cached acceleration time. */
    private double cachedAccelerationTime = Double.NaN;

    /** Cached acceleration. */
    private Acceleration cachedAcceleration = null;

    /**
     * @param id String; the id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSDEVSSimulatorInterface; the simulator to schedule plan changes on
     * @param perceivableContext PerceivableContext; the perceivable context in which this GTU will be registered
     * @throws GTUException when the preconditions of the constructor are not met
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final PerceivableContext perceivableContext) throws GTUException
    {
        Throw.when(id == null, GTUException.class, "id is null");
        Throw.when(gtuType == null, GTUException.class, "gtuType is null");
        Throw.when(perceivableContext == null, GTUException.class, "perceivableContext is null for GTU with id %s", id);
        Throw.when(perceivableContext.containsGtuId(id), GTUException.class,
                "GTU with id %s already registered in perceivableContext %s", id, perceivableContext.getId());
        Throw.when(simulator == null, GTUException.class, "simulator is null for GTU with id %s", id);

        this.id = id;
        this.uniqueNumber = ++staticUNIQUENUMBER;
        this.gtuType = gtuType;
        this.simulator = simulator;
        this.odometer = new HistoricalValue<>(HistoryManager.get(simulator), Length.ZERO);
        this.perceivableContext = perceivableContext;
        this.perceivableContext.addGTU(this);
        this.strategicalPlanner = new HistoricalValue<>(HistoryManager.get(simulator));
        this.tacticalPlanner = new HistoricalValue<>(HistoryManager.get(simulator), null);
        this.turnIndicatorStatus = new HistoricalValue<>(HistoryManager.get(simulator), TurnIndicatorStatus.NOTPRESENT);
        this.operationalPlan = new HistoricalValue<>(HistoryManager.get(simulator), null);
    }

    /**
     * @param idGenerator IdGenerator; the generator that will produce a unique id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSDEVSSimulatorInterface; the simulator to schedule plan changes on
     * @param perceivableContext PerceivableContext; the perceivable context in which this GTU will be registered
     * @throws GTUException when the preconditions of the constructor are not met
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTU(final IdGenerator idGenerator, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final PerceivableContext perceivableContext) throws GTUException
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
     * @throws GTUException when the preconditions of the parameters are not met or when the construction of the original
     *             waiting path fails
     */
    @SuppressWarnings({ "checkstyle:hiddenfield", "hiding", "checkstyle:designforextension" })
    public void init(final StrategicalPlanner strategicalPlanner, final DirectedPoint initialLocation, final Speed initialSpeed)
            throws SimRuntimeException, GTUException
    {
        Throw.when(strategicalPlanner == null, GTUException.class, "strategicalPlanner is null for GTU with id %s", this.id);
        Throw.whenNull(initialLocation, "Initial location of GTU cannot be null");
        Throw.when(Double.isNaN(initialLocation.x) || Double.isNaN(initialLocation.y) || Double.isNaN(initialLocation.z),
                GTUException.class, "initialLocation %s invalid for GTU with id %s", initialLocation, this.id);
        Throw.when(initialSpeed == null, GTUException.class, "initialSpeed is null for GTU with id %s", this.id);
        Throw.when(!getId().equals(strategicalPlanner.getGtu().getId()), GTUException.class,
                "GTU %s is initialized with a strategical planner for GTU %s", getId(), strategicalPlanner.getGtu().getId());

        this.strategicalPlanner.set(strategicalPlanner);
        this.tacticalPlanner.set(strategicalPlanner.getTacticalPlanner());
        Time now = this.simulator.getSimulatorTime().getTime();

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move will work
        DirectedPoint p = initialLocation;
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan.set(new OperationalPlan(this, p, now, new Duration(1E-6, DurationUnit.SECOND)));
            }
            else
            {
                OTSPoint3D p2 = new OTSPoint3D(p.x + 1E-6 * Math.cos(p.getRotZ()), p.y + 1E-6 * Math.sin(p.getRotZ()), p.z);
                OTSLine3D path = new OTSLine3D(new OTSPoint3D(p), p2);
                this.operationalPlan.set(OperationalPlanBuilder.buildConstantSpeedPlan(this, path, now, initialSpeed));
            }

            fireTimedEvent(GTU.INIT_EVENT, new Object[] { getId(), initialLocation, getLength(), getWidth(), getBaseColor() },
                    now);

            // and do the real move
            move(initialLocation);
        }
        catch (OperationalPlanException | OTSGeometryException | NetworkException | ParameterException exception)
        {
            throw new GTUException("Failed to create OperationalPlan for GTU " + this.id, exception);
        }
    }

    /**
     * Generate an id, but check first that we have a valid IdGenerator.
     * @param idGenerator IdGenerator; the generator that will produce a unique id of the GTU
     * @return a (hopefully unique) Id of the GTU
     * @throws GTUException when the idGenerator is null
     */
    private static String generateId(final IdGenerator idGenerator) throws GTUException
    {
        Throw.when(idGenerator == null, GTUException.class, "AbstractGTU.<init>: idGenerator is null");
        return idGenerator.nextId();
    }

    /**
     * Destructor. Don't forget to call with super.destroy() from any override to avoid memory leaks in the network.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        fireTimedEvent(GTU.DESTROY_EVENT, new Object[] { getId(), getLocation(), getOdometer() },
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
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws OperationalPlanException when there is a problem creating a good path for the GTU
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., a dead end where it is not expected
     * @throws ParameterException in there is a parameter problem
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void move(final DirectedPoint fromLocation)
            throws SimRuntimeException, OperationalPlanException, GTUException, NetworkException, ParameterException
    {
        Time now = this.simulator.getSimulatorTime().getTime();

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
        OperationalPlan newOperationalPlan = tactPlanner.generateOperationalPlan(now, fromLocation);
        this.operationalPlan.set(newOperationalPlan);
        this.cachedSpeed = null;
        this.cachedAcceleration = null;
        this.odometer.set(currentOdometer);
        if (getOperationalPlan().getAcceleration(Duration.ZERO).si < -10
                && getOperationalPlan().getSpeed(Duration.ZERO).si > 2.5)
        {
            System.err.println("(getOperationalPlan().getAcceleration(Duration.ZERO).si < -10)");
            // this.tacticalPlanner.generateOperationalPlan(now, fromLocation);
        }

        // TODO allow alignment at different intervals, also different between GTU's within a single simulation
        if (ALIGNED && newOperationalPlan.getTotalDuration().si == 0.5)
        {
            // schedule the next move at exactly 0.5 seconds on the clock
            // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
            double tNext = Math.floor(2.0 * now.si + 1.0) / 2.0;
            DirectedPoint p = (tNext - now.si < 0.5) ? newOperationalPlan.getEndLocation()
                    : newOperationalPlan.getLocation(new Duration(tNext - now.si, DurationUnit.SI));
            this.nextMoveEvent = new SimEvent<>(new OTSSimTimeDouble(new Time(tNext, TimeUnit.BASE)), this, this, "move",
                    new Object[] { p });
            ALIGN_COUNT++;
        }
        else
        {
            // schedule the next move at the end of the current operational plan
            // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
            this.nextMoveEvent = new SimEvent<>(new OTSSimTimeDouble(now.plus(newOperationalPlan.getTotalDuration())), this,
                    this, "move", new Object[] { newOperationalPlan.getEndLocation() });
        }
        this.simulator.scheduleEvent(this.nextMoveEvent);

        fireTimedEvent(GTU.MOVE_EVENT,
                new Object[] { getId(), fromLocation, getSpeed(), getAcceleration(), getTurnIndicatorStatus(), getOdometer() },
                this.simulator.getSimulatorTime());
    }

    /**
     * Interrupt the move and ask for a new plan. This method can be overridden to carry out the bookkeeping needed when the
     * current plan gets interrupted.
     * @throws OperationalPlanException when there was a problem retrieving the location from the running plan
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws OperationalPlanException when there is a problem creating a good path for the GTU
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., unreachability of a certain point
     * @throws ParameterException when there is a problem with a parameter
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void interruptMove()
            throws SimRuntimeException, OperationalPlanException, GTUException, NetworkException, ParameterException
    {
        this.simulator.cancelEvent(this.nextMoveEvent);
        move(this.operationalPlan.get().getLocation(this.simulator.getSimulatorTime().getTime()));
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
    public GTUType getGTUType()
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
    public final OTSDEVSSimulatorInterface getSimulator()
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
    public TacticalPlanner<?, ?> getTacticalPlanner()
    {
        return this.strategicalPlanner.get().getTacticalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    public TacticalPlanner<?, ?> getTacticalPlanner(final Time time)
    {
        return this.strategicalPlanner.get(time).getTacticalPlanner(time);
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
        return getOdometer(this.simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOdometer(final Time time)
    {
        if (this.operationalPlan.get(time) == null)
        {
            return this.odometer.get(time);
        }
        try
        {
            return this.odometer.get(time).plus(this.operationalPlan.get(time).getTraveledDistance(time));
        }
        catch (@SuppressWarnings("unused") OperationalPlanException ope)
        {
            return this.odometer.get(time);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        return getSpeed(this.simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed(final Time time)
    {
        if (this.cachedSpeed == null || this.cachedSpeedTime != time.si)
        {
            this.cachedSpeedTime = time.si;
            if (this.operationalPlan.get(time) == null)
            {
                this.cachedSpeed = Speed.ZERO;
            }
            else
            {
                try
                {
                    this.cachedSpeed = this.operationalPlan.get(time).getSpeed(time);
                }
                catch (OperationalPlanException ope)
                {
                    // this should not happen at all...
                    System.err.println("t = " + time);
                    System.err.println("op.validity = " + this.operationalPlan.get(time).getEndTime());
                    throw new RuntimeException("getSpeed() could not derive a valid speed for the current operationalPlan",
                            ope);
                }
            }
        }
        return this.cachedSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        return getAcceleration(this.simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration(final Time time)
    {
        if (this.cachedAcceleration == null || this.cachedAccelerationTime != time.si)
        {
            this.cachedAccelerationTime = time.si;
            if (this.operationalPlan.get(time) == null)
            {
                this.cachedAcceleration = Acceleration.ZERO;
            }
            else
            {
                try
                {
                    this.cachedAcceleration = this.operationalPlan.get(time).getAcceleration(time);
                }
                catch (OperationalPlanException ope)
                {
                    // this should not happen at all...
                    throw new RuntimeException(
                            "getAcceleration() could not derive a valid acceleration for the current operationalPlan", ope);
                }
            }
        }
        return this.cachedAcceleration;
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
     * @param maximumAcceleration set maximumAcceleration
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
     * @param maximumDeceleration set maximumDeceleration, stored as a negative number
     */
    public final void setMaximumDeceleration(final Acceleration maximumDeceleration)
    {
        if (maximumDeceleration.ge(Acceleration.ZERO))
        {
            throw new RuntimeException("Maximum deceleration of GTU " + this.id + " set to value >= 0");
        }
        this.maximumDeceleration = maximumDeceleration;
    }

    /** cache time. */
    private Time cacheLocationTime = new Time(Double.NaN, TimeUnit.BASE);

    /** caced position at time. */
    private DirectedPoint cacheLocation = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        if (this.operationalPlan.get() == null)
        {
            System.err.println(
                    "No operational plan for GTU " + this.id + " at t=" + this.getSimulator().getSimulatorTime().getTime());
            return new DirectedPoint(0, 0, 0);
        }
        try
        {
            // cache
            if (this.cacheLocationTime.si != this.simulator.getSimulatorTime().getTime().si)
            {
                this.cacheLocationTime = this.simulator.getSimulatorTime().getTime();
                this.cacheLocation = this.operationalPlan.get().getLocation(this.cacheLocationTime);
            }
            return this.cacheLocation;
        }
        catch (@SuppressWarnings("unused") OperationalPlanException exception)
        {
            return new DirectedPoint(0, 0, 0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus()
    {
        return this.turnIndicatorStatus.get();
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus(final Time time)
    {
        return this.turnIndicatorStatus.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus)
    {
        this.turnIndicatorStatus.set(turnIndicatorStatus);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Color getBaseColor()
    {
        if (this.baseColor == null)
        {
            this.baseColor = IDGTUColorer.LEGEND.get(this.uniqueNumber % IDGTUColorer.LEGEND.size()).getColor();
        }
        return this.baseColor;
    }

    /**
     * @return whether the GTU is destroyed, for the animation.
     */
    public final boolean isDestroyed()
    {
        return this.destroyed;
    }

    /**
     * @return perceivableContext
     */
    public final PerceivableContext getPerceivableContext()
    {
        return this.perceivableContext;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public final <K> K getCachedValue(final CacheKey<K> key)
    {
        TimeStampedObject<?> tso = this.cache.get(key);
        if (tso != null)
        {
            if (tso.getTimestamp().eq(this.getSimulator().getSimulatorTime().getTime()))
            {
                return ((TimeStampedObject<K>) tso).getObject(); // cacheValue() assures type correctness
            }
            this.cache.remove(key); // removes old value
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final <K> void cacheValue(final CacheKey<K> key, final K value)
    {
        Throw.whenNull(key, "Caching key may not be null.");
        Throw.whenNull(value, "Caching value may not be null.");
        this.cache.put(key, new TimeStampedObject<>(value, this.getSimulator().getSimulatorTime().getTime()));
    }

    /** {@inheritDoc} */
    @Override
    public final <K> K getOrCalculateValue(final CacheKey<K> key, final Supplier<? extends K> calculator)
    {
        K value = getCachedValue(key);
        if (value == null)
        {
            // no cache, or old cache
            value = calculator.get();
            // cache it
            cacheValue(key, value);
        }
        return value;
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
    @SuppressWarnings({ "designforextension", "needbraces" })
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractGTU other = (AbstractGTU) obj;
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
