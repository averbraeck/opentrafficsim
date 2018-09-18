package org.opentrafficsim.road.gtu.lane;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.immutablecollections.Immutable;
import nl.tudelft.simulation.immutablecollections.ImmutableHashSet;
import nl.tudelft.simulation.immutablecollections.ImmutableLinkedHashMap;
import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.immutablecollections.ImmutableSet;
import nl.tudelft.simulation.language.reflection.ClassUtil;

/**
 * Augments the AbstractLaneBasedIndividualGTU with a LaneBasedIndividualCarBuilder and animation support
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private Renderable2D<? super LaneBasedIndividualGTU> animation;

    /** Sensing positions. */
    private final Map<RelativePosition.TYPE, RelativePosition> relativePositions = new HashMap<>();

    /** cached front. */
    private final RelativePosition frontPos;

    /** cached rear. */
    private final RelativePosition rearPos;

    /** contour points. */
    private final Set<RelativePosition> contourPoints = new HashSet<>();

    /**
     * Construct a new LaneBasedIndividualCar.
     * @param id ID; the id of the GTU
     * @param gtuType GTUTYpe; the type of GTU, e.g. TruckType, CarType, BusType
     * @param length Length; the maximum length of the GTU (parallel with driving direction)
     * @param width Length; the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumSpeed Speed;the maximum speed of the GTU (in the driving direction)
     * @param front Length; front distance relative to the reference position
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param network the network that the GTU is initially registered in
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws GTUException when a parameter is invalid
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedIndividualGTU(final String id, final GTUType gtuType, final Length length, final Length width,
            final Speed maximumSpeed, final Length front, final DEVSSimulatorInterface.TimeDoubleUnit simulator, final OTSNetwork network)
            throws NamingException, GTUException
    {
        super(id, gtuType, length, width, maximumSpeed, simulator, network);

        // sensor positions.
        Length dy2 = getWidth().multiplyBy(0.5);
        this.frontPos = new RelativePosition(front, Length.ZERO, Length.ZERO, RelativePosition.FRONT);
        this.relativePositions.put(RelativePosition.FRONT, this.frontPos);
        this.rearPos = new RelativePosition(front.minus(getLength()), Length.ZERO, Length.ZERO, RelativePosition.REAR);
        this.relativePositions.put(RelativePosition.REAR, this.rearPos);
        this.relativePositions.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);
        this.relativePositions.put(RelativePosition.CENTER,
                new RelativePosition(Length.ZERO, Length.ZERO, Length.ZERO, RelativePosition.CENTER));

        // Contour positions. For now, a rectangle with the four corners.
        for (int i = -1; i <= 1; i += 2)
        {
            Length x = i < 0 ? front.minus(getLength()) : front;
            for (int j = -1; j <= 1; j += 2)
            {
                this.contourPoints.add(new RelativePosition(x, dy2.multiplyBy(j), Length.ZERO, RelativePosition.CONTOUR));
            }
        }
    }

    /**
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes with their directions
     * @param initialSpeed the initial speed of the car on the lane
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @param gtuColorer GTUColorer; the GTUColorer that will be linked from the animation to determine the color (may be null
     *            in which case a default will be used)
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when initial values are not correct
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("unchecked")
    public final void initWithAnimation(final LaneBasedStrategicalPlanner strategicalPlanner,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
            final Class<? extends Renderable2D<? super LaneBasedIndividualGTU>> animationClass, final GTUColorer gtuColorer)
            throws NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        super.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);

        // animation
        if (getSimulator() instanceof AnimatorInterface && animationClass != null)
        {
            try
            {
                Constructor<?> constructor;

                if (null == gtuColorer)
                {
                    constructor = ClassUtil.resolveConstructor(animationClass, new Object[] { this, getSimulator() });
                    this.animation = (Renderable2D<LaneBasedIndividualGTU>) constructor.newInstance(this, getSimulator());
                }
                else
                {
                    constructor =
                            ClassUtil.resolveConstructor(animationClass, new Object[] { this, getSimulator(), gtuColorer });
                    this.animation =
                            (Renderable2D<LaneBasedIndividualGTU>) constructor.newInstance(this, getSimulator(), gtuColorer);
                }
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
                    | IllegalArgumentException | InvocationTargetException exception)
            {
                throw new GTUException("Could not instantiate car animation of type " + animationClass.getName(), exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getFront()
    {
        return this.frontPos;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getRear()
    {
        return this.rearPos;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getCenter()
    {
        return this.relativePositions.get(RelativePosition.CENTER);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<TYPE, RelativePosition> getRelativePositions()
    {
        return new ImmutableLinkedHashMap<>(this.relativePositions, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<RelativePosition> getContourPoints()
    {
        return new ImmutableHashSet<>(this.contourPoints, Immutable.WRAP);
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
            catch (@SuppressWarnings("unused") Exception e)
            {
                System.err.println("Error when destroying the animation of car: " + this.getId());
            }
        }
        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedIndividualGTU [id=" + getId() + "]";
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
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
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
        private Speed maximumSpeed = null;

        /** Maximum acceleration. */
        private Acceleration maximumAcceleration = null;

        /** Maximum deceleration (a negative value). */
        private Acceleration maximumDeceleration = null;

        /** The distance of the front relative to the reference position. */
        private Length front = null;

        /** The simulator. */
        private DEVSSimulatorInterface.TimeDoubleUnit simulator = null;

        /** Animation. */
        private Class<? extends Renderable2D<? super LaneBasedIndividualGTU>> animationClass = null;

        /** GTUColorer. */
        private GTUColorer gtuColorer = null;

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
         * @param maximumSpeed set maximumSpeed
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setMaximumSpeed(final Speed maximumSpeed)
        {
            this.maximumSpeed = maximumSpeed;
            return this;
        }

        /**
         * @param maximumAcceleration Acceleration; maximum acceleration
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setMaximumAcceleration(final Acceleration maximumAcceleration)
        {
            this.maximumAcceleration = maximumAcceleration;
            return this;
        }

        /**
         * @param maximumDeceleration Acceleration; maximum deceleration (a negative value)
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setMaximumDeceleration(final Acceleration maximumDeceleration)
        {
            this.maximumDeceleration = maximumDeceleration;
            return this;
        }

        /**
         * @param simulator set simulator
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setSimulator(final DEVSSimulatorInterface.TimeDoubleUnit simulator)
        {
            this.simulator = simulator;
            return this;
        }

        /**
         * @param front distance of the front relative to the reference point
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setFront(final Length front)
        {
            this.front = front;
            return this;
        }

        /**
         * @param animationClass set animation class
         * @return the class itself for chaining the setters
         */
        public final LaneBasedIndividualCarBuilder setAnimationClass(
                final Class<? extends Renderable2D<? super LaneBasedIndividualGTU>> animationClass)
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
         * @return maximumSpeed.
         */
        public final Speed getMaximumSpeed()
        {
            return this.maximumSpeed;
        }

        /**
         * @return simulator.
         */
        public final DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
        {
            return this.simulator;
        }

        /**
         * @return animationClass.
         */
        public final Class<? extends Renderable2D<? super LaneBasedIndividualGTU>> getAnimationClass()
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
         * @param laneBasedStrategicalPlannerFactory factory for the strategical planner
         * @param route route
         * @param origin origin
         * @param destination destination
         * @return the built Car with the set properties
         * @throws Exception when not all required values have been set
         */
        public final LaneBasedIndividualGTU build(
                final LaneBasedStrategicalPlannerFactory<? extends LaneBasedStrategicalPlanner> laneBasedStrategicalPlannerFactory,
                final Route route, final Node origin, final Node destination) throws Exception
        {
            if (null == this.id || null == this.gtuType || null == this.initialLongitudinalPositions
                    || null == this.initialSpeed || null == this.length || null == this.width || null == this.maximumSpeed
                    || null == this.maximumAcceleration || null == this.maximumDeceleration || null == this.front
                    || null == this.simulator || null == this.network)
            {
                // TODO Should throw a more specific Exception type
                throw new GTUException("factory settings incomplete");
            }
            LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU(this.id, this.gtuType, this.length, this.width,
                    this.maximumSpeed, this.front, this.simulator, this.network);
            gtu.setMaximumAcceleration(this.maximumAcceleration);
            gtu.setMaximumDeceleration(this.maximumDeceleration);
            gtu.initWithAnimation(laneBasedStrategicalPlannerFactory.create(gtu, route, origin, destination),
                    this.initialLongitudinalPositions, this.initialSpeed, this.animationClass, this.gtuColorer);
            return gtu;

        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LaneBasedIndividualCarBuilder [id=" + this.id + ", gtuType=" + this.gtuType
                    + ", initialLongitudinalPositions=" + this.initialLongitudinalPositions + ", initialSpeed="
                    + this.initialSpeed + ", length=" + this.length + ", width=" + this.width + ", maximumSpeed="
                    + this.maximumSpeed + ", strategicalPlanner=" + "]";
        }

    }

}
