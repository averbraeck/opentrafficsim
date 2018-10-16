package org.opentrafficsim.graphs;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

/**
 * Plot of trajectories along a path.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class XTrajectoryPlot extends XAbstractSamplerPlot implements XYDataset
{

    /** */
    private static final long serialVersionUID = 20181013L;

    /** Single shape to provide due to non-null requirement, but actually not used. */
    private static final Shape NO_SHAPE = new Line2D.Float(0, 0, 0, 0);

    /** Updater for update times. */
    private final XGraphUpdater<Time> graphUpdater;

    /** Counter of the number of trajectories imported per lane. */
    private final Map<KpiLaneDirection, Integer> knownTrajectories = new LinkedHashMap<>();

    /** Mapping from series rank number to trajectory. */
    private List<OffsetTrajectory> curves = new ArrayList<>();

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler RoadSampler; road sampler
     * @param path List&lt;LaneDirection&gt;; path
     */
    public XTrajectoryPlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final RoadSampler sampler, final List<LaneDirection> path)
    {
        super(caption, updateInterval, simulator, sampler, path, Duration.ZERO);
        setChart(createChart());

        // setup updater to do the actual work in another thread
        this.graphUpdater = new XGraphUpdater<>("Trajectories worker", Thread.currentThread(), (t) -> 
        {
            for (KpiLaneDirection lane : getPath())
            {
                TrajectoryGroup trajectoryGroup = getSampler().getTrajectoryGroup(lane);
                int from = this.knownTrajectories.getOrDefault(lane, 0);
                int to = trajectoryGroup.size();
                for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories().subList(from, to))
                {
                    this.curves.add(new OffsetTrajectory(trajectory, getStartDistance(lane)));
                }
                this.knownTrajectories.put(lane, to);
            }
        });
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("\u2192 " + "Time [s]");
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        XYLineAndShapeRendererID renderer = new XYLineAndShapeRendererID();
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        plot.setFixedLegendItems(null);
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.TRAJECTORY;
    }

    /** {@inheritDoc} */
    @Override
    protected String getStatusLabel(final double domainValue, final double rangeValue)
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
        return this.curves.size();
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
        return this.curves.get(series).size();
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
        return this.curves.get(series).getT(item);
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
        return this.curves.get(series).getX(item);
    }

    /**
     * Extension of a line renderer to select a color based on GTU ID, and to overrule an unused shape to save memory.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        }

        /** {@inheritDoc} */
        @Override
        public Paint getItemPaint(final int row, final int col)
        {
            @SuppressWarnings("synthetic-access")
            String gtuId = XTrajectoryPlot.this.curves.get(row).getGtuId();
            int colorIndex = 0;
            for (int pos = gtuId.length(); --pos >= 0;)
            {
                Character c = gtuId.charAt(pos);
                if (Character.isDigit(c))
                {
                    colorIndex = c - '0';
                    break;
                }
            }
            return IDGTUColorer.LEGEND.get(colorIndex).getColor();
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
    }

    /**
     * Class containing a trajectory with an offset.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class OffsetTrajectory
    {
        /** The trajectory. */
        private final Trajectory<?> trajectory;

        /** The offset. */
        private final double offset;

        /**
         * Construct a new TrajectoryAndLengthOffset object.
         * @param trajectory org.opentrafficsim.kpi.sampling.Trajectory; the trajectory
         * @param offset Length; the length from the beginning of the sampled path to the start of the lane to which the
         *            trajectory belongs
         */
        OffsetTrajectory(final Trajectory<?> trajectory, final Length offset)
        {
            this.trajectory = trajectory;
            this.offset = offset.si;
        }

        /**
         * Returns the number of measurements in the trajectory.
         * @return int; number of measurements in the trajectory
         */
        public final int size()
        {
            return this.trajectory.size();
        }

        /**
         * Returns the location, including offset, of an item.
         * @param item int; item (sample) number
         * @return double; location, including offset, of an item
         */
        public final double getX(final int item)
        {
            return Try.assign(() -> this.offset + this.trajectory.getX(item),
                    "Unexpected exception while obtaining location value from trajectory for plotting.");
        }

        /**
         * Returns the time of an item.
         * @param item int; item (sample) number
         * @return double; time of an item
         */
        public final double getT(final int item)
        {
            return Try.assign(() -> (double) this.trajectory.getT(item),
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

}
