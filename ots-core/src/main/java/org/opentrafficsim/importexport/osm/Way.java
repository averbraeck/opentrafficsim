package org.opentrafficsim.importexport.osm;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Way
{
    /** The ID of the way.
     */
    private long iD;

    /** The List of the IDs of all nodes this way has.
     */
    private List<Long> waynodes;

    /** The List of all Tags this way has.
     */
    private List<Tag> waytags;

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
     * @param waynodes 
     */
    public final void setNodes(final List<Long> waynodes)
    {
        this.waynodes = waynodes;
    }

    /**
     * @param waynode 
     */
    public final void addNode(final Long waynode)
    {
        this.waynodes.add(waynode);
    }

    /**
     * @param id 
     */
    public Way(final long id)
    {
        this.iD = id;
        this.waynodes = new ArrayList<Long>();
        this.waytags = new ArrayList<Tag>();
    }

    /**
     * @return waytags
     */
    public final List<Tag> getTags()
    {
        return this.waytags;
    }

    /**
     * @param waytags 
     */
    public final void setTags(final List<Tag> waytags)
    {
        this.waytags = waytags;
    }

    /**
     * @param waytag 
     */
    public final void addTag(final Tag waytag)
    {
        this.waytags.add(waytag);
    }

    /**
     * @param tagKey 
     * @return waytag
     * @throws IOException 
     */
    public final Tag getTag(final String tagKey) throws IOException
    {
        try
        {
            for (Tag t: this.waytags)
            {
                if (t.getKey().equals(tagKey))
                {
                    return t;
                }
            }
            throw new IOException(" not found");
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
