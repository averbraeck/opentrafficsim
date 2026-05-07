package org.opentrafficsim.animation.graphs;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
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
