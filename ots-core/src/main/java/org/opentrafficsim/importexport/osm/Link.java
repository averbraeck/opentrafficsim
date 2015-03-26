package org.opentrafficsim.importexport.osm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opentrafficsim.importexport.osm.events.WarningEvent;
import org.opentrafficsim.importexport.osm.events.WarningListener;

/**
 * OpenStreetMap Link.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Link
{
    /** This is the Link ID. It is generated out of the Start ID and the End ID. */
    private String iD;
    /** This is the start Node of the link. */
    private Node start;
    /** This is the end Node of the link. */
    private Node end;
    /** This is a List of nodes that are used just for mapping purposes. */
    private List<Node> splineList;
    /** This is the length of the link.*/
    private double length;
    /** These are the tags that this link inherits from it's way. */
    private List<Tag> linktags;
    /** This is the number of lanes this link has. */
    private byte lanes;
    /** This is the number of lanes going forward. */
    private byte forwardLanes;
    /** Is this link one way?*/
    private boolean oneway;
    /**
     * @param fromNode Startnode
     * @param toNode Endnode
     * @param lt List of inherited Waytags
     * @param length length of the link
     * @param warningListener 
     */
    public Link(final Node fromNode, final Node toNode, final List<Tag> lt, final double length, final WarningListener warningListener)
    {
        if (fromNode == toNode)
        {
            throw new Error("Start and end of link are the same Node: " + fromNode);
        }
        this.iD = Objects.toString(fromNode.getID()) + Objects.toString(toNode.getID());
        this.start = fromNode;
        this.end = toNode;
        this.length = length;
        this.lanes = 1;
        this.forwardLanes = 1;
        boolean forwardDefined = false;

        List<Tag> lt2 = new ArrayList<Tag>(lt);
        List<Tag> lt3 = new ArrayList<Tag>(lt); 
        for (Tag t2: lt2)
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
            if (t2.getKey().equals("highway") && (t2.getValue().equals("cycleway") 
                    || t2.getValue().equals("footway") || t2.getValue().equals("pedestrian") 
                    || t2.getValue().equals("steps")))
            {
                this.lanes = 1;
            }
        }
        
        lt2 = new ArrayList<Tag>(lt3);
        for (Tag t2: lt2)
        {
            if (t2.getKey().equals("lanes"))
            {
                if (Link.isByte(t2.getValue()))
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
                if (Link.isByte(t2.getValue()))
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
        this.linktags = lt3;
        if (!forwardDefined && this.lanes > 1)
        {
            this.forwardLanes = (byte) (this.lanes / 2);
            String warning = "No forward lanes defined at link " + this.iD;
            warningListener.warning(new WarningEvent(this, warning));
        }
        this.splineList = new ArrayList<Node>(); 
    }
    
    /**
     * @param n1 Startnode
     * @param n2 Endnode
     * @param lt List of Tags inherited from way
     * @param length length of link
     * @param lanes total number of lanes
     * @param flanes number of forward lanes
     */
    public Link(final Node n1, final Node n2, final List<Tag> lt, final double length, final byte lanes, final byte flanes)
    {
        if (n1 == n2)
        {
            throw new Error("start and end of link are the same Node: " + n1);
        }
        this.iD = Objects.toString(n1.getID()) + Objects.toString(n2.getID());
        this.start = n1;
        this.end = n2;
        this.linktags = lt;
        this.length = length;
        this.lanes = lanes;
        this.forwardLanes = flanes;
        this.splineList = new ArrayList<Node>();
    }
    
    /**
     * @return ID
     */
    public final String getID()
    {
        return this.iD;
    }
    /**
     * @return start.
     */
    public final Node getStart()
    {
        return this.start;
    }
    /**
     * @param start set start.
     */
    public final void setStart(final Node start)
    {
        this.start = start;
    }
    /**
     * @return end.
     */
    public final Node getEnd()
    {
        return this.end;
    }
    /**
     * @param end set end.
     */
    public final void setEnd(final Node end)
    {
        this.end = end;
    }
    
    /**
     * @param lt 
     */
    public final void setTags(final List<Tag> lt)
    {
        this.linktags = lt;
    }
    
    /**
     * @return linktags
     */
    public final List<Tag> getTags()
    {
        return this.linktags;
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
    public final void addTag(final Tag tag)
    {
        this.linktags.add(tag);
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
    public final List<Node> getSplineList()
    {
        return this.splineList;
    }

    /**
     * @param splineList set splineList.
     */
    public final void setSplineList(final List<Node> splineList)
    {
        this.splineList = splineList;
    }
    
    /**
     * Append a Node to the spline of this Link.
     * @param spline Node; the Node to add to the splineList
     */
    public final void addSpline(final Node spline)
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
        for (Tag t: this.linktags)
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
        return String.format("Link %s from %d to %d", getID(), getStart().getID(), getEnd().getID());
    }
}
