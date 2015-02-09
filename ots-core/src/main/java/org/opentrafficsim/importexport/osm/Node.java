package org.opentrafficsim.importexport.osm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenStreetmap Node.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Node
{
    /** */
    private long iD;

    /** */
    private double longitude;

    /** */
    private double latitude;

    /** */
    private List<Tag> nodetags;

    /**
     * @param id 
     */
    public final void setID(final long id)
    {
        this.iD = id;
    }

    /**
     * @param longi 
     */
    public final void setLongitude(final double longi)
    {
        this.longitude = longi;
    }

    /**
     * @param lati 
     */
    public final void setLatitude(final double lati)
    {
        this.latitude = lati;
    }

    /**
     * @return ID 
     */
    public final long getID()
    {
        return this.iD;
    }

    /**
     * @return longitude
     */
    public final double getLongitude()
    {
        return this.longitude;
    }

    /**
     * @return latitude
     */
    public final double getLatitude()
    {
        return this.latitude;
    }

    /**
     * @param id 
     * @param longi 
     * @param lati 
     */
    public Node(final long id, final double longi, final double lati)
    {
        this.iD = id;
        this.longitude = longi;
        this.latitude = lati;
        this.nodetags = new ArrayList<Tag>();
    }

    /**
     * @return nodetags
     */
    public final List<Tag> getTags()
    {
        return this.nodetags;
    }

    /**
     * @param tagKey 
     * @return nodetag
     * @throws IOException 
     */
    public final Tag getTag(final String tagKey) throws IOException
    {
        try
        {
            for (Tag t: this.nodetags)
            {
                if (t.getKey() == tagKey)
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

    /**
     * Set/replace the Node Tags.
     * @param theNodeTags List&lt;Tag&gt;; the list of Node tags
     */
    public final void setTags(final List<Tag> theNodeTags)
    {
        this.nodetags = theNodeTags;
    }

    /**
     * @param nodetag 
     */
    public final void addTag(final Tag nodetag)
    {
            this.nodetags.add(nodetag);
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Node %d %.6f %.6f", getID(), getLongitude(), getLatitude());
    }
}
