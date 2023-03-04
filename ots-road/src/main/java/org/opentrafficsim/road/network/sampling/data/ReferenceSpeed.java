package org.opentrafficsim.road.network.sampling.data;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Reference speed for trajectories.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ReferenceSpeed extends ExtendedDataSpeed<GtuDataRoad>
{

    /** Single instance. */
    public static final ReferenceSpeed INSTANCE = new ReferenceSpeed();

    /**
     * 
     */
    public ReferenceSpeed()
    {
        super("referenceSpeed", "Reference speed (minimum of speed limit and maximum vehicle speed)");
    }

    /** {@inheritDoc} */
    @Override
    public final FloatSpeed getValue(final GtuDataRoad gtu)
    {
        LaneBasedGtu gtuObj = gtu.getGtu();
        try
        {
            double v1 = gtuObj.getReferencePosition().getLane().getSpeedLimit(gtuObj.getType()).si;
            double v2 = gtuObj.getMaximumSpeed().si;
            return new FloatSpeed(v1 < v2 ? v1 : v2, SpeedUnit.SI);
        }
        catch (GtuException exception)
        {
            // GTU was destroyed and is without a reference location
            return new FloatSpeed(Double.NaN, SpeedUnit.SI);
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Could not obtain reference speed from GTU " + gtuObj, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Reference Speed";
    }

}
