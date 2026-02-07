package org.opentrafficsim.draw.colorer.trajectory;

import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;

/**
 * Interface allowing trajectory colorers to report whether the whole trajectory has one color.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface TrajectoryColorer extends Colorer<TrajectorySection>
{

    /**
     * Whether the trajectory of a GTU is a single color. By default this is false.
     * @return whether the trajectory of a GTU is a single color
     */
    default boolean isSingleColor()
    {
        return false;
    }

}
