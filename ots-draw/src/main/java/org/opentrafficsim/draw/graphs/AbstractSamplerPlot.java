package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Super class for plots that use sampler data. Sub classes may obtain trajectories using {@code getTrajectories()}, or
 * alternatively maintain some other -possibly more efficient- connection to the sampler. This class also connects the plot to a
 * path, consisting of a list of lanes. Start distance along the path for each lane is provided to sub classes using
 * {@code getStartDistance(LaneData)}. Total length is obtained using {@code getEndLocation()}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractSamplerPlot extends AbstractSpaceTimePlot
{

    /** Sampler data. */
    private final SamplerData<?> samplerData;

    /** KPI lane directions registered in the sampler. */
    private final GraphPath<? extends LaneData<?>> path;

    /** Time when trajectories were last updated per series in the path. */
    private List<Time> lastUpdateTime = new ArrayList<>();

    /** Cached trajectories per series in the path. */
    private List<List<TrajectoryGroup<?>>> trajectoriesCache = new ArrayList<>();

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param scheduler PlotScheduler; scheduler.
     * @param samplerData SamplerData&lt;?&gt;; sampler data
     * @param path GraphPath&lt;? extends LaneData&gt;; path
     * @param delay Duration; amount of time that chart runs behind simulation to prevent gaps in the charted data
     */
    public AbstractSamplerPlot(final String caption, final Duration updateInterval, final PlotScheduler scheduler,
            final SamplerData<?> samplerData, final GraphPath<? extends LaneData<?>> path, final Duration delay)
    {
        super(caption, updateInterval, scheduler, delay, DEFAULT_INITIAL_UPPER_TIME_BOUND);
        this.samplerData = samplerData;
        this.path = path;
        for (int i = 0; i < path.getNumberOfSeries(); i++)
        {
            this.trajectoriesCache.add(new ArrayList<>());
            this.lastUpdateTime.add(null);
        }
    }

    /**
     * Returns all trajectories for the series, in order of the path.
     * @param series int; series number
     * @return List&lt;TrajectoryGroup&gt;; the trajectories
     */
    protected List<TrajectoryGroup<?>> getTrajectories(final int series)
    {
        if (this.lastUpdateTime.get(series) == null || this.lastUpdateTime.get(series).lt(getUpdateTime()))
        {
            List<TrajectoryGroup<?>> cache = new ArrayList<>();
            for (Section<? extends LaneData<?>> section : getPath().getSections())
            {
                cache.add(this.samplerData.getTrajectoryGroup(section.getSource(series)));
            }
            this.trajectoriesCache.set(series, cache);
            this.lastUpdateTime.set(series, getUpdateTime());
        }
        return this.trajectoriesCache.get(series);
    }

    /**
     * Returns the path.
     * @return GraphPath&lt;? extends LaneData&gt;; the path
     */
    public final GraphPath<? extends LaneData<?>> getPath()
    {
        return this.path;
    }

    /** {@inheritDoc} */
    @Override
    protected final Length getEndLocation()
    {
        return getPath().getTotalLength();
    }

    /**
     * Returns the sampler data.
     * @return SamplerData&lt;?&gt;; sampler.
     */
    protected final SamplerData<?> getSamplerData()
    {
        return this.samplerData;
    }

}
