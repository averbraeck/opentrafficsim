package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 30, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayStopLine(final String id, final Length distance, final Lane lane) throws GTUException
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
