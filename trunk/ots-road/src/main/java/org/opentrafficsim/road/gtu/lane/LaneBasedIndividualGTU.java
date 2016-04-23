package org.opentrafficsim.road.gtu.lane;

import java.io.Serializable;
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
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
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
public class LaneBasedIndividualGTU extends AbstractLaneBasedIndividualGTU
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** The animation. */
    private Renderable2D animation;

    /** Sensing positions. */
    private final Map<RelativePosition.TYPE, RelativePosition> relativePositions = new LinkedHashMap<>();

    /**
     * @param id ID; the id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions Map&lt;Lane, Length&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed Speed; the initial speed of the car on the lane
     * @param length Length; the maximum length of the GTU (parallel with driving direction)
     * @param width Length; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity Speed;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @param network the network that the GTU is initially registered in
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualGTU(final String id, final GTUType gtuType,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed, final Length length,
            final Length width, final Speed maximumVelocity, final OTSDEVSSimulatorInterface simulator,
            final LaneBasedStrategicalPlanner strategicalPlanner, final LanePerceptionFull perception, final OTSNetwork network)
            throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        this(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
                strategicalPlanner, perception, DefaultCarAnimation.class, null, network);
    }

    /**
     * Construct a new LaneBasedIndividualCar.
     * @param id ID; the id of the GTU
     * @param gtuType GTUTYpe; the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions Map&lt;Lane, Length&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed Speed; the initial speed of the car on the lane
     * @param length Length; the maximum length of the GTU (parallel with driving direction)
     * @param width Length; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumVelocity Speed;the maximum speed of the GTU (in the driving direction)
     * @param simulator OTSDEVSSimulatorInterface; the simulator
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @param gtuColorer GTUColorer; the GTUColorer that will be linked from the animation to determine the color (may be null
     *            in which case a default will be used)
     * @param network the network that the GTU is initially registered in
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when a parameter is invalid
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualGTU(final String id, final GTUType gtuType,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed, final Length length,
            final Length width, final Speed maximumVelocity, final OTSDEVSSimulatorInterface simulator,
            final LaneBasedStrategicalPlanner strategicalPlanner, final LanePerceptionFull perception,
            final Class<? extends Renderable2D> animationClass, final GTUColorer gtuColorer, final OTSNetwork network)
            throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        super(id, gtuType, initialLongitudinalPositions, initialSpeed, length, width, maximumVelocity, simulator,
                strategicalPlanner, perception, network);

        // sensor positions.
        // We take the rear position of the Car to be the reference point. So the front is the length
        // of the Car away from the reference point in the positive (driving) X-direction.
        Length dx2 = new Length(getLength().getSI() / 2.0, LengthUnit.METER);
        this.relativePositions.put(RelativePosition.FRONT, new RelativePosition(dx2, Length.ZERO, Length.ZERO,
                RelativePosition.FRONT));
        this.relativePositions.put(RelativePosition.REAR, new RelativePosition(dx2.multiplyBy(-1.0), Length.ZERO,
                Length.ZERO, RelativePosition.REAR));
        this.relativePositions.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);
        this.relativePositions.put(RelativePosition.CENTER, RelativePosition.REFERENCE_POSITION);

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
                    constructor = ClassUtil.resolveConstructor(animationClass, new Object[] { this, simulator });
                    this.animation = (Renderable2D) constructor.newInstance(this, simulator);
                }
                else
                {
                    constructor = ClassUtil.resolveConstructor(animationClass, new Object[] { this, simulator, gtuColorer });
                    this.animation = (Renderable2D) constructor.newInstance(this, simulator, gtuColorer);
                }
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                    | IllegalArgumentException | InvocationTargetException exception)
            {
                throw new NetworkException("Could not instantiate car animation of type " + animationClass.getName(), exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getFront()
    {
        return this.relativePositions.get(RelativePosition.FRONT);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getRear()
    {
        return this.relativePositions.get(RelativePosition.REAR);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getCenter()
    {
        return this.relativePositions.get(RelativePosition.CENTER);
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedIndividualGTU [relativePositions=" + this.relativePositions + "]";
    }

    /**
     * Build an individual car and use easy setter methods to instantiate the car. Typical use looks like:
     * 
     * <pre>
     * LaneBasedIndividualCar&lt;String&gt; car = new LaneBasedIndividualCarBuilder&lt;String&gt;().setId("Car:"+nr)
     *    .setLength(new Length(4.0, METER))....build(); 
     *    
     * or
     * 
     * LaneBasedIndividualCarBuilder&lt;String&gt; carBuilder = new LaneBasedIndividualCarBuilder&lt;String&gt;();
     * carBuilder.setId("Car:"+nr);
     * carBuilder.setLength(new Length(4.0, METER));
     * carBuilder.setWidth(new Length(1.8, METER));
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
    public static class LaneBasedIndividualCarBuilder implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20160000L;

        /** The id of the GTU. */
        private String id = null;

        /** The type of GTU, e.g. TruckType, CarType, BusType. */
        private GTUType gtuType = null;

        /** The initial positions of the car on one or more lanes. */
        private Set<DirectedLanePosition> initialLongitudinalPositions = null;

        /** The initial speed of the car on the lane. */
        private Speed initialSpeed = null;

        /** The length of the GTU (parallel with driving direction). */
        private Length length = null;

        /** The width of the GTU (perpendicular to driving direction). */
        private Length width = null;

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
        private LanePerceptionFull perception = null;

        /** Network. */
        private OTSNetwork network = null;

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
        public final LaneBasedIndividualCarBuilder setLength(final Length length)
        {
            this.length = length;
            return this;
        }

        /**
         * @param width set width
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setWidth(final Length width)
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
        public final LaneBasedIndividualCarBuilder setStrategicalPlanner(LaneBasedStrategicalPlanner strategicalPlanner)
        {
            this.strategicalPlanner = strategicalPlanner;
            return this;
        }

        /**
         * @param perception set perception
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setPerception(LanePerceptionFull perception)
        {
            this.perception = perception;
            return this;
        }

        /**
         * @param animationClass set animation class
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setAnimationClass(final Class<? extends Renderable2D> animationClass)
        {
            this.animationClass = animationClass;
            return this;
        }

        /**
         * @param gtuColorer set gtuColorer.
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setGtuColorer(final GTUColorer gtuColorer)
        {
            this.gtuColorer = gtuColorer;
            return this;
        }

        /**
         * @param network set network
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setNetwork(final OTSNetwork network)
        {
            this.network = network;
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
        public final Length getLength()
        {
            return this.length;
        }

        /**
         * @return width.
         */
        public final Length getWidth()
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
        public final LanePerceptionFull getPerception()
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
         * @return network
         */
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * Build one LaneBasedIndividualCar.
         * @return the built Car with the set properties
         * @throws Exception when not all required values have been set
         */
        public final LaneBasedIndividualGTU build() throws Exception
        {
            if (null == this.id || null == this.gtuType || null == this.strategicalPlanner || null == this.perception
                    || null == this.initialLongitudinalPositions || null == this.initialSpeed || null == this.length
                    || null == this.width || null == this.maximumVelocity || null == this.simulator || null == this.network)
            {
                // TODO Should throw a more specific Exception type
                throw new GTUException("factory settings incomplete");
            }

            LaneBasedIndividualGTU gtu =
                    new LaneBasedIndividualGTU(this.id, this.gtuType, this.initialLongitudinalPositions, this.initialSpeed,
                            this.length, this.width, this.maximumVelocity, this.simulator, this.strategicalPlanner,
                            this.perception, this.animationClass, this.gtuColorer, this.network);
            return gtu;

        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LaneBasedIndividualCarBuilder [id=" + this.id + ", gtuType=" + this.gtuType
                    + ", initialLongitudinalPositions=" + this.initialLongitudinalPositions + ", initialSpeed="
                    + this.initialSpeed + ", length=" + this.length + ", width=" + this.width + ", maximumVelocity="
                    + this.maximumVelocity + ", strategicalPlanner=" + this.strategicalPlanner + "]";
        }

    }

}
