package org.opentrafficsim.kpi.interfaces;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.Identifiable;

/**
 * Represents a lane for sampling.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface LaneDataInterface extends Identifiable
{

    /**
     * @return length of the lane
     */
    Length getLength();

    /**
     * @return parent link of the lane
     */
    LinkDataInterface getLinkData();

    /**
     * @return lane id
     */
    @Override
    String getId();

}
