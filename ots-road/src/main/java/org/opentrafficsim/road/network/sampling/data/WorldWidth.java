package org.opentrafficsim.road.network.sampling.data;

import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

public class WorldWidth extends ExtendedDataLength<GtuDataRoad> {

    /**
     * Constructor.
     */
    public WorldWidth()
    {
        super("Width", "Width of the GTU");
    }

    /** {@inheritDoc} */
    @Override
    public FloatLength getValue(final GtuDataRoad gtu)
    {
        return convertValue((float) gtu.getGtu().getWidth().getSI());
    }
}


