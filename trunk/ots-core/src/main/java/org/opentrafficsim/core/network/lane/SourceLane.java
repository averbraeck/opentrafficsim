package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Lane that creates GTUs.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class SourceLane<LINKID, NODEID> extends Lane<LINKID, NODEID>
{
    /** the generators on this generation lane; one per GTU type. */
    // NOT USED private final Set<GTUGeneratorIndividual<?>> generators = new HashSet<GTUGeneratorIndividual<?>>();

    /**
     * Construct a GeneratorLane.
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit on the new GeneratorLane
     * @throws NetworkException when creation of the geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SourceLane(final CrossSectionLink<LINKID, NODEID> parentLink,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> beginWidth,
        final LaneType<?> laneType, final LongitudinalDirectionality directionality,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws NetworkException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtStart, beginWidth, beginWidth, laneType, directionality,
            new DoubleScalar.Abs<FrequencyUnit>(1, FrequencyUnit.SI), speedLimit);
    }

    /**
     * String ID class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends SourceLane<String, String>
    {
        /**
         * @param parentLink Cross Section Link to which the element belongs.
         * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
         * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design
         *            line
         * @param laneType type of lane to deduce compatibility with GTU types
         * @param directionality in direction of geometry, reverse, or both
         * @param speedLimit speed limit on this lane
         * @throws NetworkException when creation of the geometry fails
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public STR(final CrossSectionLink<String, String> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> beginWidth,
            final LaneType<?> laneType, final LongitudinalDirectionality directionality,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws NetworkException
        {
            super(parentLink, lateralOffsetAtStart, beginWidth, laneType, directionality, speedLimit);
        }
    }

    /**
     * Integer ID class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends SourceLane<Integer, Integer>
    {
        /**
         * @param parentLink Cross Section Link to which the element belongs.
         * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
         *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
         * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design
         *            line
         * @param laneType type of lane to deduce compatibility with GTU types
         * @param directionality in direction of geometry, reverse, or both
         * @param speedLimit speed limit on this lane
         * @throws NetworkException when creation of the geometry fails
         */
        @SuppressWarnings("checkstyle:parameternumber")
        public INT(final CrossSectionLink<Integer, Integer> parentLink,
            final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart, final DoubleScalar.Rel<LengthUnit> beginWidth,
            final LaneType<?> laneType, final LongitudinalDirectionality directionality,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws NetworkException
        {
            super(parentLink, lateralOffsetAtStart, beginWidth, laneType, directionality, speedLimit);
        }
    }
}
