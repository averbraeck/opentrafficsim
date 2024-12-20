package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.opentrafficsim.kpi.sampling.Trajectory;

/**
 * Contains some static utilities.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class GraphUtil
{

    /**
     * Constructor.
     */
    private GraphUtil()
    {
        // no instances
    }

    /**
     * Helper method for quick filtering of trajectories by checking if the time of the trajectory has overlap with the given
     * time.
     * @param trajectory trajectory
     * @param startTime start time
     * @param endTime end time
     * @return true if the trajectory should be considered for the given time
     */
    public static boolean considerTrajectory(final Trajectory<?> trajectory, final Time startTime, final Time endTime)
    {
        return trajectory.size() > 0 && trajectory.getT(0) < endTime.si
                && trajectory.getT(trajectory.size() - 1) > startTime.si;
    }

    /**
     * Helper method for quick filtering of trajectories by checking if the position of the trajectory has overlap with the
     * given range.
     * @param trajectory trajectory
     * @param startPosition start position
     * @param endPosition end position
     * @return true if the trajectory should be considered for the given time
     */
    public static boolean considerTrajectory(final Trajectory<?> trajectory, final Length startPosition,
            final Length endPosition)
    {
        return trajectory.size() > 0 && trajectory.getX(0) < startPosition.si
                && trajectory.getX(trajectory.size() - 1) > endPosition.si;
    }

    /**
     * Ensures that the given capacity is available in the array. The array may become or may be longer than the required
     * capacity. This method assumes that the array has non-zero length, and that the capacity required is at most 1 more than
     * what the array can provide.
     * @param data data array
     * @param capacity required capacity
     * @return array with at least the requested capacity
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
     * @param data data array
     * @param capacity required capacity
     * @return array with at least the requested capacity
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
     * @param data data array
     * @param capacity required capacity
     * @return array with at least the requested capacity
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

    /**
     * Returns a chart listener that allows the series to be enabled and disabled by clicking on the respective legend item.
     * @param legend legend
     * @param visibility visibility of each series; the listener will store visibility in this list, which an
     *            {@code AbstractRenderer} can use in {@code isSeriesVisible(series)} to show or hide the series
     * @param <K> underlying key type of the series
     * @return listener that will allow series to be enabled and disabled by clicking on the respective legend item
     */
    @SuppressWarnings("unchecked")
    public static <K> ChartMouseListener getToggleSeriesByLegendListener(final LegendItemCollection legend,
            final List<Boolean> visibility)
    {
        Map<K, Paint> colors = new LinkedHashMap<>();
        Map<K, Integer> series = new LinkedHashMap<>();
        for (int i = 0; i < legend.getItemCount(); i++)
        {
            LegendItem legendItem = legend.get(i);
            colors.put((K) legendItem.getSeriesKey(), legendItem.getFillPaint());
            series.put((K) legendItem.getSeriesKey(), i);
            legendItem.setToolTipText("Click to show/hide");
        }
        if (legend.getItemCount() < 2)
        {
            return null;
        }
        return new ChartMouseListener()
        {
            @Override
            public void chartMouseClicked(final ChartMouseEvent event)
            {
                if (event.getEntity() instanceof LegendItemEntity)
                {
                    K key = (K) ((LegendItemEntity) event.getEntity()).getSeriesKey();
                    int s = series.get(key);
                    boolean visible = !visibility.get(s);
                    visibility.set(s, visible);
                    legend.get(s).setLabelPaint(visible ? Color.BLACK : Color.LIGHT_GRAY);
                    legend.get(s).setFillPaint(visible ? colors.get(key) : Color.LIGHT_GRAY);
                }
            }

            @Override
            public void chartMouseMoved(final ChartMouseEvent event)
            {
                //
            }
        };
    }
}
