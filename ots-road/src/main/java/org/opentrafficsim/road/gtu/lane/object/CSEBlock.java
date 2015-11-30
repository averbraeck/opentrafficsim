package org.opentrafficsim.road.gtu.lane.object;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 26, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CSEBlock extends AbstractCSEObject
{

    /**
     * @param geometry the geometry of the object
     * @param height the height of the object
     */
    public CSEBlock(final OTSLine3D geometry, final Length.Rel height)
    {
        super(geometry, height);
    }

    /**
     * @param cse the cross section element, e.g. lane, where the block is located
     * @param position the relative position on the design line of the link for this block
     * @return a new CrossSectionElementBlock on the right position on the cse
     * @throws NetworkException in case the position is outside the CSE
     */
    public static CSEBlock createCrossSectionElementBlock(final CrossSectionElement cse,
        final Length.Rel position) throws NetworkException
    {
        return new CSEBlock(AbstractCSEObject.createRectangleOnCSE(cse, position, new Length.Rel(0.5,
            LengthUnit.METER), cse.getWidth(position).multiplyBy(0.8)), new Length.Rel(1.0, LengthUnit.METER));
    }
}
