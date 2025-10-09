package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Form of anticipation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
        public String toString()
        {
            return "NONE";
        }
    };

    /** Assume constant speed. */
    Anticipation CONSTANT_SPEED = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistanceReference, final boolean downstream)
        {
            // upstream neighbor approaches when faster
            Length distance = downstream
                    ? neighborTriplet.headway().plus(neighborTriplet.speed().times(duration)).minus(traveledDistanceReference)
                    : neighborTriplet.headway().minus(neighborTriplet.speed().times(duration)).plus(traveledDistanceReference);
            return new NeighborTriplet(distance, neighborTriplet.speed(), neighborTriplet.acceleration());
        }

        @Override
        public String toString()
        {
            return "CONSTANT_SPEED";
        }
    };

    /** Assume constant acceleration. */
    Anticipation CONSTANT_ACCELERATION = new Anticipation()
    {
        @Override
        public NeighborTriplet anticipate(final NeighborTriplet neighborTriplet, final Duration duration,
                final Length traveledDistanceReference, final boolean downstream)
        {
            if (neighborTriplet.speed().si < -neighborTriplet.acceleration().si * duration.si)
            {
                // to stand still
                double t = neighborTriplet.speed().si / -neighborTriplet.acceleration().si;
                double dx = neighborTriplet.speed().si * t + .5 * neighborTriplet.acceleration().si * t * t;
                // upstream neighbor approaches when faster
                return new NeighborTriplet(
                        Length.ofSI(
                                neighborTriplet.headway().si + (downstream ? 1.0 : -1.0) * (dx - traveledDistanceReference.si)),
                        Speed.ZERO, Acceleration.ZERO);
            }
            double dx = neighborTriplet.speed().si * duration.si
                    + .5 * neighborTriplet.acceleration().si * duration.si * duration.si;
            double dv = neighborTriplet.acceleration().si * duration.si;
            // upstream neighbor approaches when faster
            return new NeighborTriplet(
                    Length.ofSI(neighborTriplet.headway().si + (downstream ? 1.0 : -1.0) * (dx - traveledDistanceReference.si)),
                    Speed.ofSI(neighborTriplet.speed().si + dv), neighborTriplet.acceleration());
        }

        @Override
        public String toString()
        {
            return "CONSTANT_ACCELERATION";
        }
    };

    /**
     * Anticipate movement.
     * @param neighborTriplet headway, speed and acceleration
     * @param duration duration
     * @param traveledDistanceReference distance the reference object traveled during the anticipation time
     * @param downstream whether the perceived GTU is downstream
     * @return anticipated info
     */
    NeighborTriplet anticipate(NeighborTriplet neighborTriplet, Duration duration, Length traveledDistanceReference,
            boolean downstream);

}
