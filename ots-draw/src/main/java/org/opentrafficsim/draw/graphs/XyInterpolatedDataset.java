package org.opentrafficsim.draw.graphs;

import org.jfree.data.xy.XYZDataset;

/**
 * {@code XYZDataset} extension that allows retrieval of surrounding values by adjusting the item number using the number of
 * items per column.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
interface XyInterpolatedDataset extends XYZDataset
{

    /**
     * Returns the number of items in each column.
     * @return number of items in each column
     */
    int getRangeBinCount();

}
