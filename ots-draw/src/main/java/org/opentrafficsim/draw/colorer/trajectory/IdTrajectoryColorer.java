package org.opentrafficsim.draw.colorer.trajectory;

import org.opentrafficsim.draw.colorer.IdColorer;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;

/**
 * Color trajectory based on the id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IdTrajectoryColorer extends IdColorer<TrajectorySection> implements TrajectoryColorer
{

    /**
     * Constructor.
     */
    public IdTrajectoryColorer()
    {
        super((traj) -> traj.trajectory().getGtuId());
    }

    @Override
    public boolean isSingleColor()
    {
        return true;
    }

}
