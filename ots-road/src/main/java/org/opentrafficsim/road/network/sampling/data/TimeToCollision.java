package org.opentrafficsim.road.network.sampling.data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.GtuData;

/**
 * Time-to-collision for trajectories.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TimeToCollision extends ExtendedDataTypeDuration<GtuData>
{

    /**
     * 
     */
    public TimeToCollision()
    {
        super("timeToCollision");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatDuration getValue(final GtuData gtu)
    {
        LaneBasedGTU gtuObj = gtu.getGtu();
        try
        {
            DirectedLanePosition ref = gtuObj.getReferencePosition();
            Map<Lane, GTUDirectionality> map = new LinkedHashMap<>();
            Set<LaneDirection> visited = new LinkedHashSet<>();
            map.put(ref.getLane(), ref.getGtuDirection());
            Length pos = ref.getPosition();
            Length cumulDist = Length.ZERO; // from start of lane
            Time now = gtuObj.getSimulator().getSimulatorAbsTime();
            LaneBasedGTU next = null;
            while (map.size() == 1)
            {
                Lane lane = map.keySet().iterator().next();
                GTUDirectionality dir = map.get(lane);
                if (cumulDist.gt0())
                {
                    pos = dir.isPlus() ? Length.ZERO : lane.getLength();
                }
                next = lane.getGtuAhead(pos, dir, RelativePosition.REAR, now);
                if (next == null)
                {
                    LaneDirection laneDir = new LaneDirection(lane, map.get(lane));
                    if (visited.contains(laneDir))
                    {
                        break;
                    }
                    visited.add(laneDir);
                    cumulDist = cumulDist.plus(lane.getLength());
                    map = lane.downstreamLanes(dir, gtuObj.getGTUType()).toMap();
                }
                else
                {
                    // gtu found, calculate TTC
                    if (next.getSpeed().ge(gtuObj.getSpeed()))
                    {
                        return new FloatDuration(Double.NaN, DurationUnit.SI);
                    }
                    Length ownPos = gtuObj.position(ref.getLane(), gtuObj.getFront());
                    Length nextPos = next.position(lane, next.getRear());
                    Length dist = nextPos.minus(ownPos).plus(cumulDist);
                    Speed dv = gtuObj.getSpeed().minus(next.getSpeed());
                    return new FloatDuration(dist.si / dv.si, DurationUnit.SI);
                }
            }
            return FloatDuration.NaN;
        }
        catch (GTUException exception)
        {
            // GTU was destroyed and is without a reference location
            return FloatDuration.NaN;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TTC";
    }

}
