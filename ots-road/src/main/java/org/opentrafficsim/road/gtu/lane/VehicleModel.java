package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Mass;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Interface for vehicle models.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface VehicleModel
{

    /** No bounds. */
    VehicleModel NONE = new VehicleModel()
    {
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGtu gtu)
        {
            return acceleration;
        }

        @Override
        public String toString()
        {
            return "VehicleModel [None]";
        }
    };

    /** Acceleration bounded by GTU min and max acceleration. */
    VehicleModel MINMAX = new VehicleModel()
    {
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGtu gtu)
        {
            return acceleration.si > gtu.getMaximumDeceleration().si
                    ? (acceleration.si < gtu.getMaximumAcceleration().si ? acceleration : gtu.getMaximumAcceleration())
                    : gtu.getMaximumDeceleration();
        }

        @Override
        public String toString()
        {
            return "VehicleModel [MinMax]";
        }
    };

    /**
     * Returns a bounded acceleration.
     * @param acceleration intended acceleration
     * @param gtu gtu
     * @return possible acceleration
     */
    Acceleration boundAcceleration(Acceleration acceleration, LaneBasedGtu gtu);

    /**
     * Returns the turn radius, which is actually a diameter. By default this is twice the GTU length.
     * @param gtu GTU
     * @return turn radius, which is actually a diameter
     */
    default Length getTurnRadius(final LaneBasedGtu gtu)
    {
        return gtu.getLength().times(2.0);
    }

    /**
     * GTU mass.
     * @return GTU mass
     */
    default Mass getMass()
    {
        return null;
    }

    /**
     * Moment of inertia about z-axis and center point of gravity.
     * @return moment of inertia about z-axis
     */
    default double getMomentOfInertiaAboutZ()
    {
        return 0;
    }

    /**
     * Returns whether the braking lights are on. The default implementation returns {@code true} if the deceleration is larger
     * than a speed-dependent threshold given by:<br>
     * <br>
     * c0 * g(v) + c1 + c3*v^2<br>
     * <br>
     * where c0 = 0.2, c1 = 0.15 and c3 = 0.00025 (with c2 = 0 implicit) are empirically derived averages, and g(v) is 0 below
     * 25 km/h or 1 otherwise, representing that the engine is disengaged at low speeds.
     * @param speed speed
     * @param acceleration acceleration
     * @return whether the braking lights are on
     */
    default boolean isBrakingLightsOn(final Speed speed, final Acceleration acceleration)
    {
        return acceleration.si < (speed.si < 6.944 ? 0.0 : -0.2) - 0.15 - 0.00025 * speed.si * speed.si;
    }

    /**
     * Defines (fixed) mass and moment of inertia about z-axis. Acceleration is limited using {@code VehicleModel.MINMAX}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    class MassBased implements VehicleModel
    {
        /** Mass. */
        private final Mass mass;

        /** Moment of inertia about z-axis. */
        private final double momentOfInertiaAboutZ;

        /**
         * Constructor.
         * @param mass mass
         * @param momentOfInertiaAboutZ moment of inertia about z-axis
         */
        public MassBased(final Mass mass, final double momentOfInertiaAboutZ)
        {
            this.mass = mass;
            this.momentOfInertiaAboutZ = momentOfInertiaAboutZ;
        }

        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGtu gtu)
        {
            return MINMAX.boundAcceleration(acceleration, gtu);
        }

        @Override
        public Mass getMass()
        {
            return this.mass;
        }

        @Override
        public double getMomentOfInertiaAboutZ()
        {
            return this.momentOfInertiaAboutZ;
        }
    }

}
