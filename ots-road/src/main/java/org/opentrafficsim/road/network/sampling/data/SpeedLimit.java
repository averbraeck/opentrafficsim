package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

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
public class SpeedLimit extends ExtendedDataSpeed<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public SpeedLimit()
    {
        super("speedLimit", "Speed limit");
    }

    @Override
    public final Optional<FloatSpeed> getValue(final GtuDataRoad gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        LaneBasedGtu laneGtu = gtu.getGtu();
        try
        {
            return Optional
                    .ofNullable(new FloatSpeed(laneGtu.getPosition().lane().getSpeedLimit(laneGtu.getType()).si, SpeedUnit.SI));
        }
        catch (NetworkException exception)
        {
            throw new OtsRuntimeException("Could not obtain speed limit.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "SpeedLimit";
    }

}
