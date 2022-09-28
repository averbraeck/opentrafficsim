package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Mass;

/**
 * Interface for vehicle models.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface VehicleModel
{

    /** No bounds. */
    VehicleModel NONE = new VehicleModel()
    {
        @Override
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGTU gtu)
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
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGTU gtu)
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
     * @param gtu LaneBasedGTU; gtu
     * @return Acceleration; possible acceleration
     */
    Acceleration boundAcceleration(Acceleration acceleration, LaneBasedGTU gtu);

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
     * Defines (fixed) mass and moment of inertia about z-axis.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        public Acceleration boundAcceleration(final Acceleration acceleration, final LaneBasedGTU gtu)
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
