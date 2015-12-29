package org.opentrafficsim.core.gtu;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivableContext;

/**
 * Implements the basic functionalities of any GTU: the ability to move on 3D-space according to a plan.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** the id of the GTU. */
    private final String id;

    /** the type of GTU, e.g. TruckType, CarType, BusType. */
    private final GTUType gtuType;

    /** the simulator to schedule activities on. */
    private final OTSDEVSSimulatorInterface simulator;

    /** the maximum acceleration. */
    private Acceleration maximumAcceleration;

    /** the maximum deceleration, stored as a negative number. */
    private Acceleration maximumDeceleration;

    /**
     * the odometer which measures how much distance have we covered between instantiation and the last completed operational
     * plan. In order to get a complete odometer reading, the progress of the current plan execution has to be added to this
     * value.
     */
    private Length.Rel odometer;

    /** the strategical planner that can instantiate tactical planners to determine mid-term decisions. */
    private StrategicalPlanner strategicalPlanner;

    /** the tactical planner that can generate an operational plan. */
    private TacticalPlanner tacticalPlanner = null;

    /** the current operational plan, which provides a short-term movement over time. */
    private OperationalPlan operationalPlan = null;

    /** the next move event as scheduled on the simulator, can be used for interrupting the current move. */
    private SimEvent<OTSSimTimeDouble> nextMoveEvent;

    /** the perception unit that takes care of observing the environment of the GTU. */
    private Perception perception;

    /** the model in which this GTU is registered. */
    private PerceivableContext perceivableContext;

    /**
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator the simulator to schedule plan changes on
     * @param strategicalPlanner the planner responsible for the overall 'mission' of the GTU, usually indicating where it needs
     *            to go. It operates by instantiating tactical planners to do the work.
     * @param perception the perception unit that takes care of observing the environment of the GTU
     * @param initialLocation the initial location (and direction) of the GTU
     * @param initialSpeed the initial speed of the GTU
     * @param perceivableContext the perceivable context in which this GTU will be registered
     * @throws SimRuntimeException when scheduling after the first move fails
     * @throws GTUException when the construction of the original waiting path fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
        final StrategicalPlanner strategicalPlanner, final Perception perception, final DirectedPoint initialLocation,
        final Speed initialSpeed, final PerceivableContext perceivableContext) throws SimRuntimeException, GTUException
    {
        super();
        this.id = id;
        this.gtuType = gtuType;
        this.simulator = simulator;
        this.strategicalPlanner = strategicalPlanner;
        this.perception = perception;
        this.odometer = Length.Rel.ZERO;
        this.perceivableContext = perceivableContext;
        this.perceivableContext.addGTU(this);
        Time.Abs now = this.simulator.getSimulatorTime().getTime();

        if (initialLocation != null)
        {
            // Schedule the first move now; scheduling so super constructors can still finish.
            // Store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
            this.nextMoveEvent =
                new SimEvent<>(new OTSSimTimeDouble(now), this, this, "move", new Object[]{initialLocation});
            this.simulator.scheduleEvent(this.nextMoveEvent);
        }

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so initialization will work
        DirectedPoint p = initialLocation == null ? new DirectedPoint() : initialLocation;
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan = new OperationalPlan(p, now, new Time.Rel(1.0e-6, TimeUnit.SECOND));
            }
            else
            {
                OTSPoint3D p2 =
                    new OTSPoint3D(p.x + 1E-6 * Math.cos(p.getRotZ()), p.y + 1E-6 * Math.sin(p.getRotZ()), p.z);
                OTSLine3D path = new OTSLine3D(new OTSPoint3D(p), p2);
                this.operationalPlan = OperationalPlanBuilder.buildConstantSpeedPlan(path, now, initialSpeed);
            }
        }
        catch (OperationalPlanException | OTSGeometryException exception)
        {
            throw new GTUException("Failed to create OperationalPlan for GTU " + this.id, exception);
        }
    }

    /**
     * Destructor. Don't forget to call with super.destroy() from any override to avoid memory leaks in the network.
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        this.perceivableContext.removeGTU(this);
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
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void move(final DirectedPoint fromLocation) throws SimRuntimeException, OperationalPlanException,
        GTUException, NetworkException
    {
        Time.Abs now = this.simulator.getSimulatorTime().getTime();

        // add the odometer distance from the previous operational plan
        // because a plan can be interrupted, we calculated the covered distance till 'now'
        if (this.operationalPlan != null)
        {
            this.odometer = this.odometer.plus(this.operationalPlan.getTraveledDistance(now));
        }

        // ask the tactical planner to provide an operational plan
        if (this.tacticalPlanner == null)
        {
            // ask the strategical planner to provide a tactical planner
            this.tacticalPlanner = this.strategicalPlanner.generateTacticalPlanner(this);
        }
        this.operationalPlan = this.tacticalPlanner.generateOperationalPlan(this, now, fromLocation);

        // schedule the next move at the end of the current operational plan
        // store the event, so it can be cancelled in case the plan has to be interrupted and changed halfway
        this.nextMoveEvent =
            new SimEvent<>(new OTSSimTimeDouble(now.plus(this.operationalPlan.getTotalDuration())), this, this, "move",
                new Object[]{this.operationalPlan.getEndLocation()});
        this.simulator.scheduleEvent(this.nextMoveEvent);
    }

    /**
     * Interrupt the move and ask for a new plan. This method can be overridden to carry out the bookkeeping needed when the
     * current plan gets interrupted.
     * @throws OperationalPlanException
     * @throws SimRuntimeException when scheduling of the next move fails
     * @throws OperationalPlanException when there is a problem creating a good path for the GTU
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     * @throws NetworkException in case of a problem with the network, e.g., unreachability of a certain point
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void interruptMove() throws SimRuntimeException, OperationalPlanException, GTUException, NetworkException
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
    public final TacticalPlanner getTacticalPlanner()
    {
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
    @SuppressWarnings("checkstyle:designforextension")
    public Perception getPerception()
    {
        return this.perception;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getOdometer()
    {
        if (this.operationalPlan == null)
        {
            return this.odometer;
        }
        try
        {
            return this.odometer.plus(this.operationalPlan.getTraveledDistance(this.simulator.getSimulatorTime()
                .getTime()));
        }
        catch (OperationalPlanException ope)
        {
            return this.odometer;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getVelocity(final Time.Abs time)
    {
        if (this.operationalPlan == null)
        {
            return Speed.ZERO;
        }
        try
        {
            return this.operationalPlan.getVelocity(time);
        }
        catch (OperationalPlanException ope)
        {
            // should not happen --there is a still valid operational plan. Return the end velocity of the plan.
            try
            {
                return this.operationalPlan.getVelocity(this.operationalPlan.getTotalDuration());
            }
            catch (OperationalPlanException ope2)
            {
                // this should not happen at all...
                throw new RuntimeException(
                    "getVelocity() could not derive a valid velocity for the current operationalPlan", ope2);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getVelocity()
    {
        return getVelocity(this.simulator.getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration(final Time.Abs time)
    {
        if (this.operationalPlan == null)
        {
            return Acceleration.ZERO;
        }
        try
        {
            return this.operationalPlan.getAcceleration(time);
        }
        catch (OperationalPlanException ope)
        {
            // should not happen --there is a still valid operational plan. Return the end acceleration of the plan.
            try
            {
                return this.operationalPlan.getAcceleration(this.operationalPlan.getTotalDuration());
            }
            catch (OperationalPlanException ope2)
            {
                // this should not happen at all...
                throw new RuntimeException(
                    "getAcceleration() could not derive a valid acceleration for the current operationalPlan", ope2);
            }
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
    public final Acceleration getAcceleration()
    {
        return getAcceleration(this.simulator.getSimulatorTime().getTime());
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
            System.err.println("Could not determine location, got exception: " + exception.getMessage());
            exception.printStackTrace();
            return new DirectedPoint(0, 0, 0);
        }
    }

}
