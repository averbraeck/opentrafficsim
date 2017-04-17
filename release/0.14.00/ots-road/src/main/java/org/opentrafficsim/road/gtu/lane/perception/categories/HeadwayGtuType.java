package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.GTUStatus;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTURealCopy;

/**
 * Whether a GTU needs to be wrapped, or information should be copied for later and unaltered use.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public enum HeadwayGtuType
{

    /** The GTU is wrapped, and info is taken directly from it. */
    WRAP
    {
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU gtu, final Length distance) throws GTUException
        {
            return new HeadwayGTUReal(gtu, distance, true);
        }
        
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU gtu, final Length overlapFront, final Length overlap,
                final Length overlapRear) throws GTUException
        {
            return new HeadwayGTUReal(gtu, overlapFront, overlap, overlapRear, true);
        }
    },

    /** Info regarding the GTU is copied. */
    COPY
    {
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU gtu, final Length distance) throws GTUException
        {
            // TODO more GTU statuses
            if (gtu.getTurnIndicatorStatus().isLeft())
            {
                return new HeadwayGTURealCopy(gtu, distance, GTUStatus.LEFT_TURNINDICATOR);
            }
            else if (gtu.getTurnIndicatorStatus().isRight())
            {
                return new HeadwayGTURealCopy(gtu, distance, GTUStatus.RIGHT_TURNINDICATOR);
            }
            else if (gtu.getTurnIndicatorStatus().isHazard())
            {
                return new HeadwayGTURealCopy(gtu, distance, GTUStatus.EMERGENCY_LIGHTS);
            }
            return new HeadwayGTURealCopy(gtu, distance);
        }

        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU gtu, final Length overlapFront, final Length overlap,
                final Length overlapRear) throws GTUException
        {
            return new HeadwayGTURealCopy(gtu, overlapFront, overlap, overlapRear);
        }
    };

    /**
     * Creates a headway object from a GTU, downstream or upstream.
     * @param gtu gtu
     * @param distance distance
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     */
    public abstract HeadwayGTU createHeadwayGtu(LaneBasedGTU gtu, Length distance) throws GTUException;

    /**
     * Creates a headway object from a GTU, parallel.
     * @param gtu gtu
     * @param overlapFront front overlap
     * @param overlap overlap
     * @param overlapRear rear overlap
     * @return headway object from a gtu
     * @throws GTUException when headway object cannot be created
     */
    public abstract HeadwayGTU createHeadwayGtu(LaneBasedGTU gtu, Length overlapFront, Length overlap, Length overlapRear)
            throws GTUException;

}
