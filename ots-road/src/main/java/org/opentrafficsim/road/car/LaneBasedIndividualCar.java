package org.opentrafficsim.road.car;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * Augments the AbstractLaneBasedIndividualGTU with a LaneBasedIndividualCarBuilder and animation support
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedIndividualCar extends AbstractLaneBasedIndividualGTU
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** animation. */
    private Renderable2D animation;

    /** Sensing positions. */
    private final Map<RelativePosition.TYPE, RelativePosition> relativePositions = new LinkedHashMap<>();

    /**
     * @param id ID; the id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param length Length.Rel; the maximum length of the GTU (parallel with driving direction)
     * @param width Length.Rel; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity DoubleScalar.Abs&lt;SpeedUnit&gt;;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualCar(final String id, final GTUType gtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final Length.Rel length, final Length.Rel width, final Speed maximumVelocity,
        final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
        final LanePerception perception) throws NamingException, NetworkException, SimRuntimeException, GTUException
    {
        this(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
            strategicalPlanner, perception, DefaultCarAnimation.class, null);
    }

    /**
     * Construct a new LaneBasedIndividualCar.
     * @param id ID; the id of the GTU
     * @param gtuType GTUTYpe; the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param length Length.Rel; the maximum length of the GTU (parallel with driving direction)
     * @param width Length.Rel; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity DoubleScalar.Abs&lt;SpeedUnit&gt;;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @param gtuColorer GTUColorer; the GTUColorer that will be linked from the animation to determine the color (may be null
     *            in which case a default will be used)
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualCar(final String id, final GTUType gtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final Length.Rel length, final Length.Rel width, final Speed maximumVelocity,
        final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
        final LanePerception perception, final Class<? extends Renderable2D> animationClass, final GTUColorer gtuColorer)
        throws NamingException, NetworkException, SimRuntimeException, GTUException
    {
        super(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
            strategicalPlanner, perception);

        // sensor positions.
        // We take the rear position of the Car to be the reference point. So the front is the length
        // of the Car away from the reference point in the positive (driving) X-direction.
        Length.Rel zero = new Length.Rel(0.0d, LengthUnit.METER);
        Length.Rel dx = new Length.Rel(getLength().getSI(), LengthUnit.METER);
        this.relativePositions
            .put(RelativePosition.FRONT, new RelativePosition(dx, zero, zero, RelativePosition.FRONT));
        this.relativePositions
            .put(RelativePosition.REAR, new RelativePosition(zero, zero, zero, RelativePosition.REAR));
        this.relativePositions.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);

        setMaximumAcceleration(new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2));
        setMaximumDeceleration(new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));

        // animation
        if (simulator instanceof OTSAnimatorInterface && animationClass != null)
        {
            try
            {
                Constructor<?> constructor;

                if (null == gtuColorer)
                {
                    constructor = ClassUtil.resolveConstructor(animationClass, new Object[]{this, simulator});
                    this.animation = (Renderable2D) constructor.newInstance(this, simulator);
                }
                else
                {
                    constructor =
                        ClassUtil.resolveConstructor(animationClass, new Object[]{this, simulator, gtuColorer});
                    this.animation = (Renderable2D) constructor.newInstance(this, simulator, gtuColorer);
                }
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
                this.animation = null;
            }
            catch (Exception e)
            {
                System.err.println("Car: " + this.getId());
                e.printStackTrace();
            }
        }
        super.destroy();
    }

    /**
     * Build an individual car and use easy setter methods to instantiate the car. Typical use looks like:
     * 
     * <pre>
     * LaneBasedIndividualCar&lt;String&gt; car = new LaneBasedIndividualCarBuilder&lt;String&gt;().setId("Car:"+nr)
     *    .setLength(new Length.Rel(4.0, METER))....build(); 
     *    
     * or
     * 
     * LaneBasedIndividualCarBuilder&lt;String&gt; carBuilder = new LaneBasedIndividualCarBuilder&lt;String&gt;();
     * carBuilder.setId("Car:"+nr);
     * carBuilder.setLength(new Length.Rel(4.0, METER));
     * carBuilder.setWidth(new Length.Rel(1.8, METER));
     * ...
     * LaneBasedIndividualCar&lt;String&gt; car = carBuilder.build();
     * </pre>
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
     *          initial Feb 3, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public static class LaneBasedIndividualCarBuilder
    {
        /** The id of the GTU. */
        private String id = null;

        /** The type of GTU, e.g. TruckType, CarType, BusType. */
        private GTUType gtuType = null;

        /** The initial positions of the car on one or more lanes. */
        private Set<DirectedLanePosition> initialLongitudinalPositions = null;

        /** The initial speed of the car on the lane. */
        private Speed initialSpeed = null;

        /** The length of the GTU (parallel with driving direction). */
        private Length.Rel length = null;

        /** The width of the GTU (perpendicular to driving direction). */
        private Length.Rel width = null;

        /** The maximum speed of the GTU (in the driving direction). */
        private Speed maximumVelocity = null;

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator = null;

        /** Animation. */
        private Class<? extends Renderable2D> animationClass = null;

        /** GTUColorer. */
        private GTUColorer gtuColorer = null;

        /** Strategic planner. */
        private LaneBasedStrategicalPlanner strategicalPlanner = null;

        /** Perception. */
        private LanePerception perception = null;

        /**
         * @param id set id
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setId(final String id)
        {
            this.id = id;
            return this;
        }

        /**
         * @param gtuType set gtuType
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setGtuType(final GTUType gtuType)
        {
            this.gtuType = gtuType;
            return this;
        }

        /**
         * @param initialLongitudinalPositions set initialLongitudinalPositions
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setInitialLongitudinalPositions(
            final Set<DirectedLanePosition> initialLongitudinalPositions)
        {
            this.initialLongitudinalPositions = initialLongitudinalPositions;
            return this;
        }

        /**
         * @param initialSpeed set initialSpeed
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setInitialSpeed(final Speed initialSpeed)
        {
            this.initialSpeed = initialSpeed;
            return this;
        }

        /**
         * @param length set length
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setLength(final Length.Rel length)
        {
            this.length = length;
            return this;
        }

        /**
         * @param width set width
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setWidth(final Length.Rel width)
        {
            this.width = width;
            return this;
        }

        /**
         * @param maximumVelocity set maximumVelocity
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setMaximumVelocity(final Speed maximumVelocity)
        {
            this.maximumVelocity = maximumVelocity;
            return this;
        }

        /**
         * @param simulator set simulator
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setSimulator(final OTSDEVSSimulatorInterface simulator)
        {
            this.simulator = simulator;
            return this;
        }

        /**
         * @param strategicalPlanner set strategicalPlanner
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder
            setStrategicalPlanner(LaneBasedStrategicalPlanner strategicalPlanner)
        {
            this.strategicalPlanner = strategicalPlanner;
            return this;
        }

        /**
         * @param perception set perception
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setPerception(LanePerception perception)
        {
            this.perception = perception;
            return this;
        }

        /**
         * @param animationClass set animation class
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder
            setAnimationClass(final Class<? extends Renderable2D> animationClass)
        {
            this.animationClass = animationClass;
            return this;
        }

        /**
         * @return id.
         */
        public final String getId()
        {
            return this.id;
        }

        /**
         * @return gtuType.
         */
        public final GTUType getGtuType()
        {
            return this.gtuType;
        }

        /**
         * @return initialLongitudinalPositions.
         */
        public final Set<DirectedLanePosition> getInitialLongitudinalPositions()
        {
            return this.initialLongitudinalPositions;
        }

        /**
         * @return initialSpeed.
         */
        public final Speed getInitialSpeed()
        {
            return this.initialSpeed;
        }

        /**
         * @return length.
         */
        public final Length.Rel getLength()
        {
            return this.length;
        }

        /**
         * @return width.
         */
        public final Length.Rel getWidth()
        {
            return this.width;
        }

        /**
         * @return maximumVelocity.
         */
        public final Speed getMaximumVelocity()
        {
            return this.maximumVelocity;
        }

        /**
         * @return simulator.
         */
        public final OTSDEVSSimulatorInterface getSimulator()
        {
            return this.simulator;
        }

        /**
         * @return strategicalPlanner
         */
        public final LaneBasedStrategicalPlanner getStrategicalPlanner()
        {
            return this.strategicalPlanner;
        }

        /**
         * @return perception
         */
        public final LanePerception getPerception()
        {
            return this.perception;
        }

        /**
         * @return animationClass.
         */
        public final Class<? extends Renderable2D> getAnimationClass()
        {
            return this.animationClass;
        }

        /**
         * @return gtuColorer.
         */
        public final GTUColorer getGtuColorer()
        {
            return this.gtuColorer;
        }

        /**
         * @param gtuColorer set gtuColorer.
         */
        public final void setGtuColorer(final GTUColorer gtuColorer)
        {
            this.gtuColorer = gtuColorer;
        }

        /**
         * Build one LaneBasedIndividualCar.
         * @return the built Car with the set properties
         * @throws Exception when not all required values have been set
         */
        public final LaneBasedIndividualCar build() throws Exception
        {
            if (null == this.id || null == this.gtuType || null == this.strategicalPlanner || null == this.perception
                || null == this.initialLongitudinalPositions || null == this.initialSpeed || null == this.length
                || null == this.width || null == this.maximumVelocity || null == this.simulator)
            {
                // TODO Should throw a more specific Exception type
                throw new GTUException("factory settings incomplete");
            }
            LaneBasedIndividualCar gtu =
                new LaneBasedIndividualCar(this.id, this.gtuType, this.initialLongitudinalPositions, this.initialSpeed,
                    this.length, this.width, this.maximumVelocity, this.simulator, this.strategicalPlanner,
                    this.perception, this.animationClass, this.gtuColorer);
            // System.out.println("Generated GTU " + gtu + " at t=" + this.simulator.getSimulatorTime());
            return gtu;

        }

    }

}
