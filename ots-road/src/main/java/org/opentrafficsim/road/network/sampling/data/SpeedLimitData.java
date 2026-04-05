package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;
import org.opentrafficsim.road.network.speed.SpeedLimits;

/**
 * Speed limit for trajectories.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedLimitData extends ExtendedDataSpeed<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public SpeedLimitData()
    {
        super("speedLimit", "Speed limit");
    }

    @Override
    public final Optional<FloatSpeed> getValue(final GtuDataRoad gtu)
    {
        LaneBasedGtu laneGtu = gtu.getGtu();
        SpeedLimits speedLimit = laneGtu.getLane().getSpeedLimits(laneGtu.getType());
        if (speedLimit.laneSpeedLimit() == null)
        {
            if (speedLimit.gtuTypeSpeedLimit() == null)
            {
                return Optional.empty();
            }
            return Optional.of(new FloatSpeed(speedLimit.gtuTypeSpeedLimit().speed().si, SpeedUnit.SI));
        }
        if (speedLimit.gtuTypeSpeedLimit() == null)
        {
            return Optional.of(new FloatSpeed(speedLimit.laneSpeedLimit().speed().si, SpeedUnit.SI));
        }
        double v = Math.min(speedLimit.laneSpeedLimit().speed().si, speedLimit.gtuTypeSpeedLimit().speed().si);
        return Optional.of(new FloatSpeed(v, SpeedUnit.SI));
    }

    @Override
    public final String toString()
    {
        return "SpeedLimit";
    }

}
