package org.opentrafficsim.road.network.sampling.data;

import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type in sampler to record world x coordinate. 
 * @author wjschakel
 */
public class WorldXData extends ExtendedDataLength<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public WorldXData()
    {
        super("WorldX", "World x coordinate");
    }

    /** {@inheritDoc} */
    @Override
    public FloatLength getValue(final GtuDataRoad gtu)
    {
        return convertValue((float) gtu.getGtu().getLocation().x);
    }

}
