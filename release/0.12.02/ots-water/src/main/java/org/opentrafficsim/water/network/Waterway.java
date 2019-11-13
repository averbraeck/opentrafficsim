package org.opentrafficsim.water.network;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 27, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Waterway extends OTSLink
{
    /** */
    private static final long serialVersionUID = 20150927L;

    /** description. */
    // TODO private final String description;

    /** current; positive direction is along the design line. */
    private double current;

    /** list of obstacles, sorted on distance along the design line. */
    private SortedMap<Length, Obstacle> obstacles = new TreeMap<>();

    /** list of terminals, sorted on distance along the design line. */
    // TODO private SortedMap<Length, Terminal> terminals = new TreeMap<>();


    /**
     * Construct a new waterway.
     * @param network the network.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param directionality to indicate the general direction of the waterway (FORWARD = in the direction of the design line;
     *            BACKWARD is in the opposite direction; BOTH is a waterway that can be used in both directions; NONE is a
     *            waterway that cannot be used for sailing.
     * @throws NetworkException when waterway with this id already exists
     */
    public Waterway(final Network network, final String id, final OTSNode startNode, final OTSNode endNode,
            final LinkType linkType, final OTSLine3D designLine, final LongitudinalDirectionality directionality)
            throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, directionality);
    }

    /**
     * Construct a new waterway.
     * @param network the network.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param directionalityMap the directions for different type of ships; it might be that all or certain types of ships are
     *            only allowed to use a canal in one direction. Furthermore, the directions can limit waterways for certain
     *            classes of ships. Set the LongitudinalDirectionality to NONE for ships that are not allowed to sail this
     *            waterway.
     * @throws NetworkException when waterway with this id already exists
     */
    public Waterway(final Network network, final String id, final OTSNode startNode, final OTSNode endNode,
            final LinkType linkType, final OTSLine3D designLine,
            final Map<GTUType, LongitudinalDirectionality> directionalityMap) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, directionalityMap);
    }

}