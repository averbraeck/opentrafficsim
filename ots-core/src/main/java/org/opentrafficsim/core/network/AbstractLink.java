package org.opentrafficsim.core.network;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the Link, e.g., String or Integer.
 * @param <N> the type of node that this link uses.
 */
public abstract class AbstractLink<ID, N extends AbstractNode<?, ?>> implements Serializable, LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** Link id. */
    private final ID id;

    /** Start node (directional). */
    private final N startNode;

    /** Dnd node (directional). */
    private final N endNode;

    /** Link length in a length unit. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** Link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /** Possible geometry for the link; can be null. */
    private LinearGeometry geometry;

    /** Hierarchy of the link, lower the number, higher the importance, min is 0. */
    private int hierarchy;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param capacity link capacity in vehicles per hour.
     * @param hierarchy
     */
    public AbstractLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Abs<FrequencyUnit> capacity, final int hierarchy)
    {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
        this.setHierarchy(hierarchy < 0 ? 0 : hierarchy);
        setCapacity(capacity);
    }

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param hierarchy
     */
    public AbstractLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final int hierarchy)
    {
        this(id, startNode, endNode, length, new DoubleScalar.Abs<FrequencyUnit>(Double.POSITIVE_INFINITY,
                FrequencyUnit.PER_SECOND), hierarchy);
    }

    /**
     * @return id.
     */
    public final ID getId()
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

    /**
     * @return geometry.
     */
    public final LinearGeometry getGeometry()
    {
        return this.geometry;
    }

    /**
     * @param geometry set geometry.
     */
    public final void setGeometry(final LinearGeometry geometry)
    {
        this.geometry = geometry;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return this.id.toString();
    }

    /**
     * @return hierarchy
     */
    public int getHierarchy()
    {
        return this.hierarchy;
    }

    /**
     * @param hierarchy set hierarchy
     */
    public void setHierarchy(final int hierarchy)
    {
        this.hierarchy = hierarchy;
    }

}
