package org.opentrafficsim.road.network.sampling.data;

import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

public class WorldType extends ExtendedDataString<GtuDataRoad> {
    /**
     * Constructor.
     */
    public WorldType()
    {
        super("GTU Type", "Type of the GTU");
    }

    /** {@inheritDoc} */
    @Override
    public String getValue(final GtuDataRoad gtu)
    {
        return gtu.getGtu().getType().toString();
    }
}
