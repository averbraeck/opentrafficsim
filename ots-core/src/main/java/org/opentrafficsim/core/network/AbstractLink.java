package org.opentrafficsim.core.network;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version ug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <IDL> the ID type of the Link, e.g., String or Integer.
 * @param <IDN> the ID type of the Node, e.g., String or Integer.
 * @param <P> the type of Point that the Node uses.
 * @param <N> the type of Node that this Link uses.
 */
public abstract class AbstractLink<IDL, IDN, P, N extends AbstractNode<IDN, P>> implements Link<IDL, N>, Serializable,
        LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20150101L;

    /** Link id. */
    private final IDL id;

    /** Start node (directional). */
    private final N startNode;

    /** End node (directional). */
    private final N endNode;

    /** Link length in a length unit. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** Link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /**
     * Construct a new link.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param length link length in a length unit
     * @param capacity link capacity in GTUs per hour
     */
    public AbstractLink(final IDL id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        double dx = endNode.getX() - startNode.getX();
        double dy = endNode.getY() - startNode.getY();
        double distSquared = dx * dx + dy * dy;
        if (distSquared < 0.00000000001)
        {
            System.out.println("Nodes clash: " + startNode + "(" + startNode.getX() + "," + startNode.getY() + "), "
                    + endNode + "(" + endNode.getX() + "," + endNode.getY() + ")");
        }
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        // TODO Add directionality to a link?
        this.startNode.addLinkOut(this);
        this.endNode.addLinkIn(this);
        this.length = length;
        setCapacity(capacity);
    }

    /**
     * Construct a new link with infinite capacity.
     * @param id the link id
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param length link length in a length unit
     */
    public AbstractLink(final IDL id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length)
    {
        this(id, startNode, endNode, length, new DoubleScalar.Abs<FrequencyUnit>(Double.POSITIVE_INFINITY,
                FrequencyUnit.PER_SECOND));
    }

    /**
     * @return id.
     */
    public final IDL getId()
    {
        return this.id;
    }

    /**
     * @return start node.
     */
    public final N getStartNode()
    {
        return this.startNode;
    }

    /**
     * @return end node.
     */
    public final N getEndNode()
    {
        return this.endNode;
    }

    /**
     * @return link capacity.
     */
    public final DoubleScalar.Abs<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set the link capacity.
     */
    public final void setCapacity(final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return length.
     */
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return this.id.toString();
    }

}
