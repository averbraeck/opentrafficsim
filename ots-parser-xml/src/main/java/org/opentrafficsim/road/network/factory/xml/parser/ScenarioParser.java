package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.xml.generated.MODELIDREFERRALTYPE;
import org.opentrafficsim.xml.generated.NETWORKDEMAND;
import org.opentrafficsim.xml.generated.SCENARIO;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 16, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
