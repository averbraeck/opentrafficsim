package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.xml.bindings.types.LaneKeepingPolicyType;

/**
 * LaneKeepingAdapter to convert between XML representations of LaneKeeping and an enum type.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneKeepingPolicyAdapter extends StaticFieldAdapter<LaneKeepingPolicy, LaneKeepingPolicyType>
{

    /**
     * Constructor.
     */
    public LaneKeepingPolicyAdapter()
    {
        super(LaneKeepingPolicy.class, LaneKeepingPolicyType.class);
    }

}
