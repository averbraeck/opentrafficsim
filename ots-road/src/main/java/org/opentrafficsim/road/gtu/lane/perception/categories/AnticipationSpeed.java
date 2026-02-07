package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationSpeed.SpeedSet;

/**
 * Collector of leaders which derives a set of anticipation speeds from a lane. This includes all GTUs on the lane (current),
 * all GTUs indicating left (left) and all GTUs indicating right (right).
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnticipationSpeed implements PerceptionCollector<SpeedSet, LaneBasedGtu, SpeedSet>
{

    /** Desired speed. */
    private double desiredSpeed;

    /** Look-ahead distance. */
    private double x0;

    /** Lane. */
    private RelativeLane lane;

    /**
     * Constructor.
     * @param desiredSpeed desired speed
     * @param lookAhead look-ahead distance
     * @param lane lane
     */
    public AnticipationSpeed(final Speed desiredSpeed, final Length lookAhead, final RelativeLane lane)
    {
        this.desiredSpeed = desiredSpeed.si;
        this.x0 = lookAhead.si;
        this.lane = lane;
    }

    @Override
    public Supplier<SpeedSet> getIdentity()
    {
        return new Supplier<SpeedSet>()
        {
            @Override
            public SpeedSet get()
            {
                SpeedSet identity = new SpeedSet();
                identity.left = AnticipationSpeed.this.desiredSpeed;
                identity.current = AnticipationSpeed.this.desiredSpeed;
                identity.right = AnticipationSpeed.this.desiredSpeed;
                return identity;
            }
        };
    }

    @Override
    public PerceptionAccumulator<LaneBasedGtu, SpeedSet> getAccumulator()
    {
        return new PerceptionAccumulator<LaneBasedGtu, SpeedSet>()
        {
            @Override
            public Intermediate<SpeedSet> accumulate(final Intermediate<SpeedSet> intermediate, final LaneBasedGtu object,
                    final Length distance)
            {
                if (AnticipationSpeed.this.lane.getNumLanes() < 2)
                {
                    intermediate.getObject().current =
                            Math.min(intermediate.getObject().current, anticipateSingle(object, distance));
                }
                if (!AnticipationSpeed.this.lane.isCurrent())
                {
                    if (AnticipationSpeed.this.lane.isRight())
                    {
                        if (object.getTurnIndicatorStatus().isLeft())
                        {
                            intermediate.getObject().left =
                                    Math.min(intermediate.getObject().left, anticipateSingle(object, distance));
                        }
                    }
                    else
                    {
                        if (object.getTurnIndicatorStatus().isRight())
                        {
                            intermediate.getObject().right =
                                    Math.min(intermediate.getObject().right, anticipateSingle(object, distance));
                        }
                    }
                }
                return intermediate;
            }
        };

    }

    /**
     * Anticipate a single leader by possibly lowering the anticipation speed.
     * @param gtu GTU
     * @param distance distance to GTU
     * @return possibly lowered anticipation speed
     */
    private double anticipateSingle(final Gtu gtu, final Length distance)
    {
        Speed speed = gtu.getSpeed();
        double v = speed == null ? 0.0 : speed.si;
        if (v > this.desiredSpeed || distance.si > this.x0)
        {
            return this.desiredSpeed;
        }
        double f = distance.si / this.x0;
        f = f < 0.0 ? 0.0 : f > 1.0 ? 1.0 : f;
        return (1 - f) * v + f * this.desiredSpeed;
    }

    @Override
    public Function<SpeedSet, SpeedSet> getFinalizer()
    {
        return new Function<SpeedSet, SpeedSet>()
        {
            @Override
            public SpeedSet apply(final SpeedSet intermediate)
            {
                return intermediate;
            }
        };
    }

    /**
     * Class to contain info from 1 lane, regarding 3 lanes.
     */
    public static class SpeedSet
    {
        /** Speed regarding the left lane. */
        private double left = Double.POSITIVE_INFINITY;

        /** Speed regarding the current lane. */
        private double current = Double.POSITIVE_INFINITY;

        /** Speed regarding the right lane. */
        private double right = Double.POSITIVE_INFINITY;

        /**
         * Constructor.
         */
        public SpeedSet()
        {
            //
        }

        /**
         * Returns the speed regarding the left lane.
         * @return speed regarding the left lane
         */
        public final Speed getLeft()
        {
            return Speed.ofSI(this.left);
        }

        /**
         * Returns the speed regarding the current lane.
         * @return speed regarding the current lane
         */
        public final Speed getCurrent()
        {
            return Speed.ofSI(this.current);
        }

        /**
         * Returns the speed regarding the right lane.
         * @return speed regarding the right lane
         */
        public final Speed getRight()
        {
            return Speed.ofSI(this.right);
        }
    }

}
