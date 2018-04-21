package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.Anticipation.NeighborTriplet;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUPerceived;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTURealCopy;

/**
 * Whether a GTU needs to be wrapped, or information should be copied for later and unaltered use.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public final static HeadwayGtuType WRAP = new HeadwayGtuType()
    {
        @Override
        public HeadwayGTUReal createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream) throws GTUException
        {
            return new HeadwayGTUReal(perceivedGtu, distance, true);
        }

        @Override
        public HeadwayGTUReal createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return new HeadwayGTUReal(perceivedGtu, overlapFront, overlap, overlapRear, true);
        }
    };

    /** Info regarding the GTU is copied. */
    public final static HeadwayGtuType COPY = new HeadwayGtuType()
    {
        @Override
        public HeadwayGTURealCopy createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream) throws GTUException
        {
            return new HeadwayGTURealCopy(perceivedGtu, distance);
        }

        @Override
        public HeadwayGTURealCopy createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return new HeadwayGTURealCopy(perceivedGtu, overlapFront, overlap, overlapRear);
        }
    };

    /**
     * Creates a headway object from a GTU, downstream or upstream.
     * @param perceivingGtu perceiving GTU
     * @param perceivedGtu perceived GTU
     * @param distance distance
     * @param downstream downstream (or upstream) neighbor
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    HeadwayGTU createHeadwayGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance, boolean downstream)
            throws GTUException, ParameterException;

    /**
     * Creates a headway object from a GTU, parallel.
     * @param perceivingGtu perceiving GTU
     * @param perceivedGtu perceived GTU
     * @param overlapFront front overlap
     * @param overlap overlap
     * @param overlapRear rear overlap
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     */
    HeadwayGTU createHeadwayGtu(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length overlapFront, Length overlap,
            Length overlapRear) throws GTUException;

    /**
     * Class for perceived neighbors. Adjacent neighbors are perceived exactly.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class PerceivedHeadwayGtuType implements HeadwayGtuType
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
            Time now = perceivedGtu.getSimulator().getSimulatorTime().getTime();
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
            triplet = this.anticipation.anticipate(triplet, this.tr, this.traveledDistance);
            return new HeadwayGTUPerceived(perceivedGtu, triplet.getHeadway(), triplet.getSpeed(), triplet.getAcceleration());
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                Length overlapFront, Length overlap, Length overlapRear) throws GTUException
        {
            return new HeadwayGTUPerceived(perceivedGtu, overlapFront, overlap, overlapRear, perceivedGtu.getSpeed(),
                    perceivedGtu.getAcceleration());
        }

    }

}
