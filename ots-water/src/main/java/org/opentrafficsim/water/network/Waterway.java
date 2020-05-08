package org.opentrafficsim.water.network;

import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.water.network.infra.Obstacle;
import org.opentrafficsim.water.transfer.Terminal;

/**
 * A waterway, i.e. a river, canal or sailable route on a lake or sea.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Waterway extends OTSLink
{
    /** */
    private static final long serialVersionUID = 20150927L;

    /** name. */
    private final String name;

    /** current; positive direction is along the design line. */
    private double current;

    /** list of obstacles, sorted on distance along the design line. */
    private SortedMap<Length, Obstacle> obstacles = new TreeMap<>();

    /** list of terminals, sorted on distance along the design line. */
    private SortedMap<Length, Terminal> terminals = new TreeMap<>();

    /**
     * Construct a new waterway.
     * @param network OTSNetwork; the network.
     * @param id String; the waterway id
     * @param name String; the name
     * @param startNode OTSNode; start node (directional)
     * @param endNode OTSNode; end node (directional)
     * @param linkType LinkType; Link type to indicate compatibility with GTU types
     * @param designLine OTSLine3D; the OTSLine3D design line of the Link
     * @param simulator OTSSimulatorInterface; the simulator to schedule events on
     * @throws NetworkException when waterway with this id already exists
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Waterway(final OTSNetwork network, final String id, final String name, final OTSNode startNode,
            final OTSNode endNode, final LinkType linkType, final OTSLine3D designLine, final OTSSimulatorInterface simulator)
            throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine);
        this.name = name;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

}
