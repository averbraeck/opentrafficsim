package org.opentrafficsim.road.network.factory.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OSMWay wraps an ordered set of OSMNode (identified by their ids) and a list of tags.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMWay implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of the way. */
    private final long id;

    /** The List of the IDs of all nodes this way has. */
    private List<Long> nodes;

    /** The List of all Tags this way has. */
    private List<OSMTag> waytags;

    /**
     * Retrieve the dd of this OSMWay.
     * @return long; the id of this OSMWay
     */
    public final long getId()
    {
        return this.id;
    }

    /**
     * Retrieve the list of ids that comprise this OSMWay.
     * @return List&lt;Long&gt;; a list of ids of the nodes of this OSMWay <strong>DO NOT MODIFY THE RESUL</strong>.
     */
    public final List<Long> getNodes()
    {
        return this.nodes;
    }

    /**
     * Set/replace the list of way nodes.
     * @param newNodes List&lt;Long&gt;; the new list of way nodes
     */
    public final void setNodes(final List<Long> newNodes)
    {
        this.nodes = newNodes;
    }

    /**
     * Append one node id to the list of node ids.
     * @param nodeId Long; the id of the node that must be added
     */
    public final void appendNode(final Long nodeId)
    {
        this.nodes.add(nodeId);
    }

    /**
     * Construct a new OSMWay.
     * @param id long; Id of the new OSMWay
     */
    public OSMWay(final long id)
    {
        this.id = id;
        this.nodes = new ArrayList<Long>();
        this.waytags = new ArrayList<OSMTag>();
    }

    /**
     * Retrieve the list of OSMTags of this OSMWay.
     * @return List&lt;OSMTab&gt;; the list of OSMTags of this OSMWay (modifications on this result are reflected in this
     *         OSMWay)
     */
    public final List<OSMTag> getTags()
    {
        return this.waytags;
    }

    /**
     * Set/replace the list of way tags.
     * @param newTags List&lt;OSMTag&gt;; the new list of way tags
     */
    public final void setTags(final List<OSMTag> newTags)
    {
        this.waytags = newTags;
    }

    /**
     * Add one tag to the list of tags of this OSMWay.
     * @param waytag OSMTag; the tag that must be added
     */
    public final void addTag(final OSMTag waytag)
    {
        this.waytags.add(waytag);
    }

    /**
     * Retrieve the tags that match the give key.
     * @param tagKey String; the key
     * @return List of matching Tags; the returned list is a copy; modifications of the result do not affect this OSMWay
     */
    public final List<OSMTag> getMatchingTags(final String tagKey)
    {
        List<OSMTag> result = new ArrayList<OSMTag>();
        for (OSMTag t : this.waytags)
        {
            if (t.getKey().equals(tagKey))
            {
                result.add(t);
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OSMWay [id=" + this.id + ", nodes=" + this.nodes + ", waytags=" + this.waytags + "]";
    }
}
