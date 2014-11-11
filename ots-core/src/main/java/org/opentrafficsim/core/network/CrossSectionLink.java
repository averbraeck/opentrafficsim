package org.opentrafficsim.core.network;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

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
public class CrossSectionLink<ID, N extends AbstractNode<?, ?>> extends AbstractLink<ID, N>
{
    /** list of cross-section elements. */
    private final List<CrossSectionElement> crossSectionElementList = new ArrayList<>();

    /** */
    private static final long serialVersionUID = 20141015L;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     * @param capacity link capacity in vehicles per hour.
     */
    public CrossSectionLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        super(id, startNode, endNode, length, capacity, 0 /* hierarchy */);
    }

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param length link length in a length unit.
     */
    public CrossSectionLink(final ID id, final N startNode, final N endNode, final DoubleScalar.Rel<LengthUnit> length)
    {
        super(id, startNode, endNode, length, 0 /* hierarchy */);
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse)
    {
        this.crossSectionElementList.add(cse);
    }

    /**
     * Add a cross section element at the end of the list.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param index the location to insert the element.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse, final int index)
    {
        this.crossSectionElementList.add(index, cse);
    }

    /**
     * @return crossSectionElementList.
     */
    public final List<CrossSectionElement> getCrossSectionElementList()
    {
        return this.crossSectionElementList;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        return getStartNode().getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        return new BoundingBox(getEndNode().getLocation().x - getStartNode().getLocation().x, getEndNode().getLocation().y
                - getStartNode().getLocation().y, getEndNode().getLocation().z - getStartNode().getLocation().z);
    }

}
