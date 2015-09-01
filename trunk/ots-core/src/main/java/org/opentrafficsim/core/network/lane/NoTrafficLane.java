package org.opentrafficsim.core.network.lane;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Lane without traffic, e.g. emergency lane next to highway.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Feb 28, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NoTrafficLane extends Lane
{
    /** */
    private static final long serialVersionUID = 20150228L;

    /** Map that tells that directionality is NONE for all GTU types. */
    private static final Map<GTUType, LongitudinalDirectionality> DIRECTIONALITY_NONE = new HashMap<>();

    /** Map that tells that speed is 0.0 for all GTU Types. */
    private static final Map<GTUType, Speed.Abs> SPEED_NULL = new HashMap<>();

    static
    {
        DIRECTIONALITY_NONE.put(GTUType.ALL, LongitudinalDirectionality.NONE);
        SPEED_NULL.put(GTUType.ALL, new Speed.Abs(0.0, SpeedUnit.SI));
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id String; the id of the lane. Should be unique within the parentLink.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
     * @throws OTSGeometryException when creation of the geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public NoTrafficLane(final CrossSectionLink parentLink, final String id,
        final Length.Rel lateralOffsetAtStart, final Length.Rel lateralOffsetAtEnd,
        final Length.Rel beginWidth, final Length.Rel endWidth)
        throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth, LaneType.NONE,
            DIRECTIONALITY_NONE, SPEED_NULL);
    }

    /** {@inheritDoc} */
    @Override
    protected final double getZ()
    {
        return -0.00005;
    }
}
