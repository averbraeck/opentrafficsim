package org.opentrafficsim.water.network;

import java.util.Map;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 27, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Waterway extends OTSLink
{
    /** */
    private static final long serialVersionUID = 20150927L;

    /**
     * Construct a new waterway.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param directionality to indicate the general direction of the waterway (FORWARD = in the direction of the design line;
     *            BACKWARD is in the opposite direction; BOTH is a waterway that can be used in both directions; NONE is a
     *            waterway that cannot be used for sailing.
     */
    public Waterway(String id, OTSNode startNode, OTSNode endNode, LinkType linkType, OTSLine3D designLine,
        LongitudinalDirectionality directionality)
    {
        super(id, startNode, endNode, linkType, designLine, directionality);
    }

    /**
     * Construct a new waterway.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param directionalityMap the directions for different type of ships; it might be that all or certain types of ships are
     *            only allowed to use a canal in one direction. Furthermore, the directions can limit waterways for certain
     *            classes of ships. Set the LongitudinalDirectionality to NONE for ships that are not allowed to sail this
     *            waterway.
     */
    public Waterway(String id, OTSNode startNode, OTSNode endNode, LinkType linkType, OTSLine3D designLine,
        Map<GTUType, LongitudinalDirectionality> directionalityMap)
    {
        super(id, startNode, endNode, linkType, designLine, directionalityMap);
    }

}
