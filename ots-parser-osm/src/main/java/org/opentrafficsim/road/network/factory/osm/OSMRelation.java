package org.opentrafficsim.road.network.factory.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OSMRelation wraps a set of OSMTags, a set of OSMWays and a set of OSMNodes.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMRelation implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The id of this OSMRelation. */
    private final long id;

    /** The OSMTag list of this OSMRelation. */
    private List<OSMTag> tags = new ArrayList<OSMTag>();

    /** The ordered list of ids of the OSMWays of this OSMRelation. */
    private List<Long> wayIds = new ArrayList<Long>();

    /** The ordered list of ids of the OSMNodes of this OSMRelation. */
    private List<Long> nodeIds = new ArrayList<Long>();

    /**
     * @return id
     */
    public final long getId()
    {
        return this.id;
    }

    /**
     * Retrieve the list of OSMTags of this OSMRelation.
     * @return List&lt;OSMTag&gt;; the list of OSMTags of this OSMRelation; <strong>modifications of the returned list are
     *         reflected in this OSMWay</strong>.
     */
    public final List<OSMTag> getTaglist()
    {
        return this.tags;
    }

    /**
     * Add an OSMTag to this OSMRelation.
     * @param tag OSMTag; the OSMTag that will be added
     */
    public final void addTag(final OSMTag tag)
    {
        this.tags.add(tag);
    }

    /**
     * Retrieve the list of OSMWay ids of this OSMRelation.
     * @return List&lt;Long&gt;; the list of OSMWay ids of this OSMRelation; <strong>modifications of the returned list are
     *         reflected in this OSMWay</strong>.
     */
    public final List<Long> getWays()
    {
        return this.wayIds;
    }

    /**
     * Add one OSMWay id to this OSMRelation.
     * @param way Long; the id of the OSMWay that will be added
     */
    public final void addWay(final Long way)
    {
        this.wayIds.add(way);
    }

    /**
     * Retrieve the list of OSMNode ids of this OSMRelation.
     * @return List&lt;Long&gt;; the list of OSMNode ids of this OSMRelation; <strong>modifications of the returned list are
     *         reflected in this OSMWay</strong>.
     */
    public final List<Long> getNodes()
    {
        return this.nodeIds;
    }

    /**
     * Add one OSMNode id to this OSMRelation.
     * @param node Long; the id of the OSMNode that will be added
     */
    public final void addNode(final Long node)
    {
        this.nodeIds.add(node);
    }

    /**
     * Construct a new OSMRelation.
     * @param id long; the id of the new OSMRelation
     */
    public OSMRelation(final long id)
    {
        this.id = id;
    }

    /**
     * Retrieve the OSMTags of this OSMRelation that have a specified key.
     * @param key String; the key of the returned OSMTags
     * @return List&lt;OSMTag&gt;; the OSMTags that have the specified key (modifications of the result do not affect this
     *         OSMRelation)
     */
    public final List<OSMTag> getMatchingTags(final String key)
    {
        List<OSMTag> result = new ArrayList<OSMTag>();
        for (OSMTag t : this.tags)
        {
            if (t.getKey().equals(key))
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
        return "OSMRelation [id=" + this.id + ", tags=" + this.tags + ", wayIds=" + this.wayIds + ", nodeIds=" + this.nodeIds
                + "]";
    }

}
