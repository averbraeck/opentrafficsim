package org.opentrafficsim.road.gtu.lane;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Different methods of dealing with lane bookkeeping when changing lane.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    INSTANT(false, false, false),

    /**
     * Bookkeeping changes at the start of a lane change. The GTU has to make the lateral move with possible GTU overlap in the
     * from lane. Trajectories are instantaneously recorded in the target lane.
     */
    START(false, false, false),

    /**
     * Bookkeeping changes when the reference point of the GTU enters the adjacent lane. Due to model limitations this can
     * create dead-locks and severe decelerations at low speed in dense traffic. This is advised for control of vehicles in
     * driver simulators due to the full continuous movement without overlap between GTUs.
     */
    EDGE(true, false, false),

    /**
     * The same as EDGE, but START is used when the speed drops below a low threshold. This prevents dead-locks and severe
     * decelerations, but allows GTU overlap in the from lane at low speeds. This is advised for microscopic simulations with
     * visual purposes. Using START only at low speeds makes the trajectories more correlated to the movement.
     */
    START_AND_EDGE(true, true, false),

    /**
     * The same as EDGE, but lateral crossing of an edge is only checked if something informs the bookkeeping that the GTU is
     * changing lanes. This can for example be done by a model. In this way a model can prevent lane bookkeeping errors that its
     * own limited modeling could produce. For example when canceling a lane change due to an adjacent GTU but still curving
     * over the edge.
     */
    EDGE_INFORMED(true, false, true),

    /**
     * Combines functions of START_AND_EDGE and EDGE_INFORMED.
     */
    START_AND_EDGE_INFORMED(true, true, true);

    /** Whether the lane bookkeeping is any type that contains edge-based logic. */
    private final boolean edge;

    /** Whether this is start-based lane bookkeeping below the start threshold, and edge-based lane bookkeeping otherwise. */
    private final boolean startAndEdge;

    /** Whether the bookkeeping is informed by an outside source, such as a model, that a lane change is occurring. */
    private final boolean informed;

    /**
     * Constructor.
     * @param edge whether the lane bookkeeping is any type that contains edge-based logic
     * @param startAndEdge whether this is start-based lane bookkeeping below the start threshold, and edge-based lane
     *            bookkeeping otherwise
     * @param informed whether the bookkeeping is informed by an outside source, such as a model, that a lane change is
     *            occurring
     */
    LaneBookkeeping(final boolean edge, final boolean startAndEdge, final boolean informed)
    {
        this.edge = edge;
        this.startAndEdge = startAndEdge;
        this.informed = informed;
    }

    /**
     * Whether the lane bookkeeping is any type that contains edge-based logic.
     * @return whether the lane bookkeeping is any type that contains edge-based logic
     */
    public boolean isEdge()
    {
        return this.edge;
    }

    /**
     * Whether this is start-based lane bookkeeping below the start threshold, and edge-based lane bookkeeping otherwise.
     * @return whether this is start-based lane bookkeeping below the start threshold, and edge-based lane bookkeeping otherwise
     */
    public boolean isStartAndEdge()
    {
        return this.startAndEdge;
    }

    /**
     * Whether the bookkeeping is informed by an outside source, such as a model, that a lane change is occurring.
     * @return whether the bookkeeping is informed by an outside source, such as a model, that a lane change is occurring
     */
    public boolean isInformed()
    {
        return this.informed;
    }

    /** Threshold speed below which START is used in START_AND_EDGE. */
    public static final Speed START_THRESHOLD = new Speed(5.0, SpeedUnit.KM_PER_HOUR);

}
