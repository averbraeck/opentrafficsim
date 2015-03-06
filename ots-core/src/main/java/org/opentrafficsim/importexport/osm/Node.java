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
    
    /** */
    private boolean trafficSignal = false;
    
    /** */
    private boolean stopSign = false;
    
    /** */
    private boolean yieldSign = false;
    
    /** */
    private boolean crossing = false;

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

    /**
     * Set/replace the Node Tags.
     * @param theNodeTags List&lt;Tag&gt;; the list of Node tags
     */
    public final void setTags(final List<Tag> theNodeTags)
    {
        this.setCrossing(false);
        this.setStopSign(false);
        this.setTrafficSignal(false);
        this.setYieldSign(false);
        this.nodetags = theNodeTags;
        for (Tag t: this.nodetags)
        {
            if (t.getKey().equals("highway"))
            {
                switch (t.getValue())
                {
                    case "crossing":            this.setCrossing(true);
                                                break;
                    case "give_way":            this.setYieldSign(true);
                                                break;
                    case "stop":                this.setStopSign(true);
                                                break;
                    case "traffic_signals":     this.setTrafficSignal(true);
                                                break;
                    default:                    break;
                }
            }
        }
    }

    /**
     * @param nodetag 
     */
    public final void addTag(final Tag nodetag)
    {
            this.nodetags.add(nodetag);
            if (nodetag.getKey().equals("highway"))
            {
                switch (nodetag.getValue())
                {
                    case "crossing":            this.setCrossing(true);
                                                break;
                    case "give_way":            this.setYieldSign(true);
                                                break;
                    case "stop":                this.setStopSign(true);
                                                break;
                    case "traffic_signals":     this.setTrafficSignal(true);
                                                break;
                    default:                    break;
                }
            }
    }
    
    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Node %d %.6f %.6f", getID(), getLongitude(), getLatitude());
    }
    
    /**
     * @param key 
     * @return boolean
     */
    public final boolean contains(final String key)
    {
        boolean found = false;
        for (Tag t: this.nodetags)
        {
            if (t.getKey().equals(key))
            {
                found = true;
            }
        }
        return found;
    }

    /**
     * @return trafficSignal.
     */
    public final boolean isTrafficSignal()
    {
        return this.trafficSignal;
    }

    /**
     * @param trafficSignal set trafficSignal.
     */
    public final void setTrafficSignal(final boolean trafficSignal)
    {
        this.trafficSignal = trafficSignal;
    }

    /**
     * @return stopSign.
     */
    public final boolean isStopSign()
    {
        return this.stopSign;
    }

    /**
     * @param stopSign set stopSign.
     */
    public final void setStopSign(final boolean stopSign)
    {
        this.stopSign = stopSign;
    }

    /**
     * @return yieldSign.
     */
    public final boolean isYieldSign()
    {
        return this.yieldSign;
    }

    /**
     * @param yieldSign set yieldSign.
     */
    public final void setYieldSign(final boolean yieldSign)
    {
        this.yieldSign = yieldSign;
    }

    /**
     * @return crossing.
     */
    public final boolean isCrossing()
    {
        return this.crossing;
    }

    /**
     * @param crossing set crossing.
     */
    public final void setCrossing(final boolean crossing)
    {
        this.crossing = crossing;
    }
}
