package org.opentrafficsim.draw.colorer.trajectory;

import java.awt.Color;

import org.opentrafficsim.draw.colorer.FixedColorer;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;

/**
 * Fixed colorer for trajectories.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FixedTrajectoryColorer extends FixedColorer<TrajectorySection> implements TrajectoryColorer
{

    /**
     * Constructor.
     * @param color the color
     * @param name color name
     */
    public FixedTrajectoryColorer(final Color color, final String name)
    {
        super(color, name);
    }

    /**
     * Constructor.
     * @param color the color
     */
    public FixedTrajectoryColorer(final Color color)
    {
        super(color);
    }

    @Override
    public boolean isSingleColor()
    {
        return true;
    }

}
