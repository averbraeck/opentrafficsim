package org.opentrafficsim.core.network;

import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LinkDirection
{
    /** the link. */
    private final Link link;
    
    /** the direction on the link, with or against the design line. */
    private final GTUDirectionality direction;

    /**
     * @param link the link
     * @param direction the direction on the link, with or against the design line
     */
    public LinkDirection(final Link link, final GTUDirectionality direction)
    {
        super();
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
     * @return direction
     */
    public final GTUDirectionality getDirection()
    {
        return this.direction;
    }

}

