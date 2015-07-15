package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Lane without traffic, e.g. emergency lane next to highway.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionFeb 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NoTrafficLane extends Lane
{

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the
     *            design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design
     *            line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param capacity Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage)
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit in the forbidden lane (probably not really
     *            useful)
     * @throws NetworkException when creation of the geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink<?, ?> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd, final DoubleScalar.Rel<LengthUnit> beginWidth,
            final DoubleScalar.Rel<LengthUnit> endWidth, final LaneType<?> laneType,
            final LongitudinalDirectionality directionality, final DoubleScalar.Abs<FrequencyUnit> capacity,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws NetworkException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth, laneType, directionality,
                capacity, speedLimit);
    }

    /** {@inheritDoc} */
    @Override
    protected final double getZ()
    {
        return -0.00005;
    }

}
