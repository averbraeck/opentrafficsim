package org.opentrafficsim.kpi.sampling.data;

import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Reference speed for trajectories, as defined by the used GtuData.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ReferenceSpeed extends ExtendedDataSpeed<GtuData>
{

    /** Single instance. */
    public static final ReferenceSpeed INSTANCE = new ReferenceSpeed();

    /**
     * Constructor.
     */
    public ReferenceSpeed()
    {
        super("referenceSpeed", "Reference speed");
    }

    @Override
    public final FloatSpeed getValue(final GtuData gtu)
    {
        return FloatSpeed.ofSI((float) gtu.getReferenceSpeed().si);
    }

    @Override
    public final String toString()
    {
        return "Reference Speed";
    }

}
