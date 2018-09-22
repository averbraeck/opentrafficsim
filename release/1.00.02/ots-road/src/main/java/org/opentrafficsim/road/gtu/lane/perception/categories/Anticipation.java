package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Form of anticipation.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Anticipation
{

    /** Assume no anticipation. */
    public final static Anticipation NONE = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance)
        {
            return neighborTriplet;
        }

        @Override
        public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
        {
            return Length.ZERO;
        }
    };

    /** Assume constant speed. */
    public final static Anticipation CONSTANT_SPEED = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance)
        {
            return new NeighborTriplet(
                    neighborTriplet.getHeadway().plus(neighborTriplet.getSpeed().multiplyBy(duration)).minus(traveledDistance),
                    neighborTriplet.getSpeed(), neighborTriplet.getAcceleration());
        }

        @Override
        public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
        {
            return speed.multiplyBy(duration);
        }
    };

    /** Assume constant acceleration. */
    public final static Anticipation CONSTANT_ACCELERATION = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance)
        {
            if (neighborTriplet.getSpeed().si < -neighborTriplet.getAcceleration().si * duration.si)
            {
                // to stand still
                double t = neighborTriplet.getSpeed().si / -neighborTriplet.getAcceleration().si;
                double dx = neighborTriplet.getSpeed().si * t + .5 * neighborTriplet.getAcceleration().si * t * t;
                return new NeighborTriplet(Length.createSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                        Speed.ZERO, Acceleration.ZERO);
            }
            double dx = neighborTriplet.getSpeed().si * duration.si
                    + .5 * neighborTriplet.getAcceleration().si * duration.si * duration.si;
            double dv = neighborTriplet.getAcceleration().si * duration.si;
            return new NeighborTriplet(Length.createSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                    Speed.createSI(neighborTriplet.getSpeed().si + dv), neighborTriplet.getAcceleration());
        }

        @Override
        public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
        {
            return speed.multiplyBy(duration);
        }
    };

    /**
     * Anticipate movement.
     * @param neighborTriplet headway, speed and acceleration
     * @param duration duration
     * @param traveledDistance distance the subject vehicle traveled during the anticipation time
     * @return anticipated info
     */
    NeighborTriplet anticipate(NeighborTriplet neighborTriplet, Duration duration, Length traveledDistance);

    /**
     * Anticipate own movement.
     * @param speed current speed
     * @param acceleration current acceleration
     * @param duration anticipation time
     * @return anticipated distance traveled
     */
    Length egoAnticipation(Speed speed, Acceleration acceleration, Duration duration);

    /**
     * Results from anticipation.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 feb. 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class NeighborTriplet
    {

        /** Headway. */
        private final Length headway;

        /** Speed. */
        private final Speed speed;

        /** Acceleration. */
        private final Acceleration acceleration;

        /**
         * @param headway headway
         * @param speed speed
         * @param acceleration acceleration
         */
        NeighborTriplet(final Length headway, final Speed speed, final Acceleration acceleration)
        {
            this.headway = headway;
            this.speed = speed;
            this.acceleration = acceleration;
        }

        /**
         * @return headway.
         */
        public Length getHeadway()
        {
            return this.headway;
        }

        /**
         * @return speed.
         */
        public Speed getSpeed()
        {
            return this.speed;
        }

        /**
         * @return acceleration.
         */
        public Acceleration getAcceleration()
        {
            return this.acceleration;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "NeighborTriplet [headway=" + this.headway + ", speed=" + this.speed + ", acceleration=" + this.acceleration
                    + "]";
        }
    }
}
