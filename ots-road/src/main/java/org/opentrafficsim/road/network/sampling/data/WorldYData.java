package org.opentrafficsim.road.network.sampling.data;

import java.util.Optional;

import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type in sampler to record world x coordinate.
 * @author wjschakel
 */
public class WorldYData extends ExtendedDataLength<GtuDataRoad>
{

    /**
     * Constructor.
     */
    public WorldYData()
    {
        super("WorldY", "World y coordinate");
    }

    @Override
    public Optional<FloatLength> getValue(final GtuDataRoad gtu)
    {
        return Optional.ofNullable(convertValue((float) gtu.getGtu().getLocation().y));
    }

}
