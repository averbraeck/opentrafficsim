package org.opentrafficsim.opendrive.bindings;

import org.opentrafficsim.opendrive.generated.ETrafficRule;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * LaneKeepingPolicy adapter.
 * @author wjschakel
 */
public class LaneKeepingPolicyAdapter extends XmlAdapter<ETrafficRule, LaneKeepingPolicy>
{

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
