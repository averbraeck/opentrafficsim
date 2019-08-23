package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Super class for plots that use sampler data. Sub classes may obtain trajectories using {@code getTrajectories()}, or
 * alternatively maintain some other -possibly more efficient- connection to the sampler. This class also connects the plot to a
 * path, consisting of a list of lanes. Start distance along the path for each lane is provided to sub classes using
 * {@code getStartDistance(KpiLaneDirection)}. Total length is obtained using {@code getEndLocation()}.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractSamplerPlot extends AbstractSpaceTimePlot
{

    /** Sampler. */
    private final Sampler<?> sampler;

    /** KPI lane directions registered in the sampler. */
    private final GraphPath<KpiLaneDirection> path;

    /** Time when trajectories were last updated per series in the path. */
    private List<Time> lastUpdateTime = new ArrayList<>();

    /** Cached trajectories per series in the path. */
    private List<List<TrajectoryGroup<?>>> trajectoriesCache = new ArrayList<>();

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler Sampler&lt;?&gt;; road sampler
     * @param path GraphPath&lt;KpiLaneDirection&gt;; path
     * @param delay Duration; amount of time that chart runs behind simulation to prevent gaps in the charted data
     */
    public AbstractSamplerPlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final Sampler<?> sampler, final GraphPath<KpiLaneDirection> path, final Duration delay)
    {
        super(caption, updateInterval, simulator, delay, DEFAULT_INITIAL_UPPER_TIME_BOUND);
        this.sampler = sampler;
        this.path = path;
        for (Section<KpiLaneDirection> section : path.getSections())
        {
            for (KpiLaneDirection kpiLaneDirection : section)
            {
                sampler.registerSpaceTimeRegion(new SpaceTimeRegion(kpiLaneDirection, Length.ZERO,
                        kpiLaneDirection.getLaneData().getLength(), Time.ZERO, Time.createSI(Double.MAX_VALUE)));
            }
        }
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
            for (Section<KpiLaneDirection> section : getPath().getSections())
            {
                cache.add(this.sampler.getTrajectoryGroup(section.getSource(series)));
            }
            this.trajectoriesCache.set(series, cache);
            this.lastUpdateTime.set(series, getUpdateTime());
        }
        return this.trajectoriesCache.get(series);
    }

    /**
     * Returns the path.
     * @return GraphPath&lt;KpiLaneDirection&gt;; the path
     */
    protected final GraphPath<KpiLaneDirection> getPath()
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
     * Returns the sampler.
     * @return Sampler&lt;?&gt;; sampler.
     */
    protected final Sampler<?> getSampler()
    {
        return this.sampler;
    }

}
