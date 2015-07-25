package org.opentrafficsim.importexport.osm;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.OTSNode;

/**
 * OpenStreetmap Node.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMNode
{
    /** The id of this OSMNode. */
    private final long id;

    /** The longitude of this OSMNode. */
    private final double longitude;

    /** The latitude of this OSMNode. */
    private final double latitude;

    /** The tags of this OSMNode. */
    private List<OSMTag> tags;

    /** Does this OSMNode have a traffic signal? */
    private boolean trafficSignal = false;

    /** Does this OSMNode have a stop sign? */
    private boolean stopSign = false;

    /** Does this OSMNode have a yield sign? */
    private boolean yieldSign = false;

    /** Is this OSMNode a crossing? */
    private boolean crossing = false;

    /** The number of OSMLinks originating at this OSMNode; i.e. having this node as start. */
    public int linksOriginating = 0;

    /** The number of OSMLinks ending at this OSMNode; i.e. having this node as end. */
    public int linksTerminating = 0;

    /** The OTS Node that corresponds to this OSMNode. */
    private OTSNode.STR otsNode = null;

    /**
     * @return Id
     */
    public final long getId()
    {
        return this.id;
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
     * Construct a new OSMNode.
     * @param id long; id of the new OSMNode
     * @param longitude double; longitude of the new OSMNode
     * @param latitude double; latitude of the new OSMNode
     */
    public OSMNode(final long id, final double longitude, final double latitude)
    {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.tags = new ArrayList<OSMTag>();
    }

    /**
     * Retrieve a tag of this OSMNode.
     * @param key String; the key of the tag to retrieve
     * @return OSMTag, or null if this OSMNode has no tag with the specified key
     */
    public final OSMTag getTag(final String key)
    {
        for (OSMTag tag : this.tags)
        {
            if (tag.getKey().equals(key))
            {
                return tag;
            }
        }
        return null;
    }

    /**
     * Add a tag to this OSMNode.
     * @param tag OSMTag; the tag to add to this OSMNode
     */
    public final void addTag(final OSMTag tag)
    {
        this.tags.add(tag);
        if (tag.getKey().equals("highway"))
        {
            switch (tag.getValue())
            {
                case "crossing":
                    this.setCrossing(true);
                    break;
                case "give_way":
                    this.setYieldSign(true);
                    break;
                case "stop":
                    this.setStopSign(true);
                    break;
                case "traffic_signals":
                    this.setTrafficSignal(true);
                    break;
                default:
                    break;
            }
        }
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Node %d %.6f %.6f", getId(), getLongitude(), getLatitude());
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

    /**
     * @param n OTSNode.STR
     */
    public final void setOtsNode(final OTSNode.STR n)
    {
        if (this.otsNode != null)
        {
            throw new Error("OTS Node already set, can only be set once.");
        }
        this.otsNode = n;
    }

    /**
     * @return OTSNode.STR - The associated OTS Node.
     */
    public final OTSNode.STR getOtsNode()
    {
        return this.otsNode;
    }

    /**
     * @return boolean; true if this OSMNode has no tags; false otherwise
     */
    public final boolean hasNoTags()
    {
        return this.tags.isEmpty();
    }
}
