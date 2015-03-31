package org.opentrafficsim.importexport.osm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opentrafficsim.importexport.osm.events.WarningEvent;
import org.opentrafficsim.importexport.osm.events.WarningListener;

/**
 * OpenStreetMap Link.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMLink
{
    /** This is the Link ID. It is generated out of the Start ID and the End ID. */
    private String iD;

    /** This is the start Node of the link. */
    private OSMNode start;

    /** This is the end Node of the link. */
    private OSMNode end;

    /** This is a List of nodes that are used just for mapping purposes. */
    private List<OSMNode> splineList;

    /** This is the length of the link. */
    private double length;

    /** These are the tags that this link inherits from it's way. */
    private List<OSMTag> tags;

    /** This is the number of lanes this link has. */
    private byte lanes;

    /** This is the number of lanes going forward. */
    private byte forwardLanes;

    /** Is this link one way? */
    private boolean oneway;

    /**
     * @param fromNode Startnode
     * @param toNode Endnode
     * @param lt List of inherited Waytags
     * @param length length of the link
     * @param warningListener
     */
    public OSMLink(final OSMNode fromNode, final OSMNode toNode, final List<OSMTag> lt, final double length,
            final WarningListener warningListener)
    {
        if (fromNode == toNode)
        {
            throw new Error("Start and end of link are the same Node: " + fromNode);
        }
        this.iD = Objects.toString(fromNode.getId()) + Objects.toString(toNode.getId());
        this.start = fromNode;
        this.end = toNode;
        this.length = length;
        this.lanes = 1;
        this.forwardLanes = 1;
        boolean forwardDefined = false;

        List<OSMTag> lt2 = new ArrayList<OSMTag>(lt);
        List<OSMTag> lt3 = new ArrayList<OSMTag>(lt);
        for (OSMTag t2 : lt2)
        {
            if (t2.getKey().equals("oneway") && t2.getValue().equals("yes"))
            {
                this.setOneway(true);
                lt3.remove(t2);
                this.forwardLanes = this.lanes;
            }
            if (t2.getKey().equals("highway") && t2.getValue().equals("motorway_link"))
            {
                this.setOneway(true);
                this.lanes = 1;
                this.forwardLanes = this.lanes;
            }
            if (t2.getKey().equals("highway") && t2.getValue().equals("motorway"))
            {
                this.setOneway(true);
                this.forwardLanes = this.lanes;
            }
            if (t2.getKey().equals("highway")
                    && (t2.getValue().equals("cycleway") || t2.getValue().equals("footway")
                            || t2.getValue().equals("pedestrian") || t2.getValue().equals("steps")))
            {
                this.lanes = 1;
            }
        }

        lt2 = new ArrayList<OSMTag>(lt3);
        for (OSMTag t2 : lt2)
        {
            if (t2.getKey().equals("lanes"))
            {
                if (OSMLink.isByte(t2.getValue()))
                {
                    this.lanes = Byte.parseByte(t2.getValue());
                    lt3.remove(t2);
                    if (this.oneway)
                    {
                        this.forwardLanes = this.lanes;
                        forwardDefined = true;
                    }
                }
                else
                {
                    String warning = "Illegal value for the tag 'lanes' at link " + this.iD;
                    warningListener.warning(new WarningEvent(this, warning));
                }
            }
            if (t2.getKey().equals("lanes:forward"))
            {
                if (OSMLink.isByte(t2.getValue()))
                {
                    this.forwardLanes = Byte.parseByte(t2.getValue());
                    lt3.remove(t2);
                    forwardDefined = true;
                }
                else
                {
                    String warning = "Illegal value for the tag 'lanes:forward' at link " + this.iD;
                    warningListener.warning(new WarningEvent(this, warning));
                }
            }
        }
        this.tags = lt3;
        if (!forwardDefined && this.lanes > 1)
        {
            this.forwardLanes = (byte) (this.lanes / 2);
            String warning = "No forward lanes defined at link " + this.iD;
            warningListener.warning(new WarningEvent(this, warning));
        }
        this.splineList = new ArrayList<OSMNode>();
    }

    /**
     * @param n1 Startnode
     * @param n2 Endnode
     * @param lt List of Tags inherited from way
     * @param length length of link
     * @param lanes total number of lanes
     * @param flanes number of forward lanes
     */
    public OSMLink(final OSMNode n1, final OSMNode n2, final List<OSMTag> lt, final double length, final byte lanes,
            final byte flanes)
    {
        if (n1 == n2)
        {
            throw new Error("start and end of link are the same Node: " + n1);
        }
        this.iD = Objects.toString(n1.getId()) + Objects.toString(n2.getId());
        this.start = n1;
        this.end = n2;
        this.tags = lt;
        this.length = length;
        this.lanes = lanes;
        this.forwardLanes = flanes;
        this.splineList = new ArrayList<OSMNode>();
    }

    /**
     * @return ID
     */
    public final String getId()
    {
        return this.iD;
    }

    /**
     * @return start.
     */
    public final OSMNode getStart()
    {
        return this.start;
    }

    /**
     * @param start set start.
     */
    public final void setStart(final OSMNode start)
    {
        this.start = start;
    }

    /**
     * @return end.
     */
    public final OSMNode getEnd()
    {
        return this.end;
    }

    /**
     * @param end set end.
     */
    public final void setEnd(final OSMNode end)
    {
        this.end = end;
    }

    /**
     * @param lt
     */
    public final void setTags(final List<OSMTag> lt)
    {
        this.tags = lt;
    }

    /**
     * @return List&lt;OSMTab&gt;
     */
    public final List<OSMTag> getTags()
    {
        return new ArrayList<OSMTag>(this.tags); // Create and return a copy (the tags themselves are immutable).
    }

    /**
     * @return oneway.
     */
    public final boolean isOneway()
    {
        return this.oneway;
    }

    /**
     * @param oneway set if link is one way.
     */
    public final void setOneway(final boolean oneway)
    {
        this.oneway = oneway;
    }

    /**
     * @return lanes.
     */
    public final byte getLanes()
    {
        return this.lanes;
    }

    /**
     * @return ForwardLanes.
     */
    public final byte getForwardLanes()
    {
        return this.forwardLanes;
    }

    /**
     * @param lanes set lanes.
     */
    public final void setLanes(final byte lanes)
    {
        this.lanes = lanes;
    }

    /**
     * Set the number of forward lanes of this Link.
     * @param forwardLanes byte; the number of forward lanes
     */
    public final void setForwardLanes(final byte forwardLanes)
    {
        this.forwardLanes = forwardLanes;
    }

    /**
     * Add a Tag to this Link.
     * @param tag Tag; the Tag that must be added
     */
    public final void addTag(final OSMTag tag)
    {
        this.tags.add(tag);
    }

    /**
     * @return length.
     */
    public final double getLength()
    {
        return this.length;
    }

    /**
     * @param length set length.
     */
    public final void setLength(final double length)
    {
        this.length = length;
    }

    /**
     * @return splineList.
     */
    public final List<OSMNode> getSplineList()
    {
        return this.splineList;
    }

    /**
     * @param splineList set splineList.
     */
    public final void setSplineList(final List<OSMNode> splineList)
    {
        this.splineList = splineList;
    }

    /**
     * Append a Node to the spline of this Link.
     * @param spline Node; the Node to add to the splineList
     */
    public final void addSpline(final OSMNode spline)
    {
        this.splineList.add(spline);
    }

    /**
     * Returns true if the link has a Tag with the specified key.
     * @param key
     * @return boolean
     */
    public final boolean hasTag(final String key)
    {
        for (OSMTag t : this.tags)
        {
            if (t.getKey().equals(key))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param s
     * @return is the given String a byte.
     */
    public static boolean isByte(final String s)
    {
        try
        {
            Byte.parseByte(s);
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("Link %s from %d to %d", getId(), getStart().getId(), getEnd().getId());
    }

    /**
     * Report if this OSMLink has all tags in a supplied set.
     * @param tagsToCheck
     * @return boolean; true if this Link has all the supplied tags; false otherwise
     */
    public boolean containsAllTags(List<OSMTag> tagsToCheck)
    {
        return this.tags.containsAll(tagsToCheck);
    }
}
