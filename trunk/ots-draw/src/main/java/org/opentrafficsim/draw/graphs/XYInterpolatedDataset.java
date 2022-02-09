package org.opentrafficsim.draw.graphs;

import org.jfree.data.xy.XYZDataset;

/**
 * {@code XYZDataset} extension that allows retrieval of surrounding values by adjusting the item number using the number of
 * items per column.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
interface XYInterpolatedDataset extends XYZDataset
{

    /**
     * Returns the number of items in each column.
     * @return int; number of items in each column
     */
    int getRangeBinCount();

}
