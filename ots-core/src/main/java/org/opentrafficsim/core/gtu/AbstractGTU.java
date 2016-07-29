package org.opentrafficsim.core.gtu;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivableContext;

/**
 * Implements the basic functionalities of any GTU: the ability to move on 3D-space according to a plan.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractGTU implements GTU
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** The id of the GTU. */
    private final String id;

    /** The type of GTU, e.g. TruckType, CarType, BusType. */
    private final GTUType gtuType;

    /** The simulator to schedule activities on. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The maximum acceleration. */
    private Acceleration maximumAcceleration;

    /** The maximum deceleration, stored as a negative number. */
    private Acceleration maximumDeceleration;

    /**
     * The odometer which measures how much distance have we covered between instantiation and the last completed operational
     * plan. In order to get a complete odometer reading, the progress of the current plan execution has to be added to this
     * value.
     */
    private Length odometer;

    /** The strategical planner that can instantiate tactical planners to determine mid-term decisions. */
    private StrategicalPlanner strategicalPlanner;

    /** The tactical planner that can generate an operational plan. */
    private TacticalPlanner tacticalPlanner = null;

    /** The current operational plan, which provides a short-term movement over time. */
    private OperationalPlan operationalPlan = null;

    /** The next move event as scheduled on the simulator, can be used for interrupting the current move. */
    private SimEvent<OTSSimTimeDouble> nextMoveEvent;

    /** The model in which this GTU is registered. */
    private PerceivableContext perceivableContext;

    /** Turn indicator status. */
    private TurnIndicatorStatus turnIndicatorStatus = TurnIndicatorStatus.NOTPRESENT;

    /** Is this GTU destroyed? */
    private boolean destroyed = false;

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
        Throw.when(gtuType.equals(GTUType.NONE), GTUException.class, "gtuType of an actual GTU cannot be GTUType.NONE");
        Throw.when(gtuType.equals(GTUType.ALL), GTUException.class, "gtuType of an actual GTU cannot be GTUType.ALL");
        Throw.when(perceivableContext == null, GTUException.class, "perceivableContext is null for GTU with id %s", id);
        Throw.when(perceivableContext.containsGtuId(id), GTUException.class,
                "GTU with id %s already registered in perceivableContext %s", id, perceivableContext.getId());
        Throw.when(simulator == null, GTUException.class, "simulator is null for GTU with id %s", id);

        this.id = id;
        this.gtuType = gtuType;
        this.simulator = simulator;
        this.odometer = Length.ZERO;
        this.perceivableContext = perceivableContext;
        this.perceivableContext.addGTU(this);
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
    @SuppressWarnings({ "checkstyle:hiddenfield", "hiding" })
    public final void init(final StrategicalPlanner strategicalPlanner, final DirectedPoint initialLocation,
            final Speed initialSpeed) throws SimRuntimeException, GTUException
    {
        Throw.when(strategicalPlanner == null, GTUException.class, "strategicalPlanner is null for GTU with id %s", this.id);
        Throw.whenNull(initialLocation, "Initial location of GTU cannot be null");
        Throw.when(Double.isNaN(initialLocation.x) || Double.isNaN(initialLocation.y) || Double.isNaN(initialLocation.z),
                GTUException.class, "initialLocation %s invalid for GTU with id %s", initialLocation, this.id);
        Throw.when(initialSpeed == null, GTUException.class, "initialSpeed is null for GTU with id %s", this.id);

        this.strategicalPlanner = strategicalPlanner;
        Time now = this.simulator.getSimulatorTime().getTime();

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move will work
        DirectedPoint p = initialLocation;
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan = new OperationalPlan(this, p, now, new Duration(1E-6, TimeUnit.SECOND));
            }
            else
            {
                OTSPoint3D p2 = new OTSPoint3D(p.x + 1E-6 * Math.cos(p.getRotZ()), p.y + 1E-6 * Math.sin(p.getRotZ()), p.z);
                OTSLine3D path = new OTSLine3D(new OTSPoint3D(p), p2);
                this.operationalPlan = OperationalPlanBuilder.buildConstantSpeedPlan(this, path, now, initialSpeed);
            }

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
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        this.perceivableContext.removeGTU(this);

        // cancel the next move
        if (this.nextMoveEvent != null)
        {
            this.simulator.cancelEvent(this.nextMoveEvent);
            this.nextMoveEvent = null;
        }

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
    protected void move(final DirectedPoint fromLocation) throws SimRuntimeException, OperationalPlanException, GTUException,
            NetworkException, ParameterException
    {
        Time now = this.simulator.getSimulatorTime().getTime();

        // Add the odometer distance from the currently running operational plan.
        // Because a plan can be interrupted, we explicitly calculate the covered distance till 'now'
        if (this.operationalPlan != null)
        {
            this.odometer = this.odometer.plus(this.operationalPlan.getTraveledDistance(now));
        }

        // Do we have an operational plan?
        // TODO discuss when a new tactical planner may be needed
        if (this.tacticalPlanner == null)
        {
            // Tell the strategical planner to provide a tactical planner
            this.tacticalPlanner = this.strategicalPlanner.generateTacticalPlanner();
        }
        this.operationalPlan = this.tacticalPlanner.generateOperationalPlan(now, fromLocation);

        // schedule the next move at the end of the current operational plan
        // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
        this.nextMoveEvent =
                new SimEvent<>(new OTSSimTimeDouble(now.plus(this.operationalPlan.getTotalDuration())), this, this, "move",
                        new Object[] { this.operationalPlan.getEndLocation() });
        this.simulator.scheduleEvent(this.nextMoveEvent);
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
    protected void interruptMove() throws SimRuntimeException, OperationalPlanException, GTUException, NetworkException,
            ParameterException
    {
        this.simulator.cancelEvent(this.nextMoveEvent);
        move(this.operationalPlan.getLocation(this.simulator.getSimulatorTime().getTime()));
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

    /**
     * @return simulator the simulator to schedule plan changes on
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return strategicalPlanner the planner responsible for the overall 'mission' of the GTU, usually indicating where it
     *         needs to go. It operates by instantiating tactical planners to do the work.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public StrategicalPlanner getStrategicalPlanner()
    {
        return this.strategicalPlanner;
    }

    /**
     * @return tacticalPlanner the tactical planner that can generate an operational plan
     */
    @SuppressWarnings("checkstyle:designforextension")
    public TacticalPlanner getTacticalPlanner()
    {
        // TODO discuss when a new tactical planner may be needed
        if (null == this.tacticalPlanner)
        {
            this.tacticalPlanner = this.strategicalPlanner.generateTacticalPlanner();
        }
        return this.tacticalPlanner;
    }

    /**
     * @return operationalPlan the current operational plan, which provides a short-term movement over time
     */
    public final OperationalPlan getOperationalPlan()
    {
        return this.operationalPlan;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getOdometer()
    {
        if (this.operationalPlan == null)
        {
            return this.odometer;
        }
        try
        {
            return this.odometer.plus(this.operationalPlan.getTraveledDistance(this.simulator.getSimulatorTime().getTime()));
        }
        catch (OperationalPlanException ope)
        {
            return this.odometer;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        if (this.operationalPlan == null)
        {
            return Speed.ZERO;
        }
        try
        {
            return this.operationalPlan.getSpeed(this.simulator.getSimulatorTime().getTime());
        }
        catch (OperationalPlanException ope)
        {
            // this should not happen at all...
            throw new RuntimeException("getSpeed() could not derive a valid speed for the current operationalPlan", ope);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        if (this.operationalPlan == null)
        {
            return Acceleration.ZERO;
        }
        try
        {
            return this.operationalPlan.getAcceleration(this.simulator.getSimulatorTime().getTime());
        }
        catch (OperationalPlanException ope)
        {
            // this should not happen at all...
            throw new RuntimeException(
                    "getAcceleration() could not derive a valid acceleration for the current operationalPlan", ope);
        }
    }

    /**
     * @return maximumAcceleration
     */
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedPoint getLocation()
    {
        if (this.operationalPlan == null)
        {
            System.err.println("No operational plan");
            return new DirectedPoint(0, 0, 0);
        }
        try
        {
            return this.operationalPlan.getLocation(this.simulator.getSimulatorTime().getTime());
        }
        catch (OperationalPlanException exception)
        {
            return new DirectedPoint(0, 0, 0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus()
    {
        return this.turnIndicatorStatus;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus)
    {
        this.turnIndicatorStatus = turnIndicatorStatus;
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

}
