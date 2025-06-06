package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuPerceived;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuRealCopy;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
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
public interface HeadwayGtuType
{

    /** The GTU is wrapped, and info is taken directly from it. */
    HeadwayGtuType WRAP = new HeadwayGtuType()
    {
        @Override
        public HeadwayGtuReal createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException
        {
            return new HeadwayGtuReal(perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGtuReal createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException
        {
            return new HeadwayGtuReal(perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGtuReal createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            return new HeadwayGtuReal(perceivedGtu, overlapFront, overlap, overlapRear, true);
        }
    };

    /** Info regarding the GTU is copied. */
    HeadwayGtuType COPY = new HeadwayGtuType()
    {
        @Override
        public HeadwayGtuRealCopy createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException
        {
            return new HeadwayGtuRealCopy(perceivedGtu, distance);
        }

        @Override
        public HeadwayGtuRealCopy createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException
        {
            return new HeadwayGtuRealCopy(perceivedGtu, distance);
        }

        @Override
        public HeadwayGtuRealCopy createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            return new HeadwayGtuRealCopy(perceivedGtu, overlapFront, overlap, overlapRear);
        }
    };

    /**
     * Creates a headway object from a GTU, downstream or upstream. The default implementation figures out from possible
     * negative distance whether a parallel GTU should be created.
     * @param perceivingGtu perceiving GTU
     * @param reference reference object to which distance is given (and to which perception errors should apply, e.g. Conflict)
     * @param perceivedGtu perceived GTU
     * @param distance distance
     * @param downstream downstream (or upstream) neighbor
     * @return headway object from a gtu
     * @throws GtuException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    default HeadwayGtu createHeadwayGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
            final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream)
            throws GtuException, ParameterException
    {
        if (distance.ge0())
        {
            if (downstream)
            {
                return createDownstreamGtu(perceivingGtu, perceivedGtu, distance);
            }
            return createUpstreamGtu(perceivingGtu, perceivedGtu, distance);
        }
        Length length = reference.getLength();
        Throw.when(-distance.si > length.si + perceivedGtu.getLength().si, IllegalStateException.class,
                "A GTU (%s) that is supposedly %s is actually %s.", perceivedGtu.getId(),
                downstream ? "downstream" : "upstream", downstream ? "upstream" : "downstream");
        Length overlapRear = distance.plus(length);
        Length overlap = distance.neg();
        Length overlapFront = distance.plus(perceivedGtu.getLength());
        if (overlapRear.lt0())
        {
            overlap = overlap.plus(overlapRear);
        }
        if (overlapFront.lt0())
        {
            overlap = overlap.plus(overlapFront);
        }
        return createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
    }

    /**
     * Creates a headway object from a GTU, downstream.
     * @param perceivingGtu perceiving GTU
     * @param perceivedGtu perceived GTU
     * @param distance distance
     * @return headway object from a gtu
     * @throws GtuException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    HeadwayGtu createDownstreamGtu(LaneBasedGtu perceivingGtu, LaneBasedGtu perceivedGtu, Length distance)
            throws GtuException, ParameterException;

    /**
     * Creates a headway object from a GTU, downstream.
     * @param perceivingGtu perceiving GTU
     * @param perceivedGtu perceived GTU
     * @param distance distance
     * @return headway object from a gtu
     * @throws GtuException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    HeadwayGtu createUpstreamGtu(LaneBasedGtu perceivingGtu, LaneBasedGtu perceivedGtu, Length distance)
            throws GtuException, ParameterException;

    /**
     * Creates a headway object from a GTU, parallel.
     * @param perceivingGtu perceiving GTU
     * @param perceivedGtu perceived GTU
     * @param overlapFront front overlap
     * @param overlap overlap
     * @param overlapRear rear overlap
     * @return headway object from a gtu
     * @throws GtuException when headway object cannot be created
     */
    HeadwayGtu createParallelGtu(LaneBasedGtu perceivingGtu, LaneBasedGtu perceivedGtu, Length overlapFront, Length overlap,
            Length overlapRear) throws GtuException;

    /**
     * Class for perceived neighbors. Adjacent neighbors are perceived exactly.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    class PerceivedHeadwayGtuType implements HeadwayGtuType
    {

        /** Estimation. */
        private final Estimation estimation;

        /** Anticipation. */
        private final Anticipation anticipation;

        /** Last update time. */
        private Time updateTime = null;

        /** Reaction time at update time. */
        private Duration tr;

        /** Historical moment considered at update time. */
        private Time when;

        /** Traveled distance during reaction time at update time. */
        private Length traveledDistance;

        /**
         * Constructor.
         * @param estimation estimation
         * @param anticipation anticipation
         */
        public PerceivedHeadwayGtuType(final Estimation estimation, final Anticipation anticipation)
        {
            this.estimation = estimation;
            this.anticipation = anticipation;
        }

        @Override
        public HeadwayGtu createHeadwayGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference,
                final LaneBasedGtu perceivedGtu, final Length distance, final boolean downstream)
                throws GtuException, ParameterException
        {
            Time now = perceivedGtu.getSimulator().getSimulatorAbsTime();
            if (this.updateTime == null || now.si > this.updateTime.si)
            {
                this.updateTime = now;
                this.tr = perceivingGtu.getParameters().getParameter(ParameterTypes.TR);
                Time whenTemp = now.minus(this.tr);
                if (this.when == null || whenTemp.si > this.when.si)
                {
                    // never go backwards in time if the reaction time increases
                    this.when = whenTemp;
                }
                this.traveledDistance = perceivingGtu.equals(reference)
                        ? perceivingGtu.getOdometer().minus(perceivingGtu.getOdometer(this.when)) : Length.ZERO;
            }
            NeighborTriplet triplet =
                    this.estimation.estimate(perceivingGtu, reference, perceivedGtu, distance, downstream, this.when);
            triplet = this.anticipation.anticipate(triplet, this.tr, this.traveledDistance, downstream);
            return new HeadwayGtuPerceived(perceivedGtu, triplet.headway(), triplet.speed(), triplet.acceleration());
        }

        @Override
        public HeadwayGtu createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            return createHeadwayGtu(perceivingGtu, perceivingGtu, perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGtu createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length distance) throws GtuException, ParameterException
        {
            return createHeadwayGtu(perceivingGtu, perceivingGtu, perceivedGtu, distance, false);
        }

        @Override
        public HeadwayGtu createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
        {
            return new HeadwayGtuPerceived(perceivedGtu, overlapFront, overlap, overlapRear, perceivedGtu.getSpeed(),
                    perceivedGtu.getAcceleration());
        }

    }

}
