package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUPerceived;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTURealCopy;

/**
 * Whether a GTU needs to be wrapped, or information should be copied for later and unaltered use.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface HeadwayGtuType
{

    /** The GTU is wrapped, and info is taken directly from it. */
    HeadwayGtuType WRAP = new HeadwayGtuType()
    {
        @Override
        public HeadwayGTUReal createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException
        {
            return new HeadwayGTUReal(perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGTUReal createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException
        {
            return new HeadwayGTUReal(perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGTUReal createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return new HeadwayGTUReal(perceivedGtu, overlapFront, overlap, overlapRear, true);
        }
    };

    /** Info regarding the GTU is copied. */
    HeadwayGtuType COPY = new HeadwayGtuType()
    {
        @Override
        public HeadwayGTURealCopy createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException
        {
            return new HeadwayGTURealCopy(perceivedGtu, distance);
        }

        @Override
        public HeadwayGTURealCopy createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException
        {
            return new HeadwayGTURealCopy(perceivedGtu, distance);
        }

        @Override
        public HeadwayGTURealCopy createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return new HeadwayGTURealCopy(perceivedGtu, overlapFront, overlap, overlapRear);
        }
    };

    /**
     * Creates a headway object from a GTU, downstream or upstream. The default implementation figures out from possible
     * negative distance whether a parallel GTU should be created.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; distance
     * @param downstream boolean; downstream (or upstream) neighbor
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    default HeadwayGTU createHeadwayGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance,
            boolean downstream) throws GTUException, ParameterException
    {
        if (distance.ge0())
        {
            if (downstream)
            {
                return createDownstreamGtu(perceivingGtu, perceivedGtu, distance);
            }
            return createUpstreamGtu(perceivingGtu, perceivedGtu, distance);
        }
        Throw.when(-distance.si > perceivingGtu.getLength().si + perceivedGtu.getLength().si, IllegalStateException.class,
                "A GTU that is supposedly %s is actually %s.", downstream ? "downstream" : "upstream",
                downstream ? "upstream" : "downstream");
        Length overlapRear = distance.plus(perceivingGtu.getLength());
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
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; distance
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    HeadwayGTU createDownstreamGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance)
            throws GTUException, ParameterException;

    /**
     * Creates a headway object from a GTU, downstream.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; distance
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    HeadwayGTU createUpstreamGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance)
            throws GTUException, ParameterException;

    /**
     * Creates a headway object from a GTU, parallel.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param overlapFront Length; front overlap
     * @param overlap Length; overlap
     * @param overlapRear Length; rear overlap
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     */
    HeadwayGTU createParallelGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length overlapFront, Length overlap,
            Length overlapRear) throws GTUException;

    /**
     * Class for perceived neighbors. Adjacent neighbors are perceived exactly.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
         * @param estimation Estimation; estimation
         * @param anticipation Anticipation; anticipation
         */
        public PerceivedHeadwayGtuType(final Estimation estimation, final Anticipation anticipation)
        {
            this.estimation = estimation;
            this.anticipation = anticipation;
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream) throws GTUException, ParameterException
        {
            Time now = perceivedGtu.getSimulator().getSimulatorTime();
            if (this.updateTime == null || now.si > this.updateTime.si)
            {
                this.updateTime = now;
                this.tr = perceivingGtu.getParameters().getParameter(ParameterTypes.TR);
                Time whenTemp = now.minus(this.tr);
                if (this.when == null || (this.when != null && whenTemp.si > this.when.si))
                {
                    // never go backwards in time if the reaction time increases
                    this.when = whenTemp;
                }
                this.traveledDistance = perceivingGtu.getOdometer().minus(perceivingGtu.getOdometer(this.when));
            }
            NeighborTriplet triplet = this.estimation.estimate(perceivingGtu, perceivedGtu, distance, downstream, this.when);
            triplet = this.anticipation.anticipate(triplet, this.tr, this.traveledDistance, downstream);
            return new HeadwayGTUPerceived(perceivedGtu, triplet.getHeadway(), triplet.getSpeed(), triplet.getAcceleration());
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            return createHeadwayGtu(perceivingGtu, perceivedGtu, distance, true);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            return createHeadwayGtu(perceivingGtu, perceivedGtu, distance, false);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return new HeadwayGTUPerceived(perceivedGtu, overlapFront, overlap, overlapRear, perceivedGtu.getSpeed(),
                    perceivedGtu.getAcceleration());
        }

    }

}
