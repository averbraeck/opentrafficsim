package org.opentrafficsim.core.gtu;

import java.util.Set;

import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.core.object.NonLocatedObject;

/**
 * Gtu generator in its most basic form, which is able to report a queue count at one or more positions.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface GtuGenerator extends NonLocatedObject
{

    /**
     * Returns the positions.
     * @return Set&lt;GtuGeneratorPosition&gt;; set of positions.
     */
    Set<GtuGeneratorPosition> getPositions();

    /**
     * Interface for a position that is reported on. This is a Locatable, with a queue count added to is.
     * <p>
     * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    interface GtuGeneratorPosition extends OtsLocatable
    {
        /**
         * Returns the number of GTUs in the queue.
         * @return int; number of GTUs in the queue.
         */
        int getQueueCount();
    }

}
