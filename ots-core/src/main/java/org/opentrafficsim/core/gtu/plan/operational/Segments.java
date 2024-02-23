package org.opentrafficsim.core.gtu.plan.operational;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;

/**
 * List of segments describing the longitudinal dynamics of a operational plan along the path.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Segments
{

    /** List if segments. */
    private final List<Segment> segments = new ArrayList<>();

    /**
     * Private, use method {@code off()} or {@code standStill()}.
     */
    private Segments()
    {
        //
    }

    /**
     * Add a constant acceleration segment. The start speed is set equal to the end speed of the previous segment. If the
     * acceleration reaches stand-still during the provided duration, two segments are added. The first with given negative
     * acceleration until stand-still, and a second stand-still segment for the remainder of the duration.
     * @param duration Duration; segment duration.
     * @param acceleration Acceleration; segment acceleration.
     */
    public void add(final Duration duration, final Acceleration acceleration)
    {
        Speed startSpeed = this.segments.get(this.segments.size() - 1).endSpeed();
        add(startSpeed, duration, acceleration);
    }

    /**
     * Adds a segment.
     * @param startSpeed Speed; start speed.
     * @param duration Duration; segment duration.
     * @param acceleration Acceleration; segment acceleration.
     */
    private void add(final Speed startSpeed, final Duration duration, final Acceleration acceleration)
    {
        if (acceleration.lt0() && startSpeed.si / -acceleration.si < duration.si)
        {
            Duration durationTillStandStill = Duration.instantiateSI(startSpeed.si / -acceleration.si);
            this.segments.add(new Segment(startSpeed, durationTillStandStill, acceleration));
            this.segments.add(Segment.standStill(duration.minus(durationTillStandStill)));
        }
        else
        {
            this.segments.add(new Segment(startSpeed, duration, acceleration));
        }
    }

    /**
     * Returns the number of segments.
     * @return int; number of segments.
     */
    public int size()
    {
        return this.segments.size();
    }

    /**
     * Returns a specific segment.
     * @param index int; segment index.
     * @return Segment; specific segment.
     */
    public Segment get(final int index)
    {
        return this.segments.get(index);
    }

    /**
     * Returns an immutable list of the segments.
     * @return ImmutableList&lt;Segment&gt;; segment list.
     */
    public ImmutableList<Segment> getSegments()
    {
        return new ImmutableArrayList<>(this.segments, Immutable.WRAP);
    }

    /**
     * Instantiates segments with a given start speed. Use {@code add} to add consecutive segments, who's start speed is set
     * equal to the end speed of the previous segment. If the acceleration reaches stand-still during the provided duration, two
     * segments are added. The first with given negative acceleration until stand-still, and a second stand-still segment for
     * the remainder of the duration.
     * @param startSpeed Speed; start speed of the first segment.
     * @param duration Duration; segment duration.
     * @param acceleration Acceleration; segment acceleration.
     * @return Segments; segments.
     */
    public static Segments off(final Speed startSpeed, final Duration duration, final Acceleration acceleration)
    {
        Segments segments = new Segments();
        segments.add(startSpeed, duration, acceleration);
        return segments;
    }

    /**
     * Create a stand-still segment. The start speed will be set to {@code Speed.ZERO} regardless of the end speed of the
     * previous segment in order to prevent drifting due to rounding errors.
     * @param duration Duration; segment duration.
     * @return Segment; stand-still segment.
     */
    public static Segments standStill(final Duration duration)
    {
        Segments segments = new Segments();
        segments.segments.add(Segment.standStill(duration));
        return segments;
    }

}
