package org.opentrafficsim.core.car;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedIndividualGTU;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public class LaneBasedIndividualCar<ID> extends AbstractLaneBasedIndividualGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** animation. */
    private Renderable2D animation;

    /** Sensing positions. */
    private final Map<RelativePosition.TYPE, RelativePosition> relativePositions = new HashMap<>();

    /**
     * @param id ID; the id of the GTU, could be String or Integer
     * @param gtuType GTUTYpe&lt;?&gt;; the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel GTUFollowingModel; the following model, including a reference to the simulator
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial positions of
     *            the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param length DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum length of the GTU (parallel with driving direction)
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum width of the GTU (perpendicular to driving
     *            direction)
     * @param maximumVelocity DoubleScalar.Abs&lt;SpeedUnit&gt;;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualCar(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
            final LaneChangeModel laneChangeModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final OTSDEVSSimulatorInterface simulator) throws NamingException, RemoteException, NetworkException,
            SimRuntimeException, GTUException
    {
        this(id, gtuType, gtuFollowingModel, laneChangeModel, initialLongitudinalPositions, initialSpeed, length,
                width, maximumVelocity, simulator, DefaultCarAnimation.class);
    }

    /**
     * Construct a new LaneBasedIndividualCar.
     * @param id ID; the id of the GTU, could be String or Integer
     * @param gtuType GTUTYpe&lt;?&gt;; the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel GTUFollowingModel; the following model, including a reference to the simulator
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial positions of
     *            the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param length DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum length of the GTU (parallel with driving direction)
     * @param width DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum width of the GTU (perpendicular to driving
     *            direction)
     * @param maximumVelocity DoubleScalar.Abs&lt;SpeedUnit&gt;;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualCar(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
            final LaneChangeModel laneChangeModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final OTSDEVSSimulatorInterface simulator, final Class<? extends Renderable2D> animationClass)
            throws NamingException, RemoteException, NetworkException, SimRuntimeException, GTUException
    {
        super(id, gtuType, gtuFollowingModel, laneChangeModel, initialLongitudinalPositions, initialSpeed, length,
                width, maximumVelocity, simulator);

        // sensor positions.
        // We take the rear position of the Car to be the reference point. So the front is the length
        // of the Car away from the reference point in the positive (driving) X-direction.
        DoubleScalar.Rel<LengthUnit> zero = new DoubleScalar.Rel<LengthUnit>(0.0d, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> dx = new DoubleScalar.Rel<LengthUnit>(getLength().getSI(), LengthUnit.METER);
        this.relativePositions
                .put(RelativePosition.FRONT, new RelativePosition(dx, zero, zero, RelativePosition.FRONT));
        this.relativePositions
                .put(RelativePosition.REAR, new RelativePosition(zero, zero, zero, RelativePosition.REAR));
        this.relativePositions.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);

        // animation
        if (simulator instanceof OTSAnimatorInterface && animationClass != null)
        {
            try
            {
                Constructor<?> constructor =
                        ClassUtil.resolveConstructor(animationClass, new Object[]{this, simulator});
                this.animation = (Renderable2D) constructor.newInstance(this, simulator);
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                    | IllegalArgumentException | InvocationTargetException exception)
            {
                throw new NetworkException("Could not instantiate car animation of type " + animationClass.getName(),
                        exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public RelativePosition getFront()
    {
        return this.relativePositions.get(RelativePosition.FRONT);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public RelativePosition getRear()
    {
        return this.relativePositions.get(RelativePosition.REAR);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<TYPE, RelativePosition> getRelativePositions()
    {
        return this.relativePositions;
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy()
    {
        if (this.animation != null)
        {
            try
            {
                this.animation.destroy();
            }
            catch (Exception e)
            {
                System.err.println("Car: " + this.getId());
                e.printStackTrace();
            }
        }
        super.destroy();
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        try
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> frontPositions = positions(getFront());
            Lane frontLane = frontPositions.keySet().iterator().next();
            return String.format("Car %s front: %s[%s]", getId(), frontLane, frontPositions.get(frontLane));
        }
        catch (RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
        return "Caught exception in toString";
    }

    /**
     * Build an individual car and use easy setter methods to instantiate the car. Typical use looks like:
     * 
     * <pre>
     * LaneBasedIndividualCar<String> car = new LaneBasedIndividualCarBuilder<String>().setId("Car:"+nr)
     *    .setLength(new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER))....build(); 
     *    
     * or
     * 
     * LaneBasedIndividualCarBuilder<String> carBuilder = new LaneBasedIndividualCarBuilder<String>();
     * carBuilder.setId("Car:"+nr);
     * carBuilder.setLength(new DoubleScalar.Rel<LengthUnit>(4.0, LengthUnit.METER));
     * carBuilder.setWidth(new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER));
     * ...
     * LaneBasedIndividualCar<String> car = carBuilder.build();
     * </pre>
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Feb 3, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @param <ID> the ID type of the Car, e.g. String or Integer or Long.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public static class LaneBasedIndividualCarBuilder<ID>
    {
        /** The id of the GTU, could be String or Integer. */
        private ID id = null;;

        /** The type of GTU, e.g. TruckType, CarType, BusType. */
        private GTUType<ID> gtuType = null;;

        /** The initial positions of the car on one or more lanes. */
        private Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = null;;

        /** The initial speed of the car on the lane. */
        private DoubleScalar.Abs<SpeedUnit> initialSpeed = null;

        /** CarFollowingModel used by this Car. */
        private GTUFollowingModel gtuFollowingModel = null;

        /** The lane change model. */
        private AbstractLaneChangeModel laneChangeModel = null;

        /** The maximum length of the GTU (parallel with driving direction). */
        private DoubleScalar.Rel<LengthUnit> length = null;

        /** The maximum width of the GTU (perpendicular to driving direction). */
        private DoubleScalar.Rel<LengthUnit> width = null;

        /** The maximum speed of the GTU (in the driving direction). */
        private DoubleScalar.Abs<SpeedUnit> maximumVelocity = null;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator = null;

        /** Animation. */
        private Class<? extends Renderable2D> animationClass = null;

        /**
         * @param id set id.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setId(final ID id)
        {
            this.id = id;
            return this;
        }

        /**
         * @param gtuType set gtuType.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setGtuType(final GTUType<ID> gtuType)
        {
            this.gtuType = gtuType;
            return this;
        }

        /**
         * @param gtuFollowingModel
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setGTUFollowingModel(final GTUFollowingModel gtuFollowingModel)
        {
            this.gtuFollowingModel = gtuFollowingModel;
            return this;
        }

        /**
         * @param laneChangeModel AbstractLaneChangeModel; the lane change model
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setLaneChangeModel(final AbstractLaneChangeModel laneChangeModel)
        {
            this.laneChangeModel = laneChangeModel;
            return this;
        }

        /**
         * @param initialLongitudinalPositions set initialLongitudinalPositions.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setInitialLongitudinalPositions(
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions)
        {
            this.initialLongitudinalPositions = initialLongitudinalPositions;
            return this;
        }

        /**
         * @param initialSpeed set initialSpeed.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setInitialSpeed(final DoubleScalar.Abs<SpeedUnit> initialSpeed)
        {
            this.initialSpeed = initialSpeed;
            return this;
        }

        /**
         * @param length set length.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setLength(final DoubleScalar.Rel<LengthUnit> length)
        {
            this.length = length;
            return this;
        }

        /**
         * @param width set width.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setWidth(final DoubleScalar.Rel<LengthUnit> width)
        {
            this.width = width;
            return this;
        }

        /**
         * @param maximumVelocity set maximumVelocity.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setMaximumVelocity(
                final DoubleScalar.Abs<SpeedUnit> maximumVelocity)
        {
            this.maximumVelocity = maximumVelocity;
            return this;
        }

        /**
         * @param simulator set simulator.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setSimulator(final OTSDEVSSimulatorInterface simulator)
        {
            this.simulator = simulator;
            return this;
        }

        /**
         * @param animationClass set animation class.
         * @return the class itself for chaining the setters.
         */
        public final LaneBasedIndividualCarBuilder<ID> setAnimationClass(
                final Class<? extends Renderable2D> animationClass)
        {
            this.animationClass = animationClass;
            return this;
        }

        /**
         * Build one LaneBasedIndividualCar.
         * @return the built Car with the set properties
         * @throws Exception when not all required values have been set
         */
        public final LaneBasedIndividualCar<ID> build() throws Exception
        {
            if (null == this.id || null == this.gtuType || null == this.gtuFollowingModel
                    || null == this.laneChangeModel || null == this.initialLongitudinalPositions
                    || null == this.initialSpeed || null == this.length || null == this.width
                    || null == this.maximumVelocity || null == this.simulator)
            {
                // TODO Should throw a more specific Exception type
                throw new Exception("factory settings incomplete");
            }
            return new LaneBasedIndividualCar<ID>(this.id, this.gtuType, this.gtuFollowingModel, this.laneChangeModel,
                    this.initialLongitudinalPositions, this.initialSpeed, this.length, this.width,
                    this.maximumVelocity, this.simulator, this.animationClass);
        }
    }
}
