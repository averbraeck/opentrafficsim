package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ETrafficRule;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * LaneKeepingPolicy adapter.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneKeepingPolicyAdapter extends XmlAdapter<ETrafficRule, LaneKeepingPolicy>
{

    /**
     * Constructor.
     */
    public LaneKeepingPolicyAdapter()
    {
        //
    }

    @Override
    public LaneKeepingPolicy unmarshal(final ETrafficRule v)
    {
        return ETrafficRule.LHT.equals(v) ? LaneKeepingPolicy.KEEPLEFT : LaneKeepingPolicy.KEEPRIGHT;
    }

    @Override
    public ETrafficRule marshal(final LaneKeepingPolicy v)
    {
        return LaneKeepingPolicy.KEEPLEFT.equals(v) ? ETrafficRule.LHT : ETrafficRule.RHT;
    }

}
