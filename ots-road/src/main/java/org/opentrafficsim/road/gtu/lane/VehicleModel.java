package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Mass;

/**
 * Interface for vehicle models.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "VehicleModel [MinMax]";
        }
    };

    /**
     * Returns a bounded acceleration.
     * @param acceleration Acceleration; intended acceleration
     * @param gtu LaneBasedGtu; gtu
     * @return Acceleration; possible acceleration
     */
    Acceleration boundAcceleration(Acceleration acceleration, LaneBasedGtu gtu);

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
     * Defines (fixed) mass and moment of inertia about z-axis. Acceleration is limited using {@code VehicleModel.MINMAX}.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    class MassBased implements VehicleModel
    {
        /** Mass. */
        private final Mass mass;

        /** Moment of inertia about z-axis. */
        private final double momentOfInertiaAboutZ;

        /**
         * @param mass Mass; mass
         * @param momentOfInertiaAboutZ double; moment of inertia about z-axis
         */
        public MassBased(final Mass mass, final double momentOfInertiaAboutZ)
        {
            this.mass = mass;
            this.momentOfInertiaAboutZ = momentOfInertiaAboutZ;
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGtu gtu)
        {
            return MINMAX.boundAcceleration(acceleration, gtu);
        }

        /** {@inheritDoc} */
        @Override
        public Mass getMass()
        {
            return this.mass;
        }

        /** {@inheritDoc} */
        @Override
        public double getMomentOfInertiaAboutZ()
        {
            return this.momentOfInertiaAboutZ;
        }
    }

}
