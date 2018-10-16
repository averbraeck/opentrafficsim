package org.opentrafficsim.graphs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.Trajectory;

/**
 * Contains some static utilities.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class XPlotUtil
{

    /**
     * Constructor.
     */
    private XPlotUtil()
    {
        // no instances
    }

    /**
     * Helper method for quick filtering of trajectories by checking if the time of the trajectory has overlap with the given
     * time.
     * @param trajectory Trajectory; trajectory
     * @param startTime Time; start time
     * @param endTime Time; end time
     * @return boolean; true if the trajectory should be considered for the given time
     */
    public static boolean considerTrajectory(final Trajectory<?> trajectory, final Time startTime, final Time endTime)
    {
        try
        {
            return trajectory.getT(0) < endTime.si && trajectory.getT(trajectory.size() - 1) > startTime.si;
        }
        catch (SamplingException exception)
        {
            throw new RuntimeException("Unexpected exception while checking whether the trajectory should be considered.",
                    exception);
        }
    }

    /**
     * Helper method for quick filtering of trajectories by checking if the position of the trajectory has overlap with the
     * given range.
     * @param trajectory Trajectory; trajectory
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @return boolean; true if the trajectory should be considered for the given time
     */
    public static boolean considerTrajectory(final Trajectory<?> trajectory, final Length startPosition,
            final Length endPosition)
    {
        try
        {
            return trajectory.getX(0) < startPosition.si && trajectory.getX(trajectory.size() - 1) > endPosition.si;
        }
        catch (SamplingException exception)
        {
            throw new RuntimeException("Unexpected exception while checking whether the trajectory should be considered.",
                    exception);
        }
    }

    /**
     * Ensures that the given capacity is available in the array. The array may become or may be longer than the required
     * capacity. This method assumes that the array has non-zero length, and that the capacity required is at most 1 more than
     * what the array can provide.
     * @param data double[]; data array
     * @param capacity int; required capacity
     * @return double[]; array with at least the requested capacity
     */
    public static double[] ensureCapacity(final double[] data, final int capacity)
    {
        if (data.length < capacity)
        {
            double[] out = new double[data.length + (data.length >> 1)];
            System.arraycopy(data, 0, out, 0, data.length);
            return out;
        }
        return data;
    }

    /**
     * Ensures that the given capacity is available in the array. The array may become or may be longer than the required
     * capacity. This method assumes that the array has non-zero length, and that the capacity required is at most 1 more than
     * what the array can provide.
     * @param data float[]; data array
     * @param capacity int; required capacity
     * @return float[]; array with at least the requested capacity
     */
    public static float[] ensureCapacity(final float[] data, final int capacity)
    {
        if (data.length < capacity)
        {
            float[] out = new float[data.length + (data.length >> 1)];
            System.arraycopy(data, 0, out, 0, data.length);
            return out;
        }
        return data;
    }

    /**
     * Ensures that the given capacity is available in the array. The array may become or may be longer than the required
     * capacity. This method assumes that the array has non-zero length, and that the capacity required is at most 1 more than
     * what the array can provide.
     * @param data int[]; data array
     * @param capacity int; required capacity
     * @return int[]; array with at least the requested capacity
     */
    public static int[] ensureCapacity(final int[] data, final int capacity)
    {
        if (data.length < capacity)
        {
            int[] out = new int[data.length + (data.length >> 1)];
            System.arraycopy(data, 0, out, 0, data.length);
            return out;
        }
        return data;
    }
}
