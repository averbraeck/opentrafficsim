package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Whether a GTU needs to be wrapped, or information should be copied for later and unaltered use.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PerceivedGtuType
{

    /** Default wrap implementation. */
    PerceivedGtuType WRAP = new PerceivedGtuType()
    {
    };

    /**
     * Creates a perceived object from a GTU, downstream or upstream. The default implementation figures out from distance
     * whether a parallel GTU should be created.
     * @param perceivingGtu perceiving GTU
     * @param reference reference object to which distance is given (and to which perception errors should apply, e.g. Conflict)
     * @param perceivedGtu perceived GTU
     * @param distance distance
     * @param downstream downstream (or upstream) neighbor
     * @return perception object from a gtu
     * @throws GtuException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    default PerceivedGtu createPerceivedGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream)
            throws GtuException, ParameterException
    {
        Speed v = perceivedGtu.getSpeed();
        Acceleration a = perceivedGtu.getAcceleration();
        Kinematics kinematics =
                downstream ? Kinematics.dynamicAhead(distance, v, a, true, perceivedGtu.getLength(), reference.getLength())
                        : Kinematics.dynamicBehind(distance, v, a, true, perceivedGtu.getLength(), reference.getLength());
        return PerceivedGtu.of(perceivedGtu, kinematics);
    }

    /**
     * Class for neighbors perceived with estimation and anticipation. Adjacent neighbors are perceived exactly.
     */
    class AnticipationPerceivedGtuType implements PerceivedGtuType
    {
        /** Estimation. */
        private final Estimation estimation;

        /** Anticipation. */
        private final Anticipation anticipation;

        /** Perception delay provider. */
        private final Supplier<Duration> perceptionDelay;

        /** Last update time. */
        private Duration updateTime = null;

        /** Reaction time at update time. */
        private Duration tr;

        /** Historical moment considered at update time. */
        private Duration when;

        /** Traveled distance during reaction time at update time. */
        private Length traveledDistance;

        /**
         * Constructor.
         * @param estimation estimation
         * @param anticipation anticipation
         * @param perceptionDelay perception delay provider
         */
        public AnticipationPerceivedGtuType(final Estimation estimation, final Anticipation anticipation,
                final Supplier<Duration> perceptionDelay)
        {
            this.estimation = estimation;
            this.anticipation = anticipation;
            this.perceptionDelay = perceptionDelay;
        }

        @Override
        public PerceivedGtu createPerceivedGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
                final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream)
                throws GtuException, ParameterException
        {
            Duration now = perceivedGtu.getSimulator().getSimulatorTime();
            if (this.updateTime == null || now.si > this.updateTime.si)
            {
                this.updateTime = now;
                this.tr = this.when == null ? this.perceptionDelay.get()
                        // never go backwards in time even if Tr increases more than time has passed since previous step
                        : Duration.min(this.perceptionDelay.get(), now.minus(this.when));
                this.when = now.minus(this.tr);
                this.traveledDistance = perceivingGtu.equals(reference)
                        ? perceivingGtu.getOdometer().minus(perceivingGtu.getOdometer(this.when)) : Length.ZERO;
            }
            NeighborTriplet triplet;
            if (distance.ge0())
            {
                triplet = this.estimation.estimate(perceivingGtu, reference, perceivedGtu, distance, downstream, this.when);
                triplet = this.anticipation.anticipate(triplet, this.tr, this.traveledDistance, downstream);
                Length maxNegativeHeadway = perceivedGtu.getLength().plus(reference.getLength()).neg();
                if (triplet.headway().lt(maxNegativeHeadway))
                {
                    triplet = new NeighborTriplet(maxNegativeHeadway, triplet.speed(), triplet.acceleration());
                }
            }
            else
            {
                // parallel is estimated exactly
                triplet = new NeighborTriplet(distance, perceivedGtu.getSpeed(), perceivedGtu.getAcceleration());
            }
            Kinematics kinematics = downstream
                    ? Kinematics.dynamicAhead(triplet.headway(), triplet.speed(), triplet.acceleration(), true,
                            perceivedGtu.getLength(), perceivingGtu.getLength())
                    : Kinematics.dynamicBehind(triplet.headway(), triplet.speed(), triplet.acceleration(), true,
                            perceivedGtu.getLength(), perceivingGtu.getLength());
            return PerceivedGtu.of(perceivedGtu, kinematics);
        }
    }

}
