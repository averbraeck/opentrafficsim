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

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedTemplateGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedTemplateCar extends AbstractLaneBasedTemplateGTU
{
    /** */
    private static final long serialVersionUID = 20141025L;

    /** animation. */
    private Renderable2D animation;

    /** Sensing positions. */
    private final Map<RelativePosition.TYPE, RelativePosition> relativePositions = new LinkedHashMap<>();

    /**
     * @param id ID; the id of the GTU
     * @param templateGtuType the template of the GTU
     * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param network the network that the GTU is initially registered in
     * @throws NamingException if an error occurs when adding the animation handler.
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GTUException when gtuFollowingModel is null
     * @throws InstantiationException in case Perception or StrategicPlanner instantiation fails
     * @throws IllegalAccessException in case Perception or StrategicPlanner constructor is not public
     */
    public LaneBasedTemplateCar(final String id, final LaneBasedTemplateGTUType templateGtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed, final OTSNetwork network)
        throws NamingException, NetworkException, SimRuntimeException, GTUException, InstantiationException,
        IllegalAccessException
    {
        this(id, templateGtuType, initialLongitudinalPositions, initialSpeed, DefaultCarAnimation.class, network);
    }

    /**
     * @param id ID; the id of the GTU
     * @param templateGtuType the template of the GTU
     * @param initialLongitudinalPositions Map&lt;Lane, Length.Rel&gt;; the initial positions of the car on one or more lanes
     * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the car on the lane
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation.
     * @param network the network that the GTU is initially registered in
     * @throws NamingException if an error occurs when adding the animation handler.
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GTUException when gtuFollowingModel is null
     * @throws InstantiationException in case Perception or StrategicPlanner instantiation fails
     * @throws IllegalAccessException in case Perception or StrategicPlanner constructor is not public
     */
    public LaneBasedTemplateCar(final String id, final LaneBasedTemplateGTUType templateGtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final Class<? extends Renderable2D> animationClass, final OTSNetwork network) throws NamingException,
        NetworkException, SimRuntimeException, GTUException, InstantiationException, IllegalAccessException
    {
        super(id, templateGtuType, initialLongitudinalPositions, initialSpeed, network);

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

        // animation
        if (getSimulator() instanceof OTSAnimatorInterface && animationClass != null)
        {
            try
            {
                Constructor<?> constructor =
                    ClassUtil.resolveConstructor(animationClass, new Object[]{this, getSimulator()});
                this.animation = (Renderable2D) constructor.newInstance(this, getSimulator());
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
            Map<Lane, Length.Rel> frontPositions = positions(getFront());
            Lane frontLane = frontPositions.keySet().iterator().next();
            return String.format("Car %s front:%s[%s]", getId(), frontLane, frontPositions.get(frontLane));
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        return "Caught exception in toString";
    }

    /**
     * Build a template car and use easy setter methods to instantiate the car. Typical use looks like:
     * 
     * <pre>
     * LaneBasedTemplateCar&lt;String&gt; car = new LaneBasedTemplateCarBuilder&lt;String&gt;().setId("Car:"+nr)
     *    .setInitialSpeed(new DoubleScalar.Rel&lt;SpeedUnit&gt;(80.0, KM_PER_HOUR))....build(); 
     *    
     * or
     * 
     * LaneBasedTemplateCarBuilder&lt;String&gt; carBuilder = new LaneBasedTemplateCarBuilder&lt;String&gt;();
     * carBuilder.setId("Car:"+nr);
     * carBuilder.setTemplateGtuType(TruckTemplate);
     * carBuilder.setInitialSpeed(new DoubleScalar.Rel&lt;SpeedUnit&gt;(80.0, KM_PER_HOUR));
     * ...
     * LaneBasedTemplateCar&lt;String&gt; car = carBuilder.build();
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
    public static class LaneBasedTemplateCarBuilder
    {
        /** the id of the GTU. */
        private String id = null;

        /** the type of GTU, e.g. TruckType, CarType, BusType. */
        private LaneBasedTemplateGTUType templateGtuType = null;

        /** the initial positions of the car on one or more lanes. */
        private Set<DirectedLanePosition> initialLongitudinalPositions = null;

        /** the initial speed of the car on the lane. */
        private Speed initialSpeed = null;

        /** animation. */
        private Class<? extends Renderable2D> animationClass = null;

        /** Network. */
        private OTSNetwork network = null;

        /**
         * @param id set id
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setId(final String id)
        {
            this.id = id;
            return this;
        }

        /**
         * @param templateGtuType set the template for the gtuType
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setTemplateGtuType(final LaneBasedTemplateGTUType templateGtuType)
        {
            this.templateGtuType = templateGtuType;
            return this;
        }

        /**
         * @param initialLongitudinalPositions set initialLongitudinalPositions
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setInitialLongitudinalPositions(
            final Set<DirectedLanePosition> initialLongitudinalPositions)
        {
            this.initialLongitudinalPositions = initialLongitudinalPositions;
            return this;
        }

        /**
         * @param initialSpeed set initialSpeed
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setInitialSpeed(final Speed initialSpeed)
        {
            this.initialSpeed = initialSpeed;
            return this;
        }

        /**
         * @param animationClass set animation class
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setAnimationClass(final Class<? extends Renderable2D> animationClass)
        {
            this.animationClass = animationClass;
            return this;
        }

        /**
         * @param network set network
         * @return the class itself for chaining the setters
         */
        public final LaneBasedTemplateCarBuilder setNetwork(final OTSNetwork network)
        {
            this.network = network;
            return this;
        }

        /**
         * @return the built Car with the set properties
         * @throws NamingException if an error occurs when adding the animation handler
         * @throws NetworkException when the GTU cannot be placed on the given lane
         * @throws SimRuntimeException when the move method cannot be scheduled
         * @throws GTUException when gtuFollowingModel is null
         * @throws InstantiationException in case Perception or StrategicPlanner instantiation fails
         * @throws IllegalAccessException in case Perception or StrategicPlanner constructor is not public
         */
        public final LaneBasedTemplateCar build() throws NamingException, NetworkException, SimRuntimeException,
            GTUException, InstantiationException, IllegalAccessException
        {
            if (null == this.id || null == this.initialLongitudinalPositions || null == this.initialSpeed
                || null == this.templateGtuType || null == this.animationClass || null == this.network)
            {
                // TODO Should throw a more specific Exception type
                throw new GTUException("factory settings incomplete");
            }

            return new LaneBasedTemplateCar(this.id, this.templateGtuType, this.initialLongitudinalPositions,
                this.initialSpeed, this.animationClass, this.network);
        }
    }

}
