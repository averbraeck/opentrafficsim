package org.opentrafficsim.core.network;

import java.io.Serializable;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** link id. */
    private final ID id;

    /** start node (directional). */
    private final N startNode;

    /** end node (directional). */
    private final N endNode;

    /** link length in a length unit. */
    private final DoubleScalar.Rel<LengthUnit> length;

    /** link capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar<FrequencyUnit> capacity;

    /** possible geometry for the link; can be null. */
    private LinearGeometry geometry;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param capacity link capacity in vehicles per hour.
     * @param geometry possible geometry for the link; can be null.
     */
    public AbstractLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar<FrequencyUnit> capacity, final LinearGeometry geometry)
    {
        this.id = id;
        this.startNode = startNode;
        this.endNode = endNode;
        this.length = length;
        setCapacity(capacity);
        this.geometry = geometry;
    }

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     */
    public AbstractLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length)
    {
        this(id, startNode, endNode, length, new DoubleScalar.Abs<FrequencyUnit>(Double.POSITIVE_INFINITY,
                FrequencyUnit.PER_SECOND), null);
    }

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param geometry possible geometry for the link; can be null.
     */
    public AbstractLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final LinearGeometry geometry)
    {
        this(id, startNode, endNode, length, new DoubleScalar.Abs<FrequencyUnit>(Double.POSITIVE_INFINITY,
                FrequencyUnit.PER_SECOND), geometry);
    }

    /**
     * @return link length.
     */
    public final DoubleScalar<LengthUnit> getLenght()
    {
        return this.length;
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
    public final DoubleScalar<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set the link capacity.
     */
    public final void setCapacity(final DoubleScalar<FrequencyUnit> capacity)
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
    public void setGeometry(LinearGeometry geometry)
    {
        this.geometry = geometry;
    }

}
