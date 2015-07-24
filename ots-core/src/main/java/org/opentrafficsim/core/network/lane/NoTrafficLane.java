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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class NoTrafficLane<LINKID, NODEID> extends Lane<LINKID, NODEID>
{
    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
     * @throws NetworkException when creation of the geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink<LINKID, NODEID> parentLink,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd,
        final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth) throws NetworkException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth, LaneType.NONE,
            LongitudinalDirectionality.NONE, new DoubleScalar.Abs<FrequencyUnit>(0.0, FrequencyUnit.SI),
            new DoubleScalar.Abs<SpeedUnit>(0.0, SpeedUnit.SI));
    }

    /** {@inheritDoc} */
    @Override
    protected final double getZ()
    {
        return -0.00005;
    }

    /**
     * String ID class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends NoTrafficLane<String, String>
    {
        /**
         * @param parentLink Cross Section Link to which the element belongs.
         * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
         * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
         * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design
         *            line
         * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
         * @throws NetworkException when creation of the geometry fails
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public STR(final CrossSectionLink<String, String> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd,
            final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth)
            throws NetworkException
        {
            super(parentLink, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        }
    }

    /**
     * Integer ID class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends NoTrafficLane<Integer, Integer>
    {
        /**
         * @param parentLink Cross Section Link to which the element belongs.
         * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
         * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
         * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design
         *            line
         * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
         * @throws NetworkException when creation of the geometry fails
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public INT(final CrossSectionLink<Integer, Integer> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd,
            final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth)
            throws NetworkException
        {
            super(parentLink, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        }
    }

}
