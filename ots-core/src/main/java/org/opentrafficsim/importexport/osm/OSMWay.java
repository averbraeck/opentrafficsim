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
public class OSMWay
{
    /**
     * The ID of the way.
     */
    private long iD;

    /**
     * The List of the IDs of all nodes this way has.
     */
    private List<Long> waynodes;

    /**
     * The List of all Tags this way has.
     */
    private List<OSMTag> waytags;

    /**
     * @return ID
     */
    public final long getID()
    {
        return this.iD;
    }

    /**
     * @param id
     */
    public final void setID(final long id)
    {
        this.iD = id;
    }

    /**
     * @return waynodes
     */
    public final List<Long> getNodes()
    {
        return this.waynodes;
    }

    /**
     * Set/replace the list of way nodes.
     * @param theWayNodes List&lt;Long&gt;; the new list of way nodes
     */
    public final void setNodes(final List<Long> theWayNodes)
    {
        this.waynodes = theWayNodes;
    }

    /**
     * @param waynode
     */
    public final void appendNode(final Long waynode)
    {
        this.waynodes.add(waynode);
    }

    /**
     * @param id
     */
    public OSMWay(final long id)
    {
        this.iD = id;
        this.waynodes = new ArrayList<Long>();
        this.waytags = new ArrayList<OSMTag>();
    }

    /**
     * @return waytags
     */
    public final List<OSMTag> getTags()
    {
        return this.waytags;
    }

    /**
     * Set/replace the list of way tags.
     * @param theWayTags List&lt;Tag&gt;; the new list of way tags
     */
    public final void setTags(final List<OSMTag> theWayTags)
    {
        this.waytags = theWayTags;
    }

    /**
     * @param waytag
     */
    public final void addTag(final OSMTag waytag)
    {
        this.waytags.add(waytag);
    }

    /**
     * @param tagKey
     * @return List of matching Tags
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
}
