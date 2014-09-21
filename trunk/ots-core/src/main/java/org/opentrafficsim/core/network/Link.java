package org.opentrafficsim.core.network;

import java.io.Serializable;

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
 * @param <ID> the type of ID, e.g., String or Integer.
 */
public class Link<ID> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140920L;

    /** link id. */
    private final ID id;

    /** begin node (directional). */
    private final Node<?> beginNode;

    /** end node (directional). */
    private final Node<?> endNode;

    /** link length in a length unit. */
    private final DoubleScalar<LengthUnit> length;

    /** link capacity in vehicles per hour. This is a mutable property (e.g., blockage). */
    private double capacity;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param beginNode begin node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param capacity link capacity in vehicles per hour.
     */
    public Link(final ID id, final Node<?> beginNode, final Node<?> endNode, final DoubleScalar<LengthUnit> length,
            final double capacity)
    {
        this.id = id;
        this.beginNode = beginNode;
        this.endNode = endNode;
        this.length = length;
        setCapacity(capacity);
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
     * @return link capacity.
     */
    public final double getCapacity()
    {
        return this.getLinkCapacity();
    }

    /**
     * @return begin node.
     */
    public final Node<?> getBeginNode()
    {
        return this.beginNode;
    }

    /**
     * @return end node.
     */
    public final Node<?> getEndNode()
    {
        return this.endNode;
    }

    /**
     * @return link capacity.
     */
    public final double getLinkCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set the link capacity.
     */
    public final void setCapacity(final double capacity)
    {
        this.capacity = capacity;
    }

}
