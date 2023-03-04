package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Form of anticipation.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Anticipation
{

    /** Assume no anticipation. */
    Anticipation NONE = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance, final boolean downstream)
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
    Anticipation CONSTANT_SPEED = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance, final boolean downstream)
        {
            // upstream neighbor approaches when faster
            Length distance = downstream
                    ? neighborTriplet.getHeadway().plus(neighborTriplet.getSpeed().times(duration)).minus(traveledDistance)
                    : neighborTriplet.getHeadway().minus(neighborTriplet.getSpeed().times(duration)).plus(traveledDistance);
            return new NeighborTriplet(distance, neighborTriplet.getSpeed(), neighborTriplet.getAcceleration());
        }

        @Override
        public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
        {
            return speed.times(duration);
        }
    };

    /** Assume constant acceleration. */
    Anticipation CONSTANT_ACCELERATION = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistance, final boolean downstream)
        {
            if (neighborTriplet.getSpeed().si < -neighborTriplet.getAcceleration().si * duration.si)
            {
                // to stand still
                double t = neighborTriplet.getSpeed().si / -neighborTriplet.getAcceleration().si;
                double dx = neighborTriplet.getSpeed().si * t + .5 * neighborTriplet.getAcceleration().si * t * t;
                dx = downstream ? dx : -dx; // upstream neighbor approaches when faster
                return new NeighborTriplet(Length.instantiateSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                        Speed.ZERO, Acceleration.ZERO);
            }
            double dx = neighborTriplet.getSpeed().si * duration.si
                    + .5 * neighborTriplet.getAcceleration().si * duration.si * duration.si;
            double dv = neighborTriplet.getAcceleration().si * duration.si;
            return new NeighborTriplet(Length.instantiateSI(neighborTriplet.getHeadway().si + dx - traveledDistance.si),
                    Speed.instantiateSI(neighborTriplet.getSpeed().si + dv), neighborTriplet.getAcceleration());
        }

        @Override
        public Length egoAnticipation(final Speed speed, final Acceleration acceleration, final Duration duration)
        {
            return speed.times(duration);
        }
    };

    /**
     * Anticipate movement.
     * @param neighborTriplet NeighborTriplet; headway, speed and acceleration
     * @param duration Duration; duration
     * @param traveledDistance Length; distance the subject vehicle traveled during the anticipation time
     * @param downstream boolean; whether the perceived GTU is downstream
     * @return anticipated info
     */
    NeighborTriplet anticipate(NeighborTriplet neighborTriplet, Duration duration, Length traveledDistance, boolean downstream);

    /**
     * Anticipate own movement.
     * @param speed Speed; current speed
     * @param acceleration Acceleration; current acceleration
     * @param duration Duration; anticipation time
     * @return anticipated distance traveled
     */
    Length egoAnticipation(Speed speed, Acceleration acceleration, Duration duration);
}
