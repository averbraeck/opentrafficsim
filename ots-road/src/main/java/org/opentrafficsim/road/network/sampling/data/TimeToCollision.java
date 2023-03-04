package org.opentrafficsim.road.network.sampling.data;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Time-to-collision for trajectories.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class TimeToCollision extends ExtendedDataDuration<GtuDataRoad>
{

    /**
     * 
     */
    public TimeToCollision()
    {
        super("timeToCollision", "Time to collision");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatDuration getValue(final GtuDataRoad gtu)
    {
        LaneBasedGtu gtuObj = gtu.getGtu();
        try
        {
            LanePosition ref = gtuObj.getReferencePosition();
            Set<Lane> set = new LinkedHashSet<>();
            Set<Lane> visited = new LinkedHashSet<>();
            set.add(ref.getLane());
            Length pos = ref.getPosition();
            Length cumulDist = Length.ZERO; // from start of lane
            Time now = gtuObj.getSimulator().getSimulatorAbsTime();
            LaneBasedGtu next = null;
            while (set.size() == 1)
            {
                Lane lane = set.iterator().next();
                if (cumulDist.gt0())
                {
                    pos = Length.ZERO;
                }
                next = lane.getGtuAhead(pos, RelativePosition.REAR, now);
                if (next == null)
                {
                    if (visited.contains(lane))
                    {
                        break;
                    }
                    visited.add(lane);
                    cumulDist = cumulDist.plus(lane.getLength());
                    set = new LinkedHashSet<>(lane.nextLanes(gtuObj.getType()));
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
        catch (GtuException exception)
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
