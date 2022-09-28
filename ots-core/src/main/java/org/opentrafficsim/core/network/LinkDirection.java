package org.opentrafficsim.core.network;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * Storage for a Link and a GTUDirectionality.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkDirection implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The link. */
    private final Link link;

    /** The direction on the link, with or against the design line. */
    private final GTUDirectionality direction;

    /**
     * @param link Link; the link
     * @param direction GTUDirectionality; the direction on the link, with or against the design line
     */
    public LinkDirection(final Link link, final GTUDirectionality direction)
    {
        this.link = link;
        this.direction = direction;
    }

    /**
     * @return link
     */
    public final Link getLink()
    {
        return this.link;
    }

    /**
     * @return String; link id
     * @see org.opentrafficsim.core.network.Link#getId()
     */
    public String getId()
    {
        return this.link.getId();
    }

    /**
     * @return LinkType; link type
     * @see org.opentrafficsim.core.network.Link#getLinkType()
     */
    public LinkType getLinkType()
    {
        return this.link.getLinkType();
    }

    /**
     * @return Length; length
     * @see org.opentrafficsim.core.network.Link#getLength()
     */
    public Length getLength()
    {
        return this.link.getLength();
    }

    /**
     * @return direction
     */
    public final GTUDirectionality getDirection()
    {
        return this.direction;
    }

    /**
     * @return the destination node of the linkdirection
     */
    public final Node getNodeTo()
    {
        return this.direction.equals(GTUDirectionality.DIR_PLUS) ? this.link.getEndNode() : this.link.getStartNode();
    }

    /**
     * @return the origin node of the linkdirection
     */
    public final Node getNodeFrom()
    {
        return this.direction.equals(GTUDirectionality.DIR_PLUS) ? this.link.getStartNode() : this.link.getEndNode();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.direction == null) ? 0 : this.direction.hashCode());
        result = prime * result + ((this.link == null) ? 0 : this.link.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        LinkDirection other = (LinkDirection) obj;
        if (this.direction != other.direction)
        {
            return false;
        }
        if (this.link == null)
        {
            if (other.link != null)
            {
                return false;
            }
        }
        else if (!this.link.equals(other.link))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LinkDirection [link=" + this.link + ", direction=" + this.direction + "]";
    }
}
