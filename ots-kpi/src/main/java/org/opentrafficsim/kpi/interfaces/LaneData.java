package org.opentrafficsim.kpi.interfaces;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Identifiable;

/**
 * Represents a lane for sampling.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneData extends Identifiable
{

    /**
     * Returns the length of the lane.
     * @return length of the lane
     */
    Length getLength();

    /**
     * Returns the parent link of the lane.
     * @return parent link of the lane
     */
    LinkData getLinkData();

}
