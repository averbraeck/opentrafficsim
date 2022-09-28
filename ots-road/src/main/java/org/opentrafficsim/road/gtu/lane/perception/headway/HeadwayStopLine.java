package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class HeadwayStopLine extends AbstractHeadwayLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20160630L;

    /**
     * Construct a new HeadwayStopLine.
     * @param id String; the id of the object for comparison purposes, can not be null.
     * @param distance the distance to the other object; if this constructor is used, distance cannot be null.
     * @param lane Lane; lane
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayStopLine(final String id, final Length distance, final Lane lane) throws GtuException
    {
        super(ObjectType.STOPLINE, id, distance, lane);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return super.toString();
    }

}
