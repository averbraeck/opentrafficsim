package org.opentrafficsim.draw.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.animation.gtu.colorer.IdGtuColorer;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Plot of trajectories along a path.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TrajectoryPlot extends AbstractSamplerPlot implements XYDataset
{
    /** Single shape to provide due to non-null requirement, but actually not used. */
    private static final Shape NO_SHAPE = new Line2D.Float(0, 0, 0, 0);

    /** Color map. */
    private static final Color[] COLORMAP;

    /** Strokes. */
    private static final BasicStroke[] STROKES;

    /** Shape for the legend entries to draw the line over. */
    private static final Shape LEGEND_LINE = new CubicCurve2D.Float(-20, 7, -10, -7, 0, 7, 20, -7);

    /** Updater for update times. */
    private final GraphUpdater<Time> graphUpdater;

    /** Counter of the number of trajectories imported per lane. */
    private final Map<LaneData, Integer> knownTrajectories = new LinkedHashMap<>();

    /** Per lane, mapping from series rank number to trajectory. */
    private List<List<OffsetTrajectory>> curves = new ArrayList<>();

    /** Stroke per series. */
    private List<List<Stroke>> strokes = new ArrayList<>();

    /** Number of curves per lane. This may be less than the length of {@code List<OffsetTrajectory>} due to concurrency. */
    private List<Integer> curvesPerLane = new ArrayList<>();

    /** Legend to change text color to indicate visibility. */
    private LegendItemCollection legend;

    /** Whether each lane is visible or not. */
    private final List<Boolean> laneVisible = new ArrayList<>();

    static
    {
        Color[] c = BoundsPaintScale.hue(6);
        COLORMAP = new Color[] {c[0], c[4], c[2], c[1], c[3], c[5]};
        float lw = 0.4f;
        STROKES = new BasicStroke[] {new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f),
                new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] {13f, 4f}, 0.0f),
                new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] {11f, 3f, 2f, 3f}, 0.0f)};
    }

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OtsSimulatorInterface; simulator
     * @param samplerData SamplerData&lt;?&gt;; sampler data
     * @param path GraphPath&lt;? extends LaneData&gt;; path
     */
    public TrajectoryPlot(final String caption, final Duration updateInterval, final OtsSimulatorInterface simulator,
            final SamplerData<?> samplerData, final GraphPath<? extends LaneData> path)
    {
        super(caption, updateInterval, simulator, samplerData, path, Duration.ZERO);
        for (int i = 0; i < path.getNumberOfSeries(); i++)
        {
            this.curves.add(new ArrayList<>());
            this.strokes.add(new ArrayList<>());
            this.curvesPerLane.add(0);
            this.laneVisible.add(true);
        }
        setChart(createChart());

        // setup updater to do the actual work in another thread
        this.graphUpdater = new GraphUpdater<>("Trajectories worker", Thread.currentThread(), (t) ->
        {
            for (Section<? extends LaneData> section : path.getSections())
            {
                Length startDistance = path.getStartDistance(section);
                for (int i = 0; i < path.getNumberOfSeries(); i++)
                {
                    LaneData lane = section.getSource(i);
                    if (lane == null)
                    {
                        continue; // lane is not part of this section, e.g. after a lane-drop
                    }
                    TrajectoryGroup<?> trajectoryGroup = getSamplerData().getTrajectoryGroup(lane);
                    int from = this.knownTrajectories.getOrDefault(lane, 0);
                    int to = trajectoryGroup.size();
                    double scaleFactor = section.getLength().si / lane.getLength().si;
                    for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories().subList(from, to))
                    {
                        if (getPath().getNumberOfSeries() > 1)
                        {
                            // assign a stroke with random offset, otherwise it will look artificial
                            BasicStroke stroke = STROKES[i % STROKES.length];
                            if (stroke.getDashArray() != null)
                            {
                                float dashLength = 0.0f;
                                for (float d : stroke.getDashArray())
                                {
                                    dashLength += d;
                                }
                                stroke = new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(),
                                        stroke.getMiterLimit(), stroke.getDashArray(), (float) (Math.random() * dashLength));
                            }
                            this.strokes.get(i).add(stroke);
                        }
                        this.curves.get(i).add(new OffsetTrajectory(trajectory, startDistance, scaleFactor, lane.getLength()));
                    }
                    this.knownTrajectories.put(lane, to);
                }
            }
        });
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("Time [s] \u2192");
        NumberAxis yAxis = new NumberAxis("Distance [m] \u2192");
        XYLineAndShapeRendererID renderer = new XYLineAndShapeRendererID();
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        boolean showLegend;
        if (getPath().getNumberOfSeries() < 2)
        {
            plot.setFixedLegendItems(null);
            showLegend = false;
        }
        else
        {
            this.legend = new LegendItemCollection();
            for (int i = 0; i < getPath().getNumberOfSeries(); i++)
            {
                LegendItem li = new LegendItem(getPath().getName(i));
                li.setSeriesKey(i); // lane series, not curve series
                li.setShape(STROKES[i & STROKES.length].createStrokedShape(LEGEND_LINE));
                li.setFillPaint(COLORMAP[i % COLORMAP.length]);
                this.legend.add(li);
            }
            plot.setFixedLegendItems(this.legend);
            showLegend = true;
        }
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.TRAJECTORY;
    }

    /** {@inheritDoc} */
    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return String.format("time %.0fs, distance %.0fm", domainValue, rangeValue);
    }

    /** {@inheritDoc} */
    @Override
    protected void increaseTime(final Time time)
    {
        if (this.graphUpdater != null) // null during construction
        {
            this.graphUpdater.offer(time);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getSeriesCount()
    {
        int n = 0;
        for (int i = 0; i < this.curves.size(); i++)
        {
            List<OffsetTrajectory> list = this.curves.get(i);
            int m = list.size();
            this.curvesPerLane.set(i, m);
            n += m;
        }
        return n;
    }

    /** {@inheritDoc} */
    @Override
    public Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /** {@inheritDoc} */
    @Override
    public int getItemCount(final int series)
    {
        OffsetTrajectory trajectory = getTrajectory(series);
        return trajectory == null ? 0 : trajectory.size();
    }

    /** {@inheritDoc} */
    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public double getXValue(final int series, final int item)
    {
        return getTrajectory(series).getT(item);
    }

    /** {@inheritDoc} */
    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public double getYValue(final int series, final int item)
    {
        return getTrajectory(series).getX(item);
    }

    /**
     * Get the trajectory of the series number.
     * @param series int; series
     * @return OffsetTrajectory; trajectory of the series number
     */
    private OffsetTrajectory getTrajectory(final int series)
    {
        int[] n = getLaneAndSeriesNumber(series);
        return this.curves.get(n[0]).get(n[1]);
    }

    /**
     * Returns the lane number, and series number within the lane data.
     * @param series int; overall series number
     * @return int[]; lane number, and series number within the lane data
     */
    private int[] getLaneAndSeriesNumber(final int series)
    {
        int n = series;
        for (int i = 0; i < this.curves.size(); i++)
        {
            int m = this.curvesPerLane.get(i);
            if (n < m)
            {
                return new int[] {i, n};
            }
            n -= m;
        }
        throw new RuntimeException("Discrepancy between series number and available data.");
    }

    /**
     * Extension of a line renderer to select a color based on GTU ID, and to overrule an unused shape to save memory.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private final class XYLineAndShapeRendererID extends XYLineAndShapeRenderer
    {
        /** */
        private static final long serialVersionUID = 20181014L;

        /**
         * Constructor.
         */
        XYLineAndShapeRendererID()
        {
            super(false, true);
            setDefaultLinesVisible(true);
            setDefaultShapesVisible(false);
            setDrawSeriesLineAsPath(true);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public boolean isSeriesVisible(final int series)
        {
            int[] n = getLaneAndSeriesNumber(series);
            return TrajectoryPlot.this.laneVisible.get(n[0]);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public Stroke getSeriesStroke(final int series)
        {
            if (TrajectoryPlot.this.curves.size() == 1)
            {
                return STROKES[0];
            }
            int[] n = getLaneAndSeriesNumber(series);
            return TrajectoryPlot.this.strokes.get(n[0]).get(n[1]);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public Paint getSeriesPaint(final int series)
        {
            if (TrajectoryPlot.this.curves.size() == 1)
            {
                String gtuId = getTrajectory(series).getGtuId();
                for (int pos = gtuId.length(); --pos >= 0;)
                {
                    Character c = gtuId.charAt(pos);
                    if (Character.isDigit(c))
                    {
                        return IdGtuColorer.LEGEND.get(c - '0').getColor();
                    }
                }
            }
            int[] n = getLaneAndSeriesNumber(series);
            return COLORMAP[n[0] % COLORMAP.length];
        }

        /**
         * {@inheritDoc} Largely based on the super implementation, but returns a dummy shape for markers to save memory and as
         * markers are not used.
         */
        @SuppressWarnings("synthetic-access")
        @Override
        protected void addEntity(final EntityCollection entities, final Shape hotspot, final XYDataset dataset,
                final int series, final int item, final double entityX, final double entityY)
        {

            if (!getItemCreateEntity(series, item))
            {
                return;
            }

            // if not hotspot is provided, we create a default based on the
            // provided data coordinates (which are already in Java2D space)
            Shape hotspot2 = hotspot == null ? NO_SHAPE : hotspot;
            String tip = null;
            XYToolTipGenerator generator = getToolTipGenerator(series, item);
            if (generator != null)
            {
                tip = generator.generateToolTip(dataset, series, item);
            }
            String url = null;
            if (getURLGenerator() != null)
            {
                url = getURLGenerator().generateURL(dataset, series, item);
            }
            XYItemEntity entity = new XYItemEntity(hotspot2, dataset, series, item, tip, url);
            entities.add(entity);
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "XYLineAndShapeRendererID []";
        }

    }

    /**
     * Class containing a trajectory with an offset. Takes care of bits that are before and beyond the lane without affecting
     * the trajectory itself.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class OffsetTrajectory
    {
        /** The trajectory. */
        private final Trajectory<?> trajectory;

        /** The offset. */
        private final double offset;

        /** Scale factor for space dimension. */
        private final double scaleFactor;

        /** First index of the trajectory to include, possibly cutting some measurements before the lane. */
        private int first;

        /** Size of the trajectory to consider starting at first, possibly cutting some measurements beyond the lane. */
        private int size;

        /** Length of the lane to determine {@code size}. */
        private final Length laneLength;

        /**
         * Construct a new TrajectoryAndLengthOffset object.
         * @param trajectory Trajectory&lt;?&gt;; the trajectory
         * @param offset Length; the length from the beginning of the sampled path to the start of the lane to which the
         *            trajectory belongs
         * @param scaleFactor double; scale factor for space dimension
         * @param laneLength Length; length of the lane
         */
        OffsetTrajectory(final Trajectory<?> trajectory, final Length offset, final double scaleFactor, final Length laneLength)
        {
            this.trajectory = trajectory;
            this.offset = offset.si;
            this.scaleFactor = scaleFactor;
            this.laneLength = laneLength;
        }

        /**
         * Returns the number of measurements in the trajectory.
         * @return int; number of measurements in the trajectory
         */
        public final int size()
        {
            // as trajectories grow, this calculation needs to be done on each request
            try
            {
                /*
                 * Note on overlap:
                 * 
                 * Suppose a GTU crosses a lane boundary producing the following events, where distance e->| is the front, and
                 * |->l is the tail, relative to the reference point of the GTU.
                 * @formatter:off
                 * -------------------------------------------  o) regular move event
                 *  o     e   o         o |  l    o         o   e) lane enter event on next lane
                 * -------------------------------------------  l) lane leave event on previous lane
                 *  o         o         o   (l)                 measurements on previous lane
                 *       (e) (o)       (o)        o         o   measurements on next lane
                 * @formatter:on
                 * Trajectories of a particular GTU are not explicitly tied together. Not only would this involve quite some
                 * work, it is also impossible to distinguish a lane change near the start or end of a lane, from moving
                 * longitudinally on to the next lane. The basic idea to minimize overlap is to remove all positions on the
                 * previous lane beyond the lane length, and all negative positions on the next lane, i.e. all between ( ). This
                 * would however create a gap at the lane boundary '|'. Allowing one event beyond the lane length may still
                 * result in a gap, l->o in this case. Allowing one event before the lane would work in this case, but 'e' could
                 * also fall between an 'o' and '|'. At one trajectory it is thus not known whether the other trajectory
                 * continues from, or is continued from, the extra point. Hence we require an extra point before the lane and
                 * one beyond the lane to assure there is no gap. The resulting overlap can be as large as a move, but this is
                 * better than occasional gaps.
                 */
                int f = 0;
                while (f < this.trajectory.size() - 1 && this.trajectory.getX(f + 1) < 0.0)
                {
                    f++;
                }
                this.first = f;
                int s = this.trajectory.size() - 1;
                while (s > 1 && this.trajectory.getX(s - 1) > this.laneLength.si)
                {
                    s--;
                }
                this.size = s - f + 1;
            }
            catch (SamplingException exception)
            {
                throw new RuntimeException("Unexpected exception while obtaining location value from trajectory for plotting.",
                        exception);
            }
            return this.size;
        }

        /**
         * Returns the location, including offset, of an item.
         * @param item int; item (sample) number
         * @return double; location, including offset, of an item
         */
        public final double getX(final int item)
        {
            return Try.assign(() -> this.offset + this.trajectory.getX(this.first + item) * this.scaleFactor,
                    "Unexpected exception while obtaining location value from trajectory for plotting.");
        }

        /**
         * Returns the time of an item.
         * @param item int; item (sample) number
         * @return double; time of an item
         */
        public final double getT(final int item)
        {
            return Try.assign(() -> (double) this.trajectory.getT(this.first + item),
                    "Unexpected exception while obtaining time value from trajectory for plotting.");
        }

        /**
         * Returns the ID of the GTU of this trajectory.
         * @return String; the ID of the GTU of this trajectory
         */
        public final String getGtuId()
        {
            return this.trajectory.getGtuId();
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "OffsetTrajectory [trajectory=" + this.trajectory + ", offset=" + this.offset + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrajectoryPlot [graphUpdater=" + this.graphUpdater + ", knownTrajectories=" + this.knownTrajectories
                + ", curves=" + this.curves + ", strokes=" + this.strokes + ", curvesPerLane=" + this.curvesPerLane
                + ", legend=" + this.legend + ", laneVisible=" + this.laneVisible + "]";
    }

    /**
     * Retrieve the legend.
     * @return LegendItemCollection; the legend
     */
    public LegendItemCollection getLegend()
    {
        return this.legend;
    }

    /**
     * Retrieve the lane visibility flags.
     * @return List&lt;Boolean&gt;; the lane visibility flags
     */
    public List<Boolean> getLaneVisible()
    {
        return this.laneVisible;
    }

}
