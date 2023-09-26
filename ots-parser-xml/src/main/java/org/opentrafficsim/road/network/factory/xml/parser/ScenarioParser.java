package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.eval.Eval;
import org.opentrafficsim.xml.generated.Demand;
import org.opentrafficsim.xml.generated.ModelIdReferralType;
import org.opentrafficsim.xml.generated.ScenarioType;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param scenario List&lt;ScenarioType&gt;; scenario
     * @param demand Demand; demand
     * @param eval Eval; expression evaluator.
     * @return map from ID to ID
     */
    public static final Map<String, String> parseModelIdReferral(final List<ScenarioType> scenario, final Demand demand,
            final Eval eval)
    {
        // TODO: use run to select scenario (probably outside this class, and accept a single Scenario
        Map<String, String> map = new LinkedHashMap<>();
        for (ModelIdReferralType modelIdReferral : demand.getModelIdReferral())
        {
            map.put(modelIdReferral.getId(), modelIdReferral.getModelId().get(eval));
        }
        // overwrite with scenario level ID referrals
        if (!scenario.isEmpty())
        {
            for (ModelIdReferralType modelIdReferral : scenario.get(0).getModelIdReferral())
            {
                map.put(modelIdReferral.getId(), modelIdReferral.getModelId().get(eval));
            }
        }
        return map;
    }

}
