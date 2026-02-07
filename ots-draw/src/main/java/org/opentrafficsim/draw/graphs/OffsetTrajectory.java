package org.opentrafficsim.draw.graphs;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Class containing a trajectory with an offset. Takes care of bits that are before and beyond the lane without affecting the
 * trajectory itself.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OffsetTrajectory
{

    /** The trajectory. */
    private final Trajectory<?> trajectory;

    /** The offset. */
    private final double offset;

    /** Scale factor for space dimension. */
    private final double scaleFactor;

    /**
     * Construct a new TrajectoryAndLengthOffset object.
     * @param trajectory the trajectory
     * @param offset the length from the beginning of the sampled path to the start of the lane to which the trajectory belongs
     * @param scaleFactor scale factor for space dimension
     */
    OffsetTrajectory(final Trajectory<?> trajectory, final Length offset, final double scaleFactor)
    {
        this.trajectory = trajectory;
        this.offset = offset.si;
        this.scaleFactor = scaleFactor;
    }

    /**
     * Returns the number of measurements in the trajectory.
     * @return number of measurements in the trajectory
     */
    public int size()
    {
        return this.trajectory.size();
    }

    /**
     * Returns the location, including offset, of an item.
     * @param item item (sample) number
     * @return location, including offset, of an item
     */
    public double getX(final int item)
    {
        return this.offset + this.trajectory.getX(item) * this.scaleFactor;
    }

    /**
     * Returns the time of an item.
     * @param item item (sample) number
     * @return time of an item
     */
    public double getT(final int item)
    {
        return (double) this.trajectory.getT(item);
    }

    /**
     * Returns the speed of an item.
     * @param item item (sample) number
     * @return speed of an item
     */
    public double getV(final int item)
    {
        return (double) this.trajectory.getV(item);
    }

    /**
     * Returns the acceleration of an item.
     * @param item item (sample) number
     * @return acceleration of an item
     */
    public double getA(final int item)
    {
        return (double) this.trajectory.getA(item);
    }

    /**
     * Returns value of an extended data type.
     * @param <T> value type
     * @param item item (sample) number
     * @param dataType extended data type
     * @return value of extended data type
     */
    public <T> T getValue(final int item, final ExtendedDataType<? extends T, ?, ?, ?> dataType)
    {
        return this.trajectory.getExtendedData(dataType, item);
    }

    /**
     * Returns the ID of the GTU of this trajectory.
     * @return the ID of the GTU of this trajectory
     */
    public String getGtuId()
    {
        return this.trajectory.getGtuId();
    }

    @Override
    public String toString()
    {
        return "OffsetTrajectory [trajectory=" + this.trajectory + ", offset=" + this.offset + "]";
    }

    /**
     * Section in trajectory for which a color is required.
     * @param trajectory trajectory
     * @param section section index in trajectory
     */
    public record TrajectorySection(OffsetTrajectory trajectory, Integer section)
    {
    };

}
