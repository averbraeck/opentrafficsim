package org.opentrafficsim.road.gtu.lane;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Different methods of dealing with lane bookkeeping when changing lane.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum LaneBookkeeping
{

    /**
     * Instantaneous lane changes. GTUs make a lateral jump. This is advised for scientific output as models are not well
     * developed regarding lane change movement, lane change cancellation, the leader in the start lane (particularly at low
     * speed), and how potential followers respond.
     */
    INSTANT,

    /**
     * Bookkeeping changes at the start of a lane change. The GTU has to make the lateral move with possible GTU overlap in the
     * from lane. Trajectories are instantaneously recorded in the target lane.
     */
    START,

    /**
     * Bookkeeping changes when the reference point of the GTU enters the adjacent lane. Due to model limitations this can
     * create dead-locks and severe decelerations at low speed in dense traffic. This is advised for control of vehicles in
     * driver simulators due to the full continuous movement without overlap between GTUs.
     */
    EDGE,

    /**
     * The same as EDGE, but START is used when the speed drops below a low threshold. This prevents dead-locks and severe
     * decelerations, but allows GTU overlap in the from lane at low speeds. This is advised for microscopic simulations with
     * visual purposes. Using START only at low speeds makes the trajectories more correlated to the movement.
     */
    START_AND_EDGE;

    /** Threshold speed below which START is used in START_AND_EDGE. */
    public static final Speed START_THRESHOLD = new Speed(5.0, SpeedUnit.KM_PER_HOUR);

}
