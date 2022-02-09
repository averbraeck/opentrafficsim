package org.opentrafficsim.core.network;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node. An expandable network can be
 * an (expanded) node as well. An example is shown below:
 * 
 * <pre>
 *            |
 *     -------O--------
 *            |
 * </pre>
 * 
 * can be expanded into:
 * 
 * <pre>
 *            |
 *            A
 *           /|\
 *          / | \
 *    -----B--C--D-----
 *          \ | /
 *           \|/
 *            E
 *            |
 * </pre>
 * 
 * Node O in the example is expanded into the subnetwork consisting of nodes A, B, C, D, and E, and links AB, AC, AD, BC, CD,
 * BE, CE, and DE. It also means that when node expansion takes place, the links to node O have to be replaced. In the example
 * below:
 * 
 * <pre>
 *            X
 *            |
 *     Y------O-------Z
 *            |
 *            W
 * </pre>
 * 
 * can be expanded into:
 * 
 * <pre>
 *            X
 *            |
 *            A
 *           /|\
 *          / | \
 *    Y----B--C--D----Z
 *          \ | /
 *           \|/
 *            E
 *            |
 *            W
 * </pre>
 * 
 * The node XO is replaced by XA, YO is replaced by YB, OZ is replaced by DZ, and OW is replaced by EW in the network. The
 * reverse takes place when we do node collapse.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public abstract class ExpansionNetwork extends OTSNetwork
{
    /** */
    private static final long serialVersionUID = 20150104L;

    /**
     * @param id String; the network id.
     * @param addDefaultTypes add the default GTUTypes and LinkTypes, or not
     * @param simulator OTSSimulatorInterface; the DSOL simulator engine
     */
    public ExpansionNetwork(final String id, final boolean addDefaultTypes, final OTSSimulatorInterface simulator)
    {
        super(id, addDefaultTypes, simulator);
    }

}
