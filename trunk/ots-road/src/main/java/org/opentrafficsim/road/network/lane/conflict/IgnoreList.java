package org.opentrafficsim.road.network.lane.conflict;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Contains lane combinations that should be ignored for building conflicts.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 dec. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IgnoreList
{

    /** Lane combinations to ignore. Each combination is contained in both directions. */
    private final Map<Lane, Set<Lane>> ignoreMap = new HashMap<>();

    /**
     * Add any combination of lanes on both links to the set to ignore. Order of the links does not matter.
     * @param link1 link 1
     * @param link2 link 2
     */
    public final void addLinkCombination(final CrossSectionLink link1, final CrossSectionLink link2)
    {
        for (Lane lane1 : link1.getLanes())
        {
            for (Lane lane2 : link2.getLanes())
            {
                addLaneCombination(lane1, lane2);
            }
        }
    }

    /**
     * Add lane combination to the set to ignore. Order of the lanes does not matter.
     * @param lane1 lane 1
     * @param lane2 lane 2
     */
    public final void addLaneCombination(final Lane lane1, final Lane lane2)
    {
        if (!this.ignoreMap.containsKey(lane1))
        {
            this.ignoreMap.put(lane1, new HashSet<>());
        }
        this.ignoreMap.get(lane1).add(lane2);
        if (!this.ignoreMap.containsKey(lane2))
        {
            this.ignoreMap.put(lane2, new HashSet<>());
        }
        this.ignoreMap.get(lane2).add(lane1);
    }

    /**
     * Returns whether the combination of the two lanes should be ignored. Order of the lanes does not matter.
     * @param lane1 lane 1
     * @param lane2 lane 2
     * @return whether the combination of the two lanes should be ignored
     */
    public final boolean ignore(final Lane lane1, final Lane lane2)
    {
        if (!this.ignoreMap.containsKey(lane1))
        {
            return false;
        }
        return this.ignoreMap.get(lane1).contains(lane2);
    }

}
