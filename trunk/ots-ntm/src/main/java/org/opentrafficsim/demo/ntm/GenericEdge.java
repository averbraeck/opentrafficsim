package org.opentrafficsim.demo.ntm;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <E> the edge type
 */
public class GenericEdge<E> extends DefaultWeightedEdge
{
    /** */
    private static final long serialVersionUID = 1L;

    /** edge object */
    private E edge;

    /**
     * @param edge
     */
    public GenericEdge(final E edge)
    {
        super();
        this.edge = edge;
    }

    /**
     * @return edge
     */
    public E getEdge()
    {
        return this.edge;
    }

}
