package org.opentrafficsim.core.network;

import org.opentrafficsim.core.unit.AnglePlaneUnit;
import org.opentrafficsim.core.unit.AngleSlopeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version1 Sep 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the node.
 * @param <P> the point type of the node (Point, DirectedPoint, XY, etc.).
 */
public abstract class AbstractExpansionNode<ID, P> extends AbstractNode<ID, P>
{
    /** */
    private static final long serialVersionUID = 20140921L;

    /** Network of expanded Node. */
    private Network<?, AbstractExpansionNode<ID, P>, Link<?, AbstractExpansionNode<ID, P>>> network;

    /**
     * @param id ID of ExpansionNode.
     * @param point the point when the expansion node is collapsed.
     * @param network Network of expanded Node.
     */
    public AbstractExpansionNode(final ID id, final P point,
            final Network<?, AbstractExpansionNode<ID, P>, Link<?, AbstractExpansionNode<ID, P>>> network)
    {
        super(id, point);
        this.network = network;
    }

    /**
     * @param id ID of ExpansionNode.
     * @param point the point when the expansion node is collapsed.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle.
     * @param network Network of expanded Node.
     */
    public AbstractExpansionNode(final ID id, final P point, final DoubleScalar.Abs<AnglePlaneUnit> direction,
            final DoubleScalar.Abs<AngleSlopeUnit> slope,
            final Network<?, AbstractExpansionNode<ID, P>, Link<?, AbstractExpansionNode<ID, P>>> network)
    {
        super(id, point, direction, slope);
        this.network = network;
    }

    /**
     * @return Network of expanded Node.
     */
    public final Network<?, AbstractExpansionNode<ID, P>, Link<?, AbstractExpansionNode<ID, P>>> getNetwork()
    {
        return this.network;
    }

}
