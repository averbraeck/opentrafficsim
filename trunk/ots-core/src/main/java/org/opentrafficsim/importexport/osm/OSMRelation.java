package org.opentrafficsim.importexport.osm;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMRelation
{
    /** */
    private long iD;

    /** */
    private List<OSMTag> taglist;

    /** */
    private List<Long> relWays;

    /** */
    private List<Long> relNodes;

    /**
     * @return iD
     */
    public final long getID()
    {
        return this.iD;
    }

    /**
     * @param niD
     */
    public final void setID(final long niD)
    {
        this.iD = niD;
    }

    /**
     * @return taglist
     */
    public final List<OSMTag> getTaglist()
    {
        return this.taglist;
    }

    /**
     * @param taglist
     */
    public final void setTaglist(final List<OSMTag> taglist)
    {
        this.taglist = taglist;
    }

    /**
     * @param tag
     */
    public final void addTag(final OSMTag tag)
    {
        this.taglist.add(tag);
    }

    /**
     * @return ways
     */
    public final List<Long> getWays()
    {
        return this.relWays;
    }

    /**
     * @param ways
     */
    public final void setWays(final List<Long> ways)
    {
        this.relWays = ways;
    }

    /**
     * @param way
     */
    public final void addWay(final Long way)
    {
        this.relWays.add(way);
    }

    /**
     * @return nodes
     */
    public final List<Long> getNodes()
    {
        return this.relNodes;
    }

    /**
     * @param nodes
     */
    public final void setNodes(final List<Long> nodes)
    {
        this.relNodes = nodes;
    }

    /**
     * @param node
     */
    public final void addNode(final Long node)
    {
        this.relNodes.add(node);
    }

    /**
     * @param id
     */
    public OSMRelation(final long id)
    {
        this.iD = id;
        this.relNodes = new ArrayList<Long>();
        this.relWays = new ArrayList<Long>();
        this.taglist = new ArrayList<OSMTag>();
    }

    /**
     * @param tagKey
     * @return List of matching Tags
     */
    public final List<OSMTag> getMatchingTags(final String tagKey)
    {
        List<OSMTag> result = new ArrayList<OSMTag>();
        for (OSMTag t : this.taglist)
        {
            if (t.getKey().equals(tagKey))
            {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * @return List of all Tags in this relation
     */
    public final List<OSMTag> getTags()
    {
        return this.taglist;
    }
}
