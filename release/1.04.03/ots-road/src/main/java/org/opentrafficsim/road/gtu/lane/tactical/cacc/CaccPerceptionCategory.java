package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsUtil;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsUtil.DistanceGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Perception category for CACC.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CaccPerceptionCategory extends LaneBasedAbstractPerceptionCategory implements ControllerPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20181017L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength SENSOR_RANGE = LongitudinalController.SENSOR_RANGE;

    /** Leaders. */
    private Map<RelativeLane, DownstreamNeighborsIterable> leaders = new LinkedHashMap<>();

    /** Adjacent leader. */
    private Map<LateralDirectionality, HeadwayGTU> leader = new LinkedHashMap<>();

    /** Adjacent follower. */
    private Map<LateralDirectionality, HeadwayGTU> follower = new LinkedHashMap<>();

    /** Headway GTU type for CACC vehicles. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * @param perception LanePerception; perception
     * @param randomStream StreamInterface; stream for random numbers
     * @param simulator OTSSimulatorInterface; simulator
     */
    public CaccPerceptionCategory(final LanePerception perception, final StreamInterface randomStream,
            final OTSSimulatorInterface simulator)
    {
        super(perception);
        this.headwayGtuType =
                Try.assign(() -> new CaccHeadwayGtuType(randomStream, simulator, perception.getGtu().getParameters()),
                        "Exception while obtaining GTU.");
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        // leaders
        for (RelativeLane lane : new RelativeLane[] { RelativeLane.LEFT, RelativeLane.CURRENT, RelativeLane.RIGHT })
        {
            if (getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
                Length pos = record.getStartDistance().neg();
                pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                        : pos.minus(getGtu().getFront().getDx());
                boolean ignoreIfUpstream = true;
                this.leaders.put(lane,
                        new DownstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                                getGtu().getParameters().getParameter(SENSOR_RANGE), getGtu().getFront(), this.headwayGtuType,
                                /* getGtu(), */RelativeLane.CURRENT, ignoreIfUpstream));
            }
            else
            {
                // no lane
                this.leaders.put(lane, null);
            }
        }

        // adjacent vehicles
        for (RelativeLane lane : new RelativeLane[] { RelativeLane.LEFT, RelativeLane.RIGHT })
        {
            if (getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                SortedSet<DistanceGTU> down =
                        NeighborsUtil.getFirstDownstreamGTUs(getPerception().getLaneStructure().getFirstRecord(lane),
                                getGtu().getRear(), getGtu().getFront(), RelativePosition.REAR, getTimestamp());
                if (!down.isEmpty())
                {
                    DistanceGTU l = down.first();
                    this.leader.put(lane.getLateralDirectionality(),
                            this.headwayGtuType.createHeadwayGtu(getGtu(), l.getGTU(), l.getDistance(), true));
                }
                else
                {
                    this.leader.put(lane.getLateralDirectionality(), null);
                }

                SortedSet<DistanceGTU> up =
                        NeighborsUtil.getFirstUpstreamGTUs(getPerception().getLaneStructure().getFirstRecord(lane),
                                getGtu().getFront(), getGtu().getRear(), RelativePosition.FRONT, getTimestamp());
                if (!up.isEmpty())
                {
                    DistanceGTU l = up.first();
                    this.follower.put(lane.getLateralDirectionality(),
                            this.headwayGtuType.createHeadwayGtu(getGtu(), l.getGTU(), l.getDistance(), true));
                }
                else
                {
                    this.follower.put(lane.getLateralDirectionality(), null);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public DownstreamNeighborsIterable getLeaders(final RelativeLane lane)
    {
        return this.leaders.get(lane);
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU getLeader(final LateralDirectionality lat)
    {
        return this.leader.get(lat);
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU getFollower(final LateralDirectionality lat)
    {
        return this.follower.get(lat);
    }

}
