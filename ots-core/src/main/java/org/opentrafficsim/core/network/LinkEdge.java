package org.opentrafficsim.core.network;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * The LinkEdge is a class that embeds a Link (extension of AbstractLink) to be put in a graph. Typical code to add a link
 * called <code>link</code> with nodes <code>nodeA</code> and <code>nodeB</code> to a graph is:
 * 
 * <pre>
 * SimpleWeightedGraph&lt;Node, LinkEdge&gt; linkGraph = new SimpleWeightedGraph&lt;&gt;(LinkEdge.class);
 * ...
 * Link link = new Link(nodeA, nodeB, name);
 * LinkEdge linkEdge = new LinkEdge(link);
 * linkGraph.addEdge(nodeA, nodeB, linkEdge);
 * linkGraph.setEdgeWeight(linkEdge, link.getLength().doubleValue());
 * </pre>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version Sep 12, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <LINK> the link type.
 */
public class LinkEdge<LINK extends Link> extends DefaultWeightedEdge
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Edge object. */
    private LINK link;

    /**
     * @param link LINK; the edge to take into the graph.
     */
    public LinkEdge(final LINK link)
    {
        this.link = link;
    }

    /**
     * @return the link that is taken as an edge in the graph.
     */
    public final LINK getLink()
    {
        return this.link;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LinkEdge [weight=" + getWeight() + ", link=" + this.link + "]";
    }

}
