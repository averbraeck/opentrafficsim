package org.opentrafficsim.animation.data;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.gtu.PerceptionAnimation.PerceptionData;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelFuller;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelTask;
import org.opentrafficsim.road.network.conflict.Conflict;

/**
 * Animation data for perception based on a GTU.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AnimationPerceptionData implements PerceptionData
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public AnimationPerceptionData(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    @Override
    public Length getDeltaCenterX()
    {
        return this.gtu.getCenter().dx();
    }

    @Override
    public Set<ChannelData> getChannels()
    {
        Set<ChannelData> set = new LinkedHashSet<>();
        Optional<Mental> mental = this.gtu.getTacticalPlanner().getPerception().getMental();
        if (mental.isPresent() && mental.get() instanceof ChannelFuller fuller)
        {
            for (Object channel : fuller.getChannels())
            {
                double attention;
                Duration perceptionDelay;
                try
                {
                    attention = fuller.getAttention(channel);
                    perceptionDelay = fuller.getPerceptionDelay(channel);
                }
                catch (NoSuchElementException ex)
                {
                    // Perception model just cleared its internal state
                    attention = 0.0;
                    perceptionDelay = Duration.ZERO;
                }

                double angle;
                ChannelRadius radius = ChannelRadius.NEAR;
                if (ChannelTask.LEFT.equals(channel))
                {
                    angle = Math.PI / 2.0;
                }
                else if (ChannelTask.FRONT.equals(channel))
                {
                    angle = 0.0;
                }
                else if (ChannelTask.RIGHT.equals(channel))
                {
                    angle = -Math.PI / 2.0;
                }
                else if (ChannelTask.REAR.equals(channel))
                {
                    angle = Math.PI;
                }
                else if (ChannelTask.IN_VEHICLE.equals(channel))
                {
                    angle = 0.0;
                    radius = ChannelRadius.ZERO;
                }
                else if (channel instanceof OtsShape object)
                {
                    Point2d point;
                    if (channel instanceof Conflict conflict)
                    {
                        // on a conflict we take a point 25m upstream, or the upstream conflicting node if closer
                        double x = conflict.getOtherConflict().getLongitudinalPosition().si - 25.0;
                        point = conflict.getOtherConflict().getLane().getCenterLine().getLocationExtended(x < 0.0 ? 0.0 : x);
                    }
                    else
                    {
                        point = object.getLocation();
                    }
                    angle = AngleUtil
                            .normalizeAroundZero(this.gtu.getLocation().directionTo(point) - this.gtu.getLocation().dirZ);
                    radius = ChannelRadius.FAR;
                }
                else
                {
                    continue;
                }
                set.add(new ChannelData(Angle.ofSI(angle), radius, attention, perceptionDelay));
            }
        }
        return set;
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.gtu.getLocation();
    }

    @Override
    public double getZ()
    {
        return DrawLevel.LABEL.getZ();
    }

    @Override
    public Polygon2d getRelativeContour()
    {
        return this.gtu.getRelativeContour();
    }

    @Override
    public String toString()
    {
        return "AnimationPerceptionData [gtu=" + this.gtu.getId() + "]";
    }

}
