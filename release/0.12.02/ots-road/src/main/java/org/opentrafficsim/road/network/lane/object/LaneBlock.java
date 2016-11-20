package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Block on a lane that a GTU cannot pass.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBlock extends AbstractLaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /**
     * @param id the id
     * @param lane the lane where the blockage exists
     * @param longitudinalPosition the position on the lane; position where the GTUs have to stop
     * @param height the height of the blockage
     * @throws NetworkException in case object cannot be placed on the lane
     */
    public LaneBlock(final String id, final Lane lane, final Length longitudinalPosition, final Length height)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition), height);
    }

    /**
     * @param id the id
     * @param lane the lane where the blockage exists
     * @param longitudinalPosition the position on the lane; position where the GTUs have to stop
     * @param geometry the geometry of the lane blockage
     * @param height the height of the blockage
     * @throws NetworkException in case object cannot be placed on the lane
     */
    public LaneBlock(final String id, final Lane lane, final Length longitudinalPosition, final OTSLine3D geometry,
            final Length height) throws NetworkException
    {
        super(id, lane, longitudinalPosition, geometry, height);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "LaneBlock: " + super.toString();
    }

    /** {@inheritDoc} */
    @Override
    public LaneBlock clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        // TODO
        return null;
    }

}
