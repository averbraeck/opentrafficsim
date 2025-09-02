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
import java.util.function.BiFunction;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
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
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.Colorer;
import org.opentrafficsim.draw.colorer.trajectory.TrajectoryColorer;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Plot of trajectories along a path.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrajectoryPlot extends AbstractSamplerPlot implements XYDataset
{
    /** Single shape to provide due to non-null requirement, but actually not used. */
    private static final Shape NO_SHAPE = new Line2D.Float(0, 0, 0, 0);

    /** Color map for multiple curves. */
    private static final Color[] COLORMAP;

    /** Strokes. */
    private static final BasicStroke[] STROKES;

    /** Shape for the legend entries to draw the line over. */
    private static final Shape LEGEND_LINE = new CubicCurve2D.Float(-20, 7, -10, -7, 0, 7, 20, -7);

    /** Updater for update times. */
    private final GraphUpdater<Time> graphUpdater;

    /** Counter of the number of trajectories imported per lane. */
    private final Map<LaneData<?>, Integer> knownTrajectories = new LinkedHashMap<>();

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

    /** Colorer. */
    private Colorer<? super TrajectorySection> colorer;

    /** Line renderer. */
    private XYLineAndShapeRendererColor renderer;

    static
    {
        Color[] c = Colors.hue(6);
        COLORMAP = new Color[] {c[0], c[4], c[2], c[1], c[3], c[5]};
        float lw = 0.4f;
        STROKES = new BasicStroke[] {new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f),
                new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] {13f, 4f}, 0.0f),
                new BasicStroke(lw, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[] {11f, 3f, 2f, 3f}, 0.0f)};
    }

    /**
     * Constructor.
     * @param caption caption
     * @param updateInterval regular update interval (simulation time)
     * @param scheduler scheduler.
     * @param samplerData sampler data
     * @param path path
     */
    public TrajectoryPlot(final String caption, final Duration updateInterval, final PlotScheduler scheduler,
            final SamplerData<?> samplerData, final GraphPath<? extends LaneData<?>> path)
    {
        super(caption, updateInterval, scheduler, samplerData, path, Duration.ZERO);
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
            for (Section<? extends LaneData<?>> section : path.getSections())
            {
                Length startDistance = path.getStartDistance(section);
                for (int i = 0; i < path.getNumberOfSeries(); i++)
                {
                    LaneData<?> lane = section.getSource(i);
                    if (lane == null)
                    {
                        continue; // lane is not part of this section, e.g. after a lane-drop
                    }
                    TrajectoryGroup<?> trajectoryGroup = getSamplerData().getTrajectoryGroup(lane);
                    int from = this.knownTrajectories.getOrDefault(lane, 0);
                    int to = trajectoryGroup.size();
                    double scaleFactor = section.length().si / lane.getLength().si;
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
                        this.curves.get(i).add(new OffsetTrajectory(trajectory, startDistance, scaleFactor));
                    }
                    this.knownTrajectories.put(lane, to);
                }
            }
        });
    }

    /**
     * Create a chart.
     * @return chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("Time [s] \u2192");
        NumberAxis yAxis = new NumberAxis("Distance [m] \u2192");
        this.renderer = new XYLineAndShapeRendererColor();
        XYPlot plot = new XYPlot(this, xAxis, yAxis, this.renderer);
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

    /**
     * Sets the color renderer for trajectories.
     * @param colorer color renderer
     */
    public void setColorer(final TrajectoryColorer colorer)
    {
        this.colorer = colorer;
        this.renderer.setDrawSeriesLineAsPath(colorer.isSingleColor());
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.TRAJECTORY;
    }

    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return String.format("time %.0fs, distance %.0fm", domainValue, rangeValue);
    }

    @Override
    protected void increaseTime(final Time time)
    {
        if (this.graphUpdater != null) // null during construction
        {
            this.graphUpdater.offer(time);
        }
    }

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

    /**
     * Returns the number of lanes.
     * @return the number of lanes
     */
    public int getLaneCount()
    {
        return this.curves.size();
    }

    @Override
    public Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    @Override
    public int getItemCount(final int series)
    {
        OffsetTrajectory trajectory = getTrajectory(series);
        return trajectory == null ? 0 : trajectory.size();
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        return getTrajectory(series).getT(item);
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        return getTrajectory(series).getX(item);
    }

    /**
     * Get the trajectory of the series number.
     * @param series series
     * @return trajectory of the series number
     */
    private OffsetTrajectory getTrajectory(final int series)
    {
        int[] n = getLaneAndSeriesNumber(series);
        return this.curves.get(n[0]).get(n[1]);
    }

    /**
     * Returns the lane number, and series number within the lane data.
     * @param series overall series number
     * @return lane number, and series number within the lane data
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
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private final class XYLineAndShapeRendererColor extends XYLineAndShapeRenderer
    {
        /** */
        private static final long serialVersionUID = 20181014L;

        /**
         * Constructor.
         */
        XYLineAndShapeRendererColor()
        {
            super(false, true);
            setDefaultLinesVisible(true);
            setDefaultShapesVisible(false);
            setDrawSeriesLineAsPath(true);
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public boolean isSeriesVisible(final int series)
        {
            int[] n = getLaneAndSeriesNumber(series);
            return TrajectoryPlot.this.laneVisible.get(n[0]);
        }

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

        @SuppressWarnings("synthetic-access")
        @Override
        public Paint getSeriesPaint(final int series)
        {
            int[] n = getLaneAndSeriesNumber(series);
            return COLORMAP[n[0] % COLORMAP.length];
        }

        @Override
        public Paint getItemPaint(final int row, final int column)
        {
            if (TrajectoryPlot.this.colorer == null)
            {
                return getSeriesPaint(row);
            }
            return TrajectoryPlot.this.colorer.getColor(new TrajectorySection(getTrajectory(row), column));
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

        @Override
        public String toString()
        {
            return "XYLineAndShapeRendererID []";
        }

    }

    /**
     * Trajectory colorer.
     */
    public abstract static class TrajectoryColorerXXX implements BiFunction<OffsetTrajectory, Integer, Color>
    {

        /** Blue colorer. */
        public static final TrajectoryColorerXXX BLUE = new TrajectoryColorerXXX(true)
        {
            @Override
            public Color apply(final OffsetTrajectory t, final Integer u)
            {
                return Color.BLUE;
            }
        };

        /** Id colorer. */
        public static final TrajectoryColorerXXX ID = new TrajectoryColorerXXX(true)
        {
            @Override
            public Color apply(final OffsetTrajectory t, final Integer u)
            {
                String gtuId = t.getGtuId();
                for (int pos = gtuId.length(); --pos >= 0;)
                {
                    Character c = gtuId.charAt(pos);
                    if (Character.isDigit(c))
                    {
                        return Colors.getEnumerated(c - '0');
                    }
                }
                return Color.CYAN;
            }
        };

        /** Speed colorer. */
        public static final TrajectoryColorerXXX SPEED = new TrajectoryColorerXXX(false)
        {
            /** Color scale. */
            private static final BoundsPaintScale SCALE = new BoundsPaintScale(
                    new double[] {0.0, 30.0 / 3.6, 60.0 / 3.6, 90.0 / 3.6, 120.0 / 3.6}, Colors.reverse(Colors.GREEN_RED_DARK));

            @Override
            public Color apply(final OffsetTrajectory t, final Integer u)
            {
                return SCALE.getPaint(t.getV(u));
            }
        };

        /** Acceleration colorer. */
        public static final TrajectoryColorerXXX ACCELERATION = new TrajectoryColorerXXX(false)
        {
            /** Color scale. */
            private static final BoundsPaintScale SCALE = new BoundsPaintScale(new double[] {-6.0, -4.0, -2.0, 0.0, 1.0, 2.0},
                    new Color[] {Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE});

            @Override
            public Color apply(final OffsetTrajectory t, final Integer u)
            {
                return SCALE.getPaint(t.getA(u));
            }
        };

        /** Whether this colorer has one color per trajectory. */
        private final boolean singleColor;

        /**
         * Constructor.
         * @param singleColor whether this colorer has one color per trajectory
         */
        public TrajectoryColorerXXX(final boolean singleColor)
        {
            this.singleColor = singleColor;
        }

        /**
         * Whether the trajectory of a GTU is a single color. By default this is false.
         * @return whether the trajectory of a GTU is a single color
         */
        public boolean isSingleColor()
        {
            return this.singleColor;
        }
    }

    /**
     * Colorer based on extended data in trajectory.
     * @param <T> extended data value type
     */
    public static class TrajectoryColorerExtended<T> extends TrajectoryColorerXXX
    {

        /** Extended data type. */
        private final ExtendedDataType<? extends T, ?, ?, ?> dataType;

        /** Coloring function. */
        private final Function<T, Color> colorFunction;

        /**
         * Constructor.
         * @param singleColor whether this colorer has one color per trajectory
         * @param dataType extended data type
         * @param colorFunction coloring function
         */
        public TrajectoryColorerExtended(final boolean singleColor, final ExtendedDataType<? extends T, ?, ?, ?> dataType,
                final Function<T, Color> colorFunction)
        {
            super(singleColor);
            this.dataType = dataType;
            this.colorFunction = colorFunction;
        }

        @Override
        public Color apply(final OffsetTrajectory t, final Integer u)
        {
            return this.colorFunction.apply(t.getValue(u, this.dataType));
        }

    }

    @Override
    public String toString()
    {
        return "TrajectoryPlot [graphUpdater=" + this.graphUpdater + ", knownTrajectories=" + this.knownTrajectories
                + ", curves=" + this.curves + ", strokes=" + this.strokes + ", curvesPerLane=" + this.curvesPerLane
                + ", legend=" + this.legend + ", laneVisible=" + this.laneVisible + "]";
    }

    /**
     * Retrieve the legend.
     * @return the legend
     */
    public LegendItemCollection getLegend()
    {
        return this.legend;
    }

    /**
     * Retrieve the lane visibility flags.
     * @return the lane visibility flags
     */
    public List<Boolean> getLaneVisible()
    {
        return this.laneVisible;
    }

}
