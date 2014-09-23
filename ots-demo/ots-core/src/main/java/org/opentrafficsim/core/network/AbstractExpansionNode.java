package org.opentrafficsim.core.network;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 1 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 * @param <ID> the ID type of the node.
 * @param <P> the point type of the node (Point, DirectedPoint, XY, etc.).
 */
public abstract class AbstractExpansionNode<ID, P> extends AbstractNode<ID, P>
{
    /** */
    private static final long serialVersionUID = 20140921L;
    
    /** Network of expanded Node. */
    private Network<?, ?> network;

    /**
     * @param id ID of ExpansionNode.
     * @param point the point when the expansion node is collapsed.
     * @param network Network of expanded Node.
     */
    public AbstractExpansionNode(final ID id, final P point, final Network<?, ?> network)
    {
        super(id, point);
        this.network = network;
    }

    /**
     * @return Network of expanded Node.
     */
    public final Network<?, ?> getNetwork()
    {
        return this.network;
    }

}
