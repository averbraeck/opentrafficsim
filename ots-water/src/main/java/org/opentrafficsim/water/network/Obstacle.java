package org.opentrafficsim.water.network;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class Obstacle
{
    /** */
    private static final long serialVersionUID = 20161105L;

    /** the waterway along which it is located, and the location on the waterway. */
    private final WaterwayLocation waterwayLocation;

    /**
     * @param waterwayLocation
     */
    public Obstacle(WaterwayLocation waterwayLocation)
    {
        super();
        this.waterwayLocation = waterwayLocation;
    }


}

