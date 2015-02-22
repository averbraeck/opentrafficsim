package org.opentrafficsim.core.network.lane;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.generator.GTUGeneratorIndividual;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Lane that creates GTUs.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jan 30, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SourceLane extends Lane
{
    /** the generators on this generation lane; one per GTU type. */
    private final Set<GTUGeneratorIndividual> generators = new HashSet<GTUGeneratorIndividual>();
    
    /**
     * Construct a GeneratorLane.
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @throws NetworkException when creation of the geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public SourceLane(final CrossSectionLink<?, ?> parentLink, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart,
        final DoubleScalar.Rel<LengthUnit> beginWidth, final LaneType<?> laneType,
        final LongitudinalDirectionality directionality) throws NetworkException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtStart, beginWidth, beginWidth, laneType, directionality,
            new DoubleScalar.Abs<FrequencyUnit>(1, FrequencyUnit.SI));
    }


}
