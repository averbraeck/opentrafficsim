package org.opentrafficsim.road.network.sampling.data;

import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

public class WorldLength extends ExtendedDataLength<GtuDataRoad> {

    /**
     * Constructor.
     */
    public WorldLength()
    {
        super("Length", "Length of the GTU");
    }

    /** {@inheritDoc} */
    @Override
    public FloatLength getValue(final GtuDataRoad gtu)
    {
        return convertValue((float) gtu.getGtu().getLength().getSI());
    }
}
