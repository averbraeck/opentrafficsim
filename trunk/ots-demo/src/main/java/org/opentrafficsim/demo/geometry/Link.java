package org.opentrafficsim.demo.geometry;

import nl.tudelft.simulation.dsol.animation.LocatableInterface;

import org.opentrafficsim.core.network.CrossSectionLink;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Link extends CrossSectionLink<String, Node> implements LocatableInterface
{
    /** */
    private static final long serialVersionUID = 20140921L;

    /** speed. */
    private double speed;

    /**
     * @param id the id of the link (String).
     * @param nodeA the start node of the link.
     * @param nodeB the end node of the link.
     * @param length the length of the link with a unit.
     */
    public Link(final String id, final Node nodeA, final Node nodeB, final DoubleScalar.Rel<LengthUnit> length)
    {
        super(id, nodeA, nodeB, length);
    }

    /**
     * @return speed
     */
    public final double getSpeed()
    {
        return this.speed;
    }

    /**
     * @param speed set speed
     */
    public final void setSpeed(final double speed)
    {
        this.speed = speed;
    }

}
