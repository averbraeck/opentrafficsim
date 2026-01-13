package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.DistractionField;
import org.opentrafficsim.road.network.lane.object.RoadSideDistraction;

/**
 * Channel implementation of task due to road side distraction.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskRoadSideDistraction extends AbstractTask implements ChannelTask
{

    /** Distraction field. */
    private final DistractionField distractionField;

    /** Direction. */
    private final LateralDirectionality direction;

    /** Filter to retain relevant distractions. */
    private final BiFunction<RelativeLane, RoadSideDistraction, Boolean> filter;

    /**
     * Constructor. This task applies to a subset of distractions depending on the direction:
     * <ul>
     * <li>LEFT: distractions on left lanes with NONE direction, and distractions on current lane with LEFT direction</li>
     * <li>RIGHT: distractions on right lanes with NONE direction, and distractions on current lane with RIGHT direction</li>
     * <li>NONE: distractions on current lane with NONE direction
     * </ul>
     * @param distractionField distraction field, which should be shared amongst instances
     * @param direction direction of applicable distractions
     */
    public ChannelTaskRoadSideDistraction(final DistractionField distractionField, final LateralDirectionality direction)
    {
        super("road-side distraction (" + direction + ")");
        Throw.whenNull(distractionField, "distractionField");
        Throw.whenNull(direction, "direction");
        this.distractionField = distractionField;
        this.direction = direction;
        // all distractions on current lane with direction of this task
        // all distractions on lanes on the side of this task, where the distraction direction is NONE (i.e. on the lane)
        this.filter = (lane, distraction) -> (lane.isCurrent() && this.direction.equals(distraction.getSide()))
                || (this.direction.equals(lane.getLateralDirectionality()) && distraction.getSide().isNone());
    }

    @Override
    protected double calculateTaskDemand(final LanePerception perception) throws ParameterException
    {
        return this.distractionField.getDistraction(this.filter);
    }

    @Override
    public Object getChannel()
    {
        return this.direction.isLeft() ? LEFT : (this.direction.isRight() ? RIGHT : FRONT);
    }

    /**
     * Supplier of distraction tasks. This supplier returns a persistent set in which each distraction task shares a distraction
     * field.
     */
    public static class Supplier implements Function<LanePerception, Set<ChannelTask>>
    {
        /** Set of tasks. */
        private final Set<ChannelTask> set;

        /**
         * Constructor.
         * @param gtu GTU
         */
        public Supplier(final LaneBasedGtu gtu)
        {
            DistractionField distractionField = new DistractionField(gtu);
            this.set = Set.of(new ChannelTaskRoadSideDistraction(distractionField, LateralDirectionality.LEFT),
                    new ChannelTaskRoadSideDistraction(distractionField, LateralDirectionality.NONE),
                    new ChannelTaskRoadSideDistraction(distractionField, LateralDirectionality.RIGHT));
        }

        @Override
        public Set<ChannelTask> apply(final LanePerception t)
        {
            return this.set;
        }
    }

}
