package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.LaneData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

import nl.tudelft.simulation.language.Throw;

/**
 * Super class for plots that use sampler data. Sub classes may obtain trajectories using {@code getTrajectories()}, or
 * alternatively maintain some other -possibly more efficient- connection to the sampler. This class also connects the plot to a
 * path, consisting of a list of lanes. Start distance along the path for each lane is provided to sub classes using
 * {@code getStartDistance(KpiLaneDirection)}. Total length is obtained using {@code getEndLocation()}.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class XAbstractSamplerPlot extends XAbstractSpaceTimePlot
{

    /** */
    private static final long serialVersionUID = 20181004L;

    /** Sampler. */
    private final RoadSampler sampler;

    /** KPI lane directions registered in the sampler. */
    private final List<KpiLaneDirection> path = new ArrayList<>();

    /** Start distance of lanes in path. */
    private final List<Length> startDistances = new ArrayList<>();

    /** Total length of the path. */
    private final Length totalLength;

    /** Time when trajectories were last updated. */
    private Time lastUpdateTime;

    /** Cached trajectories. */
    private List<TrajectoryGroup> trajectoriesCache;

    /**
     * Constructor.
     * @param caption String; caption
     * @param updateInterval Duration; regular update interval (simulation time)
     * @param simulator OTSSimulatorInterface; simulator
     * @param sampler RoadSampler; road sampler
     * @param path List&lt;LaneDirection&gt;; path
     * @param delay Duration; delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     */
    public XAbstractSamplerPlot(final String caption, final Duration updateInterval, final OTSSimulatorInterface simulator,
            final RoadSampler sampler, final List<LaneDirection> path, final Duration delay)
    {
        super(caption, updateInterval, simulator, delay, DEFAULT_INITIAL_UPPER_TIME_BOUND);
        this.sampler = sampler;
        Length start = Length.ZERO;
        for (LaneDirection lane : path)
        {
            KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(lane.getLane()),
                    lane.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
            sampler.registerSpaceTimeRegion(new SpaceTimeRegion(kpiLaneDirection, Length.ZERO, lane.getLength(), Time.ZERO,
                    Time.createSI(Double.MAX_VALUE)));
            this.path.add(kpiLaneDirection);
            this.startDistances.add(start);
            start = start.plus(lane.getLength());
        }
        this.totalLength = start;
    }

    /**
     * Returns all trajectories.
     * @return List&lt;TrajectoryGroup&gt;; the trajectories
     */
    protected List<TrajectoryGroup> getTrajectories()
    {
        if (this.lastUpdateTime == null || this.lastUpdateTime.lt(getUpdateTime()))
        {
            List<TrajectoryGroup> cache = new ArrayList<>();
            for (KpiLaneDirection lane : this.path)
            {
                cache.add(this.sampler.getTrajectoryGroup(lane));
            }
            this.trajectoriesCache = cache;
            this.lastUpdateTime = getUpdateTime();
        }
        return this.trajectoriesCache;
    }

    /**
     * Returns the start distance of the give lane.
     * @param lane KpiLaneDirection; lane, with direction
     * @return Length; start distance of the give lane
     */
    protected final Length getStartDistance(final KpiLaneDirection lane)
    {
        int index = this.path.indexOf(lane);
        Throw.when(index < 0, IllegalStateException.class, "KpiLaneDirection %s is not part of the plot.", lane);
        return this.startDistances.get(index);
    }
    
    /**
     * Returns the path.
     * @return List&lt;KpiLaneDirection&gt;; the path
     */
    protected final List<KpiLaneDirection> getPath()
    {
        return this.path;
    }

    /** {@inheritDoc} */
    @Override
    protected final Length getEndLocation()
    {
        return this.totalLength;
    }
    
    /** 
     * Returns the sampler.
     * @return RoadSampler; sampler.
     */
    protected final RoadSampler getSampler()
    {
        return this.sampler;
    }

}
