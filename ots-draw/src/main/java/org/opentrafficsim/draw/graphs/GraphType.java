package org.opentrafficsim.draw.graphs;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public enum GraphType
{
    /** Trajectory. */
    TRAJECTORY,

    /** Contour plot: speed. */
    SPEED_CONTOUR,

    /** Contour plot: acceleration. */
    ACCELERATION_CONTOUR,

    /** Contour plot: density. */
    DENSITY_CONTOUR,

    /** Contour plot: flow. */
    FLOW_CONTOUR,

    /** Contour plot: delay. */
    DELAY_CONTOUR,

    /** Fundamental diagram. */
    FUNDAMENTAL_DIAGRAM,

    /** Any other graph. */
    OTHER,
}
