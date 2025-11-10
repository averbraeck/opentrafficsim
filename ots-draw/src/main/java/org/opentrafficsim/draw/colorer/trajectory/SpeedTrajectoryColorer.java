package org.opentrafficsim.draw.colorer.trajectory;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.colorer.SpeedColorer;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;

/**
 * Color trajectory based on the speed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedTrajectoryColorer extends SpeedColorer<TrajectorySection> implements TrajectoryColorer
{

    /** Value function. */
    private static final Function<TrajectorySection, Speed> VALUE =
            (traj) -> Speed.ofSI(traj.trajectory().getV(traj.section()));

    /**
     * Constructor.
     * @param boundPaintScale bounds paint scale, based on values in km/h
     */
    public SpeedTrajectoryColorer(final BoundsPaintScale boundPaintScale)
    {
        super(VALUE, boundPaintScale);
    }

    /**
     * Constructor.
     * @param maximumSpeed maximum speed
     */
    public SpeedTrajectoryColorer(final Speed maximumSpeed)
    {
        super(VALUE, maximumSpeed);
    }

    /**
     * Constructor constructing a range to 150km/h.
     */
    public SpeedTrajectoryColorer()
    {
        super(VALUE);
    }

}
