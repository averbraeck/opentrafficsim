package org.opentrafficsim.core.network.lane;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Shoulder extends CrossSectionElement
{
    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralStartPosition the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     */
    public Shoulder(final CrossSectionLink<?, ?> parentLink, final DoubleScalar.Rel<LengthUnit> lateralStartPosition,
        final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth)
    {
        super(parentLink, lateralStartPosition, XXXX, beginWidth, endWidth);
    }

}
