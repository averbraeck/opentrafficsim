package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.xml.generated.MODELIDREFERRALTYPE;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.SCENARIO;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class ScenarioParser
{

    /**
     * Parse model ID referrals.
     * @param scenario scenario
     * @param demand demand
     * @return map from ID to ID
     */
    public static final Map<String, String> parseModelIdReferral(final List<SCENARIO> scenario,
            final List<NETWORKDEMAND> demand)
    {
        // TODO: use run to select scenario (probably outside this class, and accept a single SCENARIO
        Map<String, String> map = new LinkedHashMap<>();
        for (NETWORKDEMAND d : demand)
        {
            for (MODELIDREFERRALTYPE modelIdReferral : d.getMODELIDREFERRAL())
            {
                map.put(modelIdReferral.getID(), modelIdReferral.getMODELID());
            }
        }
        // overwrite with scenario level ID referrals
        for (MODELIDREFERRALTYPE modelIdReferral : scenario.get(0).getMODELIDREFERRAL())
        {
            map.put(modelIdReferral.getID(), modelIdReferral.getMODELID());
        }
        return map;
    }

}
