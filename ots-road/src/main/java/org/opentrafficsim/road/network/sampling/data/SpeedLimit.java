package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataTypeSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.sampling.GtuData;

/**
 * Speed limit for trajectories.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedLimit extends ExtendedDataTypeSpeed<GtuData>
{

    /**
     * Constructor.
     */
    public SpeedLimit()
    {
        super("SpeedLimit");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatSpeed getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        LaneBasedGTU laneGtu = gtu.getGtu();
        try
        {
            return new FloatSpeed(laneGtu.getReferencePosition().getLane().getSpeedLimit(laneGtu.getGtuType()).si,
                    SpeedUnit.SI);
        }
        catch (NetworkException | GtuException exception)
        {
            throw new RuntimeException("Could not obtain speed limit.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedLimit";
    }

}
