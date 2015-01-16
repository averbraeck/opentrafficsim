package org.opentrafficsim.importexport.osm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Link
{
    /** This is the Link ID. It is generated out of the Start ID and the End ID */
    private String iD;
    /** This is the startnode of the link.*/
    private Node start;
    /** This is the endnode of the link.*/
    private Node end;
    /** This is a List of nodes that are used just for mapping purposes */
    private List<Node> splineList;
    /** This is the length of the link.*/
    private double length;
    /** These are the tags that this link inherits from it's way.*/
    private List<Tag> linktags;
    /** This is the number of lanes this link has*/
    private byte lanes;
    /** This is the number of lanes going forward*/
    private byte flanes;
    /** Is this link one way?*/
    private boolean oneway;
    /**
     * @param n1 Startnode
     * @param n2 Endnode
     * @param lt List of inherited Waytags
     * @param length length of the link
     */
    public Link(final Node n1, final Node n2, final List<Tag> lt, final double length)
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
        this.lanes = 2;
        this.flanes = 1;
        boolean forwardDefined = false;
        Tag t = new Tag("oneway", "yes");
        if (lt.contains(t))
        {
            this.setOneway(true);
            lt.remove(t);
            this.lanes = 1;
            this.flanes = this.lanes;
        }
        List<Tag> lt2 = lt;
        for (Tag t2: lt2)
        {
            if (t2.getKey() == "lanes")
            {
                this.lanes = Byte.parseByte(t2.getValue());
                lt.remove(t2);
                if (this.oneway == true)
                {
                    this.flanes = this.lanes;
                    forwardDefined = true;
                }
            }
            if (t2.getKey() == "lanes:forward")
            {
                this.flanes = Byte.parseByte(t2.getValue());
                lt.remove(t2);
                forwardDefined = true;
            }
        }
        if (!forwardDefined)
        {
            this.flanes = (byte) (this.lanes / 2);
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
        this.flanes = flanes;
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
    public boolean isOneway()
    {
        return this.oneway;
    }

    /**
     * @param oneway set if link is one way.
     */
    public void setOneway(boolean oneway)
    {
        this.oneway = oneway;
    }

    /**
     * @return lanes.
     */
    public byte getLanes()
    {
        return this.lanes;
    }

    /**
     * @return ForwardLanes.
     */
    public byte getForwardLanes()
    {
        return this.flanes;
    }
    
    /**
     * @param lanes set lanes.
     */
    public void setLanes(byte lanes)
    {
        this.lanes = lanes;
    }

    /**
     * @param flanes set flanes.
     */
    public void setForwardLanes(byte flanes)
    {
        this.flanes = flanes;
    }
    /**
     * @param t
     */
    public final void addTag(Tag t)
    {
        this.linktags.add(t);
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
    public List<Node> getSplineList()
    {
        return this.splineList;
    }

    /**
     * @param splineList set splineList.
     */
    public void setSplineList(final List<Node> splineList)
    {
        this.splineList = splineList;
    }
    
    public void addSpline(final Node spline)
    {
        this.splineList.add(spline);
    }
    
    public String toString()
    {
        return String.format("Link %s from %d to %d", getID(), getStart().getID(), getEnd().getID());
    }
}
